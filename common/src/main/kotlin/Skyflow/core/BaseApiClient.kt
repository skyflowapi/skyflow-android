package Skyflow.core

import Skyflow.*

/**
 * Shared bearer-token lifecycle for both products' API clients — legacy `APIClient` and FlowVault
 * `FlowDBAPIClient` (see docs/sdk-split-plan.md).
 *
 * Fetching, validating (via [JWTUtils]), caching, and refreshing the bearer token is entirely
 * contract-agnostic, so it lives here once. Each product's api client extends this and adds only
 * its own request methods and endpoints (v1 `/v1/vaults/…` vs v2 `/v2/…`). Both the class and its
 * subclasses are `internal`, so this is purely an implementation detail — no public API impact.
 */
internal abstract class BaseApiClient(
    val vaultId: String,
    val vaultURL: String,
    private val tokenProvider: TokenProvider,
    val logLevel: LogLevel,
    private var token: String = "",
) {
    protected val tag: String? = this::class.qualifiedName

    // `protected open` so a product can harden it (see FlowDBAPIClient). The legacy client does NOT
    // override it, so skyvault keeps the exact 1.27.0 behavior (JWTUtils may throw on a malformed
    // token). This change is additive — no behavior change for existing callers.
    protected open fun isValidToken(token: String?): Boolean {
        return if (token != "") !JWTUtils.isExpired(token!!) else false
    }

    fun getAccessToken(callback: Callback) {
        try {
            if (!isValidToken(token)) {
                Logger.info(tag, Messages.RETRIEVING_BEARER_TOKEN.getMessage(), logLevel)
                tokenProvider.getBearerToken(object : Callback {
                    override fun onSuccess(responseBody: Any) {
                        Logger.info(tag, Messages.BEARER_TOKEN_RECEIVED.getMessage(), logLevel)
                        if (!isValidToken(responseBody.toString())) {
                            callback.onFailure(SkyflowInternalError(SkyflowErrorCode.INVALID_BEARER_TOKEN, tag, logLevel))
                        } else {
                            token = "Bearer $responseBody"
                            callback.onSuccess(token)
                        }
                    }

                    override fun onFailure(exception: Any) {
                        Logger.error(tag, Messages.RETRIEVING_BEARER_TOKEN_FAILED.getMessage(), logLevel)
                        callback.onFailure(SkyflowInternalError(SkyflowErrorCode.BEARER_TOKEN_REJECTED, tag, logLevel))
                    }
                })
            } else {
                callback.onSuccess(token)
            }
        } catch (e: Exception) {
            callback.onFailure(SkyflowInternalError(SkyflowErrorCode.INVALID_BEARER_TOKEN, tag, logLevel))
        }
    }
}
