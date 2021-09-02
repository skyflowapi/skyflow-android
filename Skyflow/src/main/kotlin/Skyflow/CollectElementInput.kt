package Skyflow

class CollectElementInput(internal var table: String, internal var column: String,internal var type: SkyflowElementType,
                          internal var styles: Styles = Styles(),internal var label: String = "",
                          internal var placeholder: String = ""
) {
}