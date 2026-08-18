package Skyflow.collect.client

import Skyflow.SkyflowElementType
import Skyflow.TextField
import org.json.JSONObject

/**
 * Captures the actual value entered into each CVV collect element that opted in via
 * [Skyflow.CollectElementOptions.returnMockValue], so its token can be swapped for a fixed mock
 * placeholder in the response, without the real value ever reaching the app.
 *
 * Because Android collect elements are in-process objects that directly know their own element
 * type, options and entered value, no identifier plumbing is needed: we read [TextField.fieldType],
 * the element's options and [TextField.getValue] at request-assembly time and key the entered value
 * by table name (inserts) or record id / skyflowID (updates), mirroring how the vault echoes records
 * back. Elements that did not opt in are skipped entirely, so their real token is returned unchanged.
 */
internal class CVVMap(
    val byTable: Map<String, Map<String, String>>,
    val byRecordId: Map<String, Map<String, String>>
) {
    fun isEmpty(): Boolean = byTable.isEmpty() && byRecordId.isEmpty()

    companion object {
        val EMPTY = CVVMap(emptyMap(), emptyMap())

        /**
         * Builds the map from a set of collect elements. Only CVV elements that opted in via
         * [Skyflow.CollectElementOptions.returnMockValue] are captured. Update elements carry their
         * own skyflowID (they are the ones filtered by a non-empty skyflowId) and are keyed by record
         * id; insert elements are keyed by table name.
         */
        internal fun capture(elements: List<TextField>): CVVMap {
            val byTable = LinkedHashMap<String, MutableMap<String, String>>()
            val byRecordId = LinkedHashMap<String, MutableMap<String, String>>()
            for (element in elements) {
                if (element.fieldType != SkyflowElementType.CVV || !element.options.returnMockValue) continue
                val value = element.getValue()
                val skyflowId = element.skyflowId
                if (!skyflowId.isNullOrEmpty()) {
                    byRecordId.getOrPut(skyflowId) { LinkedHashMap() }[element.columnName] = value
                } else {
                    byTable.getOrPut(element.tableName) { LinkedHashMap() }[element.columnName] = value
                }
            }
            return CVVMap(byTable, byRecordId)
        }

        /**
         * Builds the map for the standalone update flow, where the skyflowID is supplied by the
         * caller rather than carried on the elements. Only opted-in CVV elements are keyed by that
         * record id.
         */
        internal fun captureForUpdate(elements: List<TextField>, skyflowId: String): CVVMap {
            val columns = LinkedHashMap<String, String>()
            for (element in elements) {
                if (element.fieldType != SkyflowElementType.CVV || !element.options.returnMockValue) continue
                columns[element.columnName] = element.getValue()
            }
            return if (columns.isEmpty()) EMPTY else CVVMap(emptyMap(), mapOf(skyflowId to columns))
        }
    }
}

/**
 * Fixed mock CVV placeholders, keyed by CVV length. Deliberately hardcoded (not random) so a
 * downstream proxy can reliably identify the mock for detokenization. If the value ever needs to
 * change, change it HERE — these two constants are the single source of truth.
 */
internal const val MOCK_CVV_3 = "999"
internal const val MOCK_CVV_4 = "9999"

/**
 * Returns the fixed mock CVV for a value of [length] digits: 4-or-more digits -> [MOCK_CVV_4],
 * otherwise -> [MOCK_CVV_3]. A zero/empty length yields "" (nothing was entered, nothing to mock).
 * The mock is a fixed value and may coincidentally equal the user's real CVV; that edge case is
 * accepted by design.
 */
internal fun mockCVV(length: Int): String = when {
    length <= 0 -> ""
    length >= 4 -> MOCK_CVV_4
    else -> MOCK_CVV_3
}

/**
 * Replaces the token value of every captured CVV column in [tokens] with the fixed mock placeholder
 * for the entered length (see [mockCVV]).
 *
 * tokens is keyed only by the TOP-LEVEL column name. Nested sub-fields appear as separate entries
 * in that column's list, each carrying a dotted "path" field. The replacement rule:
 *   - Flat column (no dot in column name): replace entries that have NO "path" field.
 *   - Nested column (e.g. "address.city.street"): split at first dot → topKey="address",
 *     nestedPath="city.street"; replace ONLY the entry whose "path" is EXACTLY "city.street".
 *     Exact equality prevents "city" from matching "city.street" or "city.ward".
 *
 * One mock is generated per column (same value applied to all matching entries). Updates are matched
 * by record id first, then inserts by table name. Non-CVV columns and hashed data are untouched.
 * The mock is a fixed value (see [mockCVV]); it may coincidentally equal an entered CVV, which is
 * acceptable — entered values never leave the device to the app.
 */
internal fun replaceCVVTokensInRecord(
    tokens: JSONObject,
    tableName: String,
    skyflowId: String,
    cvvMap: CVVMap
) {
    if (cvvMap.isEmpty()) return
    val columns = cvvMap.byRecordId[skyflowId]
        ?: (if (tableName.isNotEmpty()) cvvMap.byTable[tableName] else null)
        ?: return
    for ((column, enteredValue) in columns) {
        val dotIndex = column.indexOf('.')
        val topKey = if (dotIndex == -1) column else column.substring(0, dotIndex)
        val nestedPath = if (dotIndex == -1) null else column.substring(dotIndex + 1)

        val entries = tokens.optJSONArray(topKey) ?: continue
        val mock = mockCVV(enteredValue.length)
        for (i in 0 until entries.length()) {
            val entry = entries.optJSONObject(i) ?: continue
            val entryPath = if (entry.has("path")) entry.optString("path") else null
            val matches = if (nestedPath == null) entryPath == null else entryPath == nestedPath
            if (matches && entry.has("token")) {
                entry.put("token", mock)
            }
        }
    }
}
