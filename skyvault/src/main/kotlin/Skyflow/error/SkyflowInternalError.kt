package Skyflow

/**
 * The shared `common/` code throws under the neutral name `SkyflowInternalError`. In the legacy SDK
 * that name maps straight onto the v1 public [SkyflowError] class, so `common`'s throws produce real
 * `Skyflow.SkyflowError` instances (v1 backward compatibility). It is `internal` because 1.27.0 never
 * exposed a `SkyflowInternalError` type — consumers catch `SkyflowError`.
 *
 * (In the FlowVault SDK `SkyflowInternalError` is instead a distinct internal exception class, and
 * `SkyflowError` there is a typed response data class — see that module's error/.)
 */
internal typealias SkyflowInternalError = SkyflowError
