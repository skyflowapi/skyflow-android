package Skyflow


@Description("This class contains the parameters required for skyflow client initialisation.")
class Configuration(
    @Description("Unique ID of a vault.")
    val vaultID: String = "",
    @Description("The URL of the vault.")
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