package Skyflow

import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import Skyflow.utils.Utils
import com.skyflow_android.BuildConfig


fun init(configuration: Configuration) : Client{
    val tag = Client::class.qualifiedName
    if (Utils.isNonGaVersion(BuildConfig.SDK_VERSION) && !Utils.isNonProdVaultUrl(configuration.vaultURL)) {
        Logger.warn(tag, Messages.BETA_BUILD_WARNING.getMessage(), configuration.options.logLevel)
    }
    Logger.info(tag, Messages.CLIENT_INITIALIZED.getMessage(), configuration.options.logLevel)
    return Client(configuration)
}