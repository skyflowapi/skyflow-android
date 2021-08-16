package com.skyflowandroid.reveal.client

class SkyflowVault internal constructor(
    private val workspaceUrl: String,
    private val vaultId: String
) {
    private val VERSION = "/v1/vaults/"
    val vaultUrl: String

    private fun contructVaultUrl(): String {
        return StringBuilder(workspaceUrl)
            .append(VERSION)
            .append(vaultId).toString()
    }

    init {
        vaultUrl = contructVaultUrl()
    }
}