package Skyflow

class CollectOptions(
    val tokens: Boolean = true,
    val upsert: List<UpsertOptions>? = null,
    val skyflowIds: Map<String, String>? = null
)
