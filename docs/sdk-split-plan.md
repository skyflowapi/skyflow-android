# Splitting `skyflow-android` into Legacy + FlowVault SDKs — Architecture & Migration Plan

**Status:** Implemented on `saileshwar/SK-3053-package-split`. Both modules build, assemble, and publish; skyvault v1 suite and flowvault v2 suite are green.
**Scope:** Split the single `skyflow-android` codebase into two independently publishable Android (AAR) SDKs that share one common core, with the FlowVault SDK built by *extending* the shared core.
**Sources:** Legacy (v1) baseline restored from `main` (1.27.0); FlowVault (v2) deltas taken from the `1.28.0-beta.1` tag / `beta-release/26.7.0`.
**Integration branch:** `saileshwar/SK-3053-package-split`, cut from `origin/main`.

> **Naming:** the Gradle **modules/folders** are `skyvault` (legacy), `flowvault` (FlowVault), and the shared `common/` source folder. The **published artifact ids are unchanged** — `skyflow-android-sdk` and `skyflow-flowvault-android-sdk` — so consumers are unaffected.

---

## 1. Goal

Produce two independently versioned, independently published SDKs that share a single common code layer. **Which SDK an app gets is decided only by which artifact it installs** — no build flags, no runtime mode switch.

| Module (folder) | Surface | Published artifact | Sourced from | First version |
|---|---|---|---|---|
| `skyvault` | **Legacy / v1** (existing public API) | `skyflow-android-sdk` | `main` | continues current semver |
| `flowvault` | **FlowVault / v2** (FlowDB `/v2` API) | `skyflow-flowvault-android-sdk` | `1.28.0-beta.1` deltas | `1.0.0` |
| `common/` (shared **source folder**, not a module) | shared code + base types | n/a — compiled into each module | common ancestor of both | — |

Organizing principle: **create common code both SDKs use, and implement FlowVault by extending it** — not by branching inside shared code. There is no runtime `isFlowDB` toggle; the variant is fixed per module at build time.

This is a **three-way factoring of a common ancestor**:
- `common/` ≈ contract-agnostic infra present in both `main` and the `1.28.0-beta.1` tag.
- `skyvault` ≈ `main` minus common (the v1 contract layer).
- `flowvault` ≈ the `1.28.0-beta.1` FlowDB additions minus common, expressed as common extensions.

---

## 2. Why `common/` is a shared source folder, not a `:common` module

Kotlin `internal` is scoped to a **compilation module**. The container extension functions (`fun Container<CollectContainer>.collect(...)`, `.create(...)`, `.reveal(...)`) depend on `internal` members of `Container`, `Client`, `TextField`, `Label`, and `Utils` (e.g. `Container.client`, `Container.collectElements`, `TextField.setupField`, `TextField.getState`). This "friend surface" is large.

- If `common` were a **separate Gradle module**, every one of those internals would have to be promoted to `public` (guarded by `@RestrictTo(LIBRARY_GROUP)` + lint), leaking a large surface, and we'd need fat-AAR bundling so apps don't install `common` separately.
- Instead, **`common/` is a plain source folder added to both modules' source sets.** It compiles *into* each module, so:
  - Kotlin `internal` works natively → common internals are visible to that module's contract layer, invisible to apps. This is the Kotlin "split-package / internal-friend" mechanism.
  - Each AAR is self-contained; an app installs exactly one; the import stays `Skyflow.*` — drop-in identical for both SDKs.
  - **Boundary is enforced by construction:** `common/` and each layer share the `Skyflow` package, so a leak is not an *import* (nothing to grep) but an unqualified reference resolved at compile time. The guard is therefore the **dual-module compile itself**: a `common/` file that references a legacy-only symbol fails to compile in the FlowVault module (the symbol is absent there), and a FlowVault-only reference fails in skyvault. Because PR CI builds *both* modules (`runOnGitHub`), any boundary violation breaks the build. (A `Konsist` AST test could add a second, redundant guard; it is not required for correctness and is left as a future enhancement rather than a network dependency in this branch.)

Trade-off (accepted): common is compiled twice (once per module). This mirrors the JS reference's "core is a folder, not a package" decision and its accepted duplication.

---

## 3. Target repo layout

