package Skyflow

@Description("This is the description for TokenProvider interface")
interface TokenProvider {
    @Description("This is description for getBearerToken function")
    fun getBearerToken(
        @Description("Description for callback param")
        callback: Callback
    )
}