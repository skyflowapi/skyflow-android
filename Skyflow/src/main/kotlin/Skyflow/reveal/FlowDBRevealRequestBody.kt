package Skyflow.reveal

import Skyflow.Label
import org.json.JSONArray
import org.json.JSONObject

internal class FlowDBRevealRequestBody {
    companion object {
        internal fun buildRequestBody(vaultID: String, elements: MutableList<Label>): JSONObject {
            val tokensArray = JSONArray()
            val tokenGroupRedactionsMap = LinkedHashMap<String, String?>()

            for (element in elements) {
                val input = element.revealInput
                val token = input.token ?: continue
                tokensArray.put(token)
                val tokenGroupName = input.tokenGroupName
                if (!tokenGroupName.isNullOrEmpty() && !tokenGroupRedactionsMap.containsKey(tokenGroupName)) {
                    tokenGroupRedactionsMap[tokenGroupName] = input.redaction
                }
            }

            val body = JSONObject()
                .put("vaultID", vaultID)
                .put("tokens", tokensArray)

            if (tokenGroupRedactionsMap.isNotEmpty()) {
                val tokenGroupRedactions = JSONArray()
                for ((tokenGroupName, redaction) in tokenGroupRedactionsMap) {
                    val entry = JSONObject().put("tokenGroupName", tokenGroupName)
                    if (redaction != null) entry.put("redaction", redaction)
                    tokenGroupRedactions.put(entry)
                }
                body.put("tokenGroupRedactions", tokenGroupRedactions)
            }

            return body
        }
    }
}
