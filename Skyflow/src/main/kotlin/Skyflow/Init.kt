package Skyflow

import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage

@Description("This function initialises skyflow client.")
fun init(
    @Description("The configuration for the skyflow client.")
    configuration: Configuration
) : Client{
    val tag = Client::class.qualifiedName
    Logger.info(tag, Messages.CLIENT_INITIALIZED.getMessage(), configuration.options.logLevel)
    return Client(configuration)
}