package Skyflow.collect.client

import Skyflow.SkyflowElementType
import Skyflow.TextField
import org.json.JSONObject
import java.security.SecureRandom

/**
 * Captures the actual value entered into each CVV collect element so its token can be swapped
 * for a mock placeholder in the response, without the real value ever reaching the app.
 *
 * Because Android collect elements are in-process objects that directly know their own element
 * type and entered value, no identifier plumbing is needed: we read [TextField.fieldType] and
 * [TextField.getValue] at request-assembly time and key the entered value by table name (inserts)
 * or record id / skyflowID (updates), mirroring how the vault echoes records back.
 */
internal class CVVMap(
    val byTable: Map<String, Map<String, String>>,
    val byRecordId: Map<String, Map<String, String>>
) {
    fun isEmpty(): Boolean = byTable.isEmpty() && byRecordId.isEmpty()

    companion object {
        val EMPTY = CVVMap(emptyMap(), emptyMap())

        /**
         * Builds the map from a set of collect elements. Update elements carry their own skyflowID
         * (they are the ones filtered by a non-empty skyflowId) and are keyed by record id; insert
         * elements are keyed by table name.
         */
        internal fun capture(elements: List<TextField>): CVVMap {
            val byTable = LinkedHashMap<String, MutableMap<String, String>>()
            val byRecordId = LinkedHashMap<String, MutableMap<String, String>>()
            for (element in elements) {
                if (element.fieldType != SkyflowElementType.CVV) continue
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
         * caller rather than carried on the elements. All CVV elements are keyed by that record id.
         */
        internal fun captureForUpdate(elements: List<TextField>, skyflowId: String): CVVMap {
            val columns = LinkedHashMap<String, String>()
            for (element in elements) {
                if (element.fieldType != SkyflowElementType.CVV) continue
                columns[element.columnName] = element.getValue()
            }
            return if (columns.isEmpty()) EMPTY else CVVMap(emptyMap(), mapOf(skyflowId to columns))
        }
    }
}

private val secureRandom = SecureRandom()

/**
 * Generates a numeric mock CVV placeholder of [length] digits that is guaranteed to differ from
 * [actualValue]. Leading zeros are allowed because this is a display string, not a number. Each
 * attempt is a single secure-random draw (digit = nextInt(10) per position); it regenerates on the
 * rare collision. It runs a handful of times per submit, so speed is not a concern.
 */
internal fun generateMockCVV(length: Int, actualValue: String): String {
    if (length <= 0) return ""
    while (true) {
        val builder = StringBuilder(length)
        for (i in 0 until length) {
            builder.append(secureRandom.nextInt(10))
        }
        val candidate = builder.toString()
        if (candidate != actualValue) return candidate
    }
}

/**
 * Replaces the token value of every captured CVV column in [tokens] with a freshly generated mock
 * placeholder that matches the entered length and never equals that element's own entered value.
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
 *
 * Cross-element collision is intentionally ignored: a mock may coincidentally equal a *different*
 * element's entered value, but entered values never leave the device to the app, so there is no
 * observable leak. Only the per-element guarantee (mock != that element's own entered value) matters.
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
        val mock = if (enteredValue.isEmpty()) "" else generateMockCVV(enteredValue.length, enteredValue)
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
