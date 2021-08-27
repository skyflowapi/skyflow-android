package Skyflow


interface TokenProvider {
    fun getBearerToken(callback: Callback)
}