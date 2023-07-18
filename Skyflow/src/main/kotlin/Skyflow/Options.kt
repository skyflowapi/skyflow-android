package Skyflow

@Description("This class contains additional parameters for skyflow client configuration.")
class Options(
    @Description("The log level for the client.")
    val logLevel: LogLevel = LogLevel.ERROR,
    @Description("The working environment.")
    val env: Env = Env.PROD
) {
}