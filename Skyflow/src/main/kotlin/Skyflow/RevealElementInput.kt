package Skyflow


class RevealElementInput(
    internal var token: String? = null,
    @Deprecated("redaction parameter is deprecated", level = DeprecationLevel.WARNING) internal var redaction: RedactionType? = null,
    internal var inputStyles: Styles = Styles(),
    internal var labelStyles:Styles = Styles(),
    internal var errorTextStyles:Styles=Styles(),
    internal var label: String="",
    internal var altText:String = ""
) {

}