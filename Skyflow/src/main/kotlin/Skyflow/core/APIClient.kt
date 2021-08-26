package Skyflow.core

import android.util.Base64
import Skyflow.InsertOptions
import Skyflow.Callback
import Skyflow.TokenProvider
import Skyflow.collect.client.CollectAPICallback
import Skyflow.reveal.RevealApiCallback
import Skyflow.reveal.RevealRequestRecord
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap


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
        if (!isValidToken(token)) {
            tokenProvider.getAccessToken(object : Callback {
                override fun onSuccess(responseBody: String) {
                    token = responseBody
                    callback.onSuccess(responseBody)
                }

                override fun onFailure(exception: Exception?) {
                    callback.onFailure(exception)
                }
            })
        } else {
            callback.onSuccess(token)
        }
    }


    internal fun post(records: String, callback: Callback, options: InsertOptions){
        val collectApiCallback = CollectAPICallback(this, records, callback, options)
        this.getAccessToken(collectApiCallback)
    }

    internal fun get(records:MutableList<RevealRequestRecord>, callback : Callback){
        val revealApiCallback = RevealApiCallback(callback, this, records)
        this.getAccessToken(revealApiCallback)
    }

    fun constructBatchRequestBody(records:  String, options: InsertOptions) : JSONObject{
        val postPayload:MutableList<Any> = mutableListOf()
        val insertTokenPayload:MutableList<Any> = mutableListOf()
        val obj = JSONObject(records)
        val obj1 = obj.getJSONArray("records")
        var i = 0
        while ( i < obj1.length())
        {
            val jsonObj = obj1.getJSONObject(i)
            val map = HashMap<String,Any>()
            map["tableName"] = jsonObj["table"]
            map["fields"] = jsonObj["fields"]
            map["method"] = "POST"
            map["quorum"] = true
            postPayload.add(map)
            if(options.tokens)
            {
                val temp2 = HashMap<String,Any>()
                temp2["method"] = "GET"
                temp2["tableName"] = jsonObj["table"] as String
                temp2["ID"] = "\$responses.$i.records.0.skyflow_id"
                temp2["tokenization"] = true
                insertTokenPayload.add(temp2)
            }
            i++
        }
        val body = HashMap<String,Any>()
        body["records"] = postPayload + insertTokenPayload
        return JSONObject(body as Map<*, *>)
    }

}