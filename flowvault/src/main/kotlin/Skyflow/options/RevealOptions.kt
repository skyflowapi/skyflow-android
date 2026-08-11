package Skyflow

data class TokenGroupRedaction(
    val tokenGroupName: String,
    val redaction: String
)

data class RevealOptions(
    val tokenGroupRedactions: List<TokenGroupRedaction>? = null
)
