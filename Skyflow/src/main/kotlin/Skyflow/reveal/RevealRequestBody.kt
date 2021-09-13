package Skyflow.reveal

import Skyflow.Callback
import Skyflow.Label

class RevealRequestBody {

    companion object {
        internal  fun createRequestBody(elements: MutableList<Label>) : String
        {
            val payload = mutableListOf<HashMap<String,String>>()
            for (element in elements) {

                    val entry = HashMap<String, String>()
                    entry["token"] = element.revealInput.token
                    entry["redaction"] = element.revealInput.redaction.toString()
                    payload.add(entry)

            }

            val result = HashMap<String,Any>()
            result["records"] = payload
            return result.toString()
        }
    }
}