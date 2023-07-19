package Skyflow

@Description("Contaions additional options for the reveal element.")
class RevealElementOptions(
    @Description("Format for the element.")
    var formatRegex : String = "",
    @Description("Text to replace with, when the format matches.")
    var replaceText:String? = null
) {}