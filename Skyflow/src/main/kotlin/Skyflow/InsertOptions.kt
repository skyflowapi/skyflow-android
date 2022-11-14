package Skyflow

import org.json.JSONArray

class InsertOptions(
    val tokens : Boolean = true,
    val upsert : JSONArray = JSONArray()
){}