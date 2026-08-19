package com.Skyflow

import Skyflow.AdditionalFieldsRecord
import Skyflow.Client
import Skyflow.CollectElementInput
import Skyflow.Configuration
import Skyflow.ContainerType
import Skyflow.Env
import Skyflow.LogLevel
import Skyflow.Options
import Skyflow.SkyflowElementType
import Skyflow.TextField
import Skyflow.collect.client.FlowDBCollectRequestBody
import Skyflow.create
import android.app.Activity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Regression tests for the update-record merge (re-audit F2): an update element and an
 * additionalFields update record that share a (tableName, skyflowId) must be sent as ONE record,
 * mirroring v1's "${table}_${skyflowID}" merge.
 */
@RunWith(RobolectricTestRunner::class)
class UpdateMergeTest {

    private lateinit var client: Client
    private lateinit var activity: Activity

    @Before
    fun setup() {
        val configuration = Configuration(
            "vault123",
            "https://vault.url.com",
            AccessTokenProvider(),
            options = Options(logLevel = LogLevel.ERROR, env = Env.DEV)
        )
        client = Client(configuration)
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
    }

    private fun updateElement(table: String, column: String, skyflowId: String, value: String): TextField {
        val input = CollectElementInput(
            tableName = table, column = column, type = SkyflowElementType.INPUT_FIELD,
            placeholder = column, skyflowId = skyflowId
        )
        val element = client.container(ContainerType.COLLECT).create(activity, input) as TextField
        element.onAttachedToWindow()
        element.setText(value)
        return element
    }

    @Test
    fun `element and additionalFields update with the same skyflowId merge into one record`() {
        val elem = updateElement("cards", "card_number", "rec-1", "4111111111111111")
        val add = AdditionalFieldsRecord("cards", mapOf("expiry" to "12/25"), "rec-1")

        val body = FlowDBCollectRequestBody.buildCombinedUpdateBody(
            "vault123", listOf(elem), listOf(add), LogLevel.ERROR
        )

        val records = body.getJSONArray("records")
        assertEquals("must be a single merged record", 1, records.length())
        val rec = records.getJSONObject(0)
        assertEquals("rec-1", rec.getString("skyflowID"))
        assertEquals("cards", rec.getString("tableName"))
        val data = rec.getJSONObject("data")
        assertEquals(elem.getValue(), data.getString("card_number"))
        assertEquals("12/25", data.getString("expiry"))
    }

    @Test
    fun `different skyflowIds produce separate update records`() {
        val elem = updateElement("cards", "card_number", "rec-1", "4111111111111111")
        val add = AdditionalFieldsRecord("cards", mapOf("expiry" to "12/25"), "rec-2")

        val body = FlowDBCollectRequestBody.buildCombinedUpdateBody(
            "vault123", listOf(elem), listOf(add), LogLevel.ERROR
        )

        assertEquals(2, body.getJSONArray("records").length())
    }

    @Test
    fun `additionalFields overwrites an element column on collision (last-writer-wins, matches v1)`() {
        val elem = updateElement("cards", "cvv", "rec-1", "111")
        val add = AdditionalFieldsRecord("cards", mapOf("cvv" to "999"), "rec-1")

        val body = FlowDBCollectRequestBody.buildCombinedUpdateBody(
            "vault123", listOf(elem), listOf(add), LogLevel.ERROR
        )

        val records = body.getJSONArray("records")
        assertEquals(1, records.length())
        assertEquals("999", records.getJSONObject(0).getJSONObject("data").getString("cvv"))
    }
}
