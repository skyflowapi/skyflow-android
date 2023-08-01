package Skyflow

@Description("Defines the behavior of a class that can provide a bearer token.")
interface TokenProvider {
    @Description("Function that retrieves a Skyflow bearer token from your backend.")
    fun getBearerToken(
        @Description("Called when the bearer token is available.")
        callback: Callback
    )
}