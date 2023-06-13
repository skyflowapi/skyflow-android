package Skyflow

@Description("This is the description for CollectElementOptions class")
class CollectElementOptions(
    @Description("Description for required param")
    var required:Boolean = false,
    @Description("Description for enableCardIcon param")
    var enableCardIcon : Boolean = true,
    @Description("Description for format param")
    var format: String = ""
    ) {
}