package Skyflow

import Skyflow.core.APIClient
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

    fun get(records: JSONObject, options: RevealOptions? = RevealOptions(), callback: Callback) {
        this.apiClient.get(records, callback)
    }
}