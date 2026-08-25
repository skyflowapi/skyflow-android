package Skyflow

/**
 * Legacy (v1) reveal-element input. Extends the neutral [BaseRevealElementInput] and adds the v1
 * per-token `redaction` field. The public constructor is byte-identical to 1.27.0 (redaction is the
 * 2nd param, default PLAIN_TEXT). FlowVault's RevealElementInput has no `redaction`.
 */
class RevealElementInput(
    token: String? = null,
    internal var redaction: RedactionType? = RedactionType.PLAIN_TEXT,
    inputStyles: Styles = Styles(),
    labelStyles: Styles = Styles(),
    errorTextStyles: Styles = Styles(),
    label: String = "",
    altText: String = ""
) : BaseRevealElementInput() {
    init {
        this.token = token
        this.inputStyles = inputStyles
        this.labelStyles = labelStyles
        this.errorTextStyles = errorTextStyles
        this.label = label
        this.altText = altText
    }
}
