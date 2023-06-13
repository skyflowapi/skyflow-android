package Skyflow

@Description("This is the description for RevealElementInput class")
class RevealElementInput(
    @Description("Description for token param")
    internal var token: String? = null,
    @Description("Description for redaction param")
    internal var redaction: RedactionType? = RedactionType.PLAIN_TEXT,
    @Description("Description for inputStyles param")
    internal var inputStyles: Styles = Styles(),
    @Description("Description for labelStyles param")
    internal var labelStyles: Styles = Styles(),
    @Description("Description for errorTextStyles param")
    internal var errorTextStyles: Styles = Styles(),
    @Description("Description for label param")
    internal var label: String = "",
    @Description("Description for altText param")
    internal var altText: String = ""
) {}