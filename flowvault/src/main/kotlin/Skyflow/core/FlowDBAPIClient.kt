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
    // Bearer-token lifecycle (isValidToken / getAccessToken) is inherited from BaseApiClient.

    fun post(requestBody: JSONObject, callback: Callback, options: CollectOptions, endpoint: String = "insert", cvvMap: CVVMap = CVVMap.EMPTY) {
        try {
            val collectApiCallback = FlowDBCollectAPICallback(this, requestBody, callback, options, logLevel, endpoint, cvvMap)
            this.getAccessToken(collectApiCallback)
        } catch (e: Exception) {
            callback.onFailure(Utils.constructError(e))
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
            callback.onFailure(Utils.constructError(e))
        }
    }
}
