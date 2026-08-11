package Skyflow

/**
 * v1 public error type. In 1.27.0 `SkyflowError` was the thrown exception; the class moved to
 * core as [SkyflowInternalError] (neutral, shared), and this alias preserves the exact v1 name,
 * constructor, and `catch (e: SkyflowError)` behavior. (In the FlowVault SDK the name `SkyflowError`
 * is instead a typed response data class — a deliberate per-product difference.)
 */
typealias SkyflowError = SkyflowInternalError
