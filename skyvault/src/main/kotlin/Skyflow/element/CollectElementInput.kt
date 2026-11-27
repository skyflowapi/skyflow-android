package Skyflow

import com.Skyflow.collect.elements.validations.ValidationSet

/**
 * Legacy (v1) collect-element input. Extends the neutral [BaseCollectElementInput] and keeps the
 * v1 public constructor param names (`table`, `skyflowID`), mapping them onto the neutral
 * `tableName` / `skyflowId` storage that core reads. Constructor shapes are byte-identical to
 * 1.27.0 (primary without `type` for the update interface; secondary with `type` + `altText`).
 *
 * A third constructor describes a **file-collection** field, mirroring iOS's
 * `CollectElementInput(..., type: .FILE_INPUT)`. It takes the skyvault-only [FileElementType] (not the
 * shared [SkyflowElementType]) and stores it in [fileType] rather than the base `type`, so file
 * element types never reach flowvault — which has its own `CollectElementInput` and never sees this
 * constructor. Pass the resulting input to `container.createFileElement(...)`.
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

    /**
     * File-collection input. Same class and option names as a text field, as in skyflow-js / iOS;
     * [type] is [FileElementType.FILE_INPUT] (single) or [FileElementType.MULTI_FILE_INPUT] (multi).
     * `inputStyles` styles the picker trigger, `labelStyles` the label and `errorTextStyles` the error
     * line; `placeholder` is the trigger's prompt before a file is chosen. For a single file, pass
     * [skyflowID] to attach to an existing record; it is ignored for [FileElementType.MULTI_FILE_INPUT].
     */
    constructor(
        table: String? = null,
        column: String? = null,
        inputStyles: Styles = Styles(),
        labelStyles: Styles = Styles(),
        errorTextStyles: Styles = Styles(),
        label: String = "",
        placeholder: String = "",
        type: FileElementType,
        skyflowID: String? = null
    ) : this(
        table,
        column,
        inputStyles,
        labelStyles,
        errorTextStyles,
        label,
        placeholder,
        ValidationSet(),
        skyflowID
    ) {
        this.fileType = type
    }

    /**
     * The file element type when this input describes a file field (set by the file constructor);
     * `null` for a text/card input. skyvault-only — kept off [BaseCollectElementInput] so flowvault's
     * public surface is untouched.
     */
    internal var fileType: FileElementType? = null

    // v1 read aliases onto the neutral base storage (preserve the 1.27.0 internal read surface).
    internal val table: String? get() = tableName
    internal val skyflowID: String? get() = skyflowId
}
