package com.skyflowandroid.reveal.client

class SkyflowRevealClient(
    workspaceUrl: String,
    vaultId: String,
    private val tokenProvider: TokenProvider
) {
    private val skyflowVault = SkyflowVault(workspaceUrl, vaultId)
    private val httpClient = SkyflowHttpClient(skyflowVault.vaultUrl, tokenProvider)

    fun reveal(tokens: SkyflowTokens, redaction: String, callback: ApiCallback) {
        httpClient.get(tokens.getRequestQueryParams(redaction), callback);
    }

}