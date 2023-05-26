package Skyflow

class RevealElementInput(
    internal var token: String? = null,
    internal var redaction: RedactionType? = RedactionType.PLAIN_TEXT,
    internal var inputStyles: Styles = Styles(),
    internal var labelStyles: Styles = Styles(),
    internal var errorTextStyles: Styles = Styles(),
    internal var label: String = "",
    internal var altText: String = ""
) {}