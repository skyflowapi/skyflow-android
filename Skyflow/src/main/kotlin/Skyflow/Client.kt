package Skyflow

import Skyflow.core.APIClient
import Skyflow.reveal.GetByIdRecord
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
        return Container<T>(apiClient)
    }

    fun detokenize(records: JSONObject, options: RevealOptions? = RevealOptions(), callback: Callback) {
        this.apiClient.get(records, callback)
    }
    fun getById(records: MutableList<GetByIdRecord>, callback: Callback)
    {
        this.apiClient.get(records, callback)
    }
}