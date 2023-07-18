package Skyflow

@Description("This class contains the additional options for skyflow collect element.")
class CollectElementOptions(
    @Description("Indicates whether the field is marked as required. Defaults to 'false'.")
    var required:Boolean = false,
    @Description("Indicates whether card icon should be enabled (only for CARD_NUMBER inputs).")
    var enableCardIcon : Boolean = true,
    @Description("The format for the element.")
    var format: String = ""
    ) {
}