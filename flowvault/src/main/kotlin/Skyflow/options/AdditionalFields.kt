package Skyflow

data class AdditionalFields(val records: List<AdditionalFieldsRecord>)

data class AdditionalFieldsRecord(
    val tableName: String,
    val data: Map<String, Any>,
    val skyflowId: String? = null
)
