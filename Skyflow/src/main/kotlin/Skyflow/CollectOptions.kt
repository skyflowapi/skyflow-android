package Skyflow

data class CollectOptions(
    val upsert: List<UpsertOptions>? = null,
    val additionalFields: AdditionalFields? = null
)
