package Skyflow

import com.Skyflow.collect.elements.validations.ValidationSet

/**
 * Neutral, contract-agnostic collect-element input shared by both SDKs (see docs/sdk-split-plan.md).
 *
 * Core reads the neutral [tableName] / [skyflowId] storage. Each product's `CollectElementInput`
 * extends this and provides its own public constructor param naming, mapping onto this storage:
 *  - legacy: `table` / `skyflowID`
 *  - FlowVault: `tableName` / `skyflowId`
 */
abstract class BaseCollectElementInput {
    internal var tableName: String? = null
    internal var column: String? = null
    internal var inputStyles: Styles = Styles()
    internal var labelStyles: Styles = Styles()
    internal var errorTextStyles: Styles = Styles()
    internal var label: String = ""
    internal var placeholder: String = ""
    internal var validations: ValidationSet = ValidationSet()
    internal var skyflowId: String? = null

    internal lateinit var type: SkyflowElementType

    @Deprecated(
        "altText parameter is deprecated",
        level = DeprecationLevel.WARNING
    )
    internal lateinit var altText: String
}
