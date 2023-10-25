package Skyflow

import com.Skyflow.collect.elements.validations.ValidationSet

class CollectElementInput(
    internal var table: String? = null,
    internal var column: String? = null,
    internal var inputStyles: Styles = Styles(),
    internal var labelStyles: Styles = Styles(),
    internal var errorTextStyles: Styles = Styles(),
    internal var label: String = "",
    internal var placeholder: String = "",
    internal var validations: ValidationSet = ValidationSet()
) {

    internal lateinit var type: SkyflowElementType

    @Deprecated(
        "altText parameter is deprecated",
        level = DeprecationLevel.WARNING
    )
    internal lateinit var altText: String

    constructor(
        table: String? = null,
        column: String? = null,
        type: SkyflowElementType,
        inputStyles: Styles = Styles(),
        labelStyles: Styles = Styles(),
        errorTextStyles: Styles = Styles(),
        label: String = "",
        placeholder: String = "",
        altText: String = "",
        validations: ValidationSet = ValidationSet()
    ) : this(
        table,
        column,
        inputStyles,
        labelStyles,
        errorTextStyles,
        label,
        placeholder,
        validations
    ) {
        this.type = type
        this.altText = altText
    }
}