package com.skyflow_android.core

import android.util.Base64
import com.skyflow_android.collect.client.CollectAPICallback
import com.skyflow_android.collect.client.InsertOptions
import com.skyflowandroid.collect.client.JWTUtils
import com.skyflowandroid.collect.client.TokenProvider
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.json.JSONArray;
import org.json.JSONException;
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.google.gson.JsonObject

import com.google.gson.JsonElement

import com.google.gson.Gson




object JWTUtils {
    @Throws(java.lang.Exception::class)
    fun decoded(JWTEncoded: String): JSONObject {
        try {
            val split = JWTEncoded.split(".").toTypedArray()
            return JSONObject(getJson(split[1]));
        } catch (e: UnsupportedEncodingException) {
            println(e.toString())
            return JSONObject();
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

        private fun isTokenValid() : Boolean{
            return !JWTUtils.isExpired(token)
            }

    private fun getAccessToken(callback: SkyflowCallback) {
        if (!isTokenValid()) {
            tokenProvider.getAccessToken(object : SkyflowCallback {
                override fun success(responseBody: String) {
                    token = responseBody
                    callback.success(responseBody);
                }

                override fun failure(exception: Exception?) {
                    callback.failure(exception)
                }
            })
        } else {
            callback.success(token);
        }
    }


        internal fun post(records:JSONObject, callback: SkyflowCallback, options: InsertOptions){
            val collectApiCallback = CollectAPICallback(this, records, callback, options)
            this.getAccessToken(collectApiCallback)
        }

        fun constructBatchRequestBody(records: JSONObject, options: InsertOptions) : JSONObject{
            val postPayload:MutableList<Any> = mutableListOf();
            val insertTokenPayload:MutableList<Any> = mutableListOf();

            for (record1 in (records["records"]) as ArrayList<Any>) {
                for ((index, record) in record1 as HashMap<String, Any>) {

                    var temp: HashMap<String, Any> = hashMapOf()
                    temp = record as HashMap<String, Any>
                    temp["method"] = "POST"
                    temp["quorum"] = true
                    postPayload.add(temp)

                    if (options.tokens) {
                        var temp2: HashMap<String, Any> = hashMapOf()
                        temp2["method"] = "GET"
                        temp2["tableName"] = record["tableName"] as String
                        temp2["ID"] = "\$responses.\$index.records.0.skyflow_id"
                        temp2["tokenization"] = true
                        insertTokenPayload.add(temp2)
                    }
                }
            }
            val body : MutableList<Any> = mutableListOf()
            body.add(postPayload)
            body.add(insertTokenPayload)
            val temp = HashMap<String, Any>()
            temp["records"] = body
            return JSONObject(temp as Map<*, *>)
        }

    }


class DemoTokenProvider : TokenProvider{
    override fun getAccessToken(callback: SkyflowCallback) {
        val okhttp = OkHttpClient()
        val url = "http://localhost:8000/js/analystToken"
        val request = Request.Builder().url(url).build()
//        val call = okhttp.newCall(request)
        try {
            val thread = Thread {
                run {
                    try {
                        val call: Call = okhttp.newCall(request)
                        val response: Response = call.execute()
                        callback.success(response.toString())
                    } catch (e: IOException) {
                        callback.failure(e)
                    }
                }
            }
            thread.start()
        }catch (e: Exception){
            callback.failure(e)
        }
    }

}

//fun main(){
//    val tokenProvider = DemoTokenProvider()
//    val api = APIClient("ffe21f44f68a4ae3b4fe55ee7f0a85d6", "https://na1.area51.vault.skyflowapis.com/v1/vaults/%22", tokenProvider)
//
//    try{
//    val records = """{"records" : [{"tableName": "persons", "fields": {"cvv": "123", "cardExpiration":"1221",
//                         "cardNumber": "1232132132311231", "name": {"first_name": "Bob"}}},
//                        {"tableName": "persons", "fields": {"cvv": "123", "cardExpiration":"1221","cardNumber": "1232132132311231",
//                        "name": {"first_name": "Bobb"}}}]}"""
//
//        val gson = Gson()
//        val element: JsonElement = gson.fromJson(records, JsonElement::class.java)
//        val jsonObj = element.asJsonObject
//        var obj = JSONObject(records)
////        print(JSONObject(jsonObj.toString()))
//        api.constructBatchRequestBody(obj, InsertOptions());
//    }
//    catch (e:Exception){
//        print(e.printStackTrace())
//    }
//    var obj = JSONObject(records);
//    val map: HashMap<String, ArrayList<Any>> = Gson().fromJson(records, HashMap::class.java) as HashMap<String, ArrayList<Any>>
//    print(map)

//    var body: HashMap<String, Any> = Gson().fromJson(obj.toString(), HashMap::class.java);
//}