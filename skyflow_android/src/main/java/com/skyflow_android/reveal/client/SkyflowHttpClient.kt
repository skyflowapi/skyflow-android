package com.skyflowandroid.reveal.client

import android.os.Handler
import android.os.Looper
import android.util.Base64
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.*

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

internal class SkyflowHttpClient(
    private val vaultUrl: String,
    private val tokenProvider: TokenProvider
) {
    var url: URL = URL(vaultUrl)
    private var token: String? = null

    private fun isValidToken(token: String?): Boolean {
        if (token != null) {
            return !JWTUtils.isExpired(token);
        } else {
            return false;
        }
    }

    private fun getAccessToken(callback: ApiCallback) {
        if (!isValidToken(token)) {
            tokenProvider.getAccessToken(object : ApiCallback {
                override fun success(responseBody: String) {
                    token = responseBody
                    callback.success(responseBody);
                }

                override fun failure(exception: Exception?) {
                    callback.failure(exception)
                }
            })
        } else {
            callback.success(token!!);
        }
    }

    private fun getConnectionWithHeaders(token: String, connectionUrl: URL): HttpURLConnection {
        val con = connectionUrl.openConnection() as HttpURLConnection
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "Bearer $token");
        return con;
    }

    private fun request(
        connectionUrl: URL,
        method: String,
        requestBody: String?,
        callback: ApiCallback
    ) {
        this.getAccessToken(object : ApiCallback {
            override fun success(token: String) {
                try {
                    val thread = Thread {
                        run {
                            val con = getConnectionWithHeaders(token, connectionUrl);
                            con.requestMethod = method;
                            con.doOutput = method == "POST";
                            if (requestBody != null) {
                                con.outputStream.use { os ->
                                    val input: ByteArray = requestBody.toByteArray(Charsets.UTF_8);
                                    os.write(input, 0, input.size)
                                }
                            }
                            val inputStream: InputStream
                            val status: Int = con.responseCode
                            inputStream = if (status != HttpURLConnection.HTTP_OK) {
                                con.errorStream
                            } else {
                                con.inputStream
                            }
                            BufferedReader(
                                InputStreamReader(inputStream, "utf-8")
                            ).use { br ->
                                val response = StringBuilder()
                                var responseLine: String? = null
                                while (br.readLine().also { responseLine = it } != null) {
                                    response.append(responseLine!!.trim { it <= ' ' })
                                }
                                Handler(Looper.getMainLooper()).post {
                                    if (status == HttpURLConnection.HTTP_OK) {
                                        callback.success(response.toString());
                                    } else {
                                        callback.failure(Exception(response.toString()));
                                    }
                                }
                            }
                        }
                    }
                    thread.start();
                } catch (e: Exception) {
                    callback.failure(e)
                }
            }

            override fun failure(exception: Exception?) {
                callback.failure(exception)
            }

        })

    }

    fun post(requestBody: String, callback: ApiCallback) {
        request(url, "POST", requestBody, callback);
    }

    fun get(queryString: String, callback: ApiCallback) {
        request(URL(url.toString() + queryString), "GET", null, callback);
    }
}