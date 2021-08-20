package com.skyflow_android.core

import com.skyflowandroid.collect.client.TokenProvider

class SkyflowConfiguration(
    workspaceUrl: String,
    vaultId: String,
    private val tokenProvider: TokenProvider
) {}