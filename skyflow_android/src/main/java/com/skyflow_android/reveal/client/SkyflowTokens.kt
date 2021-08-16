package com.skyflowandroid.reveal.client

class SkyflowTokens: HashMap<String, String>() {

    fun getRequestQueryParams(redaction: String): String {
       return "/tokens?token_ids=" + this.keys.joinToString("&token_ids=") + "&redaction=$redaction"
    }
}