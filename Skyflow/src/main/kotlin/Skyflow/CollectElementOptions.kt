package Skyflow

@Description("Contains the additional options for Collect Element.")
class CollectElementOptions(
    @Description("If `true`, the field is required. Defaults to `false`.")
    var required:Boolean = false,
    @Description("If `true`, enables the card icon. Defaults to `true`.")
    var enableCardIcon : Boolean = true,
    @Description("Format of the Collect element.")
    var format: String = ""
    ) {
}