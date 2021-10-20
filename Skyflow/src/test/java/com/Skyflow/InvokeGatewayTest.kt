package com.Skyflow

import Skyflow.*
import Skyflow.core.elements.state.StateforText
import android.app.Activity
import android.view.ViewGroup
import android.widget.CheckBox
import junit.framework.Assert
import junit.framework.Assert.assertEquals
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
    fun emptyGatewayURL()
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
    fun testInvalidGatewayURL()
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
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID,params = arrayOf(cvv.columnName))
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
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

}