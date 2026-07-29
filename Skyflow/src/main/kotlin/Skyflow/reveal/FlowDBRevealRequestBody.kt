package Skyflow.reveal

import Skyflow.Label
import Skyflow.RevealOptions
import org.json.JSONArray
import org.json.JSONObject

internal class FlowDBRevealRequestBody {
    companion object {
        internal fun buildRequestBody(
            vaultID: String,
            elements: MutableList<Label>,
            options: RevealOptions? = null
        ): JSONObject {
            val tokensArray = JSONArray()
            for (element in elements) {
                val token = element.revealInput.token ?: continue
                tokensArray.put(token)
            }

            val body = JSONObject()
                .put("vaultID", vaultID)
                .put("tokens", tokensArray)

            val redactions = options?.tokenGroupRedactions
            if (!redactions.isNullOrEmpty()) {
                val tokenGroupRedactions = JSONArray()
                for (tgr in redactions) {
                    tokenGroupRedactions.put(
                        JSONObject()
                            .put("tokenGroupName", tgr.tokenGroupName)
                            .put("redaction", tgr.redaction)
                    )
                }
                body.put("tokenGroupRedactions", tokenGroupRedactions)
            }

            return body
        }
    }
}
