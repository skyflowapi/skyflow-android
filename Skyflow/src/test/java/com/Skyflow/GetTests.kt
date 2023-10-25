package com.Skyflow

import Skyflow.*
import Skyflow.core.APIClient
import Skyflow.get.GetAPICallback
import Skyflow.get.GetOptions
import Skyflow.get.GetRecord
import Skyflow.get.GetResponse
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
class GetTests {
    lateinit var skyflow: Client
    private lateinit var logLevel: LogLevel
    private lateinit var request: Request
    private lateinit var apiClient: APIClient

    private val apiClientTag = APIClient::class.qualifiedName
    private val getResponseTag = GetResponse::class.qualifiedName
    private val utilsTag = Utils::class.qualifiedName

    private val getRecords = JSONObject()
    private val recordsArray = JSONArray()
    private val getRecord = JSONObject()
    private val getRecordColumnDetails = JSONObject()

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

        val ids = JSONArray()
        ids.put("skyflow_id1")
        ids.put("skyflow_id2")

        getRecord.put("ids", ids)
        getRecord.put("table", "table_name")
        getRecord.put("redaction", RedactionType.PLAIN_TEXT)

        val values = JSONArray()
        values.put("value1")
        values.put("value2")

        getRecordColumnDetails.put("table", "table_name")
        getRecordColumnDetails.put("columnName", "column_name")
        getRecordColumnDetails.put("columnValues", values)
        getRecordColumnDetails.put("redaction", RedactionType.PLAIN_TEXT)

        recordsArray.put(getRecord)
        recordsArray.put(getRecordColumnDetails)
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

        val skyflowClient = init(skyflowConfiguration)
        getRecords.put("records", recordsArray)

