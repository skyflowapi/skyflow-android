package Skyflow

import org.json.JSONArray
import org.json.JSONObject

@Description("This class contains the additional parameters for the collect method.")
class CollectOptions(
    @Description("Indicates whether tokens for the collected data should be returned or not. Defaults to 'true'.")
    val token:Boolean = true,
    @Description("Insert the non-PCI elements data into the vault in the format of the records object.")
    val additionalFields: JSONObject? = null,
    @Description("This parameter takes a JSONArray, if provided, upsert operation will be performed instead of insert.")
    val upsert : JSONArray? = null
) {
}