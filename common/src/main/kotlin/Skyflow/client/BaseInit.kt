package Skyflow

import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage

internal fun <T : BaseSkyflowClient> initClient(
    sdkName: String,
    sdkVersion: String,
    logLevel: LogLevel,
    factory: () -> T
): T {
    // Stamp this product's identity into core so error messages / telemetry self-report it.
    SdkInfo.name = sdkName
    SdkInfo.version = sdkVersion
    val client = factory()
    Logger.info(client::class.qualifiedName, Messages.CLIENT_INITIALIZED.getMessage(), logLevel)
    return client
}
