package com.Skyflow

import Skyflow.*
import Skyflow.core.resolveSdkIdentity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ResponseTest {

    // --- B1 regression: the "__SKYFLOW_SDK_ID__" placeholder must never leak into error messages ---

    @Test
    fun `no error code leaks the SDK identity placeholder`() {
        SkyflowErrorCode.values().forEach {
            assertFalse("$it leaks __SKYFLOW_SDK_ID__", resolveSdkIdentity(it.message).contains("__SKYFLOW_SDK_ID__"))
        }
    }

    @Test
    fun `flowvault renders its own SDK version in error messages`() {
        // FlowVault stamps SdkInfo from its BuildConfig (1.0.0) at init(); simulate that here.
        SdkInfo.version = "1.0.0"
        val msg = SkyflowErrorCode.EMPTY_VAULT_URL.getMessage()
        assertFalse(msg.contains("__SKYFLOW_SDK_ID__"))
        assertTrue(msg.startsWith("Android SDK v1.0.0"))
    }

    // CollectResponse

    @Test
    fun `CollectResponse fromJson parses success record`() {
        val json = """{"records":[{"tableName":"cards","skyflowId":"id1","tokens":{"card_number":[{"token":"tok1","tokenGroupName":"group1"}]},"httpCode":200}]}"""
        val response = CollectResponse.fromJson(json)
        assertEquals(1, response.records.size)
        val record = response.records[0]
        assertEquals("cards", record.tableName)
        assertEquals("id1", record.skyflowId)
        assertEquals(200, record.httpCode)
        assertNull(record.error)
        assertNotNull(record.tokens)
        val tokenList = record.tokens?.get("card_number")
        assertEquals("tok1", tokenList?.firstOrNull()?.token)
    }

    @Test
    fun `CollectResponse fromJson preserves the nested-path token field`() {
        // FlowDB adds a `path` to tokens for nested JSON-path tokenization. CollectRecordToken captures
        // token/tokenGroupName/path (see CollectResponse.parseTokens); this verifies `path` survives parsing.
        val json = """{"records":[{"tableName":"cards","skyflowId":"id1","tokens":{"card_number":[{"token":"tok1","tokenGroupName":"grp1","path":"a.b.c"}]},"httpCode":200}]}"""
        val response = CollectResponse.fromJson(json)
        val token = response.records[0].tokens?.get("card_number")?.firstOrNull()
        assertEquals("tok1", token?.token)
        assertEquals("grp1", token?.tokenGroupName)
        assertEquals("a.b.c", token?.path)
    }

    @Test
    fun `CollectResponse fromJson parses error record`() {
        val json = """{"records":[{"tableName":"cards","error":"not found","httpCode":404}]}"""
        val response = CollectResponse.fromJson(json)
        assertEquals(1, response.records.size)
        val record = response.records[0]
        assertEquals(404, record.httpCode)
        assertEquals("not found", record.error)
        assertNull(record.tokens)
    }

    @Test
    fun `CollectResponse fromJson returns empty on invalid json`() {
        val response = CollectResponse.fromJson("not-json")
        assertEquals(0, response.records.size)
    }

    // RevealResponse

    @Test
    fun `RevealResponse fromJson parses success record`() {
        val json = """{"records":[{"token":"tok1","tokenGroupName":"group1","httpCode":200}]}"""
        val response = RevealResponse.fromJson(json)
        assertEquals(1, response.records.size)
        val record = response.records[0]
        assertEquals("tok1", record.token)
        assertEquals("group1", record.tokenGroupName)
        assertEquals(200, record.httpCode)
        assertNull(record.error)
    }

    @Test
    fun `RevealResponse fromJson parses error record`() {
        val json = """{"records":[{"token":"tok1","error":"token expired","httpCode":400}]}"""
        val response = RevealResponse.fromJson(json)
        val record = response.records[0]
        assertEquals("tok1", record.token)
        assertEquals("token expired", record.error)
        assertEquals(400, record.httpCode)
    }

    @Test
    fun `RevealResponse fromJson returns empty on invalid json`() {
        val response = RevealResponse.fromJson("{}")
        assertEquals(0, response.records.size)
    }

    @Test
    fun `RevealResponse fromJson parses typed metadata`() {
        val json = """{"records":[{"token":"tok1","httpCode":200,"metadata":{"tableName":"cards","skyflowId":"id1"}}]}"""
        val response = RevealResponse.fromJson(json)
        val record = response.records[0]
        assertNotNull(record.metadata)
        assertEquals("cards", record.metadata?.tableName)
        assertEquals("id1", record.metadata?.skyflowId)
    }

    // SkyflowError

    @Test
    fun `SkyflowError fromJson parses flat format`() {
        val json = """{"grpcCode":13,"httpCode":500,"message":"internal error","httpStatus":"INTERNAL"}"""
        val error = SkyflowError.fromJson(json)
        assertEquals(13, error.grpcCode)
        assertEquals(500, error.httpCode)
        assertEquals("internal error", error.message)
        assertEquals("INTERNAL", error.httpStatus)
    }

    @Test
    fun `SkyflowError fromJson parses wrapped error format`() {
        val json = """{"error":{"httpCode":404,"message":"not found"}}"""
        val error = SkyflowError.fromJson(json)
        assertEquals(404, error.httpCode)
        assertEquals("not found", error.message)
    }

    // Data class equality

    @Test
    fun `CollectOptions equality works as data class`() {
        val a = CollectOptions(upsert = null, additionalFields = null)
        val b = CollectOptions(upsert = null, additionalFields = null)
        assertEquals(a, b)
    }

    @Test
    fun `RevealOptions equality works as data class`() {
        val a = RevealOptions(tokenGroupRedactions = null)
        val b = RevealOptions(tokenGroupRedactions = null)
        assertEquals(a, b)
    }

    @Test
    fun `TokenGroupRedaction stores values correctly`() {
        val tgr = TokenGroupRedaction(tokenGroupName = "group1", redaction = "PLAIN_TEXT")
        assertEquals("group1", tgr.tokenGroupName)
        assertEquals("PLAIN_TEXT", tgr.redaction)
    }
}
