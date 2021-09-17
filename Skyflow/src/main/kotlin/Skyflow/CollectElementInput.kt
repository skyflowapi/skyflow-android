package Skyflow

class CollectElementInput(internal var table: String, internal var column: String, internal var type: SkyflowElementType,
                          internal var inputStyles: Styles = Styles(), internal var labelStyles:Styles=Styles(), internal var errorTextStyles:Styles=Styles(),
                          internal var label: String = "",
                          internal var placeholder: String = "",
                          internal var altText: String = ""
) {
}