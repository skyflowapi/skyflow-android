package com.Skyflow

import Skyflow.Callback
import Skyflow.Client
import Skyflow.Configuration
import Skyflow.Env
import Skyflow.Label
import Skyflow.LogLevel
import Skyflow.Options
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import Skyflow.SkyflowInternalError
import Skyflow.reveal.FlowDBRevealApiCallback
import Skyflow.reveal.RevealValueCallback
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Regression tests for the flowvault error-delivery fixes (re-audit H1 & H2).
 */
@RunWith(RobolectricTestRunner::class)
class ErrorDeliveryTest {

    private lateinit var client: Client

    @Before
    fun setup() {
        val configuration = Configuration(
            "vault123",
            "https://vault.url.com",
            AccessTokenProvider(),
            options = Options(logLevel = LogLevel.ERROR, env = Env.DEV)
        )
        client = Client(configuration)
    }

    // ---- H1: bearer-token failures must reach the app as a typed SkyflowError ----

    /**
     * getAccessToken hands the FlowDB callbacks a raw SkyflowInternalError. The callback must convert
     * it to the {errors:[{error:{code,description}}]} shape the app adapter's SkyflowError.fromJson
     * parses — so the app sees the real code (400) + message, NOT a mangled 500 from
     * fromJson(nonJsonString).
     */
    @Test
    fun `H1 bearer-token failure delivers a typed SkyflowError with the real code`() {
        var delivered: SkyflowError? = null
        // Mirrors the app-facing adapter in CollectContainer.collect / RevealContainer.reveal.
        val adapter = object : Callback {
            override fun onSuccess(responseBody: Any) {}
            override fun onFailure(exception: Any) {
                delivered = SkyflowError.fromJson(exception.toString())
            }
        }
        val apiCallback = FlowDBRevealApiCallback(adapter, client.apiClient, JSONObject())

        apiCallback.onFailure(SkyflowInternalError(SkyflowErrorCode.INVALID_BEARER_TOKEN, "tag", LogLevel.ERROR))

        assertNotNull(delivered)
        // Real code (400), not the fromJson-failure fallback (500).
        assertEquals(SkyflowErrorCode.INVALID_BEARER_TOKEN.code, delivered!!.httpCode)
        // Real reason, not the JSON-parser error text.
        assertFalse(
            "bearer error must not be mangled into a JSON-parse message",
            delivered!!.message?.contains("must begin with") == true
        )
    }

    // ---- H2: reveal must fire exactly one of onSuccess / onFailure ----

    /**
     * If the app's RevealCallback.onSuccess throws, the SDK must NOT also fire onFailure (the M4
     * contract, previously fixed for collect but not the reveal RevealValueCallback path).
     */
    @Test
    fun `H2 reveal app onSuccess throwing does not trigger onFailure`() {
        var onFailureCalled = false
        val appCallback = object : Callback {
            override fun onSuccess(responseBody: Any) { throw RuntimeException("app handler bug") }
            override fun onFailure(exception: Any) { onFailureCalled = true }
        }
        val revealValueCallback = RevealValueCallback(appCallback, mutableListOf<Label>(), LogLevel.ERROR)

        try {
            revealValueCallback.onSuccess("{}")
        } catch (e: RuntimeException) {
            // Expected: the app's own exception propagates out instead of being swallowed into onFailure.
        }

        assertFalse("onFailure must not fire when the app's onSuccess throws", onFailureCalled)
    }
}
