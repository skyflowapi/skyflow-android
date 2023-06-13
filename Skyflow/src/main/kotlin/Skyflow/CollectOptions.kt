package Skyflow

import org.json.JSONArray
import org.json.JSONObject

@Description("This is the description for CollectOptions class")
class CollectOptions(
    @Description("Description for token param")
    val token:Boolean = true,
    @Description("Description for additionalFields param")
    val additionalFields: JSONObject? = null,
    @Description("Description for upsert param")
    val upsert : JSONArray? = null
) {
}