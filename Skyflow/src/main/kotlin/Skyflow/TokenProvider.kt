package Skyflow


interface TokenProvider {
    fun getAccessToken(callback: Callback)
}