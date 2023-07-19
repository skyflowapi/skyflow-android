package Skyflow

import org.json.JSONArray

@Description("Contains the additional parameters for the insert method.")
class InsertOptions(
    @Description("Indicates whether tokens for the collected data should be returned or not. Defaults to 'true'.")
    val tokens : Boolean = true,
    @Description("Takes a JSONArray, if provided, upsert operation will be performed instead of insert.")
    val upsert : JSONArray? = null
){}