```
skyflow-android/
  common/                            # SHARED SOURCE (not a Gradle module)
    src/main/kotlin/Skyflow/…        # contract-agnostic .kt (package Skyflow, unchanged)
    src/main/res/…                   # shared drawables/anim/styles/font/xml/values
    src/test/kotlin/…                # shared / core-behavior tests
  skyvault/                          # LEGACY module (v1)
    build.gradle                     # artifactId = skyflow-android-sdk, own version line
    src/main/kotlin/Skyflow/…        # v1 contract layer only
    src/test/…                       # restored v1 suites
  flowvault/                         # FLOWVAULT module (v2)
    build.gradle                     # artifactId = skyflow-flowvault-android-sdk, version 1.0.0
    src/main/kotlin/Skyflow/…        # v2 contract layer only (FlowDB*)
    src/test/…                       # v2 suites
  settings.gradle                    # includes the two modules only
  scripts/bump_version.sh            # takes a product argument
  .github/workflows/                 # PR builds both; per-product release pipelines
  docs/sdk-split-plan.md             # this document
  samples/                           # sample(s) built against each published SDK
```

Each module's `build.gradle` folds in `common/` (both keep `namespace "com.skyflow_android"` — safe because installing both SDKs is disallowed):

```groovy
android {
  namespace "com.skyflow_android"
  sourceSets.main {
    kotlin.srcDirs += "$rootDir/common/src/main/kotlin"   // kotlin.srcDirs, NOT java.srcDirs —
    res.srcDirs    += "$rootDir/common/src/main/res"       // java.srcDirs double-compiles the .kt
  }
}
```

> **Only `kotlin.srcDirs`.** Adding `common` under `java.srcDirs` as well makes the Kotlin compiler pick the files up twice → "conflicting overloads" errors. Use `kotlin.srcDirs` alone.
>
> **Tests are per-module, not a shared `common/src/test`.** Each layer's tests construct that layer's public types (`CollectElementInput(table=…)` for v1 vs `CollectElementInput(tableName=…)` for v2) and assert that layer's wire keys, so they are not byte-identical and cannot be a single shared source set. skyvault keeps `main`'s v1 suites; flowvault carries the beta v2 suites. The overlapping *neutral* suites (`ValidationTests`, `InputFormattingTest`, `UtilsTest`, `ComposableElementsTests`) exist in each module compiled against that layer's types, so both products get that coverage.

---

## 4. File classification

