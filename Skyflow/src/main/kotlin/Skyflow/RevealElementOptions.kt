package Skyflow

@Description("This class contaions additional options for the reveal element.")
class RevealElementOptions(
    @Description("The format for the element.")
    var formatRegex : String = "",
    @Description("The text to replace with, when the format matches.")
    var replaceText:String? = null
) {}