package Skyflow

data class UpsertOptions(
    val tableName: String,
    val updateType: UpdateType,
    val uniqueColumns: List<String>
)
