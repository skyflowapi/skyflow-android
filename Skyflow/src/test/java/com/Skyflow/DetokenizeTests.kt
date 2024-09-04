package com.Skyflow

import Skyflow.*
import Skyflow.core.APIClient
import Skyflow.utils.Utils
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DetokenizeTests {

    lateinit var skyflow: Client
    private lateinit var logLevel: LogLevel
    private lateinit var request: Request
    private lateinit var apiClient: APIClient

    private val apiClientTag = APIClient::class.qualifiedName
    private val utilsTag = Utils::class.qualifiedName

    @Before
    fun setup() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098d2c09",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        skyflow = Client(configuration)
        logLevel = LogLevel.ERROR
        request = Request.Builder().url("https://www.url.com").build()
        apiClient = APIClient(
            "b359c43f1b84f098d2c09193",
            "https://vaulturl.com/v1/vaults",
            AccessTokenProvider(),
            LogLevel.ERROR
        )
    }

    private fun getErrorMessage(error: JSONObject): String {
        val errors = error.getJSONArray("errors")
        val skyflowError = errors.getJSONObject(0).get("error") as SkyflowError
        return skyflowError.getErrorMessage()
    }

    @Test
    fun testEmptyVaultID() {
        val skyflowConfiguration = Configuration(
            vaultID = "",
            vaultURL = "https://vaulturl.com",
            AccessTokenProvider()
        )

        val detokenizeRecords = JSONObject()
        val detokenizeRecordsArray = JSONArray()

        val recordObj = JSONObject()
        recordObj.put("token", "895630c8-cb87-4876-8df5-0a785ebfcdda")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)

        detokenizeRecordsArray.put(recordObj)
        detokenizeRecords.put("records", detokenizeRecordsArray)

        val skyflowClient = init(skyflowConfiguration)

        skyflowClient.detokenize(records = detokenizeRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID, tag = utilsTag)
                Assert.assertEquals(
                    skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject)
                )
            }
        })

    }

    @Test
    fun testEmptyVaultURL() {
        val skyflowConfiguration = Configuration(
            vaultID = "vault_id",
            vaultURL = "",
            AccessTokenProvider()
        )

        val detokenizeRecords = JSONObject()
        val detokenizeRecordsArray = JSONArray()

        val recordObj = JSONObject()
        recordObj.put("token", "895630c8-cb87-4876-8df5-0a785ebfcdda")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)

        detokenizeRecordsArray.put(recordObj)
        detokenizeRecords.put("records", detokenizeRecordsArray)

        val skyflowClient = init(skyflowConfiguration)

        skyflowClient.detokenize(records = detokenizeRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL, tag = utilsTag)
                Assert.assertEquals(
                    skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject)
                )
            }

        })
    }

    @Test
    fun testInvalidVaultURL() {
        val skyflowConfiguration = Configuration(
            vaultID = "vault_id",
            vaultURL = "http://vault.url.com",
            AccessTokenProvider()
        )

        val detokenizeRecords = JSONObject()
        val detokenizeRecordsArray = JSONArray()

        val recordObj = JSONObject()
        recordObj.put("token", "895630c8-cb87-4876-8df5-0a785ebfcdda")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)

        detokenizeRecordsArray.put(recordObj)
        detokenizeRecords.put("records", detokenizeRecordsArray)

        val skyflowClient = init(skyflowConfiguration)

        skyflowClient.detokenize(records = detokenizeRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(
                    SkyflowErrorCode.INVALID_VAULT_URL,
                    tag = utilsTag,
                    params = arrayOf(skyflowConfiguration.vaultURL)
                )
                Assert.assertEquals(
                    skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject)
                )
            }
        })
    }

    @Test
    fun testMissingToken() {
        val detokenizeRecords = JSONObject()
        val detokenizeRecordsArray = JSONArray()

        val recordObj = JSONObject()
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)

        detokenizeRecordsArray.put(recordObj)
        detokenizeRecords.put("records", detokenizeRecordsArray)

        val skyflowError =
            SkyflowError(SkyflowErrorCode.TOKEN_KEY_NOT_FOUND, apiClientTag, logLevel, arrayOf("0"))

        try {
            apiClient.constructBodyForDetokenize(detokenizeRecords)
        } catch (exception: Exception) {
            Assert.assertEquals(
                skyflowError.getErrorMessage(),
                exception.message
            )
        }
    }

    @Test
    fun testEmptyToken() {
        val detokenizeRecords = JSONObject()
        val detokenizeRecordsArray = JSONArray()

        val recordObj = JSONObject()
        recordObj.put("token", "")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)

        detokenizeRecordsArray.put(recordObj)
        detokenizeRecords.put("records", detokenizeRecordsArray)

        val skyflowError =
            SkyflowError(SkyflowErrorCode.EMPTY_TOKEN, apiClientTag, logLevel, arrayOf("0"))

        try {
            apiClient.constructBodyForDetokenize(detokenizeRecords)
        } catch (exception: Exception) {
            Assert.assertEquals(
                skyflowError.getErrorMessage(),
                exception.message
            )
        }
    }

    @Test
    fun testMissingRecords() {
        val detokenizeRecords = JSONObject()
        val detokenizeRecordsArray = JSONArray()

        val recordObj = JSONObject()
        recordObj.put("token", "1234")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)

        detokenizeRecordsArray.put(recordObj)

        val skyflowError =
            SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND, apiClientTag, logLevel)

        try {
            apiClient.constructBodyForDetokenize(detokenizeRecords)
        } catch (exception: Exception) {
            Assert.assertEquals(
                skyflowError.getErrorMessage(),
                exception.message
            )
        }
    }

    @Test
    fun testEmptyRecords() {
        val detokenizeRecords = JSONObject()
        val detokenizeRecordsArray = JSONArray()

        val recordObj = JSONObject()
        recordObj.put("token", "123")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)

        detokenizeRecordsArray.put(recordObj)
        detokenizeRecords.put("records", JSONArray())

        val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_RECORDS, apiClientTag, logLevel)

        try {
            apiClient.constructBodyForDetokenize(detokenizeRecords)
        } catch (exception: Exception) {
            Assert.assertEquals(
                skyflowError.getErrorMessage(),
                exception.message
            )
        }
    }

    @Test
    fun testInvalidRecordsType() {
        val detokenizeRecords = JSONObject()
        val detokenizeRecordsArray = JSONArray()

        val recordObj = JSONObject()
        recordObj.put("token", "1234")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)

        detokenizeRecordsArray.put(recordObj)
        detokenizeRecords.put("records", JSONObject())

        val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORDS, apiClientTag, logLevel)

        try {
            apiClient.constructBodyForDetokenize(detokenizeRecords)
        } catch (exception: Exception) {
            Assert.assertEquals(
                skyflowError.getErrorMessage(),
                exception.message
            )
        }
    }

    @Test
    fun testDefaultRedactionType() {
        val detokenizeRecords = JSONObject()
        val detokenizeRecordsArray = JSONArray()

        val recordObj = JSONObject()
        recordObj.put("token", "123")

        detokenizeRecordsArray.put(recordObj)
        detokenizeRecords.put("records", detokenizeRecordsArray)

        val requestRecord = apiClient.constructBodyForDetokenize(detokenizeRecords)
        Assert.assertEquals(RedactionType.PLAIN_TEXT.toString(), requestRecord[0].redaction)
    }
}
