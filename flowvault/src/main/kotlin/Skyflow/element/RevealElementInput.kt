package Skyflow

/**
 * FlowVault (v2) reveal-element input. Extends the neutral [BaseRevealElementInput]. Unlike the
 * legacy input it carries no per-token `redaction` field — v2 redaction is expressed through
 * `RevealOptions.tokenGroupRedactions`. Constructor shape is byte-identical to 1.28.0-beta.1.
 */
class RevealElementInput(
    token: String? = null,
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
