package Skyflow

import org.json.JSONArray

@Description("This is the description for InsertOptions class")
class InsertOptions(
    @Description("Description for tokens param")
    val tokens : Boolean = true,
    @Description("Description for upsert param")
    val upsert : JSONArray? = null
){}