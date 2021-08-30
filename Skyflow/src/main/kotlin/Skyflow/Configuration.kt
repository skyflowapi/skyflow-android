package Skyflow

import android.util.Log

class Configuration(
    val vaultID: String,
    var vaultURL: String,
    val tokenProvider: TokenProvider
){
    init {
        vaultURL += if( vaultURL.last().toString() != "/"){
            "/v1/vaults/"
        } else{
            "v1/vaults/"
        }
    }
}