package Skyflow

import com.Skyflow.collect.elements.validations.ValidationSet

@Description("Contains the parameters that are required for the collect element.")
class CollectElementInput(
    @Description("Data belongs to this table.")
    internal var table: String? = null,
    @Description("Data should be inserted into the column.")
    internal var column: String? = null,
    @Description("Skyflow.ElementType enum.")
    internal var type: SkyflowElementType,
    @Description("Styles that are applied to the form element.")
    internal var inputStyles: Styles = Styles(),
    @Description("Styles that are applied to the label of the collect element.")
    internal var labelStyles:Styles=Styles(),
    @Description("Styles that are applied to the errorText of the collect element.")
    internal var errorTextStyles:Styles=Styles(),
    @Description("Label of the form element.")
    internal var label: String = "",
    @Description("Placeholder for the form element.")
    internal var placeholder: String = "",
    @Description("String that acts as an initial value to the collect element.")                      
    @Deprecated("altText parameter is deprecated" , level = DeprecationLevel.WARNING)
    internal var altText: String = "",
    @Description("Set of validations to the collect element.")
    internal var validations : ValidationSet = ValidationSet()
) {
}