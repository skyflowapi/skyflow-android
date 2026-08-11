package Skyflow

/**
 * Mutable, per-product SDK identity that core reads for error messages and telemetry
 * (see docs/sdk-split-plan.md).
 *
 * Core must not depend on a specific product's [BuildConfig], so instead of reading it
 * directly, core reads this registration. Each SDK stamps it from its own
 * `BuildConfig.SDK_NAME` / `BuildConfig.SDK_VERSION` inside its `init()` entry point.
 * Defaults match the legacy product so behavior is unchanged if a message is rendered
 * before `init()` runs.
 */
object SdkInfo {
    var name: String = "skyflow-android-sdk"
    var version: String = "1.27.0"
}
