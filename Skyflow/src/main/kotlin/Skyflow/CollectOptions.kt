package Skyflow

import org.json.JSONArray
import org.json.JSONObject

class CollectOptions(val token:Boolean = true, val additionalFields: JSONObject? = null, val upsert : JSONArray? = null) {
}