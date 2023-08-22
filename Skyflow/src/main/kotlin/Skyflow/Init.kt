package Skyflow

import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage

@Description("Initializes the Skyflow client.")
fun init(
    @Description("Configuration for the Skyflow client.")
    configuration: Configuration
) : Client{
    val tag = Client::class.qualifiedName
    Logger.info(tag, Messages.CLIENT_INITIALIZED.getMessage(), configuration.options.logLevel)
    return Client(configuration)
}