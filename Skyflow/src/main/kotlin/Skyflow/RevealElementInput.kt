package Skyflow

class RevealElementInput(
    internal var token: String,
    internal var redaction: RedactionType,
    internal var styles: Styles = Styles(),
    internal var labelStyles:Styles = Styles(),
    internal var label: String = "",
    internal var defaultText:String = ""
) {

}