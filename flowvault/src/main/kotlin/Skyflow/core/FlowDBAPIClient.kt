package Skyflow.core

import Skyflow.*
import Skyflow.collect.client.CVVMap
import Skyflow.collect.client.FlowDBCollectAPICallback
import Skyflow.reveal.FlowDBRevealApiCallback
import Skyflow.utils.Utils
import org.json.JSONObject

internal class FlowDBAPIClient(
    vaultId: String,
    vaultURL: String,
    tokenProvider: TokenProvider,
    logLevel: LogLevel,
    token: String = "",
    val okHttpClient: okhttp3.OkHttpClient = okhttp3.OkHttpClient()
) : BaseApiClient(vaultId, vaultURL, tokenProvider, logLevel, token) {
    // Bearer-token lifecycle (getAccessToken) is inherited from BaseApiClient.

    // Harden the token check for v2: a malformed/opaque bearer token (no dots, or no `exp` claim)
    // makes JWTUtils throw. In the base flow that throw is uncaught on the token-provider callback
    // thread (a crash). Treat any such token as invalid so getAccessToken emits a typed
    // INVALID_BEARER_TOKEN instead. (The legacy client intentionally keeps the 1.27.0 behavior.)
    override fun isValidToken(token: String?): Boolean = try {
        super.isValidToken(token)
    } catch (e: Exception) {
        false
    }

    fun post(requestBody: JSONObject, callback: Callback, options: CollectOptions, endpoint: String = "insert", cvvMap: CVVMap = CVVMap.EMPTY) {
        try {
            val recordsArray = requestBody.optJSONArray("records")
            if (recordsArray == null || recordsArray.length() == 0) {
                // Nothing to collect (no elements and no additionalFields) — fail fast, mirroring reveal.
                throw SkyflowInternalError(SkyflowErrorCode.EMPTY_RECORDS, tag, logLevel)
            }
            val collectApiCallback = FlowDBCollectAPICallback(this, requestBody, callback, options, logLevel, endpoint, cvvMap)
            this.getAccessToken(collectApiCallback)
        } catch (e: Exception) {
            callback.onFailure(Utils.constructErrorResponse(e))
        }
    }

    fun get(requestBody: JSONObject, callback: Callback) {
        try {
            val tokensArray = requestBody.optJSONArray("tokens")
            if (tokensArray == null || tokensArray.length() == 0) {
                throw SkyflowInternalError(SkyflowErrorCode.EMPTY_RECORDS, tag, logLevel)
            }
            val revealApiCallback = FlowDBRevealApiCallback(callback, this, requestBody)
            this.getAccessToken(revealApiCallback)
        } catch (e: Exception) {
            callback.onFailure(Utils.constructErrorResponse(e))
        }
    }
}
