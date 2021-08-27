package Skyflow

import Skyflow.core.APIClient
import Skyflow.reveal.RevealRequestRecord
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONObject
import kotlin.reflect.KClass

class Client (
    configuration: Configuration
){
    private val apiClient = APIClient(configuration.vaultID, configuration.vaultURL, configuration.tokenProvider)

    fun insert(records:  JSONObject, options: InsertOptions? = InsertOptions(), callback: Callback){
        apiClient.post(records, callback, options!!)
    }

    fun <T:ContainerProtocol> container(type: KClass<T>) : Container<T> {
        return Container<T>(this)
    }


    fun get(records: JSONObject, options: RevealOptions? = RevealOptions(), callback: Callback) {
        try {
            val obj = records.getJSONArray("records")
            val list = mutableListOf<RevealRequestRecord>()
            var i = 0
            while (i < obj.length()) {
                val jsonobj1 = obj.getJSONObject(i)
                list.add(
                    RevealRequestRecord(
                        jsonobj1.get("id").toString(),
                        jsonobj1.get("redaction").toString()
                    )
                )
                i++
            }
            this.apiClient.get(list, callback)
        }catch (e: Exception){
            callback.onFailure(e)
        }
    }
}