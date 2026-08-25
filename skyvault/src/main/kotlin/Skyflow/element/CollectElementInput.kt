package Skyflow

import com.Skyflow.collect.elements.validations.ValidationSet

/**
 * Legacy (v1) collect-element input. Extends the neutral [BaseCollectElementInput] and keeps the
 * v1 public constructor param names (`table`, `skyflowID`), mapping them onto the neutral
 * `tableName` / `skyflowId` storage that core reads. Constructor shapes are byte-identical to
 * 1.27.0 (primary without `type` for the update interface; secondary with `type` + `altText`).
 */
class CollectElementInput : BaseCollectElementInput {
    constructor(
        table: String? = null,
        column: String? = null,
        inputStyles: Styles = Styles(),
        labelStyles: Styles = Styles(),
        errorTextStyles: Styles = Styles(),
        label: String = "",
        placeholder: String = "",
        validations: ValidationSet = ValidationSet(),
        skyflowID: String? = null
    ) : super() {
        this.tableName = table
        this.column = column
        this.inputStyles = inputStyles
        this.labelStyles = labelStyles
        this.errorTextStyles = errorTextStyles
        this.label = label
        this.placeholder = placeholder
        this.validations = validations
        this.skyflowId = skyflowID
    }

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
        validations: ValidationSet = ValidationSet(),
        skyflowID: String? = null
    ) : this(
        table,
        column,
        inputStyles,
        labelStyles,
        errorTextStyles,
        label,
        placeholder,
        validations,
        skyflowID
    ) {
        this.type = type
        this.altText = altText
    }

    // v1 read aliases onto the neutral base storage (preserve the 1.27.0 internal read surface).
    internal val table: String? get() = tableName
    internal val skyflowID: String? get() = skyflowId
}
