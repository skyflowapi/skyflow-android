package Skyflow

@Description("This interface defines the behavior of a class that can provide a bearer token.")
interface TokenProvider {
    @Description("This function gets a bearer token.")
    fun getBearerToken(
        @Description("This function is called when the bearer token is available.")
        callback: Callback
    )
}