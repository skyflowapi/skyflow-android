package Skyflow


@Description("Parameters to initialize a Skyflow client.")
class Configuration(
    @Description("ID of the vault to connect to.")
    val vaultID: String = "",
    @Description("URL of the vault to connect to.")
    var vaultURL: String = "",
    @Description("An implementation of the token provider interface.")
    val tokenProvider: TokenProvider,
    @Description("Additional options for configuration.")
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