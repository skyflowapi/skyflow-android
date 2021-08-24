package com.skyflow_android.reveal.client

import com.skyflow_android.reveal.elements.SkyflowLabel

class RevealRequestBody {

    companion object {
        internal  fun createRequestBody(elements: MutableList<SkyflowLabel>) : String
        {
            val payload = mutableListOf<HashMap<String,String>>()
            for (element in elements) {
                val entry = HashMap<String,String>()
                entry["id"] = element.revealInput.id
                entry["redaction"] = element.revealInput.redaction
                payload.add(entry)
            }

            val result = HashMap<String,Any>()
            result["records"] = payload
            return result.toString()
        }
    }
}