package Skyflow

import android.util.Log

class Configuration(
    val vaultID: String,
    var vaultURL: String,
    val tokenProvider: TokenProvider
){
    init {
        if( vaultURL.endsWith("/")){
            vaultURL += "v1/vaults/"
        } else{
            vaultURL += "/v1/vaults/"
        }
    }
}