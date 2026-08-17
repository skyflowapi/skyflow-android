package Skyflow

/**
 * FlowVault (v2) configuration. Extends the neutral [BaseConfiguration] and adds nothing to the
 * URL: unlike the legacy `Configuration` (which suffixes `/v1/vaults/` in its `init`), the v2
 * `/v2/...` path segments are applied inside `FlowDBAPIClient`, so the stored `vaultURL` is left
 * untouched. Exposes the same public param surface as 1.28.0-beta.1, including `okHttpClient`.
 */
class Configuration(
    vaultID: String = "",
    vaultURL: String = "",
    tokenProvider: TokenProvider,
    options: Options = Options(),
    okHttpClient: okhttp3.OkHttpClient = okhttp3.OkHttpClient(),
) : BaseConfiguration(vaultID, vaultURL, tokenProvider, options, okHttpClient)
