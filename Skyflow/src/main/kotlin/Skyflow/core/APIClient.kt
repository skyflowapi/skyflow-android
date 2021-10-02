package Skyflow.core

import Skyflow.*
import android.util.Base64
import Skyflow.collect.client.CollectAPICallback
import Skyflow.reveal.GetByIdRecord
import Skyflow.reveal.RevealApiCallback
import Skyflow.reveal.RevealByIdCallback
import Skyflow.reveal.RevealRequestRecord
import Skyflow.utils.Utils
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
    private var token: String = ""
){

    private fun isValidToken(token: String?): Boolean {
        return if (token != "") {
            !JWTUtils.isExpired(token!!)
        } else {
            false
        }
    }

    private fun getAccessToken(callback: Callback) {
        try {
            if (!isValidToken(token)) {
                tokenProvider.getBearerToken(object : Callback {
                    override fun onSuccess(responseBody: Any) {
                        token = "Bearer $responseBody"
                        callback.onSuccess(token)
                    }

                    override fun onFailure(exception: Exception) {
                        callback.onFailure(exception)
                    }
                })
            } else {
                callback.onSuccess(token)
            }
        }catch (e: Exception){
            callback.onFailure(e)
        }
    }


    internal fun post(records: JSONObject, callback: Callback, options: InsertOptions){
        val collectApiCallback = CollectAPICallback(this, records, callback, options)
        this.getAccessToken(collectApiCallback)
    }

    internal fun get(records:JSONObject, callback : Callback){

        try {
            val obj = records.getJSONArray("records")
            val list = mutableListOf<RevealRequestRecord>()
            var i = 0
            while (i < obj.length()) {
                val jsonobj1 = obj.getJSONObject(i)
                list.add(
                    RevealRequestRecord(
                        jsonobj1.get("token").toString(),
                        jsonobj1.get("redaction").toString()
                    )
                )
                i++
            }
            val revealApiCallback = RevealApiCallback(callback, this, list)
            this.getAccessToken(revealApiCallback)
        }catch (e: Exception){
            callback.onFailure(e)
        }

    }

    fun get(records: MutableList<GetByIdRecord>, callback: Callback) {
        val revealApiCallback = RevealByIdCallback(callback, this, records)
        this.getAccessToken(revealApiCallback)
    }

    fun invokeGateway(
        gatewayConfig: GatewayConfiguration,
        callback: Callback
    ) {
        val isValidResponseBody = Utils.checkDuplicateInResponseBody(gatewayConfig.responseBody,callback,HashSet())
        if(!isValidResponseBody) return
        val requestBody = gatewayConfig.requestBody
        val isBodyConstructed = Utils.constructRequestBodyForGateway(requestBody,callback)
        if(isBodyConstructed)
        {
            val newGateway = gatewayConfig.copy(requestBody = requestBody)
            val gateway = GatewayApiCallback(newGateway,callback)
            this.getAccessToken(gateway)
        }
    }
}