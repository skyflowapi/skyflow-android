package Skyflow

@Description("Additional configuration for the Skyflow client.")
class Options(
    @Description("Log level for the client.")
    val logLevel: LogLevel = LogLevel.ERROR,
    @Description("Working environment.")
    val env: Env = Env.PROD
) {
}