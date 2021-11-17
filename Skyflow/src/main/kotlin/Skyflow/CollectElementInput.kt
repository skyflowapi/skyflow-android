package Skyflow

import com.Skyflow.collect.elements.validations.SkyflowValidationSet

class CollectElementInput(internal var table: String? = null, internal var column: String? = null, internal var type: SkyflowElementType,
                          internal var inputStyles: Styles = Styles(), internal var labelStyles:Styles=Styles(), internal var errorTextStyles:Styles=Styles(),
                          internal var label: String = "",
                          internal var placeholder: String = "",
                          internal var altText: String = "",
                          internal var validations : SkyflowValidationSet = SkyflowValidationSet()
) {
}