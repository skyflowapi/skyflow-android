package Skyflow

@Description("Configuration for Reveal Elements.")
class RevealElementInput(
    @Description("A token to retrieve the value of.")
    internal var token: String? = null,
    @Description("Redaction type applied to the data. Defaults to `RedactionType.PLAIN_TEXT`.")
    internal var redaction: RedactionType? = RedactionType.PLAIN_TEXT,
    @Description("Input styles for the Reveal Element.")
    internal var inputStyles: Styles = Styles(),
    @Description("Styles for the Reveal Element's label.")
    internal var labelStyles: Styles = Styles(),
    @Description("Styles for the Reveal Element's error text.")
    internal var errorTextStyles: Styles = Styles(),
    @Description("Label for the Reveal Element.")
    internal var label: String = "",
    @Description("Alternative text for the Reveal Element.")
    internal var altText: String = ""
) {}