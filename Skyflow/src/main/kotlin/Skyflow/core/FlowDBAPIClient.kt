package Skyflow.core

import Skyflow.*
import Skyflow.collect.client.FlowDBCollectAPICallback
import Skyflow.reveal.FlowDBRevealApiCallback
import Skyflow.utils.Utils
import org.json.JSONObject

internal class FlowDBAPIClient(
    val vaultId: String,
    val vaultURL: String,
    private val tokenProvider: TokenProvider,
    val logLevel: LogLevel,
    private var token: String = "",
    val okHttpClient: okhttp3.OkHttpClient = okhttp3.OkHttpClient()
) {
    private val tag = FlowDBAPIClient::class.qualifiedName

    private fun isValidToken(token: String?): Boolean {
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
                            callback.onFailure(SkyflowError(SkyflowErrorCode.INVALID_BEARER_TOKEN, tag, logLevel))
                        } else {
                            token = "Bearer $responseBody"
                            callback.onSuccess(token)
                        }
                    }

                    override fun onFailure(exception: Any) {
                        Logger.error(tag, Messages.RETRIEVING_BEARER_TOKEN_FAILED.getMessage(), logLevel)
                        callback.onFailure(SkyflowError(SkyflowErrorCode.BEARER_TOKEN_REJECTED, tag, logLevel))
                    }
                })
            } else {
                callback.onSuccess(token)
            }
        } catch (e: Exception) {
            callback.onFailure(SkyflowError(SkyflowErrorCode.INVALID_BEARER_TOKEN, tag, logLevel))
        }
    }

    fun post(requestBody: JSONObject, callback: Callback, options: CollectOptions, endpoint: String = "insert") {
        try {
            val collectApiCallback = FlowDBCollectAPICallback(this, requestBody, callback, options, logLevel, endpoint)
            this.getAccessToken(collectApiCallback)
        } catch (e: Exception) {
            callback.onFailure(Utils.constructError(e))
        }
    }

    fun get(requestBody: JSONObject, callback: Callback) {
        try {
            val tokensArray = requestBody.optJSONArray("tokens")
            if (tokensArray == null || tokensArray.length() == 0) {
                throw SkyflowError(SkyflowErrorCode.EMPTY_RECORDS, tag, logLevel)
            }
            val revealApiCallback = FlowDBRevealApiCallback(callback, this, requestBody)
            this.getAccessToken(revealApiCallback)
        } catch (e: Exception) {
            callback.onFailure(Utils.constructError(e))
        }
    }
}
