# Splitting `skyflow-android` into Legacy + FlowVault SDKs — Architecture & Migration Plan

**Status:** Proposed
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
  - **Boundary is enforced by construction:** a `common/` file that references a legacy-only symbol fails to compile in the FlowVault module (the symbol is absent there), and vice-versa. A `Konsist` unit test adds an explicit guard.

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
    java.srcDirs += "$rootDir/common/src/main/kotlin"
    res.srcDirs  += "$rootDir/common/src/main/res"
  }
  sourceSets.test { java.srcDirs += "$rootDir/common/src/test/kotlin" }
}
```

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

- **Bearer-token lifecycle.** `JWTUtils` + `getAccessToken` are duplicated in `APIClient` and `FlowDBAPIClient`. Extract one core `internal` token client both layers reuse.
- **`Client.kt` → base-client pattern.** Core defines an abstract **`BaseSkyflowClient`** holding the neutral shell: `configuration`, `elementMap`, and the two `container(...)` factories. Each product's concrete `Client` extends it:
  - legacy `Client : BaseSkyflowClient` wires the v1 `APIClient` and keeps the v1 client methods (`insert`/`get`/`getById`/`detokenize` + the deprecated connection calls);
  - FlowVault `Client : BaseSkyflowClient` wires `FlowDBAPIClient` and has **no standalone client methods** (only the inherited `container(...)`).
  `Container.client` is typed as `BaseSkyflowClient`; each layer's container extensions reach their contract-specific api client via the concrete `Client` (e.g. `(client as Client).apiClient`). The public type name `Client` is unchanged in each package (no v1 breaking change).
- **Divergent request/input types → base + extend (not a superset).** Types whose *shape* differs across contracts follow the same base/subclass pattern instead of forking or supersetting: core holds the neutral base (the shared field, e.g. `column`), and each package extends it with its own field name — legacy adds `table`, FlowVault adds `tableName`. Applied to the collect-element input in Phase 2 when the v2 `tableName` shape is introduced.
- **Containers.** `create(...)`, `validateVaultConfig()`/`checkVaultDetails`, `validateElements()`/`validateElement()`, and the neutral orchestration skeleton → **core**. The contract-specific `post()`/`get()`, typed `collect(CollectCallback)` / `reveal(RevealCallback)`, `update(...)`, and the untyped `collect(Callback)` / `reveal(Callback)` → **each layer** (legacy restores `main`'s v1 versions; FlowVault keeps the beta v2 byte-for-byte).
- **`utils/Utils.kt`.** Neutral helpers (`constructError`, `constructErrorResponse`, `checkUrl`, `checkInputFormatOptions`, `checkIfElementsMounted`, element/regex/uuid, `checkVaultDetails`) → **core**; v1 helpers (`constructBatchRequestBody`, `constructRequestBodyForGet` → `get.GetRecord`, `validateGetInputAndOptions`) → **legacy**.
- **`Configuration.kt`.** Keep neutral in core (no URL mutation). Move `main`'s `init{}` `v1/vaults/` suffixing into the **legacy** api client (v2 already suffixes `/v2/…` in its client). This keeps the shared config type contract-free.

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

- **PR CI (`pr.yml`)** builds + tests BOTH modules in one command — root aggregate task `runOnGitHub` depending on `:skyflow-android-sdk:{lint,test,build}` and `:skyflow-flowvault-android-sdk:{lint,test,build}`.
- **Legacy release** (existing workflows, retargeted to the legacy module): `release.yml` (tag `[0-9]+.[0-9]+.[0-9]+`), `beta_release.yml` (tag `*.*.*-beta.*`), `internal_release.yml` (push `release/*`) → build/publish **legacy only**.
- **FlowVault release** (new, mirrored): `flowvault_release.yml` (tag `flowvault/[0-9]+.[0-9]+.[0-9]+`), `flowvault_beta_release.yml` (tag `flowvault/*-beta.*`), `flowvault_internal_release.yml` (push `flowvault-release/*`) → build/publish **flowvault only**, mirroring the private/dev channel.
- Each release workflow passes the product argument to `bump_version.sh`.

---

## 10. Tests

Classify test files like source:
- **Shared** UI/validation (`ValidationTests`, `ComposableElementsTests`, `InputFormattingTest`, `UtilsTest`) → `common/src/test`, run under **both** modules.
- **FlowVault** (`MockCVVTest`, v2 `ResponseTest`, FlowDB assertions) → flowvault module.
- **Legacy** (restored from `main`): `DetokenizeTests`, `GetTests`, `RevealTest`, `CollectTest`, `CollectRequestBodyTest`, `CallbackResponseFormatTest`, `UpdateBySkyflowIdTest`, `InvokeConnectionTest`, `SoapConnectionTest`, `UnitTests`, and the v1 half of `ResponseTest`.

All suites must pass under both modules (Robolectric). Backward-compat rule from §6 applies when a test encodes beta-era wire keys.

---

## 11. Migration sequencing

- **Phase 0 — Branch.** Cut `SK-3041/package-split` from `origin/main`. Use `git mv` throughout to preserve blame/history. *(done)*
- **Phase 1 — `common/` + legacy module (from main).** Create `common/` + `skyvault/`; `git mv` neutral files → `common/`, v1 files → the legacy module; wire the shared source set; split `Utils`; extract the token client; neutralize `Configuration`; add `SdkInfo` + stamp in legacy `init()`. Legacy builds and all restored v1 tests pass; public surface unchanged.
- **Phase 2 — FlowVault module (from beta).** Create `flowvault/`; bring `FlowDB*` + v2 types from `1.28.0-beta.1` as core extensions; per-layer options/responses; stamp `SdkInfo` in flowvault `init()`; add v2 tests. Both modules build.
- **Phase 3 — Versioning + CI.** Bump-script product arg; per-module versions; PR-builds-both; per-product release workflows keyed to tag namespaces (+ mirrored internal channel).
- **Phase 4 — Samples + docs.** Point sample(s) at each published SDK; finalize this document.

---

## 12. Verification

1. **Build both:** `./gradlew :skyflow-android-sdk:assembleRelease :skyflow-flowvault-android-sdk:assembleRelease` — zero errors.
2. **Test both:** `./gradlew runOnGitHub` — all suites green (restored legacy + FlowVault + shared).
3. **Boundary:** the Konsist guard passes; a deliberate legacy-symbol import inside `common/` fails the FlowVault build.
4. **Visibility:** an app cannot resolve a core `internal` symbol (e.g. `Container.client`) — compile error.
5. **Real package manager (per SDK):** `publishToMavenLocal` each module, then build a sample depending on `com.skyflowapi.android:skyflow-android-sdk:<v>` and another on `…:skyflow-flowvault-android-sdk:1.0.0` (both from mavenLocal) — each compiles with `import Skyflow.*` and the correct per-contract API; shared `res/` (card-brand drawables, error animation) resolves in each.
6. **Release routing:** a plain-semver / `*-beta.*` tag triggers only legacy publish; a `flowvault/*` tag triggers only flowvault publish.
7. **Backward-compat smoke:** the legacy sample uses v1 signatures (`insert`, untyped `collect(Callback)`, `RevealElementInput(redaction=…)`, `CollectOptions(token=…)`) and compiles unchanged against `skyflow-android-sdk`.
