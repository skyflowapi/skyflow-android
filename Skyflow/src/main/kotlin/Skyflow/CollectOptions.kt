package Skyflow

import org.json.JSONArray
import org.json.JSONObject

@Description("Options for a Collect Element.")
class CollectOptions(
    @Description("If `true`, returns tokens for the collected data. Defaults to `true`.")
    val token:Boolean = true,
    @Description("Additional, non-sensitive data to insert into the vault. Uses the format of a [`records`](https://docs.skyflow.com/record/#RecordService_InsertRecord) object.")
    val additionalFields: JSONObject? = null,
    @Description("Upsert configuration for the element.")
    val upsert : JSONArray? = null
) {
}