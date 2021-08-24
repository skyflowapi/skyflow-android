package com.skyflow_android.collect.client

import com.skyflow_android.collect.elements.SkyflowElement
import org.json.JSONArray
import org.json.JSONObject

class CollectRequestBody {
    companion object {
        internal fun createRequestBody(elements: MutableList<SkyflowElement>) : String
        {
            val tableMap: HashMap<String,MutableList<SkyflowElement>> = HashMap()
            for (element in elements) {
                val tempRecordObject = JSONObject()
                tempRecordObject.put("table", element.tableName)
                if (tableMap[(element.tableName)] != null){
                    tableMap[(element.tableName)]!!.add(element)
                }
                else{
                    val tempArray = mutableListOf<SkyflowElement>()
                    tempArray.add(element)
                    tableMap[(element.tableName)] = tempArray
                }
            }

            val recordsArray = JSONArray()
            val requestObject = JSONObject()
            for ((key, value ) in tableMap){
                val recordObject = JSONObject()
                recordObject.put("table", key)
                val fieldsObject = JSONObject()
                for (element in value){
                    createJSONKey(fieldsObject, element.columnName, element.getOutput())
                }
                recordObject.put("fields", fieldsObject)
                recordsArray.put(recordObject)
            }
            requestObject.put("records", recordsArray)
            return requestObject.toString()
        }

        private fun createJSONKey(fieldsObject: JSONObject, columnName: String, value: String){
            val keys = columnName.split(".").toTypedArray()
            if(fieldsObject.has(keys[0])){
                if(keys.size > 1){
                    createJSONKey(fieldsObject.get(keys[0]) as JSONObject, keys.drop(1).joinToString("."), value)
                }
            }else{
                if(keys.size > 1){
                    val tempObject = JSONObject()
                    fieldsObject.put(keys[0], tempObject)
                    createJSONKey(tempObject, keys.drop(1).joinToString("."), value)
                }
                else {
                    fieldsObject.put(keys[0], value)
                }
            }
        }
    }
}