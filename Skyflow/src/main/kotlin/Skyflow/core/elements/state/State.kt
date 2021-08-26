package Skyflow

open class State(var columnName:String,var isRequired:Boolean? = false) {

    open fun show():String
    {
        var result = ""
        result = """
        "$columnName": {
            "isRequired": $isRequired
        }
        """
        return result
    }

    open fun getState() : HashMap<String,Any>
    {
        val result = HashMap<String,Any>()
        result["isRequired"] = isRequired as Any
        result["columnName"] = columnName as Any

        return result
    }

}