package com.skyflowandroid.collect.client

import org.json.JSONArray
import org.json.JSONObject

class SkyflowRecord(
    private val tableName: String
) : JSONObject() {

    override fun put(name: String, value: Any ): JSONObject {
        val keys = name.split(".")
        val lastIndex = keys.size - 1
        var obj:JSONObject = this
        if(lastIndex == 0){
            return super.put(name, value)
        }
        for(i in keys.indices){
            if(i == lastIndex){
                obj.put(keys[i], value)
            } else {
                if(!obj.has(keys[i])){
                    obj.put(keys[i], JSONObject())
                } else {
                    if(obj.get(keys[i]) !is JSONObject){
                        obj.put(keys[i], JSONObject())
                    }
                }
                obj = obj.get(keys[i]) as JSONObject
            }
        }
        return this
    }

    fun getTokenizeRequestObject(index: Int = 0): JSONArray {
        val recordFields = JSONObject();
        recordFields.put("tableName", tableName);
        recordFields.put("method", "POST");
        recordFields.put("quorum", true);
        recordFields.put("fields", this);
        val tokenizeRequest = JSONObject();
        tokenizeRequest.put("tableName", tableName);
        tokenizeRequest.put("method", "GET");
        tokenizeRequest.put("ID", "\$responses.$index.records.$index.skyflow_id");
        tokenizeRequest.put("tokenization", true);
        val record = JSONArray();
        record.put(recordFields);
        record.put(tokenizeRequest);
        return record;
    }

}