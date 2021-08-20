package com.skyflow_android.core

import com.google.gson.JsonObject
import com.skyflow_android.collect.client.CollectContainer
import com.skyflow_android.collect.client.InsertOptions
import com.skyflow_android.core.container.Container
import com.skyflow_android.core.container.ContainerOptions
import com.skyflow_android.core.container.ContainerProtocol
import com.skyflow_android.core.container.ContainerTypes
import org.json.JSONObject

class Skyflow (
    val vaultId: String,
    val vaultURL: String,
){

    val apiClient = APIClient(vaultId, vaultURL, DemoTokenProvider())

    public fun insert(records: JSONObject, options: InsertOptions? = InsertOptions(), callback: SkyflowCallback){
        apiClient.post(records, callback, options!!)
    }

        fun <T:ContainerProtocol> container(type:Class<T>) : Container<T>{
            return Container<T>(this)
        }


//    public fun reveal(records: HashMap<String, Any>, options: RevealOptions? = RevealOptions(), callback: SkyflowCallback)   {
//        let tokens : [[String : Any]] = records["records"] as! [[String : Any]]
//        var list : [RevealRequestRecord] = []
//        for token in tokens
//        {
//            list.append(RevealRequestRecord(token: token["id"] as! String, redaction: token["redaction"] as! String))
//        }
//        self.apiClient.get(records: list, callback: callback)
//    }
}

//fun main(){
//    var skyflow = Skyflow()
//}