### CORE → `common/` (contract-agnostic)
- **UI / widgets:** `BaseElement`, `Element`, `TextField`, `Label`, `CollectElementInput`, `CollectElementOptions`, `RevealElementInput`, `RevealElementOptions`, `core/elements/state/{State,StateforText}`, `composable/{ComposableErrorsList,ComposableEvents,ComposableStyles}`.
- **Validations:** all of `collect/elements/validations/*`.
- **Element utils / types:** `collect/elements/utils/{CardType,DateValidator,Spacespan,VibratorHelper}`, `ElementType`, `CardMetadata`.
- **Styles:** `Style`, `Styles`, `Margin`, `Padding`.
- **Errors / logging:** `SkyflowException.kt` (public `SkyflowError` data class), `SkyflowError.kt` (`SkyflowInternalError`), `SkyflowErrorCode`, `core/Logger`, `LogLevel`, `core/Messages`.
- **Auth / callback:** `TokenProvider`, `Callback`.
- **Config / entry:** `Configuration`, `Options`, `Env`, `Init`.
- **Container base:** `Container`, `ContainerType`, `core/container/ContainerProtocol`, `ContainerOptions`.
- **Misc:** `utils/EventName`, all of `res/`, `RedactionType` (documented superset — v1 uses it; v2's `FlowDBRevealRequestRecord` also references it).

### LEGACY → `skyvault/` (v1-only)
`collect/client/{CollectAPICallback,CollectRequestBody,CollectRequestRecord,MixedAPICallback,UpdateAPICallback,UpdateRequestRecord}`, `reveal/{RevealApiCallback,RevealByIdCallback,RevealRequestBody,RevealRequestRecord,RevealResponse,RevealResponseByID,GetByIdRecord,RevealValueCallback}`, `get/*`, `soap/*`, `core/ConnectionApiCallback`, `ConnectionConfig`, `ContentType`, `RequestMethod`, `InsertOptions`, plus the v1 shapes of `CollectOptions` / `RevealOptions` / response types.

### FLOWVAULT → `flowvault/` (v2-only)
`core/FlowDBAPIClient` (v2 dispatch), `collect/client/{FlowDBCollectAPICallback,FlowDBCollectRequestBody,FlowDBMixedAPICallback,MockCVV}`, `reveal/{FlowDBRevealApiCallback,FlowDBRevealRequestBody,FlowDBRevealRequestRecord,RevealValueCallback}`, `CollectResponse`, `RevealResponse`, the v2 `CollectOptions`, `RevealOptions` (+ `TokenGroupRedaction`), `AdditionalFields`, `UpsertOptions`, `UpdateType`.

### SPLIT-NEEDED (decompose — §5)
`Client.kt`, `core/APIClient.kt`, `core/FlowDBAPIClient.kt`, `CollectContainer.kt`, `RevealContainer.kt`, `composable/ComposableContainer.kt`, `utils/Utils.kt`.

> **Per-layer types with shared names.** `CollectOptions`, `RevealOptions`, `CollectResponse`/`CollectRecord`/`CollectCallback`, `RevealResponse`/`RevealRecord`/`RevealCallback`, `UpsertOptions`, `InsertOptions`, `GetOptions` diverge between contracts, so each layer defines its own with the **same public name**. Apps install one module, so each name resolves to the correct shape.
>
> **Superset fields stay on shared core UI types** rather than forking the type: `RevealElementInput.redaction` (read only by v1), `CollectElementInput.skyflowId`/`tableName` (used by v2 update-by-id).

---

## 5. SPLIT-NEEDED decomposition

- **Bearer-token lifecycle.** `JWTUtils` (JWT decode + expiry check) was promoted to a single shared `common/src/main/kotlin/Skyflow/core/JWTUtils.kt` (`object JWTUtils`), so both `APIClient` (legacy) and `FlowDBAPIClient` (FlowVault) reuse it instead of each declaring its own. The thin `getAccessToken` wrapper stays inside each api client (it references that client's `token`/`logLevel` state); only the contract-agnostic `JWTUtils` moved to common.
- **`Client.kt` → base-client pattern.** Core defines an abstract **`BaseSkyflowClient`** holding the neutral shell: `configuration`, `elementMap`, and the two `container(...)` factories. Each product's concrete `Client` extends it:
  - legacy `Client : BaseSkyflowClient` wires the v1 `APIClient` and keeps the v1 client methods (`insert`/`get`/`getById`/`detokenize` + the deprecated connection calls);
  - FlowVault `Client : BaseSkyflowClient` wires `FlowDBAPIClient` and has **no standalone client methods** (only the inherited `container(...)`).
  `Container.client` is typed as `BaseSkyflowClient`; each layer's container extensions reach their contract-specific api client via the concrete `Client` (e.g. `(client as Client).apiClient`). The public type name `Client` is unchanged in each package (no v1 breaking change).
- **Divergent request/input types → base + extend (not a superset).** Types whose *shape* differs across contracts follow the base/subclass pattern instead of forking or supersetting: core holds the neutral base, and each package extends it with its own public constructor param names mapped onto neutral storage. Implemented as:
  - `BaseCollectElementInput` (neutral `tableName`/`skyflowId`/`column`/styles/…) → legacy `CollectElementInput(table=…, skyflowID=…)` (with `internal val table`/`skyflowID` read aliases) and FlowVault `CollectElementInput(tableName=…, skyflowId=…)`.
  - `BaseRevealElementInput` (neutral `token`/styles/label) → legacy `RevealElementInput(redaction=…)` (adds per-token redaction) and FlowVault `RevealElementInput` (no redaction — moved to `RevealOptions.tokenGroupRedactions`).
  - `BaseConfiguration` (neutral `vaultID`/`vaultURL`/`tokenProvider`/`options` **plus a neutral superset `okHttpClient`** the v2 client injects and v1 ignores) → legacy `Configuration` (its `init{}` appends `/v1/vaults/`) and FlowVault `Configuration` (no suffix; `/v2/…` applied inside `FlowDBAPIClient`).
  - The shared `Element` reads `skyflowID`; a read-only `internal val skyflowId get() = skyflowID` alias lets v2 call sites use the lowercase spelling without forking `Element`.
- **Containers.** `create(...)`, `validateVaultConfig()`/`checkVaultDetails`, `validateElements()`/`validateElement()`, and the neutral orchestration skeleton → **core**. The contract-specific `post()`/`get()`, typed `collect(CollectCallback)` / `reveal(RevealCallback)`, `update(...)`, and the untyped `collect(Callback)` / `reveal(Callback)` → **each layer** (legacy restores `main`'s v1 versions; FlowVault keeps the beta v2 byte-for-byte).
- **`utils/Utils.kt`.** Neutral helpers (`constructError`, `constructErrorResponse`, `checkUrl`, `checkInputFormatOptions`, `checkIfElementsMounted`, element/regex/uuid, `checkVaultDetails`) → **core**; v1 helpers (`constructBatchRequestBody`, `constructRequestBodyForGet` → `get.GetRecord`, `validateGetInputAndOptions`) → **legacy**.
- **`Configuration.kt`.** Neutral base (`BaseConfiguration`) in core with no URL mutation; the legacy `Configuration` subclass keeps `main`'s `init{}` `/v1/vaults/` suffix (base+extend, above) so the 81 v1 tests that assert `configuration.vaultURL` stay green, and the v2 client suffixes `/v2/…` itself.

---

## 6. Contract differences to preserve

| Aspect | Legacy (v1, from `main`) | FlowVault (v2, from beta) |
|---|---|---|
| Endpoints | `{vaultURL}/v1/vaults/{id}`, `…/detokenize` | `{vaultURL}/v2/records/{insert\|update}`, `…/v2/tokens/detokenize` |
| Collect response keys | `records[].table`, `.fields`, `fields.skyflow_id` | `records[].tableName`, `.tokens`, `.skyflowId`, `.hashedData`, `.httpCode` |
| Callbacks | untyped `Callback` (raw `JSONObject`) | typed `CollectCallback` / `RevealCallback` |
| Reveal redaction | per-element `RevealElementInput.redaction: RedactionType` | request-level `RevealOptions.tokenGroupRedactions` |
| Collect options | `CollectOptions(token: Boolean, additionalFields: JSONObject, upsert: JSONArray)` | `CollectOptions(upsert: List<UpsertOptions>, additionalFields: AdditionalFields)` |

**Backward compatibility wins:** because the branch is cut from `main`, the legacy contract is the starting point. Where a dormant/beta test encodes beta-era wire keys (`tableName`/`skyflowId`/`tokens`), the legacy code keeps `main`'s released keys (`table`/`fields`/`skyflow_id`) and the test is fixed.

---

## 7. SDK self-identity

Add to **core**:
```kotlin
object SdkInfo {           // defaults match the legacy product
    var name: String = "skyflow-android-sdk"
    var version: String = "<legacy semver>"
}
```
Each layer's `init()` stamps `SdkInfo.name`/`version` from its own `BuildConfig.SDK_NAME`/`SDK_VERSION` (fed by each module's `ext { mArtifactId, mVersionName }`). Core error messages / telemetry read `SdkInfo` instead of `BuildConfig`.

---

## 8. Versioning & tags

- Independent semver per module; each `build.gradle` has its own `ext { mArtifactId, mVersionName }`.
  - `skyflow-android-sdk` → continues the current version line; publishes `skyflow-android-sdk` / `-beta` / `-dev` (via the existing `-Pbeta` / `-Pdev` flags).
  - `skyflow-flowvault-android-sdk` → starts at `1.0.0`; publishes `skyflow-flowvault-android-sdk` / `-beta` / `-dev`.
- **Tag namespaces:** legacy keeps plain-semver tags (`x.y.z`) and `*.*.*-beta.*`; FlowVault uses `flowvault/x.y.z` (and `flowvault/x.y.z-beta.n`).
- `scripts/bump_version.sh` gains a leading **product** argument (`legacy` | `flowvault`) selecting which module's `build.gradle` the `sed` edits; the existing `<version> [devsha]` behavior is unchanged.

---

## 9. CI / CD

- **PR CI (`pr.yml`)** builds + tests BOTH modules — it runs `:skyvault:build :flowvault:build` and the root aggregate task `runOnGitHub`, which depends on `:skyvault:{lint,test}` and `:flowvault:{lint,test}`. (Task paths use the Gradle **module** names `:skyvault` / `:flowvault`; the *published artifact ids* remain `skyflow-android-sdk` / `skyflow-flowvault-android-sdk`.)
- **Legacy release** (existing workflows, retargeted to the legacy module): `release.yml` (tag `[0-9]+.[0-9]+.[0-9]+`), `beta_release.yml` (tag `*.*.*-beta.*`), `internal_release.yml` (push `release/*`) → build/publish **legacy only**.
- **FlowVault release** (new, mirrored): `flowvault_release.yml` (tag `flowvault/[0-9]+.[0-9]+.[0-9]+`), `flowvault_beta_release.yml` (tag `flowvault/*-beta.*`), `flowvault_internal_release.yml` (push `flowvault-release/*`) → build/publish **flowvault only**, mirroring the private/dev channel.
- Each release workflow passes the product argument to `bump_version.sh`.

---

## 10. Tests

Tests live **per module** (see §3 — they construct layer-specific public types and assert layer-specific wire keys, so they are not a single shared source set):

- **skyvault (v1, from `main`):** `DetokenizeTests`, `GetTests`, `RevealTest`, `CollectTest`, `CollectRequestBodyTest`, `CallbackResponseFormatTest`, `UpdateBySkyflowIdTest`, `InvokeConnectionTest`, `SoapConnectionTest`, `UnitTests`, `ResponseTest`, plus the neutral `ValidationTests` / `InputFormattingTest` / `UtilsTest` / `ComposableElementsTests`. Green.
- **flowvault (v2, from beta):** the beta test tree. Beta had already **emptied** its v1-flow placeholder files (`CollectTest`, `RevealTest`, `GetTests`, `DetokenizeTests`, `CollectRequestBodyTest`, `CallbackResponseFormatTest`, `UpdateBySkyflowIdTest` — 0 bytes, no `@Test`); those were dropped rather than carried as empty files. The live v2 suite is **93 tests**: `MockCVVTest` (14), `ResponseTest` (11, v2 shapes), `ComposableElementsTests` (27), `InputFormattingTest` (25), `UtilsTest` (9), `ValidationTests` (6), `ExampleUnitTest` (1). All green.

Both suites run under Robolectric. Backward-compat rule from §6 applies where a test would encode beta-era wire keys on the legacy side.

---

## 11. Migration sequencing

- **Phase 0 — Branch.** Cut `SK-3041/package-split` from `origin/main`. Use `git mv` throughout to preserve blame/history. *(done)*
- **Phase 1 — `common/` + legacy module (from main).** Create `common/` + `skyvault/`; `git mv` neutral files → `common/`, v1 files → the legacy module; wire the shared source set; split `Utils`; extract the token client; neutralize `Configuration`; add `SdkInfo` + stamp in legacy `init()`. Legacy builds and all restored v1 tests pass; public surface unchanged.
- **Phase 2 — FlowVault module (from beta).** Create `flowvault/`; bring `FlowDB*` + v2 types from `1.28.0-beta.1` as core extensions; per-layer options/responses; stamp `SdkInfo` in flowvault `init()`; add v2 tests. Both modules build. *(done)*
- **Phase 3 — Versioning + CI.** Bump-script product arg; per-module versions; PR-builds-both; per-product release workflows keyed to tag namespaces (+ mirrored internal channel). *(done)*
- **Phase 4 — Samples + docs.** Verify per-SDK publish (`publishToMavenLocal`) and finalize this document. *(done)*

---

## 12. Verification

1. **Build both:** `./gradlew :skyvault:assembleRelease :flowvault:assembleRelease` — zero errors. ✅
2. **Test both:** `./gradlew runOnGitHub` — skyvault v1 suite + flowvault 93-test v2 suite green. ✅
3. **Boundary (by construction):** both modules compile in CI; because `common/` is compiled into *each*, a common reference to a symbol that lives only in one layer breaks the other module's build. Proven while building: the flowvault module compiles with `common` + v2 source only (no skyvault source present), confirming `common` is self-contained. ✅
4. **Visibility:** an app cannot resolve a core `internal` symbol (e.g. `Container.client`) — compile error (Kotlin `internal` is module-scoped; common is folded into the AAR, not re-exported).
5. **Real package manager (per SDK):** `publishToMavenLocal` produced `com.skyflowapi.android:skyflow-android-sdk:1.27.0` and `com.skyflowapi.android:skyflow-flowvault-android-sdk:1.0.0`; the flowvault POM lists only external deps (kotlin-stdlib, core-ktx, material, okhttp) with **no `common` module dependency** — the shared source is compiled into the AAR, so a consumer resolves one self-contained artifact with `import Skyflow.*`. ✅
6. **Release routing:** a plain-semver / `*.*.*-beta.*` tag triggers only legacy publish (`release.yml` / `beta_release.yml` → `:skyvault:publish`); a `flowvault/*` tag triggers only flowvault publish (`flowvault_release.yml` / `flowvault_beta_release.yml` → `:flowvault:publish`). Internal channels: `release/*` → skyvault dev, `flowvault-release/*` → flowvault dev.
7. **Backward-compat smoke:** the legacy `samples/` app (standalone reference, `implementation project(':skyvault')` — matching how `main` referenced `:Skyflow`; not part of the Gradle build's `settings.gradle`, same as on `main`) uses v1 signatures (`insert`, untyped `collect(Callback)`, `RevealElementInput(redaction=…)`, `CollectOptions(token=…)`) against the legacy artifact.
