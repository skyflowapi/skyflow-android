package Skyflow

@Description("Contains the parameters required for reveal element.")
class RevealElementInput(
    @Description("Value of the Skyflow token.")
    internal var token: String? = null,
    @Description("Redaction to apply for retrieved data. E.g. RedactionType.MASKED.")
    internal var redaction: RedactionType? = RedactionType.PLAIN_TEXT,
    @Description("Styles applied to the element.")
    internal var inputStyles: Styles = Styles(),
    @Description("Styles applied to the label of the reveal element.")
    internal var labelStyles: Styles = Styles(),
    @Description("Styles applied to the errorText of the reveal element.")
    internal var errorTextStyles: Styles = Styles(),
    @Description("The label for the element.")
    internal var label: String = "",
    @Description("String that is shown before reveal, will show token if altText is not provided.")
    internal var altText: String = ""
) {}