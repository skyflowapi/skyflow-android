package com.skyflow_android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.skyflow_android.collect.client.InsertOptions
import com.skyflow_android.core.APIClient
import com.skyflow_android.core.DemoTokenProvider
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testingFunction()
    }

    fun testingFunction(){
        val tokenProvider = DemoTokenProvider()
        val api = APIClient("ffe21f44f68a4ae3b4fe55ee7f0a85d6", "https://na1.area51.vault.skyflowapis.com/v1/vaults/%22", tokenProvider)

        try{
            val records = """{"records" : [{"tableName": "persons", "fields": {"cvv": "123", "cardExpiration":"1221",
                         "cardNumber": "1232132132311231", "name": {"first_name": "Bob"}}},
                        {"tableName": "persons", "fields": {"cvv": "123", "cardExpiration":"1221","cardNumber": "1232132132311231",
                        "name": {"first_name": "Bobb"}}}]}"""

//            val gson = Gson()
//            val element: JsonElement = gson.fromJson(records, JsonElement::class.java)
//            val jsonObj = element.asJsonObject
            var obj = JSONObject(records)
//        print(JSONObject(jsonObj.toString()))
            api.constructBatchRequestBody(obj, InsertOptions());
    }catch (e: Exception){
            Log.d("TAG", "testingFunction: " + e.stackTrace)
    }}

}