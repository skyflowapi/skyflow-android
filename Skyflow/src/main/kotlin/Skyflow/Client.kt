package Skyflow

import Skyflow.core.APIClient
import Skyflow.reveal.RevealRequestRecord
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONObject
import kotlin.reflect.KClass

class Client (
    configuration: Configuration
){
    private val apiClient = APIClient(configuration.vaultId, configuration.workspaceUrl, configuration.tokenProvider)

    fun insert(records:  String, options: InsertOptions? = InsertOptions(), callback: Callback){
        apiClient.post(records, callback, options!!)
    }

    fun <T:ContainerProtocol> container(type: KClass<T>) : Container<T> {
        return Container<T>(this)
    }


    fun get(records: String, options: RevealOptions? = RevealOptions(), callback: Callback)   {
        val jsonobj = JSONObject(records)
        val obj = jsonobj.getJSONArray("records")
        val list = mutableListOf<RevealRequestRecord>()
        var i = 0
        while(i<obj.length())
        {
            val jsonobj1 = obj.getJSONObject(i)
            list.add(RevealRequestRecord(jsonobj1.get("id").toString(),jsonobj1.get("redaction").toString()))
            i++
        }
        this.apiClient.get(list,callback)
    }
}