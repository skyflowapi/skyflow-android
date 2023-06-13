package Skyflow

@Description("This is the description for Options class")
class Options(
    @Description("Description for logLevel param")
    val logLevel: LogLevel = LogLevel.ERROR,
    @Description("Description for env param")
    val env: Env = Env.PROD
) {
}