        skyflowClient.get(records = getRecords, GetOptions(), object : Callback {
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

        val skyflowClient = init(skyflowConfiguration)
        getRecords.put("records", recordsArray)

        skyflowClient.get(records = getRecords, GetOptions(), object : Callback {
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

        val skyflowClient = init(skyflowConfiguration)
        getRecords.put("records", recordsArray)

        skyflowClient.get(records = getRecords, GetOptions(), object : Callback {
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
    fun testMissingRecords() {
        getRecords.put("random", recordsArray)
        val skyflowError = SkyflowError(
            SkyflowErrorCode.RECORDS_KEY_NOT_FOUND, utilsTag, logLevel
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testEmptyRecords() {
        getRecords.put("records", JSONArray())
        val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_RECORDS, utilsTag, logLevel)

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testInvalidRecordsType() {
        getRecords.put("records", JSONObject())
        val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORDS, utilsTag, logLevel)

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testEmptyRecordObject() {
        recordsArray.put(JSONObject())
        getRecords.put("records", recordsArray)
        val skyflowError = SkyflowError(
            SkyflowErrorCode.EMPTY_RECORD_OBJECT, utilsTag, logLevel,
            arrayOf("2")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testMissingTableInRecordObject() {
        getRecord.remove("table")
        getRecords.put("records", recordsArray)

        val skyflowError = SkyflowError(
            SkyflowErrorCode.MISSING_TABLE_KEY, utilsTag, logLevel
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testEmptyTableInRecordObject() {
        getRecord.put("table", String())
        getRecords.put("records", recordsArray)
        val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TABLE_KEY, utilsTag, logLevel)

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testInvalidTableTypeInRecordObject() {
        getRecord.put("table", JSONObject())
        getRecords.put("records", recordsArray)
        val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME, utilsTag, logLevel)

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testEmptyIDsInRecordObject() {
        getRecord.put("ids", JSONArray())
        getRecords.put("records", recordsArray)
        val skyflowError = SkyflowError(
            SkyflowErrorCode.EMPTY_RECORD_IDS_IN_GET, utilsTag, logLevel, arrayOf("0")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testInvalidIDsTypeInRecordObject() {
        getRecord.put("ids", JSONObject())
        getRecords.put("records", recordsArray)
        val skyflowError = SkyflowError(
            SkyflowErrorCode.INVALID_RECORD_IDS_TYPE, utilsTag, logLevel, arrayOf("0")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testInvalidIdTypeInRecordObject() {
        getRecord.getJSONArray("ids").put(123)
        getRecords.put("records", recordsArray)
        val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORD_ID_TYPE, utilsTag, logLevel)

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testEmptyIdValueInRecordObject() {
        getRecord.getJSONArray("ids").put(String())
        getRecords.put("records", recordsArray)
        val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_ID, utilsTag, logLevel)

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testInvalidRedactionTypeInRecordObject() {
        getRecord.put("redaction", 1)
        getRecords.put("records", recordsArray)
        val skyflowError = SkyflowError(
            SkyflowErrorCode.INVALID_REDACTION_TYPE, utilsTag, logLevel, arrayOf("0")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testEmptyRedactionInRecordObject() {
        getRecord.put("redaction", String())
        getRecords.put("records", recordsArray)
        val skyflowError = SkyflowError(
            SkyflowErrorCode.MISSING_REDACTION_VALUE, utilsTag, logLevel, arrayOf("0")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testTokensAsTruePassedWithRedaction() {
        getRecords.put("records", recordsArray)
        val skyflowError = SkyflowError(
            SkyflowErrorCode.REDACTION_WITH_TOKENS_NOT_SUPPORTED, utilsTag, logLevel, arrayOf("0")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(true), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testTokensAsTruePassedWithoutRedaction() {
        getRecord.remove("redaction")
        recordsArray.remove(1)
        getRecords.put("records", recordsArray)

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(true), logLevel)
        } catch (exception: Exception) {
            Assert.fail(exception.message)
        }
    }

    @Test
    fun testTokensAsFalsePassedWithoutRedaction() {
        getRecord.remove("redaction")
        getRecords.put("records", recordsArray)
        val skyflowError = SkyflowError(
            SkyflowErrorCode.REDACTION_KEY_ERROR, utilsTag, logLevel, arrayOf("0")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testTokensAsFalsePassedWithRedaction() {
        getRecords.put("records", recordsArray)

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(false), logLevel)
        } catch (exception: Exception) {
            Assert.fail(exception.message)
        }
    }

    @Test
    fun testColumnValuesPassedWithoutColumnName() {
        getRecordColumnDetails.remove("columnName")
        getRecords.put("records", recordsArray)

        val skyflowError = SkyflowError(
            SkyflowErrorCode.MISSING_RECORD_COLUMN_NAME, utilsTag, logLevel, arrayOf("0")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testColumnNamePassedWithoutColumnValues() {
        getRecordColumnDetails.remove("columnValues")
        getRecords.put("records", recordsArray)

        val skyflowError = SkyflowError(
            SkyflowErrorCode.MISSING_RECORD_COLUMN_VALUES, utilsTag, logLevel, arrayOf("0")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testTokensAsTruePassedWithColumnDetails() {
        getRecord.remove("redaction")
        getRecordColumnDetails.remove("redaction")
        getRecords.put("records", recordsArray)

        val skyflowError = SkyflowError(
            SkyflowErrorCode.TOKENS_NOT_SUPPORTED_WITH_COLUMN_DETAILS, utilsTag, logLevel
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(true), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testBothSkyflowIdsAndColumnDetailsPassed() {
        getRecord.put("columnName", "column_name")
        getRecord.put("columnValues", JSONArray())
        getRecords.put("records", recordsArray)

        val skyflowError = SkyflowError(
            SkyflowErrorCode.BOTH_IDS_AND_COLUMN_DETAILS_SPECIFIED, utilsTag, logLevel, arrayOf("0")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testInvalidColumnNameTypeInRecordObject() {
        getRecordColumnDetails.put("columnName", 1234)
        getRecords.put("records", recordsArray)

        val skyflowError = SkyflowError(
            SkyflowErrorCode.INVALID_RECORD_COLUMN_NAME_TYPE, utilsTag, logLevel, arrayOf("1")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testEmptyColumnNameInRecordObject() {
        getRecordColumnDetails.put("columnName", String())
        getRecords.put("records", recordsArray)

        val skyflowError = SkyflowError(
            SkyflowErrorCode.EMPTY_RECORD_COLUMN_NAME, utilsTag, logLevel, arrayOf("1")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testInvalidColumnValuesTypeInRecordObject() {
        getRecordColumnDetails.put("columnValues", JSONObject())
        getRecords.put("records", recordsArray)

        val skyflowError = SkyflowError(
            SkyflowErrorCode.INVALID_RECORD_COLUMN_VALUES_TYPE, utilsTag, logLevel, arrayOf("1")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testEmptyColumnValuesInRecordObject() {
        getRecordColumnDetails.put("columnValues", JSONArray())
        getRecords.put("records", recordsArray)

        val skyflowError = SkyflowError(
            SkyflowErrorCode.EMPTY_RECORD_COLUMN_VALUES, utilsTag, logLevel, arrayOf("1")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testInvalidColumnValueTypeInRecordObject() {
        getRecordColumnDetails.put("columnValues", JSONArray(arrayOf("value", 1)))
        getRecords.put("records", recordsArray)

        val skyflowError = SkyflowError(
            SkyflowErrorCode.INVALID_COLUMN_VALUE_TYPE, utilsTag, logLevel
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testEmptyColumnValueInRecordObject() {
        getRecordColumnDetails.put("columnValues", JSONArray(arrayOf("value1", "")))
        getRecords.put("records", recordsArray)

        val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_COLUMN_VALUE, utilsTag, logLevel)

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testNeitherSkyflowIdsNorColumnDetailsPassed() {
        getRecord.remove("ids")
        getRecords.put("records", recordsArray)

        val skyflowError = SkyflowError(
            SkyflowErrorCode.NEITHER_IDS_NOR_COLUMN_DETAILS_SPECIFIED,
            utilsTag, logLevel, arrayOf("0")
        )

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
        } catch (exception: Exception) {
            Assert.assertEquals(skyflowError.getErrorMessage(), exception.message)
        }
    }

    @Test
    fun testCorrectRecordsPassedShouldNotFailValidation() {
        val getRecordToken = getRecord
        getRecordToken.remove("redaction")
        recordsArray.remove(1)
        recordsArray.put(getRecordToken)
        getRecords.put("records", recordsArray)

        try {
            Utils.validateGetInputAndOptions(getRecords, GetOptions(true), logLevel)
        } catch (exception: Exception) {
            Assert.fail(exception.message)
        }
    }

    @Test
    fun testConstructRequestBodyForGet() {
        try {
            getRecords.put("records", recordsArray)
            Utils.validateGetInputAndOptions(getRecords, GetOptions(), logLevel)
            val requestBody = Utils.constructRequestBodyForGet(getRecords)
            Assert.assertEquals(2, requestBody.size)

            for ((index, request) in requestBody.withIndex()) {
                Assert.assertEquals(RedactionType.PLAIN_TEXT.toString(), request.redaction)
                Assert.assertEquals("table_name", request.table)

                when (index) {
                    0 -> {
                        Assert.assertNull(request.columnName)
                        Assert.assertNull(request.columnValues)
                        Assert.assertEquals(2, request.skyflowIds!!.size)
                    }
                    1 -> {
                        Assert.assertNull(request.skyflowIds)
                        Assert.assertEquals("column_name", request.columnName)
                        Assert.assertEquals(2, request.columnValues!!.size)
                    }
                }
            }
        } catch (e: Exception) {
            Assert.fail(e.message)
        }
    }

    @Test
    fun testConstructRequestBodyForGetWithTokens() {
        try {
            val record1 = getRecord
            getRecord.remove("redaction")

            recordsArray.remove(1)
            recordsArray.put(record1)

            getRecords.put("records", recordsArray)
            Utils.validateGetInputAndOptions(getRecords, GetOptions(true), logLevel)
            val requestBody = Utils.constructRequestBodyForGet(getRecords)
            Assert.assertEquals(2, requestBody.size)

            for (request in requestBody) {
                Assert.assertEquals("table_name", request.table)
                Assert.assertNull(request.redaction)
                Assert.assertNull(request.columnName)
                Assert.assertNull(request.columnValues)
                Assert.assertEquals(2, request.skyflowIds!!.size)
            }
        } catch (e: Exception) {
            Assert.fail(e.message)
        }
    }

    @Test
    fun testValidGet() {
        getRecords.put("records", recordsArray)

        skyflow.get(getRecords, GetOptions(), object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                Assert.fail(getErrorMessage((exception as JSONObject)))
            }
        })
    }

    @Test
    fun testGetResponseCaseSuccess() {
        val getResponse = GetResponse(1, object : Callback {
            override fun onSuccess(responseBody: Any) {
                val response = responseBody as JSONObject
                Assert.assertTrue(response.has("records"))
                Assert.assertEquals(1, response.getJSONArray("records").length())
                Assert.assertFalse(response.has("errors"))
            }

            override fun onFailure(exception: Any) {
            }
        }, LogLevel.ERROR)

        val successRecords = JSONArray()
        val record = JSONObject()
            .put("fields", JSONObject())
            .put("table", "table_name")
        successRecords.put(record)
        getResponse.insertResponse(successRecords, true)
    }

    @Test
    fun testGetResponseCaseNullResponse() {
        GetResponse(1, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val expectedError = SkyflowError(
                    SkyflowErrorCode.UNKNOWN_ERROR,
                    getResponseTag, logLevel, params = arrayOf("Failed to Get records")
                )
                Assert.assertEquals(
                    expectedError.getErrorMessage(), getErrorMessage(exception as JSONObject)
                )
            }
        }, LogLevel.ERROR).insertResponse(null, false)
    }

    @Test
    fun testGetResponseCasePartial() {
        val getResponse = GetResponse(2, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val response = exception as JSONObject
                Assert.assertTrue(response.has("records"))
                Assert.assertTrue(response.has("errors"))
                Assert.assertEquals(1, response.getJSONArray("records").length())
                Assert.assertEquals(1, response.getJSONArray("errors").length())

            }
        }, LogLevel.ERROR)

        val successRecords = JSONArray()
        val record = JSONObject()
            .put("fields", JSONObject())
            .put("table", "table_name")
        successRecords.put(record)
        getResponse.insertResponse(successRecords, true)

        val errorRecords = JSONArray()
        val error = JSONObject()
            .put("error", "unknown error")
            .put("ids", JSONArray())
        errorRecords.put(error)
        getResponse.insertResponse(errorRecords, false)
    }

    @Test
    fun testGetResponseCaseFailure() {
        val getResponse = GetResponse(1, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val response = exception as JSONObject
                Assert.assertFalse(response.has("success"))
                Assert.assertTrue(response.has("errors"))
                Assert.assertEquals(1, response.getJSONArray("errors").length())
            }
        }, LogLevel.ERROR)

        val errorRecords = JSONArray()
        val error = JSONObject()
            .put("error", "unknown error")
            .put("ids", JSONArray())
        errorRecords.put(error)
        getResponse.insertResponse(errorRecords, false)
    }

    @Test
    fun testGetAPICallback() {
        val records = mutableListOf<GetRecord>()
        records.add(
            GetRecord(
                skyflowIds = arrayListOf("id1", "id2"),
                table = "table_name",
                redaction = RedactionType.PLAIN_TEXT.toString()
            )
        )

        records.add(
            GetRecord(
                table = "table_name",
                redaction = RedactionType.PLAIN_TEXT.toString(),
                columnName = "column_name",
                columnValues = arrayListOf("value1", "value2")
            )
        )

        val getCallback = GetAPICallback(object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                Assert.assertEquals(
                    SkyflowError(SkyflowErrorCode.FAILED_TO_GET).getErrorMessage(),
                    getErrorMessage((exception as JSONObject))
                )
            }
        }, apiClient, records, GetOptions())

        getCallback.onSuccess("token")
    }

    @Test
    fun testGetAPICallbackFailure() {
        val records = mutableListOf<GetRecord>()
        records.add(
            GetRecord(
                skyflowIds = arrayListOf("id1", "id2"),
                table = "table_name",
                redaction = RedactionType.PLAIN_TEXT.toString()
            )
        )

        records.add(
            GetRecord(
                table = "table_name",
                redaction = RedactionType.PLAIN_TEXT.toString(),
                columnName = "column_name",
                columnValues = arrayListOf("value1", "value2")
            )
        )

        val skyflowError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, params = arrayOf("failed"))

        val getCallback = GetAPICallback(object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                Assert.assertEquals(
                    skyflowError.getErrorMessage(),
                    getErrorMessage((exception as JSONObject))
                )
            }
        }, apiClient, records, GetOptions())

        getCallback.onFailure(skyflowError)
    }
}