package Skyflow.core

import Skyflow.*
import android.util.Base64
import Skyflow.collect.client.CollectAPICallback
import Skyflow.reveal.GetByIdRecord
import Skyflow.reveal.RevealApiCallback
import Skyflow.reveal.RevealByIdCallback
import Skyflow.reveal.RevealRequestRecord
import Skyflow.utils.Utils
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashSet


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
        val currentTime = (cal.timeInMillis / 1000 + 10).toString()
        return currentTime > expireTime
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getJson(strEncoded: String): String {
        val decodedBytes: ByteArray = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, Charset.forName("UTF-8"))
    }
}

class APIClient (
    val vaultId: String,
    val vaultURL: String,
    private val tokenProvider: TokenProvider,
    val logLevel: LogLevel,
    private var token: String = ""
){
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
                        Log.d("token success", "onSuccess: ")
                        Logger.info(tag, Messages.BEARER_TOKEN_RECEIVED.getMessage(), logLevel)
                        token = "Bearer $responseBody"
                        callback.onSuccess(token)
                    }

                    override fun onFailure(exception: Any) {
                        Logger.error(tag, Messages.RETRIEVING_BEARER_TOKEN_FAILED.getMessage(), logLevel)
                        val error = SkyflowError(SkyflowErrorCode.INVALID_BEARER_TOKEN)
                        callback.onFailure(error)
                    }
                })
            } else {
                callback.onSuccess(token)
            }
        }catch (e: Exception){
            val error = SkyflowError(SkyflowErrorCode.INVALID_BEARER_TOKEN,tag,logLevel)
            callback.onFailure(error)
        }
    }


    internal fun post(records: JSONObject, callback: Callback, options: InsertOptions){
        val finalRecords = Utils.constructBatchRequestBody(records, options,callback,logLevel)
        if(!finalRecords.toString().equals("{}"))
        {
            Log.d("post", "post: $records")
            val collectApiCallback = CollectAPICallback(this, records, callback, options,logLevel)
            this.getAccessToken(collectApiCallback)
        }
        else
            return

    }

     fun get(records:JSONObject, callback : Callback){

        try {
            if(vaultURL.isEmpty() || vaultURL.equals("/v1/vaults/"))
            {
                throw SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL,tag,logLevel)
            }
            else if(vaultId.isEmpty())
            {
                throw SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID,tag,logLevel)
            }
            else if (!records.has("records")) {
                throw SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND,tag,logLevel)
            }
            else if(records.get("records").toString().isEmpty())
            {
                throw SkyflowError(SkyflowErrorCode.EMPTY_RECORDS,tag,logLevel)
            }
            else if(records.get("records") !is JSONArray)
            {
                throw SkyflowError(SkyflowErrorCode.INVALID_RECORDS,tag,logLevel)
            }
            else {
                if(!records.has("records"))
                    throw SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND,tag,logLevel)
                else if(records.get("records") !is JSONArray)
                    throw SkyflowError(SkyflowErrorCode.INVALID_RECORDS,tag,logLevel)
                val jsonArray = records.getJSONArray("records")
                if(jsonArray.length() == 0)
                    throw SkyflowError(SkyflowErrorCode.EMPTY_RECORDS,tag,logLevel)
                val list = mutableListOf<RevealRequestRecord>()
                var i = 0
                while (i < jsonArray.length()) {
                    val jsonobj1 = jsonArray.getJSONObject(i)
                    if (!jsonobj1.has("token")) {
                        throw SkyflowError(SkyflowErrorCode.MISSING_TOKEN,tag,logLevel)
                    }
                    else if (jsonobj1.get("token").toString().isEmpty()) {
                        throw SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID,tag,logLevel)
                    }

                    else {
                        list.add(
                            RevealRequestRecord(
                                jsonobj1.get("token").toString(),
                                "null"
                            )
                        )
                    }
                    i++
                }
                val revealApiCallback = RevealApiCallback(callback, this, list)
                this.getAccessToken(revealApiCallback)
            }
        }catch (e: Exception){
            callback.onFailure(Utils.constructError(e))
        }
    }

    fun get(records: MutableList<GetByIdRecord>, callback: Callback) {
        val revealApiCallback = RevealByIdCallback(callback, this, records)
        this.getAccessToken(revealApiCallback)
    }

    fun invokeConnection(
        connectionConfig: ConnectionConfig,
        callback: Callback
    ) {
        val isValidResponseBody = Utils.checkDuplicateInResponseBody(connectionConfig.responseBody,callback,HashSet(),logLevel)
        if(!isValidResponseBody) return
        val requestBody = JSONObject()
        Utils.copyJSON(connectionConfig.requestBody,requestBody)
        val isBodyConstructed = Utils.constructRequestBodyForConnection(requestBody,callback, logLevel)
        connectionConfig.requestBody = JSONObject()
        if(isBodyConstructed)
        {
            val newConnection = connectionConfig.copy(requestBody = requestBody)
            val connection = ConnectionApiCallback(newConnection,callback, logLevel)
            this.getAccessToken(connection)
        }
        else
            return
    }
}