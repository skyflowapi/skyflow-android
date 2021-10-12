package com.Skyflow

import Skyflow.*
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.widget.CheckBox
import androidx.test.core.app.ApplicationProvider
import com.skyflow_android.R
import junit.framework.Assert
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

class InvokeGatewayTest {

    lateinit var skyflow : Client
    lateinit var context: Context
    lateinit var baseStyle: Style
    lateinit var completeStyle: Style
    lateinit var focusStyle: Style
    lateinit var invalidStyle: Style
    lateinit var emptyStyle: Style
    lateinit var styles : Styles
//    @get:Rule
//    var activityRule: ActivityScenarioRule<MainActivity>
//            = ActivityScenarioRule(MainActivity::class.java)

    // lateinit var layout : LinearLayout

    @Before
    fun setup()
    {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098d2c09",
            "https://sb1.area51.vault.skyflowapis.tech",
            DemoTokenProvider()
        )
        skyflow = Client(configuration)
        context = ApplicationProvider.getApplicationContext<Context>()
        val padding = Skyflow.Padding(8, 8, 8, 8)
        baseStyle = Skyflow.Style(
            Color.parseColor("#403E6B"),
            10f,
            padding,
            null,
            R.font.roboto_light,
            Gravity.START,
            Color.parseColor("#403E6B")
        )
        completeStyle = Skyflow.Style(
            Color.GREEN,
            10f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.END,
            Color.GREEN
        )
        focusStyle = Skyflow.Style(
            Color.parseColor("#403E6B"),
            10f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.END,
            Color.GREEN
        )
        emptyStyle = Skyflow.Style(
            Color.YELLOW,
            10f,
            padding,
            4,
            R.font.roboto_light,
            Gravity.CENTER,
            Color.YELLOW
        )
        invalidStyle = Skyflow.Style(Color.RED, 15f, padding, 6, R.font.roboto_light, Gravity.START, Color.RED)
        styles = Skyflow.Styles(baseStyle, completeStyle, emptyStyle, focusStyle, invalidStyle)
    }

    @Test
    fun testEmptyVaultId()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "",
            "https://sb1.area51.vault.skyflowapis.tech",
            DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val requestBody = JSONObject()
        val responseBody = JSONObject()
        val queryParams = JSONObject()
        val pathParams = JSONObject()
        pathParams.put("cardNumber","cardNumber")
        val requestHeader = JSONObject()
        requestHeader.put("Authorization","")
        val url = "BuildConfig.GATEWAY_CVV_GEN_URL " // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = requestHeader,pathParams = pathParams,methodName = RequestMethod.POST,requestBody = requestBody, responseBody =  responseBody,queryParams = queryParams)
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
    fun testEmptyVaultURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "9898989898",
            "",
            DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val requestBody = JSONObject()
        val responseBody = JSONObject()
        val queryParams = JSONObject()
        val pathParams = JSONObject()
        pathParams.put("cardNumber","cardNumber")
        val requestHeader = JSONObject()
        requestHeader.put("Authorization","")
        val url = "BuildConfig.GATEWAY_CVV_GEN_URL " // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = requestHeader,pathParams = pathParams,methodName = RequestMethod.POST,requestBody = requestBody, responseBody =  responseBody,queryParams = queryParams)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                Assert.assertEquals(skyflowError.getErrorMessage(),
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
            DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val requestBody = JSONObject()
        val responseBody = JSONObject()
        val queryParams = JSONObject()
        val pathParams = JSONObject()
        pathParams.put("cardNumber","cardNumber")
        val requestHeader = JSONObject()
        requestHeader.put("Authorization","")
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = requestHeader,pathParams = pathParams,methodName = RequestMethod.POST,requestBody = requestBody, responseBody =  responseBody,queryParams = queryParams)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL,params = arrayOf(skyflowConfiguration.vaultURL))
                Assert.assertEquals(skyflowError.getErrorMessage(),
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
            DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val requestBody = JSONObject()
        val responseBody = JSONObject()
        val cvvResponse = JSONObject()
          val queryParams = JSONObject()
        val pathParams = JSONObject()
        pathParams.put("cardNumber","cardNumber")
        val requestHeader = JSONObject()
        requestHeader.put("Authorization","")
        val url = "" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = requestHeader,pathParams = pathParams,methodName = RequestMethod.POST,requestBody = requestBody, responseBody =  responseBody,queryParams = queryParams)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_GATEWAY_URL)
                Assert.assertEquals(skyflowError.getErrorMessage(),
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
            DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val requestBody = JSONObject()
        val responseBody = JSONObject()
        val queryParams = JSONObject()
        val pathParams = JSONObject()
        pathParams.put("cardNumber","cardNumber")
        val requestHeader = JSONObject()
        requestHeader.put("Authorization","")
        val url = "something" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = requestHeader,pathParams = pathParams,methodName = RequestMethod.POST,requestBody = requestBody, responseBody =  responseBody,queryParams = queryParams)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_GATEWAY_URL,params = arrayOf(gatewayRequestBody.gatewayURL))
                Assert.assertEquals(skyflowError.getErrorMessage(),
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
            DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val requestBody = JSONObject()
        val responseBody = JSONObject()
        val queryParams = JSONObject()
        val pathParams = JSONObject()
        pathParams.put("cardNumber",JSONObject())
        val requestHeader = JSONObject()
        requestHeader.put("Authorization","")
        val url = "https://www.something.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = requestHeader,pathParams = pathParams,methodName = RequestMethod.POST,requestBody = requestBody, responseBody =  responseBody,queryParams = queryParams)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_GATEWAY_URL,params = arrayOf(gatewayRequestBody.gatewayURL))
                Assert.assertEquals(skyflowError.getErrorMessage(),
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
            DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val requestBody = JSONObject()
        val responseBody = JSONObject()
        val queryParams = JSONObject()
        queryParams.put("check", CheckBox(context))
        val pathParams = JSONObject()
        pathParams.put("cardNumber","")
        val requestHeader = JSONObject()
        requestHeader.put("Authorization","")
        val url = "https://www.something.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = requestHeader,pathParams = pathParams,methodName = RequestMethod.POST,requestBody = requestBody, responseBody =  responseBody,queryParams = queryParams)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_GATEWAY_URL,params = arrayOf(gatewayRequestBody.gatewayURL))
                Assert.assertEquals(skyflowError.getErrorMessage(),
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
            DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val requestBody = JSONObject()
        val responseBody = JSONObject()
        val queryParams = JSONObject()
        val pathParams = JSONObject()
        pathParams.put("cardNumber","")
        val requestHeader = JSONObject()
        requestHeader.put("Authorization",JSONObject())
        val url = "https://www.something.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = requestHeader,pathParams = pathParams,methodName = RequestMethod.POST,requestBody = requestBody, responseBody =  responseBody,queryParams = queryParams)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_GATEWAY_URL,params = arrayOf(gatewayRequestBody.gatewayURL))
                Assert.assertEquals(skyflowError.getErrorMessage(),
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
            DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(context,RevealElementInput())
        val requestBody = JSONObject()
        val responseBody = JSONObject()
        val queryParams = JSONObject()
        val pathParams = JSONObject()
        pathParams.put("cardNumber",cvv)
        pathParams.put("cvv",cvv)
        val requestHeader = JSONObject()
        requestHeader.put("Authorization","")
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,requestHeader = requestHeader,pathParams = pathParams,methodName = RequestMethod.POST,requestBody = requestBody, responseBody =  responseBody,queryParams = queryParams)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.DUPLICATE_ELEMENT_FOUND)
                Assert.assertEquals(skyflowError.getErrorMessage(),
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