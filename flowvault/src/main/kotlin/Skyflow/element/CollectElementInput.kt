package Skyflow

import com.Skyflow.collect.elements.validations.ValidationSet

/**
 * FlowVault (v2) collect-element input. Extends the neutral [BaseCollectElementInput] and keeps the
 * v2 public constructor param names (`tableName`, `skyflowId`), mapping them onto the neutral
 * storage that core reads. Constructor shapes are byte-identical to the beta (1.28.0-beta.1)
 * surface: a primary without `type` (used by the update interface) and a secondary with
 * `type` + `altText`.
 */
class CollectElementInput : BaseCollectElementInput {
    constructor(
        tableName: String? = null,
        column: String? = null,
        inputStyles: Styles = Styles(),
        labelStyles: Styles = Styles(),
        errorTextStyles: Styles = Styles(),
        label: String = "",
        placeholder: String = "",
        validations: ValidationSet = ValidationSet(),
        skyflowId: String? = null
    ) : super() {
        this.tableName = tableName
        this.column = column
        this.inputStyles = inputStyles
        this.labelStyles = labelStyles
        this.errorTextStyles = errorTextStyles
        this.label = label
        this.placeholder = placeholder
        this.validations = validations
        this.skyflowId = skyflowId
    }

    constructor(
        tableName: String? = null,
        column: String? = null,
        type: SkyflowElementType,
        inputStyles: Styles = Styles(),
        labelStyles: Styles = Styles(),
        errorTextStyles: Styles = Styles(),
        label: String = "",
        placeholder: String = "",
        altText: String = "",
        validations: ValidationSet = ValidationSet(),
        skyflowId: String? = null
    ) : this(
        tableName,
        column,
        inputStyles,
        labelStyles,
        errorTextStyles,
        label,
        placeholder,
        validations,
        skyflowId
    ) {
        this.type = type
        this.altText = altText
    }
}
