package Skyflow

@Description("Contains additional parameters for skyflow client configuration.")
class Options(
    @Description("Log level for the client.")
    val logLevel: LogLevel = LogLevel.ERROR,
    @Description("Working environment.")
    val env: Env = Env.PROD
) {
}