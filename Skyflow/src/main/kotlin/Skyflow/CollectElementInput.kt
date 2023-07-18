package Skyflow

import com.Skyflow.collect.elements.validations.ValidationSet

@Description("This class contains the parameters that are required for the collect element.")
class CollectElementInput(
    @Description("The data belongs to this table.")
    internal var table: String? = null,
    @Description("The data should be inserted into the column.")
    internal var column: String? = null,
    @Description("Skyflow.ElementType enum.")
    internal var type: SkyflowElementType,
    @Description("The styles that are applied to the form element.")
    internal var inputStyles: Styles = Styles(),
    @Description("the styles that are applied to the label of the collect element.")
    internal var labelStyles:Styles=Styles(),
    @Description("The styles that are applied to the errorText of the collect element.")
    internal var errorTextStyles:Styles=Styles(),
    @Description("The Label of the form element.")
    internal var label: String = "",
    @Description("The placeholder for the form element.")
    internal var placeholder: String = "",
    @Description("the string that acts as an initial value to the collect element.")                      
    @Deprecated("altText parameter is deprecated" , level = DeprecationLevel.WARNING)
    internal var altText: String = "",
    @Description("A set of validations to the collect element.")
    internal var validations : ValidationSet = ValidationSet()
) {
}