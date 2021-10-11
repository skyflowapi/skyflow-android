package Skyflow



class Configuration(
    val vaultID: String,
    var vaultURL: String,
    val tokenProvider: TokenProvider,
    val options: Options = Options(),
){
    init {
        if( vaultURL.endsWith("/")){
            vaultURL += "v1/vaults/"
        } else{
            vaultURL += "/v1/vaults/"
        }
    }
}