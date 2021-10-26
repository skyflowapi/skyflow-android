package com.Skyflow

import Skyflow.*
import Skyflow.core.GatewayApiCallback
import Skyflow.core.elements.state.StateforText
import Skyflow.utils.Utils
import android.app.Activity
import android.view.ViewGroup
import android.widget.CheckBox
import junit.framework.Assert
import junit.framework.Assert.assertEquals
import junit.framework.TestCase
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import java.lang.annotation.ElementType

@RunWith(RobolectricTestRunner::class)
class InvokeGatewayTest {

    lateinit var skyflow : Client
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity
    lateinit var layoutParams: ViewGroup.LayoutParams

    @Before
    fun setup()
    {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098d2c09",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        skyflow = Client(configuration)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()

    }

    @Test
    fun testEmptyVaultId()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val url = "BuildConfig.GATEWAY_CVV_GEN_URL " // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testEmptyVaultURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "9898989898",
            "",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val url = "BuildConfig.GATEWAY_CVV_GEN_URL " // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })


    }

    @Test
    fun testInvalidVaultURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "http://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL,params = arrayOf(skyflowConfiguration.vaultURL))
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })


    }
    @Test
    fun emptyGatewayURL1()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val url = "" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_GATEWAY_URL)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidGatewayURL1()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val url = "something" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_GATEWAY_URL,params = arrayOf(gatewayRequestBody.gatewayURL))
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun testInvalidPathParams()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput())

        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val card_number = collectContainer.create(activity, CollectElementInput(type = SkyflowElementType.CARD_NUMBER))

        val pathParams = JSONObject()
        pathParams.put("card_number",card_number)
        pathParams.put("cvv",cvv)
        pathParams.put("cardNumber",JSONObject())
        val url = "https://www.something.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,pathParams = pathParams,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_PATH_PARAMS,params = arrayOf(gatewayRequestBody.gatewayURL))
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidQueryParams()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput())

        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val card_number = collectContainer.create(activity, CollectElementInput(type = SkyflowElementType.CARD_NUMBER))

        val queryParams = JSONObject()
        queryParams.put("cvv",cvv)
        queryParams.put("card_number",queryParams)
        queryParams.put("check", CheckBox(activity))
        val url = "https://www.something.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST,queryParams = queryParams)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_QUERY_PARAMS,params = arrayOf(gatewayRequestBody.gatewayURL))
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidRequestHeader()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val requestHeader = JSONObject()
        requestHeader.put("Authorization",JSONObject())
        val url = "https://www.something.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = requestHeader,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_HEADER_PARAMS,params = arrayOf(gatewayRequestBody.gatewayURL))
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testDuplicateInResponseBody()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput())
        val pathParams = JSONObject()
        pathParams.put("cardNumber",cvv)
        pathParams.put("cvv",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = JSONObject(),pathParams = pathParams,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testRevealElementNotMountedInRequestBody()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val requestBody = JSONObject()
        requestBody.put("cardNumber",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(cvv.label.text.toString()))
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testCollectElementNotMountedInRequestBody()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(column = "cvv",type = SkyflowElementType.CVV))
        val requestBody = JSONObject()
        requestBody.put("cardNumber",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(cvv.columnName))
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testElementNotValidInRequestBody()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(column = "cvv",type = SkyflowElementType.CVV))
        val requestBody = JSONObject()
        requestBody.put("cardNumber",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        activity.addContentView(cvv,layoutParams)
        cvv.inputField.setText("12")
        cvv.state = StateforText(cvv)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_INPUT,params = arrayOf("for cvv [INVALID_LENGTH_MATCH]"))
                assertEquals(skyflowError.getErrorMessage().trim(),
                    getErrorMessage(exception as JSONObject).trim())
            }

        })
    }

    @Test
    fun testCollectElementNotMountedInPathParams() //to do
    {

        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(column = "cvv",type = SkyflowElementType.CVV))
        val pathParams = JSONObject()
        pathParams.put("cardNumber",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...

        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = JSONObject(),pathParams = pathParams,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(cvv.columnName))
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testRevealElementNotMountedInPathParams()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val pathparams = JSONObject()
        pathparams.put("cardNumber",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = JSONObject(),pathParams = pathparams,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(cvv.label.text.toString()))
               assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }




    @Test
    fun testCollectElementNotMountedIntQueryParams()
    {

        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(column = "cvv",type = SkyflowElementType.CVV))
        val queryParams = JSONObject()
        queryParams.put("cardNumber",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...

        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = JSONObject(),queryParams = queryParams,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(cvv.columnName))
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testRevealElementNotMountedIntQueryParams()
    {

        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val queryparams = JSONObject()
        queryparams.put("cardNumber",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = JSONObject(),queryParams = queryparams,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(cvv.label.text.toString()))
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    fun getErrorMessage(error: JSONObject): String {
        val errors = error.getJSONArray("errors")
        val skyflowError = errors.getJSONObject(0).get("error") as SkyflowError
        return skyflowError.getErrorMessage()
    }



    //invoke gateway

    @Test
    fun testEmptyVaultIdForGateway()
    {
        val configuration = Configuration(
            "",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )

        val client = Client(configuration)
        val url = "BuildConfig.GATEWAY_CVV_GEN_URL " // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        client.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyVaultURLForGateway() {
        val skyflowConfiguration = Skyflow.Configuration(
            "9898989898",
            "",
            AccessTokenProvider()
        )

        val client = Client(skyflowConfiguration)
        val url = "BuildConfig.GATEWAY_CVV_GEN_URL " // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        client.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testInvalidVaultURLForGateway()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "http://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient =  Client(skyflowConfiguration)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL,params = arrayOf(skyflowConfiguration.vaultURL))
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })


    }
    @Test
    fun emptyGatewayURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient =  Client(skyflowConfiguration)
        val url = "" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_GATEWAY_URL)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidGatewayURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient =  Client(skyflowConfiguration)
        val url = "something" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_GATEWAY_URL,params = arrayOf(gatewayRequestBody.gatewayURL))
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testAddQueryParams()
    {
        val queryParams = JSONObject()
        queryParams.put("card_number","4111")
        queryParams.put("cvv","123")
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com",RequestMethod.POST,queryParams = queryParams)
        val requestUrlBuilder = gatewayConfiguration.gatewayURL.toHttpUrlOrNull()?.newBuilder()
        Utils.addQueryParams(requestUrlBuilder!!,gatewayConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
            }

        },LogLevel.ERROR)

        val newRequest = "https://www.google.com?card_number=4111&cvv=123".toHttpUrlOrNull()?.newBuilder()

        TestCase.assertEquals(requestUrlBuilder.toString().trim(), newRequest.toString().trim())

    }

    @Test
    fun testValidInputForAddRequestHeader()
    {
        val requestHeader = JSONObject()
        requestHeader.put("card_number","4111")
        requestHeader.put("cvv","123")
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com",RequestMethod.POST,requestHeader = requestHeader)
        val request = Request
            .Builder()
            .addHeader("Content-Type","application/json")
            .url(gatewayConfiguration.gatewayURL)
        val isValid = Utils.addRequestHeader(request,gatewayConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
            }
        },LogLevel.ERROR)

        TestCase.assertTrue(isValid)
    }

    @Test
    fun testInvalidInputForAddRequestHeader()
    {
        val requestHeader = JSONObject()
        requestHeader.put("card_number",JSONObject())
        requestHeader.put("cvv","123")
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com",RequestMethod.POST,requestHeader = requestHeader)
        val request = Request
            .Builder()
            .addHeader("Content-Type","application/json")
            .url(gatewayConfiguration.gatewayURL)
        val isValid = Utils.addRequestHeader(request,gatewayConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }override fun onFailure(exception: Any) {

            }
        },LogLevel.ERROR)

        TestCase.assertFalse(isValid)
    }

    @Test
    fun testInvalidQueryParamsForElementNotMounting() //element not mounting
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val queryParams = JSONObject()
        queryParams.put("card_number","4111")
        queryParams.put("cvv",cvv)
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com",RequestMethod.POST,queryParams = queryParams)
        val requestUrlBuilder = gatewayConfiguration.gatewayURL.toHttpUrlOrNull()?.newBuilder()
        Utils.addQueryParams(requestUrlBuilder!!,gatewayConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                    params = arrayOf("cvv")).getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        },LogLevel.ERROR)

    }

    @Test
    fun testInvalidQueryParamsForCollectElementNotMounting() //element not mounting
    {
        val collectContainer = skyflow.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(type = SkyflowElementType.CVV,column = "cvv"))
        val queryParams = JSONObject()
        queryParams.put("card_number","4111")
        queryParams.put("cvv",cvv)
        queryParams.put("array", arrayOf(cvv,"1234"))
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com",RequestMethod.POST,queryParams = queryParams)
        val requestUrlBuilder = gatewayConfiguration.gatewayURL.toHttpUrlOrNull()?.newBuilder()
        Utils.addQueryParams(requestUrlBuilder!!,gatewayConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                    params = arrayOf("cvv")).getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        },LogLevel.ERROR)
    }


    @Test
    fun testInvalidQueryParamsForInvalidCollectElement() //element not mounting
    {
        val collectContainer = skyflow.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(type = SkyflowElementType.CVV,column = "cvv"))
        val queryParams = JSONObject()
        queryParams.put("card_number","4111")
        queryParams.put("cvv",cvv)
        queryParams.put("array", arrayOf(cvv,"1234"))
        cvv.inputField.setText("12")
        activity.addContentView(cvv,layoutParams)
        cvv.state = StateforText(cvv)
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com",RequestMethod.POST,queryParams = queryParams)
        val requestUrlBuilder = gatewayConfiguration.gatewayURL.toHttpUrlOrNull()?.newBuilder()
        Utils.addQueryParams(requestUrlBuilder!!,gatewayConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.INVALID_INPUT,
                    params = arrayOf("for cvv [INVALID_LENGTH_MATCH]")).getErrorMessage().trim(),
                    UnitTests.getErrorMessage(exception as JSONObject).trim())
            }

        },LogLevel.ERROR)

    }




    @Test
    fun testAddPathParams()
    {
        val pathParams = JSONObject()
        pathParams.put("card_number","4111")
        pathParams.put("cvv","123")
        val url = "https://www.google.com/{card_number}/{cvv}"
        val generatedUrl = Utils.addPathparamsToURL(url,pathParams,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
            }

        },LogLevel.ERROR)
        TestCase.assertEquals(generatedUrl, "https://www.google.com/4111/123")
    }

    @Test
    fun testInvalidPathParamsForRevealElementNotMounting() //element not mounting
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val pathParams = JSONObject()
        pathParams.put("card_number","4111")
        pathParams.put("cvv",cvv)
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com",RequestMethod.POST,pathParams = pathParams)
        val requestUrlBuilder = gatewayConfiguration.gatewayURL.toHttpUrlOrNull()?.newBuilder()
        Utils.addQueryParams(requestUrlBuilder!!,gatewayConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                    params = arrayOf("cvv")).getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        },LogLevel.ERROR)

    }

    @Test
    fun testInvalidPathParamsForCollectElementNotMounting() //element not mounting
    {
        val collectContainer = skyflow.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(type = SkyflowElementType.CVV))
        val pathParams = JSONObject()
        pathParams.put("card_number","4111")
        pathParams.put("cvv",cvv)
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com",RequestMethod.POST,pathParams = pathParams)
        val requestUrlBuilder = gatewayConfiguration.gatewayURL.toHttpUrlOrNull()?.newBuilder()
        Utils.addQueryParams(requestUrlBuilder!!,gatewayConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                    params = arrayOf("cvv")).getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        },LogLevel.ERROR)

    }

    @Test
    fun testInvalidPathParamsForInvalidCollectElement() //element not mounting
    {
        val collectContainer = skyflow.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(type = SkyflowElementType.CVV,column = "cvv"))
        val pathParams = JSONObject()
        pathParams.put("card_number","4111")
        pathParams.put("cvv",cvv)
        cvv.inputField.setText("12")
        activity.addContentView(cvv,layoutParams)
        cvv.state = StateforText(cvv)
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com",RequestMethod.POST,pathParams = pathParams)
        val requestUrlBuilder = gatewayConfiguration.gatewayURL.toHttpUrlOrNull()?.newBuilder()
        Utils.addQueryParams(requestUrlBuilder!!,gatewayConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.INVALID_INPUT,
                    params = arrayOf("for cvv [INVALID_LENGTH_MATCH]")).getErrorMessage().trim(),
                    UnitTests.getErrorMessage(exception as JSONObject).trim())
            }

        },LogLevel.ERROR)

    }




    @Test
    fun testDuplicateInResponseBodyForReveal()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput())
        val responseBody = JSONObject()
        responseBody.put("cardNumber",cvv)
        responseBody.put("cvv",cvv)
        activity.addContentView(cvv,layoutParams)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        Utils.checkDuplicateInResponseBody(responseBody,object :Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.DUPLICATE_ELEMENT_FOUND)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, HashSet(),LogLevel.ERROR)
    }



    @Test
    fun testDuplicateInResponseBodyForCollect()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(type = SkyflowElementType.CVV))
        val responseBody = JSONObject()
        val nestedJson = JSONObject()
        nestedJson.put("cardNumber",cvv)
        responseBody.put("nested",nestedJson)
        responseBody.put("cvv",cvv)
        activity.addContentView(cvv,layoutParams)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        Utils.checkDuplicateInResponseBody(responseBody,object :Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.DUPLICATE_ELEMENT_FOUND)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, HashSet(),LogLevel.ERROR)
    }

    @Test
    fun testRemoveNullJSON()
    {
        val records = JSONObject()
        records.put("card_number","1234")
        records.put("cvv","123")
        records.put("name",JSONObject())

        val nested = JSONObject()
        nested.put("some","123")
        nested.put("nestedJson",JSONObject())
        records.put("nested",nested)

        Utils.removeEmptyAndNullFields(records)
        TestCase.assertTrue(!records.has("name"))

    }

    @Test
    fun testCheckInvalidFieldsForCollectElementNotMounted() //in response body
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","name",
            SkyflowElementType.CARDHOLDER_NAME,placeholder = "name"
        )
        val name = container.create(activity,collectInput, options) as? TextField

        val records = JSONObject()
        records.put("cvv",cvv)
        activity.addContentView(cvv,layoutParams)
        records.put("name",name)

        Utils.checkInvalidFields(records, JSONObject(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                Assert.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf("name")).getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testCheckInvalidFieldsForRevealElementNotMounted() //in response body
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","name",
            SkyflowElementType.CARDHOLDER_NAME,placeholder = "name"
        )
        val name = container.create(activity,collectInput, options) as? TextField

        val records = JSONObject()
        records.put("cvv",cvv)
        records.put("name",name)
        activity.addContentView(name,layoutParams)

        Utils.checkInvalidFields(records, JSONObject(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                Assert.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf("cvv")).getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testCheckInvalidFields() //in response body
    {

        val records = JSONObject()
        records.put("cvv","123")

        Utils.checkInvalidFields(records, JSONObject(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                Assert.assertEquals("invalid field cvv present in response body",
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }




    @Test
    fun testConstructRequestBody()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards",null,
            SkyflowElementType.CARDHOLDER_NAME,placeholder = "name"
        )
        val name = container.create(activity,collectInput, options) as? TextField
        name!!.inputField.setText("4111 1111 1111 1111")
        val records = JSONObject()
        records.put("name",name)
        activity.addContentView(name,layoutParams)
        val isConstructed = Utils.constructRequestBodyForGateway(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {

            }

        },LogLevel.ERROR)

        TestCase.assertTrue(isConstructed)
    }

    @Test
    fun testConstructRequestBodyFailedForCollectElementNotMounted() //for unmounting
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        val records = JSONObject()
        records.put("cardNumber",card_number)
        val containerOptions = ContainerOptions()
        // activity.addContentView(card_number,layoutParams)
        val isConstructed = Utils.constructRequestBodyForGateway(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                    params = arrayOf("card_number")).getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        },LogLevel.ERROR)

        TestCase.assertFalse(isConstructed)

        Utils.constructJsonKeyForGatewayRequest(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                    params = arrayOf("card_number")).getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        },LogLevel.ERROR)
    }

    @Test
    fun testConstructRequestBodyFailedForRevealElementNotMounted() //for unmounting
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val records = JSONObject()
        val array = JSONArray()
        array.put(cvv)
        array.put(card_number)
        array.put("1234")
        records.put("exp","11/22")
        records.put("JsonArray",array)

        val isConstructed = Utils.constructRequestBodyForGateway(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                    params = arrayOf("cvv")).getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))

            }

        },LogLevel.ERROR)

        TestCase.assertFalse(isConstructed)

    }



    @Test
    fun testConstructRequestBodyFailedForInvalidElement()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val records = JSONObject()
        val nested = JSONObject()
        val array = arrayOf(card_number,cvv,"1234")

        nested.put("card",array)
        nested.put("number","1234")
        records.put("jsonobject",nested)
        activity.addContentView(card_number,layoutParams)
        card_number!!.inputField.setText("4111 11 1111 1111")
        val isConstructed = Utils.constructRequestBodyForGateway(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.INVALID_INPUT,
                    params = arrayOf("for card_number [INVALID_CARD_NUMBER]\n")).getErrorMessage()
                    .trim(),
                    UnitTests.getErrorMessage(exception as JSONObject).trim())
            }

        },LogLevel.ERROR)

        TestCase.assertFalse(isConstructed)
    }


    @Test
    fun testConstructRequestBodyEmptyKey()
    {
        val records = JSONObject()
        records.put("","1234")
        val isConstructed = Utils.constructRequestBodyForGateway(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.EMPTY_COLUMN_NAME).getErrorMessage()
                    .trim(),
                    (exception as SkyflowError).getErrorMessage().trim())
            }

        },LogLevel.ERROR)

        TestCase.assertFalse(isConstructed)
    }

    @Test
    fun testConstructRequestBodyForInvalidDatatype()
    {
        val records = JSONObject()
        val nested = JSONObject()
        nested.put("mm",CheckBox(activity))
        records.put("mm",nested)
        val isConstructed = Utils.constructRequestBodyForGateway(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_BODY,params = arrayOf("mm")).getErrorMessage()
                    .trim(),
                    (exception as SkyflowError).getErrorMessage().trim())
            }

        },LogLevel.ERROR)

        TestCase.assertFalse(isConstructed)
    }



    @Test
    fun testAddPathParamsEmptyKey()
    {
        val pathParams = JSONObject()
        pathParams.put("","4111")
        pathParams.put("cvv","123")
        val url = "https://www.google.com/{card_number}/{cvv}"
        val generatedUrl = Utils.addPathparamsToURL(url,pathParams,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_PATH_PARAMS).getErrorMessage()
                    .trim(),
                    (exception as SkyflowError).getErrorMessage().trim())
            }

        },LogLevel.ERROR)
    }

    @Test
    fun testAddQueryParamsEmptyKey()
    {
        val queryParams = JSONObject()
        queryParams.put("","4111")
        queryParams.put("cvv","123")
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com",RequestMethod.POST,queryParams = queryParams)
        val requestUrlBuilder = gatewayConfiguration.gatewayURL.toHttpUrlOrNull()?.newBuilder()
        Utils.addQueryParams(requestUrlBuilder!!,gatewayConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_QUERY_PARAMS).getErrorMessage()
                    .trim(),
                    (exception as SkyflowError).getErrorMessage().trim())
            }

        },LogLevel.ERROR)
    }

    @Test
    fun testconstructResponseBodyFromGateway()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))

        val records = JSONObject()
        val nested = JSONObject()
        nested.put("card_number",card_number)
        records.put("nested",nested)
        records.put("cvv",cvv)
        activity.addContentView(cvv,layoutParams)
        activity.addContentView(card_number,layoutParams)

        val recordFromGateway = JSONObject()
        recordFromGateway.put("cvv","123")
        val records1 = JSONObject()
        records1.put("card_number",card_number)
        recordFromGateway.put("nested",records1)
        recordFromGateway.put("exp","11/22")

        val response = Utils.constructResponseBodyFromGateway(records,recordFromGateway
            ,object : Callback
            {
                override fun onSuccess(responseBody: Any) {
                }

                override fun onFailure(exception: Any) {

                }

            },LogLevel.ERROR)

        TestCase.assertNotNull(response)

        val response1 = Utils.constructJsonKeyForGatewayResponse(records,recordFromGateway
            ,object : Callback
            {
                override fun onSuccess(responseBody: Any) {
                }

                override fun onFailure(exception: Any) {

                }

            },LogLevel.ERROR)

        TestCase.assertNotNull(response1)
        TestCase.assertTrue(response1.has("success"))
    }

    //end invokegateway


    //gatewayapicallback

    @Test
    fun testGatewayApiCallbackInvalidPathparams()
    {
        val queryParams = JSONObject()
        queryParams.put("card_number","4111")
        queryParams.put("cvv","123")

        val pathParams = JSONObject()
        pathParams.put("cvv",JSONObject())
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com/{cvv}",RequestMethod.POST,queryParams = queryParams,pathParams = pathParams)
        GatewayApiCallback(gatewayConfiguration,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_PATH_PARAMS,params = arrayOf("cvv"))
                TestCase.assertEquals(skyflowError.getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        },LogLevel.ERROR).onSuccess("token")
    }

    @Test
    fun testGatewayApiCallbackInvalidQueryparams()
    {
        val queryParams = JSONObject()
        queryParams.put("card_number","4111")
        queryParams.put("cvv",CheckBox(activity))

        val pathParams = JSONObject()
        pathParams.put("cvv","123")
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com/{cvv}",RequestMethod.POST,queryParams = queryParams,pathParams = pathParams)
        GatewayApiCallback(gatewayConfiguration,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_QUERY_PARAMS,params = arrayOf("cvv"))
                TestCase.assertEquals(skyflowError.getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        },LogLevel.ERROR).onSuccess("token")
    }

    @Test
    fun testGatewayApiCallbackInvalidRequestHeader()
    {
        val requestHeader = JSONObject()
        requestHeader.put("card_number","4111")
        requestHeader.put("cvv",CheckBox(activity))

        val gatewayConfiguration = GatewayConfiguration("https://www.google.com/",RequestMethod.POST,requestHeader = requestHeader)
        GatewayApiCallback(gatewayConfiguration,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_HEADER_PARAMS,params = arrayOf("cvv"))
                TestCase.assertEquals(skyflowError.getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        },LogLevel.ERROR).onSuccess("Bearer token")
    }


    @Test
    fun testValidGatewayApiCallback()
    {
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com/",RequestMethod.POST)
        val gateway =   GatewayApiCallback(gatewayConfiguration,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                TestCase.assertEquals("failed",exception.toString())
            }

        },LogLevel.ERROR)

        gateway.onSuccess("Bearer token")
        gateway.onFailure("failed")
    }

    @Test
    fun testGatewayApiCallbackInvalidUrl()
    {
        val gatewayConfiguration = GatewayConfiguration("httpsm/",RequestMethod.POST)
        GatewayApiCallback(gatewayConfiguration,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.INVALID_GATEWAY_URL,params = arrayOf(gatewayConfiguration.gatewayURL))
                    .getErrorMessage(), UnitTests.getErrorMessage(exception as JSONObject))
            }

        },LogLevel.ERROR).onSuccess("Bearer token")

    }

    //end gatewayapi
    
}