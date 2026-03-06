package Skyflow.collect.client

internal data class UpdateRequestRecord(
    val table: String,
    val skyflowID: String,
    val columns: MutableMap<String, Any>
)
