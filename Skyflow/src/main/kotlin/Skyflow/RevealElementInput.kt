package Skyflow

@Description("This class contains the parameters required for reveal element.")
class RevealElementInput(
    @Description("The value of the skyflow token.")
    internal var token: String? = null,
    @Description("The redaction to apply for retrieved data. E.g. RedactionType.MASKED.")
    internal var redaction: RedactionType? = RedactionType.PLAIN_TEXT,
    @Description("The styles applied to the element.")
    internal var inputStyles: Styles = Styles(),
    @Description("The styles applied to the label of the reveal element.")
    internal var labelStyles: Styles = Styles(),
    @Description("The styles applied to the errorText of the reveal element.")
    internal var errorTextStyles: Styles = Styles(),
    @Description("The label for the element.")
    internal var label: String = "",
    @Description("The string that is shown before reveal, will show token if altText is not provided.")
    internal var altText: String = ""
) {}