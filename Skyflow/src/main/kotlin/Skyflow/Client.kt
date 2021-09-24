package Skyflow

import Skyflow.core.APIClient
import Skyflow.reveal.GetByIdRecord
import Skyflow.utils.Utils
import android.util.Log
import android.webkit.URLUtil
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONObject
import kotlin.Exception
import kotlin.reflect.KClass

class Client (
    val configuration: Configuration
){
    private val apiClient = APIClient(configuration.vaultID, configuration.vaultURL, configuration.tokenProvider)

    fun insert(records:  JSONObject, options: InsertOptions? = InsertOptions(), callback: Callback){
        val isUrlValid =Utils.checkUrl(configuration.vaultURL)
        if(isUrlValid)
            apiClient.post(records, callback, options!!)
        else
            callback.onFailure(Exception("Url is not valid/not secure"))
    }

    fun <T:ContainerProtocol> container(type: KClass<T>) : Container<T> {
        return Container<T>(apiClient)
    }

    fun detokenize(records: JSONObject, callback: Callback) {
        val isUrlValid =Utils.checkUrl(configuration.vaultURL)
        if(isUrlValid)
            this.apiClient.get(records, callback)
        else
            callback.onFailure(Exception("Url is not valid/not secure"))

    }
    fun getById(records: JSONObject, callback: Callback)
    {
        val isUrlValid =Utils.checkUrl(configuration.vaultURL)
        if(isUrlValid) {
            try {
                val jsonArray = records.getJSONArray("records")
                var i = 0
                val result = mutableListOf<GetByIdRecord>()
                while (i < jsonArray.length()) {
                    val jsonObj = jsonArray.getJSONObject(i)
                    var skyflow_ids = jsonObj.get("ids")
                    try {
                        skyflow_ids = skyflow_ids as ArrayList<String>
                    } catch (e: Exception) {
                        callback.onFailure(Exception("skyflow_ids is not an ArrayList"))
                        return
                    }
                    val record = GetByIdRecord(skyflow_ids,
                        jsonObj.get("table").toString(),
                        jsonObj.get("redaction").toString())
                    result.add(record)
                    i++
                }
                this.apiClient.get(result, callback)
            } catch (e: Exception) {
                callback.onFailure(e)
            }
        }
        else
            callback.onFailure(Exception("Url is not valid/not secure"))

    }

    fun invokeGateway(gatewayConfig:JSONObject,callback: Callback)
    {
        try {
           val gatewayUrl = gatewayConfig.getString("gatewayURL")
           val checkUrl = Utils.checkUrl(gatewayUrl)
           if(checkUrl) {
               val requestBody = gatewayConfig.getJSONObject("requestBody")
               val constructed_body = Utils.gateWayConstructBody(requestBody)
               Log.d("body", constructed_body.toString())
             /*  if (!constructed_body.equals(JSONObject()))
                   this.apiClient.getGatewayConfig(gatewayUrl, constructed_body, callback)
               else
                   callback.onFailure(Exception("Invalid requestBody"))*/
           }
            else
           {
               callback.onFailure(Exception("Url is not valid/not secure"))
           }
        }
        catch (e:Exception)
        {
            callback.onFailure(e)
        }
    }


}