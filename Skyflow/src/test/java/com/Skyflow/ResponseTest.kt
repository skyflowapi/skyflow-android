package com.Skyflow

import Skyflow.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ResponseTest {

    // CollectResponse

    @Test
    fun `CollectResponse fromJson parses success record`() {
        val json = """{"records":[{"tableName":"cards","skyflowId":"id1","fields":{"card_number":"tok1"},"httpCode":200}]}"""
        val response = CollectResponse.fromJson(json)
        assertEquals(1, response.records.size)
        val record = response.records[0]
        assertEquals("cards", record.tableName)
        assertEquals("id1", record.skyflowId)
        assertEquals(200, record.httpCode)
        assertNull(record.error)
        assertNotNull(record.tokens)
        assertEquals("tok1", record.tokens?.get("card_number"))
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
