/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package com.Skyflow

import Skyflow.*
import Skyflow.core.ConnectionApiCallback
import Skyflow.core.elements.state.StateforText
import Skyflow.utils.Utils
import android.app.Activity
import android.view.ViewGroup
import android.widget.CheckBox
import junit.framework.Assert.assertEquals
import junit.framework.TestCase
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class InvokeConnectionTest {

    lateinit var skyflow : Client
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity
    lateinit var layoutParams: ViewGroup.LayoutParams
    lateinit var logLevel: LogLevel
    @Before
    fun setup()
    {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098d2c09",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        skyflow = Client(configuration)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()
        logLevel = LogLevel.ERROR

    }
    @Test
    fun emptyConnectionURL1()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val url = "" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_CONNECTION_URL)
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidConnectionURL1()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val url = "something" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_CONNECTION_URL,params = arrayOf(connectionRequestBody.connectionURL))
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun testInvalidPathParams()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
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
        val connectionRequestBody = ConnectionConfig(connectionURL = url,pathParams = pathParams,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_PATH_PARAMS,params = arrayOf(connectionRequestBody.connectionURL))
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidQueryParams()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vault.url.com",
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
        val connectionRequestBody = ConnectionConfig(connectionURL = url,methodName = RequestMethod.POST,queryParams = queryParams)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_QUERY_PARAMS,params = arrayOf(connectionRequestBody.connectionURL))
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidRequestHeader()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val requestHeader = JSONObject()
        requestHeader.put("Authorization",JSONObject())
        val url = "https://www.something.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = requestHeader,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_HEADER_PARAMS,params = arrayOf(connectionRequestBody.connectionURL))
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testDuplicateInResponseBody()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput())
        val pathParams = JSONObject()
        pathParams.put("cardNumber",cvv)
        pathParams.put("cvv",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),pathParams = pathParams,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun testRevealElementNotMountedInRequestBody()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val requestBody = JSONObject()
        requestBody.put("cvv",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf("cvv"))
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testCollectElementNotMountedInRequestBody()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(column = "cvv",type = SkyflowElementType.CVV))
        val requestBody = JSONObject()
        requestBody.put("cvv",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf("cvv"))
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testElementNotValidInRequestBody()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(column = "cvv",type = SkyflowElementType.CVV))
        val requestBody = JSONObject()
        requestBody.put("cardNumber",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        activity.addContentView(cvv,layoutParams)
        cvv.inputField.setText("12")
        cvv.state = StateforText(cvv)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_INPUT,params = arrayOf("for cvv INVALID_LENGTH_MATCH"))
                assertEquals(skyflowError.getInternalErrorMessage().trim(),
                    getErrorMessage(exception as JSONObject).trim())
            }

        })
    }

    @Test
    fun testCollectElementNotMountedInPathParams() //to do
    {

        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(column = "cvv",type = SkyflowElementType.CVV))
        val pathParams = JSONObject()
        pathParams.put("cardNumber",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...

        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),pathParams = pathParams,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(cvv.columnName))
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testRevealElementNotMountedInPathParams()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val pathparams = JSONObject()
        pathparams.put("cardNumber",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),pathParams = pathparams,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(cvv.label.text.toString()))
               assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testCollectElementNotMountedIntQueryParams()
    {

        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(column = "cvv",type = SkyflowElementType.CVV))
        val queryParams = JSONObject()
        queryParams.put("cardNumber",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...

        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),queryParams = queryParams,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(cvv.columnName))
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testRevealElementNotMountedIntQueryParams()
    {

        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val queryparams = JSONObject()
        queryparams.put("cardNumber",cvv)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),queryParams = queryparams,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(cvv.label.text.toString()))
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    fun getErrorMessage(error: JSONObject): String {
        val errors = error.getJSONArray("errors")
        val skyflowError = errors.getJSONObject(0).get("error") as SkyflowError
        val message = skyflowError.getInternalErrorMessage()
        if(message.indexOf("-") !=-1)
            return message.substring(message.indexOf("-")+2)
        else
            return message
    }

    @Test
    fun testInvalidVaultURLForConnection()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "http://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient =  Client(skyflowConfiguration)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL,params = arrayOf(skyflowConfiguration.vaultURL))
                assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })


    }
    @Test
    fun emptyConnectionURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient =  Client(skyflowConfiguration)
        val url = "" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_CONNECTION_URL)
                assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidConnectionURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient =  Client(skyflowConfiguration)
        val url = "something" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_CONNECTION_URL,params = arrayOf(connectionRequestBody.connectionURL))
                assertEquals(skyflowError.getInternalErrorMessage(),
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
        val connectionConfiguration = ConnectionConfig("https://www.google.com",RequestMethod.POST,queryParams = queryParams)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        callback.addQueryParams()
        //test ContentType
        callback.setContentType(ContentType.APPLICATIONORJSON.type)
        assertEquals(callback.getContentType(),ContentType.APPLICATIONORJSON.type)
        //test queryparams
        TestCase.assertEquals("123", callback.queryMap.get("cvv"))
        TestCase.assertEquals("4111", callback.queryMap.get("card_number"))
    }

    @Test
    fun testValidInputForAddRequestHeader()
    {
        val requestHeader = JSONObject()
        requestHeader.put("card_number","4111")
        requestHeader.put("cvv","123")
        val connectionConfiguration = ConnectionConfig("https://www.google.com",RequestMethod.POST,requestHeader = requestHeader)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        callback.addRequestHeader()
        TestCase.assertEquals("123", callback.headerMap.get("cvv"))
        TestCase.assertEquals("4111", callback.headerMap.get("card_number"))
    }

    @Test
    fun testInvalidInputForAddRequestHeader()
    {
        val requestHeader = JSONObject()
        requestHeader.put("card_number",JSONObject())
        requestHeader.put("cvv","123")
        val connectionConfiguration = ConnectionConfig("https://www.google.com",RequestMethod.POST,requestHeader = requestHeader)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.addRequestHeader()
        }
        catch (e:Exception)
        {
            val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_HEADER_PARAMS, params =  arrayOf("card_number"))
            TestCase.assertEquals(skyflowError.getInternalErrorMessage(), (e as SkyflowError).getInternalErrorMessage())

        }
    }

    @Test
    fun testInvalidQueryParamsForElementNotMounting() //element not mounting
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val queryParams = JSONObject()
        queryParams.put("card_number","4111")
        queryParams.put("cvv",cvv)
        val connectionConfiguration = ConnectionConfig("https://www.google.com",RequestMethod.POST,queryParams = queryParams)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.addQueryParams()
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                params = arrayOf("cvv")).getInternalErrorMessage(),
                (e as SkyflowError).getInternalErrorMessage())
        }
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
        val connectionConfiguration = ConnectionConfig("https://www.google.com",RequestMethod.POST,queryParams = queryParams)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.addQueryParams()
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                params = arrayOf("cvv")).getInternalErrorMessage(),
                (e as SkyflowError).getInternalErrorMessage())
        }
    }
    @Test
    fun testInvalidQueryParamsForInvalidCollectElement() //element not mounting todo
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
        val connectionConfiguration = ConnectionConfig("https://www.google.com",RequestMethod.POST,queryParams = queryParams)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.addQueryParams()
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.INVALID_INPUT,
                params = arrayOf("for cvv INVALID_LENGTH_MATCH")).getInternalErrorMessage(),
                (e as SkyflowError).getInternalErrorMessage())
        }

    }
    @Test
    fun testAddPathParams()
    {
        val pathParams = JSONObject()
        pathParams.put("card_number","4111")
        pathParams.put("cvv","123")
        val url = "https://www.google.com/{card_number}/{cvv}"
        val connectionConfiguration = ConnectionConfig(url,RequestMethod.POST,pathParams = pathParams)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        assertEquals(callback.addPathParamsToURL(),"https://www.google.com/4111/123")
    }

    @Test
    fun testInvalidPathParamsForRevealElementNotMounting() //element not mounting
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))
        val pathParams = JSONObject()
        pathParams.put("card_number","4111")
        pathParams.put("cvv",cvv)
        val connectionConfiguration = ConnectionConfig("https://www.google.com",RequestMethod.POST,pathParams = pathParams)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.addPathParamsToURL()
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                params = arrayOf("cvv")).getInternalErrorMessage(),
                (e as SkyflowError).getInternalErrorMessage())
        }
    }

    @Test
    fun testInvalidPathParamsForCollectElementNotMounting() //element not mounting
    {
        val collectContainer = skyflow.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(type = SkyflowElementType.CVV))
        val pathParams = JSONObject()
        pathParams.put("card_number","4111")
        pathParams.put("cvv",cvv)
        val connectionConfiguration = ConnectionConfig("https://www.google.com",RequestMethod.POST,pathParams = pathParams)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.addPathParamsToURL()
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                params = arrayOf("cvv")).getInternalErrorMessage(),
                (e as SkyflowError).getInternalErrorMessage())
        }
    }

    @Test
    fun testInvalidPathParamsForInvalidCollectElement() //to do
    {
        val collectContainer = skyflow.container(ContainerType.COLLECT)
        val cvv = collectContainer.create(activity, CollectElementInput(type = SkyflowElementType.CVV,column = "cvv"))
        val pathParams = JSONObject()
        pathParams.put("card_number","4111")
        pathParams.put("cvv",cvv)
        cvv.inputField.setText("12")
        activity.addContentView(cvv,layoutParams)
        cvv.state = StateforText(cvv)
        val connectionConfiguration = ConnectionConfig("https://www.google.com",RequestMethod.POST,pathParams = pathParams)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.addPathParamsToURL()
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.INVALID_INPUT,
                params = arrayOf("for cvv [INVALID_LENGTH_MATCH]")).getInternalErrorMessage(),
                (e as SkyflowError).getInternalErrorMessage())
        }
    }
    @Test
    fun testDuplicateInResponseBodyForReveal()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
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
        val connectionConfiguration = ConnectionConfig(url,RequestMethod.POST,responseBody = responseBody)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.validateResponseBody(responseBody, HashSet())
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.DUPLICATE_ELEMENT_FOUND).getInternalErrorMessage(), (e as SkyflowError).getInternalErrorMessage())
        }
    }
    @Test
    fun testDuplicateInResponseBodyForCollect()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://vaulturl.com",
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
        val connectionConfiguration = ConnectionConfig(url,RequestMethod.POST,responseBody = responseBody)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.validateResponseBody(responseBody, HashSet())
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.DUPLICATE_ELEMENT_FOUND).getInternalErrorMessage(), (e as SkyflowError).getInternalErrorMessage())
        }
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
        val connectionConfiguration = ConnectionConfig("https://www.url.com",RequestMethod.POST, requestBody = records)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        callback.constructRequestBodyForConnection(records)
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
        val connectionConfiguration = ConnectionConfig("https://www.url.com",RequestMethod.POST, requestBody = records)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.constructRequestBodyForConnection(records)
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                params = arrayOf("cardNumber")).getInternalErrorMessage(),
                (e as SkyflowError).getInternalErrorMessage())
        }
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

        val connectionConfiguration = ConnectionConfig("https://www.url.com",RequestMethod.POST, requestBody = records)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.constructRequestBodyForConnection(records)
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                params = arrayOf("JsonArray")).getInternalErrorMessage(),
               (e as SkyflowError).getInternalErrorMessage())
        }
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
        val connectionConfiguration = ConnectionConfig("https://www.url.com",RequestMethod.POST, requestBody = records)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.constructRequestBodyForConnection(records)
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.INVALID_INPUT,
                params = arrayOf("for card_number INVALID_CARD_NUMBER\n")).getInternalErrorMessage(),
                (e as SkyflowError).getInternalErrorMessage())
        }
    }
    @Test
    fun testConstructRequestBodyEmptyKey()
    {
        val records = JSONObject()
        records.put("","1234")
        val connectionConfiguration = ConnectionConfig("https://www.url.com",RequestMethod.POST, requestBody = records)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.constructRequestBodyForConnection(records)
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_REQUEST_BODY).getInternalErrorMessage(),
                (e as SkyflowError).getInternalErrorMessage())
        }
    }
    @Test
    fun testConstructRequestBodyForInvalidDatatype()
    {
        val records = JSONObject()
        val nested = JSONObject()
        nested.put("mm",CheckBox(activity))
        records.put("mm",nested)
        val connectionConfiguration = ConnectionConfig("https://www.url.com",RequestMethod.POST, requestBody = records)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.constructRequestBodyForConnection(records)
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_BODY,params = arrayOf("mm")).getInternalErrorMessage(),
                (e as SkyflowError).getInternalErrorMessage())
        }
    }
    @Test
    fun testAddPathParamsEmptyKey()
    {
        val pathParams = JSONObject()
        pathParams.put("","4111")
        pathParams.put("cvv","123")
        val url = "https://www.google.com/{card_number}/{cvv}"
        val connectionConfiguration = ConnectionConfig(url,RequestMethod.POST,pathParams = pathParams)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.addPathParamsToURL()
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_PATH_PARAMS).getInternalErrorMessage(), (e as SkyflowError).getInternalErrorMessage())
        }
    }
    @Test
    fun testAddQueryParamsEmptyKey()
    {
        val queryParams = JSONObject()
        queryParams.put("","4111")
        queryParams.put("cvv","123")
        val connectionConfiguration = ConnectionConfig("https://www.google.com",RequestMethod.POST,queryParams = queryParams)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        try {
            callback.addQueryParams()
        }
        catch (e:Exception)
        {
            TestCase.assertEquals(SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_QUERY_PARAMS).getInternalErrorMessage(), (e as SkyflowError).getInternalErrorMessage())
        }
    }

    @Test
    fun testconstructResponseBodyFromConnection()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv", token = "123"))

        val records = JSONObject()
        val nested = JSONObject()
        nested.put("card_number",card_number)
        records.put("nested",nested)
        records.put("cvv",cvv)
        activity.addContentView(cvv,layoutParams)
        activity.addContentView(card_number,layoutParams)

        val recordFromConnection = JSONObject()
        recordFromConnection.put("cvv","123")
        val records1 = JSONObject()
        records1.put("card_number",card_number)
        recordFromConnection.put("nested",records1)
        recordFromConnection.put("exp","11/22")
        val connectionConfiguration = ConnectionConfig("https://www.url.com",RequestMethod.POST, requestBody = records)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        callback.constructRequestBodyForConnection(records)
    }
    @Test
    fun testConnectionApiCallbackInvalidPathparams()
    {
        val queryParams = JSONObject()
        queryParams.put("card_number","4111")
        queryParams.put("cvv","123")

        val pathParams = JSONObject()
        pathParams.put("cvv",JSONObject())
        val connectionConfiguration = ConnectionConfig("https://www.google.com/{cvv}",RequestMethod.POST,queryParams = queryParams,pathParams = pathParams)
        ConnectionApiCallback(connectionConfiguration, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_PATH_PARAMS,params = arrayOf("cvv"))
                TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, LogLevel.ERROR, skyflow).onSuccess("token")
    }
    @Test
    fun testConnectionApiCallbackInvalidQueryparams()
    {
        val queryParams = JSONObject()
        queryParams.put("card_number","4111")
        queryParams.put("cvv",CheckBox(activity))

        val pathParams = JSONObject()
        pathParams.put("cvv","123")
        val connectionConfiguration = ConnectionConfig("https://www.google.com/{cvv}",RequestMethod.POST,queryParams = queryParams,pathParams = pathParams)
        ConnectionApiCallback(connectionConfiguration, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_QUERY_PARAMS,params = arrayOf("cvv"))
                TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, LogLevel.ERROR, skyflow).onSuccess("token")
    }
    @Test
    fun testConnectionApiCallbackInvalidRequestHeader()
    {
        val requestHeader = JSONObject()
        requestHeader.put("card_number","4111")
        requestHeader.put("cvv",CheckBox(activity))

        val connectionConfiguration = ConnectionConfig("https://www.google.com/",RequestMethod.POST,requestHeader = requestHeader)
        ConnectionApiCallback(connectionConfiguration, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_HEADER_PARAMS,params = arrayOf("cvv"))
                TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, LogLevel.ERROR, skyflow).onSuccess("Bearer token")
    }
    @Test
    fun testValidConnectionApiCallback()
    {
        val connectionConfiguration = ConnectionConfig("https://www.google.com/",RequestMethod.POST)
        val connection =   ConnectionApiCallback(connectionConfiguration, object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                TestCase.assertEquals("failed",exception.toString())
            }

        }, LogLevel.ERROR, skyflow)

        connection.onFailure("failed")
    }
    @Test
    fun testConnectionApiCallbackInvalidUrl()
    {
        val connectionConfiguration = ConnectionConfig("httpsm/",RequestMethod.POST)
        ConnectionApiCallback(connectionConfiguration, object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.INVALID_CONNECTION_URL,params = arrayOf(connectionConfiguration.connectionURL))
                    .getInternalErrorMessage(), UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, LogLevel.ERROR, skyflow).onSuccess("Bearer token")

    }
    //token null and empty

    fun getNullCvv() : Label
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv"))

        activity.addContentView(cvv,layoutParams)
        return cvv
    }

    fun getEmptyTokenCvv() : Label
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv",token = ""))

        activity.addContentView(cvv,layoutParams)
        return cvv
    }
    fun getCvvWithFormatRegex() : Label
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv",token = "1234"),
            RevealElementOptions(formatRegex = "..$"))

        activity.addContentView(cvv,layoutParams)
        return cvv
    }
    @Test
    fun tokenNullInRequestBody()
    {
        val requestBody = JSONObject()
        requestBody.put("cvv",getNullCvv())
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        skyflow.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TOKEN_IN_CONNECTION_REQUEST, params = arrayOf("cvv"))
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun tokenNullInJSONObjectRequestBody()
    {
        val requestBody = JSONObject()
        requestBody.put("cvv1",JSONObject().put("cvv",getNullCvv()))
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        skyflow.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TOKEN_IN_CONNECTION_REQUEST, params = arrayOf("cvv"))
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun tokenNullInJSONArrayRequestBody()
    {
        val requestBody = JSONObject()
        requestBody.put("cvv1",JSONArray().put(getNullCvv()))
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        skyflow.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TOKEN)
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun tokenNullInArrayRequestBody()
    {
        val requestBody = JSONObject()
        requestBody.put("cvv1", arrayOf(getNullCvv()))
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        skyflow.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TOKEN)
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun tokenEmptyInRequestBody()
    {
        val requestBody = JSONObject()
        requestBody.put("cvv",getEmptyTokenCvv())
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        skyflow.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID)
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun tokenEmptyInJSONObjectRequestBody()
    {
        val requestBody = JSONObject()
        requestBody.put("cvv1",JSONObject().put("cvv",getEmptyTokenCvv()))
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        skyflow.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID)
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun tokenEmptyInJSONArrayRequestBody()
    {
        val requestBody = JSONObject()
        requestBody.put("cvv1",JSONArray().put(getEmptyTokenCvv()))
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        skyflow.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID)
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun tokenEmptyInArrayRequestBody()
    {
        val requestBody = JSONObject()
        requestBody.put("cvv1", arrayOf(getEmptyTokenCvv()))
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),requestBody = requestBody,methodName = RequestMethod.POST)
        skyflow.invokeConnection(connectionRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID)
                assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun tokenEmptyInPathparams()
    {
        val pathParams = JSONObject()
        pathParams.put("cvv1", getEmptyTokenCvv())
        val url = "https://www.google.com/{cvv1}" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),pathParams = pathParams,methodName = RequestMethod.POST)
        ConnectionApiCallback(connectionRequestBody, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID)
                TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, LogLevel.ERROR, skyflow).onSuccess("token")
    }
    @Test
    fun tokenNullInPathparams()
    {
        val pathParams = JSONObject()
        pathParams.put("cvv",getNullCvv())
        val url = "https://www.google.com/{cvv}" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),pathParams = pathParams,methodName = RequestMethod.POST)
        ConnectionApiCallback(connectionRequestBody, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TOKEN)
                TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, LogLevel.ERROR, skyflow).onSuccess("token")
    }
    @Test
    fun tokenEmptyInQueryparams()
    {
        val queryParams = JSONObject()
        queryParams.put("cvv1", getEmptyTokenCvv())
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),queryParams = queryParams,methodName = RequestMethod.POST)
        ConnectionApiCallback(connectionRequestBody, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID)
                TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }val configuration = Configuration(
            "b359c43f1b844ff4bea0f098d2c09",
            "https://vaulturl.com",
            AccessTokenProvider()
        )

        }, LogLevel.ERROR, skyflow).onSuccess("token")
    }
    @Test
    fun tokenNullInQueryParams()
    {
        val queryParams = JSONObject()
        queryParams.put("cvv",getNullCvv())
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),queryParams = queryParams,methodName = RequestMethod.POST)
        ConnectionApiCallback(connectionRequestBody, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TOKEN)
                TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, LogLevel.ERROR, skyflow).onSuccess("token")
    }
    @Test
    fun tokenEmptyInQueryparamsArray()
    {
        val queryParams = JSONObject()
        queryParams.put("cvv1", arrayOf(getEmptyTokenCvv(),2,3))
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),queryParams = queryParams,methodName = RequestMethod.POST)
        ConnectionApiCallback(connectionRequestBody, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID)
                TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, LogLevel.ERROR, skyflow).onSuccess("token")
    }
    @Test
    fun tokenNullInQueryParamsArray()
    {
        val queryParams = JSONObject()
        queryParams.put("cvv", arrayOf(getNullCvv(),2,3))
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(),queryParams = queryParams,methodName = RequestMethod.POST)
        ConnectionApiCallback(connectionRequestBody, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TOKEN)
                TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, LogLevel.ERROR, skyflow).onSuccess("token")
    }

    @Test
    fun testEmptyVaultId() //when formatRegex given
    {
        val configuration = Configuration(
            "",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient = Client(configuration)
        val requestBody = JSONObject()
        requestBody.put("cvv", arrayOf(getCvvWithFormatRegex(),2,3))
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(), requestBody = requestBody,methodName = RequestMethod.POST)
        ConnectionApiCallback(connectionRequestBody, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, LogLevel.ERROR, skyflowClient).onSuccess("token")
    }

    @Test
    fun testEmptyVaultURL()
    {
        val configuration = Configuration(
            "123",
            "",
            AccessTokenProvider()
        )
        val skyflowClient = Client(configuration)
        val requestBody = JSONObject()
        requestBody.put("cvv", arrayOf(getCvvWithFormatRegex(),2,3))
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(), requestBody = requestBody,methodName = RequestMethod.POST)
        ConnectionApiCallback(connectionRequestBody, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, LogLevel.ERROR, skyflowClient).onSuccess("token")
    }

    @Test
    fun testInvalidVaultURL()
    {
        val configuration = Configuration(
            "1243",
            "http://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflowClient = Client(configuration)
        val requestBody = JSONObject()
        requestBody.put("cvv", arrayOf(getCvvWithFormatRegex(),2,3))
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,requestHeader = JSONObject(), requestBody = requestBody,methodName = RequestMethod.POST)
        ConnectionApiCallback(connectionRequestBody, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL, params = arrayOf(configuration.vaultURL))
                TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        }, LogLevel.ERROR, skyflowClient).onSuccess("token")
    }
}
class ApiCallback : Callback
{
    override fun onSuccess(responseBody: Any) {

    }

    override fun onFailure(exception: Any) {
    }

}