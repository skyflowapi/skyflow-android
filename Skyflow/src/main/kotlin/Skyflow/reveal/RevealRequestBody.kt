package Skyflow.reveal

import Skyflow.Label
import org.json.JSONArray
import org.json.JSONObject

internal class RevealRequestBody {

    companion object {
        internal fun createRequestBody(elements: MutableList<Label>): JSONObject {
            val payload = JSONArray()
            for (element in elements) {
                val entry = JSONObject()
                entry.put("token", element.revealInput.token)
                entry.put("redaction", element.revealInput.redaction)
                payload.put(entry)
            }
            val result = JSONObject()
            result.put("records", payload)
            return result
        }
    }
}