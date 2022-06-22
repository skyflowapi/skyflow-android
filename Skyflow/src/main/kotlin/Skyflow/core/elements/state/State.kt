/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package Skyflow

import org.json.JSONObject

open class State(var columnName:String,var isRequired:Boolean? = false) {

    open fun show(): String {
        return """
        "$columnName": {
            "isRequired": $isRequired
        }    """
    }

    open fun getInternalState() : JSONObject
    {
        val result = JSONObject()
        result.put("isRequired", isRequired)
        result.put("columnName", columnName)
        return result } }