package Skyflow

@Description("Defines the behavior of a class that can provide a bearer token.")
interface TokenProvider {
    @Description("Gets a bearer token.")
    fun getBearerToken(
        @Description("Called when the bearer token is available.")
        callback: Callback
    )
}