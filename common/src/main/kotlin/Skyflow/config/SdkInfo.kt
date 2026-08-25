package Skyflow

/**
 * Per-product SDK version that core reads for error-message rendering (see docs/sdk-split-plan.md).
 * Each SDK stamps it from its own `BuildConfig.SDK_VERSION` inside `init()`; the default matches the
 * legacy product so a message rendered before `init()` runs is still well-formed.
 *
 * The SDK *name* is intentionally NOT held here: it must never appear in error messages (which stay
 * byte-identical to 1.27.0's "Android SDK v<version>"), and telemetry reads `BuildConfig.SDK_NAME`
 * directly (see Utils.fetchMetrics).
 */
internal object SdkInfo {
    var version: String = "1.27.0"
}
