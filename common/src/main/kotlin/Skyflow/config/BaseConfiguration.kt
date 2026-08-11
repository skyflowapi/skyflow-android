package Skyflow

/**
 * Contract-agnostic configuration shared by both SDKs (see docs/sdk-split-plan.md).
 *
 * Holds only the neutral fields. Any contract-specific transform of these values (e.g. the
 * legacy v1 `.../v1/vaults/` URL suffix) belongs in that product's `Configuration` subclass,
 * NOT here — core must stay neutral. Core reads configuration through this base type.
 */
open class BaseConfiguration(
    val vaultID: String = "",
    var vaultURL: String = "",
    val tokenProvider: TokenProvider,
    val options: Options = Options(),
    // Neutral superset field: the FlowVault (v2) API client injects a custom OkHttpClient; the
    // legacy (v1) client ignores it and uses the default. Kept here so core stays contract-free.
    val okHttpClient: okhttp3.OkHttpClient = okhttp3.OkHttpClient(),
)
