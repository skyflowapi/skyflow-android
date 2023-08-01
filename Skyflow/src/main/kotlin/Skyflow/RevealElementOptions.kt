package Skyflow

@Description("Configuration options for a Reveal Element.")
class RevealElementOptions(
    @Description("Format of the Reveal element.")
    var formatRegex : String = "",
    @Description("Text to replace with, when the format matches.")
    var replaceText:String? = null
) {}