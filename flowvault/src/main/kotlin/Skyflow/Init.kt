package Skyflow

import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import com.skyflow_android.BuildConfig


fun init(configuration: Configuration) : Client{
    // Stamp this product's identity into core so error messages / telemetry self-report it.
    SdkInfo.name = BuildConfig.SDK_NAME
    SdkInfo.version = BuildConfig.SDK_VERSION
    val tag = Client::class.qualifiedName
    Logger.info(tag, Messages.CLIENT_INITIALIZED.getMessage(), configuration.options.logLevel)
    return Client(configuration)
}
