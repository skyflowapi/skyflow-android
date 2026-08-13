package Skyflow

/**
 * Neutral, contract-agnostic reveal-element input shared by both SDKs (see docs/sdk-split-plan.md).
 * Holds only the fields the shared reveal UI (`Label`) needs. Contract-specific fields live in the
 * per-product subclass — the legacy `RevealElementInput` adds `redaction` (per-token redaction);
 * the FlowVault one has none (redaction moved to `RevealOptions.tokenGroupRedactions`).
 */
abstract class BaseRevealElementInput {
    internal var token: String? = null
    internal var inputStyles: Styles = Styles()
    internal var labelStyles: Styles = Styles()
    internal var errorTextStyles: Styles = Styles()
    internal var label: String = ""
    internal var altText: String = ""
}
