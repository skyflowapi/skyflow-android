package Skyflow

import com.Skyflow.collect.elements.validations.ValidationSet

@Description("This is the description for CollectElementInput class")
class CollectElementInput(
    @Description("Description for table param")
    internal var table: String? = null,
    @Description("Description for column param")
    internal var column: String? = null,
    @Description("Description for type param")
    internal var type: SkyflowElementType,
    @Description("Description for inputStyles param")
    internal var inputStyles: Styles = Styles(),
    @Description("Description for labelStyles param")
    internal var labelStyles:Styles=Styles(),
    @Description("Description for errorTextStyles param")
    internal var errorTextStyles:Styles=Styles(),
    @Description("Description for label param")
    internal var label: String = "",
    @Description("Description for placeholder param")
    internal var placeholder: String = "",
    @Description("Description for altText param")                      
    @Deprecated("altText parameter is deprecated" , level = DeprecationLevel.WARNING)
    internal var altText: String = "",
    @Description("Description for validations param")
    internal var validations : ValidationSet = ValidationSet()
) {
}