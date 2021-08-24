package com.skyflow_android.core

import com.skyflow_android.core.protocol.TokenProvider

class SkyflowConfiguration(
    val workspaceUrl: String,
    val vaultId: String,
    val tokenProvider: TokenProvider
)