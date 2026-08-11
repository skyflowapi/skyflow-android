package Skyflow

/**
 * Legacy (v1) configuration. Extends the neutral [BaseConfiguration] and preserves the v1
 * behavior of appending the `/v1/vaults/` path to the vault URL, exactly as before the split
 * (so the observable `vaultURL` value is byte-identical to 1.27.0). The FlowVault SDK's
 * `Configuration` extends the same base with no such suffix.
 */
class Configuration(
    vaultID: String = "",
    vaultURL: String = "",
    tokenProvider: TokenProvider,
    options: Options = Options(),
) : BaseConfiguration(vaultID, vaultURL, tokenProvider, options) {
    init {
        if (this.vaultURL.endsWith("/")) {
            this.vaultURL += "v1/vaults/"
        } else {
            this.vaultURL += "/v1/vaults/"
        }
    }
}
