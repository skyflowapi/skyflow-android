package Skyflow



class Configuration(
    val vaultID: String = "",
    var vaultURL: String = "",
    val tokenProvider: TokenProvider,
    val options: Options = Options(),
    val okHttpClient: okhttp3.OkHttpClient = okhttp3.OkHttpClient(),
)