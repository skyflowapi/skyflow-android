package Skyflow

import Skyflow.core.APIClient
import Skyflow.reveal.GetByIdRecord
import android.util.Log
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONObject
import java.lang.Exception
import kotlin.reflect.KClass

class Client (
     configuration: Configuration
){
    private val apiClient = APIClient(configuration.vaultID, configuration.vaultURL, configuration.tokenProvider)

    fun insert(records:  JSONObject, options: InsertOptions? = InsertOptions(), callback: Callback){
        apiClient.post(records, callback, options!!)
    }

    fun <T:ContainerProtocol> container(type: KClass<T>) : Container<T> {
        return Container<T>(apiClient)
    }

    fun detokenize(records: JSONObject, options: RevealOptions? = RevealOptions(), callback: Callback) {
        this.apiClient.get(records, callback)
    }
    fun getById(records: JSONObject, callback: Callback)
    {
        try {
            val jsonArray = records.getJSONArray("records")
            var i=0
            val result = mutableListOf<GetByIdRecord>()
            while (i<jsonArray.length())
            {
                val jsonObj = jsonArray.getJSONObject(i)
                var skyflow_ids = jsonObj.get("ids")
                try {
                    skyflow_ids = skyflow_ids  as ArrayList<String>
                }
                catch (e:Exception)
                {
                    callback.onFailure(Exception("skyflow_ids is not an ArrayList"))
                    return
                }
                val record = GetByIdRecord(skyflow_ids,jsonObj.get("table").toString(),jsonObj.get("redaction").toString())
                result.add(record)
                i++
            }
            this.apiClient.get(result, callback)
        }
        catch (e:Exception)
        {
            callback.onFailure(e)
        }

    }
}