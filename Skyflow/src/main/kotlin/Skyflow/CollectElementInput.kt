package Skyflow

import com.Skyflow.collect.elements.validations.ValidationSet

@Description("Configuration for a Collect Element.")
class CollectElementInput(
    @Description("Table that the data belongs to.")
    internal var table: String? = null,
    @Description("Column that the data belongs to.")
    internal var column: String? = null,
    @Description("Type of the element.")
    internal var type: SkyflowElementType,
    @Description("Styles for the element.")
    internal var inputStyles: Styles = Styles(),
    @Description("Styles for the element's label.")
    internal var labelStyles:Styles=Styles(),
    @Description("Styles for the element's error text.")
    internal var errorTextStyles:Styles=Styles(),
    @Description("Label for the element.")
    internal var label: String = "",
    @Description("Placeholder text for the element.")
    internal var placeholder: String = "",
    @Description("Alt text for the element.")                      
    @Deprecated("altText parameter is deprecated" , level = DeprecationLevel.WARNING)
    internal var altText: String = "",
    @Description("Input validation rules for the element.")
    internal var validations : ValidationSet = ValidationSet()
) {
}