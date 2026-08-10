package Skyflow.reveal

import Skyflow.RedactionType

internal class FlowDBRevealRequestRecord(
    val token: String,
    val tokenGroupName: String? = null,
    val redaction: RedactionType? = null
)
