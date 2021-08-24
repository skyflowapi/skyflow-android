package com.skyflow_android.core

import android.util.Log
import com.skyflow_android.collect.client.InsertOptions
import com.skyflow_android.core.container.Container
import com.skyflow_android.core.container.ContainerProtocol
import com.skyflow_android.core.protocol.SkyflowCallback
import com.skyflow_android.reveal.client.RevealOptions
import com.skyflow_android.reveal.client.RevealRequestRecord
import org.json.JSONObject
import kotlin.reflect.KClass

class Skyflow (
    configuration: SkyflowConfiguration
){

    private val apiClient = APIClient(configuration.vaultId, configuration.workspaceUrl, configuration.tokenProvider)

    fun insert(records:  String, options: InsertOptions? = InsertOptions(), callback: SkyflowCallback){
        apiClient.post(records, callback, options!!)
    }

    fun <T:ContainerProtocol> container(type: KClass<T>) : Container<T>{
        return Container<T>(this)
    }


    fun get(records: String, options: RevealOptions? = RevealOptions(), callback: SkyflowCallback)   {
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