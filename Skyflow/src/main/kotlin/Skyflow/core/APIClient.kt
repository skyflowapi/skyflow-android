package Skyflow.core

import Skyflow.*
import android.util.Base64
import Skyflow.collect.client.CollectAPICallback
import Skyflow.get.GetAPICallback
import Skyflow.get.GetOptions
import Skyflow.reveal.GetByIdRecord
import Skyflow.reveal.RevealApiCallback
import Skyflow.reveal.RevealByIdCallback
import Skyflow.reveal.RevealRequestRecord
import Skyflow.soap.SoapApiCallback
import Skyflow.soap.SoapConnectionConfig
import Skyflow.soap.SoapValueCallback
import Skyflow.utils.Utils
import org.json.JSONArray
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*

object JWTUtils {
    @Throws(java.lang.Exception::class)
    fun decoded(JWTEncoded: String): JSONObject {
        return try {
            val split = JWTEncoded.split(".").toTypedArray()
            JSONObject(getJson(split[1]))
        } catch (e: UnsupportedEncodingException) {
            println(e.toString())
            JSONObject()
        }
    }

    fun isExpired(JWTEncoded: String): Boolean {
        val expireTime = decoded(JWTEncoded).getString("exp")
        val cal = Calendar.getInstance()
        val currentTime = ((cal.timeInMillis / 1000)).toString()
        return currentTime > expireTime
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getJson(strEncoded: String): String {
        val decodedBytes: ByteArray = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, Charset.forName("UTF-8"))
    }
}

internal class APIClient(
    val vaultId: String,
    val vaultURL: String,
    private val tokenProvider: TokenProvider,
    val logLevel: LogLevel,
    private var token: String = ""
) {
    private val tag = APIClient::class.qualifiedName
    private fun isValidToken(token: String?): Boolean {
        return if (token != "") {
            !JWTUtils.isExpired(token!!)
        } else {
            false
        }
    }

    fun getAccessToken(callback: Callback) {
        try {
            if (!isValidToken(token)) {
                Logger.info(tag, Messages.RETRIEVING_BEARER_TOKEN.getMessage(), logLevel)
                tokenProvider.getBearerToken(object : Callback {
                    override fun onSuccess(responseBody: Any) {
                        Logger.info(tag, Messages.BEARER_TOKEN_RECEIVED.getMessage(), logLevel)
                        if (!isValidToken(responseBody.toString())) {
                            val error =
                                SkyflowError(SkyflowErrorCode.INVALID_BEARER_TOKEN, tag, logLevel)
                            callback.onFailure(error)
                        } else {
                            token = "Bearer $responseBody"
                            callback.onSuccess(token)
                        }
                    }

                    override fun onFailure(exception: Any) {
                        Logger.error(
                            tag,
                            Messages.RETRIEVING_BEARER_TOKEN_FAILED.getMessage(),
                            logLevel
                        )
                        val error =
                            SkyflowError(SkyflowErrorCode.BEARER_TOKEN_REJECTED, tag, logLevel)
                        callback.onFailure(error)
                    }
                })
            } else {
                callback.onSuccess(token)
            }
        } catch (e: Exception) {
            val error = SkyflowError(SkyflowErrorCode.INVALID_BEARER_TOKEN, tag, logLevel)
            callback.onFailure(error)
        }
    }

    fun post(records: JSONObject, callback: Callback, options: InsertOptions) {
        try {
            val finalRecords = Utils.constructBatchRequestBody(records, options, logLevel)
            val collectApiCallback = CollectAPICallback(this, records, callback, options, logLevel)
            this.getAccessToken(collectApiCallback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun get(records: JSONObject, callback: Callback) {

        try {
            val list = constructBodyForDetokenize(records)
            val revealApiCallback = RevealApiCallback(callback, this, list)
            this.getAccessToken(revealApiCallback)
        } catch (e: Exception) {
            callback.onFailure(Utils.constructError(e))
        }
    }

    fun getById(records: MutableList<GetByIdRecord>, callback: Callback) {
        val revealApiCallback = RevealByIdCallback(callback, this, records)
        this.getAccessToken(revealApiCallback)
    }

    fun get(records: JSONObject, options: GetOptions? = GetOptions(false), callback: Callback) {
        try {
            Utils.validateGetInputAndOptions(records, options, logLevel)
            val requestBody = Utils.constructRequestBodyForGet(records)
            val getAPICallback = GetAPICallback(callback, this, requestBody, options)
            this.getAccessToken(getAPICallback)
        } catch (e: Exception) {
            callback.onFailure(Utils.constructError(e))
        }
    }

    fun invokeConnection(connectionConfig: ConnectionConfig, callback: Callback, client: Client) {
        val connection = ConnectionApiCallback(connectionConfig, callback, logLevel, client)
        this.getAccessToken(connection)
    }

    fun invokeSoapConnection(
        soapConnectionConfig: SoapConnectionConfig,
        client: Client,
        callback: Callback
    ) {
        val soapValueCallback = SoapValueCallback(client, soapConnectionConfig, callback, logLevel)
        val connection = SoapApiCallback(soapConnectionConfig, soapValueCallback, logLevel, client)
        this.getAccessToken(connection)
    }

    fun constructBodyForDetokenize(records: JSONObject): MutableList<RevealRequestRecord> {
        if (!records.has("records")) {
            throw SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND, tag, logLevel)
        }
        if (records.get("records") !is JSONArray) {
            throw SkyflowError(SkyflowErrorCode.INVALID_RECORDS, tag, logLevel)
        }
        if (records.getJSONArray("records").length() == 0) {
            throw SkyflowError(SkyflowErrorCode.EMPTY_RECORDS, tag, logLevel)
        }
        val jsonArray = records.getJSONArray("records")
        val list = mutableListOf<RevealRequestRecord>()
        var i = 0
        while (i < jsonArray.length()) {
            val recordObject = jsonArray.getJSONObject(i)
            if (!recordObject.has("token")) {
                throw SkyflowError(
                    SkyflowErrorCode.TOKEN_KEY_NOT_FOUND, tag, logLevel, arrayOf("$i")
                )
            } else if (recordObject.get("token").toString().isEmpty()) {
                throw SkyflowError(
                    SkyflowErrorCode.EMPTY_TOKEN, tag, logLevel, arrayOf("$i")
                )
            } else if (recordObject.has("redaction")) {
                val redaction = recordObject.get("redaction")
                if (redaction.toString().isEmpty()) {
                    throw SkyflowError(
                        SkyflowErrorCode.EMPTY_REDACTION_VALUE, tag, logLevel, arrayOf("$i")
                    )
                } else if (redaction !is RedactionType) {
                    throw SkyflowError(
                        SkyflowErrorCode.INVALID_REDACTION_TYPE, tag, logLevel, arrayOf("$i")
                    )
                } else {
                    list.add(
                        RevealRequestRecord(
                            recordObject.get("token").toString(),
                            redaction.toString()
                        )
                    )
                }
            } else {
                list.add(
                    RevealRequestRecord(
                        recordObject.get("token").toString(),
                        RedactionType.PLAIN_TEXT.toString()
                    )
                )
            }
            i++
        }
        return list
    }
}