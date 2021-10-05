package Skyflow.collect.client

import Skyflow.Callback
import org.json.JSONArray
import org.json.JSONObject
import Skyflow.Element
import com.google.gson.JsonObject
import kotlin.Exception

class CollectRequestBody {
    companion object {
        internal fun createRequestBody(
            elements: MutableList<Element>,
            additionalFields: JSONObject,
            callback: Callback
        ) : String
        {
            val tableMap: HashMap<String,MutableList<CollectRequestRecord>> = HashMap()
            val tableWithColumn : HashSet<String> = HashSet()
            for (element in elements) {
                if (tableMap[(element.tableName)] != null){
                    if(tableWithColumn.contains(element.tableName+element.columnName))
                    {
                        callback.onFailure(Exception("duplicate column "+element.columnName+ " found in "+element.tableName))
                        return ""
                    }
                    tableWithColumn.add(element.tableName+element.columnName)
                    val obj = CollectRequestRecord(element.columnName,element.getValue())
                    tableMap[(element.tableName)]!!.add(obj)
                }
                else{
                    val obj = CollectRequestRecord(element.columnName,element.getValue())
                    val tempArray = mutableListOf<CollectRequestRecord>()
                    tempArray.add(obj)
                    tableWithColumn.add(element.tableName+element.columnName)
                    tableMap[(element.tableName)] = tempArray
                }
            }

            if(!additionalFields.equals(JsonObject()) && additionalFields.has("records"))
            {
                try {
                    val records = additionalFields.getJSONArray("records")
                    var i = 0
                    while (i < records.length()) {
                        val jsonobj = records.getJSONObject(i)
                        val tableName = jsonobj.get("table")
                        if(tableName !is String)
                            throw Exception("invalid table name in additionalFields")
                        if(!jsonobj.getJSONObject("fields").toString().equals("{}")) {
                            val fields = jsonobj.getJSONObject("fields")
                            val keys = fields.names()
                            val field_list = mutableListOf<CollectRequestRecord>()
                            for (j in 0 until keys!!.length()) {
                                val obj = CollectRequestRecord(keys.getString(j),
                                    fields.get(keys.getString(j)))
                                field_list.add(obj)
                            }
                            if (tableMap[tableName] != null) {
                                for (k in 0 until field_list.size) {
                                    if (tableWithColumn.contains(tableName + field_list.get(k).columnName)) {
                                        callback.onFailure(Exception("duplicate column " + field_list.get(
                                            k).columnName + " found in " + tableName))
                                        return ""
                                    } else {
                                        tableWithColumn.add(tableName + field_list.get(k).columnName)
                                    }
                                }
                                tableMap[tableName]!!.addAll(field_list)
                            } else {
                                val tempArray = mutableListOf<CollectRequestRecord>()
                                for (k in 0 until field_list.size) {
                                    if (tableWithColumn.contains(tableName + field_list.get(k).columnName)) {
                                        callback.onFailure(Exception("duplicate column " + field_list.get(
                                            k).columnName + " found in " + tableName))
                                        return ""
                                    } else
                                        tableWithColumn.add(tableName + field_list.get(k).columnName)
                                }
                                tempArray.addAll(field_list)
                                tableMap[tableName] = tempArray
                            }
                        }
                        i++
                    }
                }
                catch (e:Exception)
                {
                    callback.onFailure(e)
                    return ""
                }
            }
            val recordsArray = JSONArray()
            val requestObject = JSONObject()
            for ((key, value ) in tableMap){
                val recordObject = JSONObject()
                recordObject.put("table", key)
                val fieldsObject = JSONObject()
                for (element in value){
                    createJSONKey(fieldsObject, element.columnName, element.value)
                }
                recordObject.put("fields", fieldsObject)
                recordsArray.put(recordObject)
            }
            requestObject.put("records", recordsArray)
            return requestObject.toString()
        }

        private fun createJSONKey(fieldsObject: JSONObject, columnName: String, value: Any){
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