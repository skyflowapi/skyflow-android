package Skyflow

import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage

@Description("This is the description for init function")
fun init(
    @Description("Description for configuration param")
    configuration: Configuration
) : Client{
    val tag = Client::class.qualifiedName
    Logger.info(tag, Messages.CLIENT_INITIALIZED.getMessage(), configuration.options.logLevel)
    return Client(configuration)
}