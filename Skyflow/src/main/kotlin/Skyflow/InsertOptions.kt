package Skyflow

import org.json.JSONArray

@Description("Contains additional parameters for the insert method.")
class InsertOptions(
    @Description("If `true`, returns tokens for the collected data. Defaults to `true`.")
    val tokens : Boolean = true,
    @Description("Upsert configuration for the element.")
    val upsert : JSONArray? = null
){}