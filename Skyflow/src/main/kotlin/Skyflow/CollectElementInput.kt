package Skyflow

import com.Skyflow.collect.elements.validations.ValidationSet

class CollectElementInput(internal var table: String? = null, internal var column: String? = null, internal var type: SkyflowElementType,
                          internal var inputStyles: Styles = Styles(), internal var labelStyles:Styles=Styles(), internal var errorTextStyles:Styles=Styles(),
                          internal var label: String = "",
                          internal var placeholder: String = "",
                          @Deprecated("altText parameter is deprecated" , level = DeprecationLevel.WARNING) internal var altText: String = "",
                          internal var validations : ValidationSet = ValidationSet()
) {
}