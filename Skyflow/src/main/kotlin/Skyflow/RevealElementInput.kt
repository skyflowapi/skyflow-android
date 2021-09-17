package Skyflow

public class RevealElementInput(
    internal var token: String,
    internal var redaction: RedactionType,
    internal var inputStyles: Styles = Styles(),
    internal var labelStyles:Styles = Styles(),
    internal var errorTextStyles:Styles=Styles(),
    internal var label: String="",
    internal var altText:String = ""
) {

}