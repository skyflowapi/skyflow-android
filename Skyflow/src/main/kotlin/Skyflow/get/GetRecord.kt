package Skyflow.get

internal class GetRecord(
    val skyflowIds: ArrayList<String>? = null,
    val table: String,
    val redaction: String? = null,
    val columnName: String? = null,
    val columnValues: ArrayList<String>? = null
) {
    override fun toString(): String {
        return "GetRecord(skyflowIds=$skyflowIds, table='$table', redaction=$redaction, columnName=$columnName, columnValues=$columnValues)"
    }
}