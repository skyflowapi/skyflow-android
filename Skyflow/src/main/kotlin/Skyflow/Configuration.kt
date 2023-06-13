package Skyflow


@Description("This is the description for Configuration class")
class Configuration(
    @Description("Description for vaultID param")
    val vaultID: String = "",
    @Description("Description for vaultURL param")
    var vaultURL: String = "",
    @Description("Description for tokenProvider param")
    val tokenProvider: TokenProvider,
    @Description("Description for options param")
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