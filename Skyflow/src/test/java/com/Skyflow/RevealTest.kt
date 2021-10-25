package com.Skyflow

import Skyflow.*
import Skyflow.core.APIClient
import Skyflow.core.GatewayApiCallback
import Skyflow.core.Logger
import Skyflow.reveal.*
import Skyflow.utils.Utils
import android.app.Activity
import android.view.ViewGroup
import junit.framework.Assert.*
import junit.framework.TestCase
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
class RevealTest {
    lateinit var skyflow: Client
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity
    lateinit var layoutParams: ViewGroup.LayoutParams

    @Before
    fun setup() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val container = RevealContainer()
        skyflow = Client(configuration)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()
    }

    @Test
    fun testRevealElementInput()
    {

        val revealInput = RevealElementInput("2429-2390-5964-3689",RedactionType.DEFAULT,label = "card number")
        assertEquals(revealInput.token,"2429-2390-5964-3689")
        assertEquals(revealInput.redaction, RedactionType.DEFAULT)
        assertEquals(revealInput.label,"card number")
    }

    @Test
    fun testCreateSkyflowRevealContainer()
    {
        val container = skyflow.container(ContainerType.REVEAL)
         val revealInput = RevealElementInput("2429-2390-5964-3689",RedactionType.DEFAULT,label = "card number")
        val cardNumber = container.create(activity,revealInput, RevealElementOptions())

        assertEquals(cardNumber.placeholder.text.toString(),"2429-2390-5964-3689")
    }

    @Test
    fun testCheckRevealElementsArray()
    {
        val container = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput("2429-2390-5964-3689",RedactionType.DEFAULT,label = "card number")
        val cardNumber = container.create(activity,revealInput, RevealElementOptions())

        assertEquals(container.revealElements.count(),1)
        assertEquals(container.revealElements[0].revealInput.token,cardNumber.revealInput.token)
        assertNotNull(container.revealElements[0].revealInput.token)
    }

    @Test
    fun testWitInvalidVaultURLWithLabel()
    {
        val skyflowConfiguration = Configuration(
            "b359c43f1b844ff4bea0f098",
            "http://sb1.area51.vault.skyflapis.tech",
            AccessTokenProvider()
        )
        val skyflow = Client(skyflowConfiguration)
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "expire_date"

        )
        val revealElement = revealContainer.create(activity, revealInput, Skyflow.RevealElementOptions())
        activity.addContentView(revealElement,layoutParams)
        revealContainer.reveal(object : Callback
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
    fun testEmptyVaultIDWithLabel()
    {
        val skyflowConfiguration = Configuration(
            "",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflow = Client(skyflowConfiguration)
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
           label =  "expire_date"
        )
        val revealElement = revealContainer.create(activity, revealInput, Skyflow.RevealElementOptions())
        revealContainer.reveal(object: Callback {
            override fun onSuccess(responseBody: Any) {
               }
            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                junit.framework.Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))

            }})
    }

    @Test
    fun testEmptyVaultURLWithLabel()
    {
        val skyflowConfiguration = Configuration(
            "b359c43f1b844ff4bea0f0",
            "",
            AccessTokenProvider()
        )
        val skyflow = Client(skyflowConfiguration)
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "expire_date"
        )
        val revealElement = revealContainer.create(activity, revealInput, Skyflow.RevealElementOptions())
        revealContainer.reveal(object: Callback {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }})
    }

    @Test
    fun testElementNotMounted()
    {
        val skyflowConfiguration = Configuration(
            "b359c43f1b844ff4bea0f0",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflow = Client(skyflowConfiguration)
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "some",RedactionType.PLAIN_TEXT,
            label =  "expire_date"
        )
        val revealElement = revealContainer.create(activity, revealInput, Skyflow.RevealElementOptions())
        revealContainer.reveal(object: Callback {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(revealElement.label.text.toString()))
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }})
    }


    @Test
    fun testTokenEmpty()
    {
        val skyflowConfiguration = Configuration(
            "b359c43f1b844ff4bea0f0",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflow = Client(skyflowConfiguration)
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "",RedactionType.PLAIN_TEXT,
            label =  "expire_date"
        )
        val revealElement = revealContainer.create(activity, revealInput, Skyflow.RevealElementOptions())
        activity.addContentView(revealElement,layoutParams)
        revealContainer.reveal(object: Callback {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }})
    }

    @Test
    fun testMissingToken()
    {
        val skyflowConfiguration = Configuration(
            "b359c43f1b844ff4bea0f0",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflow = Client(skyflowConfiguration)
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            null,RedactionType.PLAIN_TEXT,
            label =  "expire_date"
        )
        val revealElement = revealContainer.create(activity, revealInput, Skyflow.RevealElementOptions())
        activity.addContentView(revealElement,layoutParams)
        revealContainer.reveal(object: Callback {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                assertEquals(revealElement.getValue(),"")
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TOKEN)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }})
    }
   

    @Test
    fun testEmptyStyles()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "expire_date",altText = "expire date"

        )
        val revealElement = revealContainer.create(activity, revealInput, Skyflow.RevealElementOptions())
        assertEquals(2,revealElement.revealInput.inputStyles.base.borderWidth)
    }

    @Test
    fun testEmptyStyle()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "expire_date",inputStyles = Styles()

        )
        val revealElement = revealContainer.create(activity, revealInput, Skyflow.RevealElementOptions())
        assertEquals(2,revealElement.revealInput.inputStyles.base.borderWidth)

    }

    @Test
    fun testNullStyle()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "expire_date",inputStyles = Styles(null)

        )
        val revealElement = revealContainer.create(activity, revealInput, Skyflow.RevealElementOptions())
        assertEquals(2,revealElement.revealInput.inputStyles.base.borderWidth)


    }

    fun getErrorMessage(error: JSONObject): String {
        val errors = error.getJSONArray("errors")
        val skyflowError = errors.getJSONObject(0).get("error") as SkyflowError
        return skyflowError.getErrorMessage()
    }

    @Test
    fun testValidReveal()
    {
        val skyflowConfiguration = Configuration(
            "b359c43f1b844ff4bea0f0",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflow = Client(skyflowConfiguration)
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "cards",RedactionType.PLAIN_TEXT,
            label =  "expire_date"
        )
        val revealElement = revealContainer.create(activity, revealInput, Skyflow.RevealElementOptions())
        activity.addContentView(revealElement,layoutParams)
        revealContainer.reveal(object: Callback {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                //its valid
            }})
    }


    @Test
    fun testCheckRevealContainer()
    {
        val container = skyflow.container(ContainerType.REVEAL)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "expire_date",inputStyles = Styles()

        )
        val revealElement = container.create(activity, revealInput, Skyflow.RevealElementOptions())
        junit.framework.Assert.assertEquals("51b1406a-0a30-49bf-b303-0eef66bd502d", container.revealElements[0].revealInput.token)
    }
    @Test
    fun testRevealElementNotMounted()
    {

        val container = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "card number",inputStyles = Styles(null)

        )
        val cardNumber = container.create(activity,revealInput,RevealElementOptions())
        TestCase.assertEquals(false, Utils.checkIfElementsMounted(cardNumber))
    }


    @Test
    fun testRevealElementMounted()
    {

        val container = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "card number",inputStyles = Styles(null)

        )
        val cardNumber = container.create(activity,revealInput)
        activity.addContentView(cardNumber,layoutParams)
        TestCase.assertEquals(true, Utils.checkIfElementsMounted(cardNumber))
    }




    //RevealResponse

    @Test
    fun testRevealResponse()
    {
        val revealResponse = RevealResponse(2,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val response = exception as JSONObject
                TestCase.assertTrue(response.has("records"))
                TestCase.assertTrue(response.has("errors"))
            }

        },LogLevel.ERROR)

        val successResponse = JSONObject()
        val records = JSONArray()
        records.put(JSONObject().put("valueType","value"))
        successResponse.put("records",records)
        revealResponse.insertResponse(successResponse,true)

        val failedResponse = JSONObject()
        failedResponse.put("error","unknown error")
        failedResponse.put("token","1234")
        revealResponse.insertResponse(failedResponse,false)
    }

    @Test
    fun testRevealResponseNoFailedResponse()
    {
        val revealResponse = RevealResponse(1,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
                val response = responseBody as JSONObject
                TestCase.assertTrue(response.has("records"))
            }

            override fun onFailure(exception: Any) {

            }

        },LogLevel.ERROR)

        val successResponse = JSONObject()
        val records = JSONArray()
        records.put(JSONObject().put("valueType","value"))
        successResponse.put("records",records)
        revealResponse.insertResponse(successResponse,true)
    }

    @Test
    fun testRevealResponseNoSuccess()
    {
        val revealResponse = RevealResponse(1,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val response = exception as JSONObject
                TestCase.assertTrue(response.has("errors"))
            }

        },LogLevel.ERROR)
        val failedResponse = JSONObject()
        failedResponse.put("error","unknown error")
        failedResponse.put("token","1234")
        revealResponse.insertResponse(failedResponse,false)
    }

    //end RevealResponse


    //RevealResponseById

    @Test
    fun testRevealResponseById()
    {
        val revealResponseByID = RevealResponseByID(2,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val response = exception as JSONObject
                TestCase.assertTrue(response.has("success"))
                TestCase.assertTrue(response.has("errors"))
            }

        },LogLevel.ERROR)

        val successResponse = JSONArray()
        successResponse.put(JSONObject().put("fields","fields object"))
        revealResponseByID.insertResponse(successResponse,true)

        val failedResponse = JSONArray()
        val resObj = JSONObject()
        val skyflowError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR , logLevel = LogLevel.ERROR,params = arrayOf("unknown"))
        resObj.put("error", skyflowError)
        resObj.put("ids", "[\"123\",\"456\"]")
        failedResponse.put(resObj)
        revealResponseByID.insertResponse(failedResponse,false)
    }

    @Test
    fun testRevealResponseByIdNoFailedResponse()
    {
        val revealResponseByID = RevealResponseByID(1,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
                val response = responseBody as JSONObject
                TestCase.assertTrue(response.has("success"))
            }

            override fun onFailure(exception: Any) {

            }

        },LogLevel.ERROR)

        val successResponse = JSONArray()
        successResponse.put(JSONObject().put("fields","fields object"))
        revealResponseByID.insertResponse(successResponse,true)
    }

    @Test
    fun testRevealResponseByIdNoSuccessResponse()
    {
        val revealResponseByID = RevealResponseByID(1,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val response = exception as JSONObject
                TestCase.assertTrue(response.has("errors"))
            }

        },LogLevel.ERROR)
        val failedResponse = JSONArray()
        val resObj = JSONObject()
        val skyflowError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR , logLevel = LogLevel.ERROR,params = arrayOf("unknown"))
        resObj.put("error", skyflowError)
        resObj.put("ids", "[\"123\",\"456\"]")
        failedResponse.put(resObj)
        revealResponseByID.insertResponse(failedResponse,false)
    }
    //end RevealResponseById


    //RevealApicallback

    @Test
    fun testRevealApiCallback()
    {
        val apiClient = APIClient("b359c43f1b844ff4bea0f098d2c09193","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
        val revealRecords = mutableListOf<RevealRequestRecord>()
        revealRecords.add(RevealRequestRecord("a1d84ea3-d2d4-4eeb-a21f-928ff9d01d1c","null"))
        revealRecords.add(RevealRequestRecord("3456","null"))
        val revealApiCallback = RevealApiCallback(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                TestCase.assertEquals(SkyflowError(SkyflowErrorCode.FAILED_TO_REVEAL).getErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        },apiClient,records = revealRecords)
        revealApiCallback.onSuccess("token")
    }

    @Test
    fun testOnFailedRevealApiCallback()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
        val revealRecords = mutableListOf<RevealRequestRecord>()
        revealRecords.add(RevealRequestRecord("1234","null"))
        revealRecords.add(RevealRequestRecord("3456","null"))
        val revealApiCallback = RevealApiCallback(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                TestCase.assertEquals("failed",exception.toString())
            }

        },apiClient,records = revealRecords)
        revealApiCallback.onFailure("failed")

        RevealApiCallback(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                TestCase.assertEquals("failed", UnitTests.getErrorMessage(exception as JSONObject))
            }

        },apiClient,records = revealRecords).onFailure(Exception("failed"))
    }



    //end revealapicallback


    //revealbyid callback

    @Test
    fun testRevealByIdCallback()
    {
        val records = mutableListOf<GetByIdRecord>()
        records.add(GetByIdRecord(arrayListOf("1234"),"cards",RedactionType.REDACTED.toString()))
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
        val revealByidCallback = RevealByIdCallback(
            object : Callback
            {
                override fun onSuccess(responseBody: Any) {

                }

                override fun onFailure(exception: Any) {
                    TestCase.assertEquals(SkyflowError(SkyflowErrorCode.FAILED_TO_REVEAL).getErrorMessage(),
                        UnitTests.getErrorMessage(exception as JSONObject))
                }

            }
            ,apiClient,records = records)

        revealByidCallback.onSuccess("token")
    }

    @Test
    fun testOnFailureRevealByIdCallback()
    {
        val records = mutableListOf<GetByIdRecord>()
        records.add(GetByIdRecord(arrayListOf("1234"),"cards",RedactionType.REDACTED.toString()))
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
        val revealByidCallback = RevealByIdCallback(
            object : Callback
            {
                override fun onSuccess(responseBody: Any) {

                }

                override fun onFailure(exception: Any) {
                    TestCase.assertEquals("failed",exception.toString())
                }

            }
            ,apiClient,records = records)

        revealByidCallback.onFailure("failed")

        RevealByIdCallback(
            object : Callback
            {
                override fun onSuccess(responseBody: Any) {

                }

                override fun onFailure(exception: Any) {
                    TestCase.assertEquals("failed",
                        UnitTests.getErrorMessage(exception as JSONObject))
                }

            }
            ,apiClient,records = records).onFailure(Exception("failed"))
    }

    //end revealbyid callback


    //revealValueCallback

    @Test
    fun testOnSuccessInRevealValue()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "expire_date",inputStyles = Styles()

        )
        val expiry_date = revealContainer.create(activity, revealInput, Skyflow.RevealElementOptions())
        val revealInput1 = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "cvv",inputStyles = Styles()

        )
        val cvv = revealContainer.create(activity, revealInput1, Skyflow.RevealElementOptions())
        activity.addContentView(expiry_date,layoutParams)
        activity.addContentView(cvv,layoutParams)
        val list = mutableListOf<Label>()
        list.add(expiry_date)
        list.add(cvv)

        val response = """
            {"records":[{"token":"51b1406a-0a30-49bf-b303-0eef66bd502d","value":"12/22"},{"token":"51b1406a-0a30-49bf-b303-0eef66bd502d","value":"123"}]}
        """.trimIndent()
        RevealValueCallback(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
                TestCase.assertTrue(JSONObject(responseBody.toString()).has("success"))
                TestCase.assertTrue(JSONObject(responseBody.toString()).get("success") is JSONArray)
            }

            override fun onFailure(exception: Any) {
            }

        },list).onSuccess(JSONObject(response))



    }

    @Test
    fun TestOnFailureInRevealValue()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "expire_date",inputStyles = Styles()

        )
        val expiry_date = revealContainer.create(activity, revealInput, Skyflow.RevealElementOptions())
        val revealInput1 = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "cvv",inputStyles = Styles()

        )
        val cvv = revealContainer.create(activity, revealInput1, Skyflow.RevealElementOptions())
        activity.addContentView(expiry_date,layoutParams)
        activity.addContentView(cvv,layoutParams)
        val list = mutableListOf<Label>()
        list.add(expiry_date)
        list.add(cvv)

        val response = """
            {"errors":[{"token":"51b1406a-0a30-49bf-b303-0eef66bd502d","value":"12/22"},{"token":"51b1406a-0a30-49bf-b303-0eef66bd502d","value":"123"}],"records":[{"error":"Skyflow.SkyflowError: Server error Token not found for name.toString()","token":"name.toString()"}]}
        """.trimIndent()
        RevealValueCallback(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                TestCase.assertTrue(JSONObject(exception.toString()).has("errors"))
                TestCase.assertTrue(JSONObject(exception.toString()).get("errors") is JSONArray)

            }

        },list).onFailure(JSONObject(response))

    }

    @Test
    fun TestOnFailureInRevealValueWithoutRecords()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "expire_date",inputStyles = Styles()

        )
        val expiry_date = revealContainer.create(activity, revealInput, Skyflow.RevealElementOptions())
        val revealInput1 = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "cvv",inputStyles = Styles()

        )
        val cvv = revealContainer.create(activity, revealInput1, Skyflow.RevealElementOptions())
        activity.addContentView(expiry_date,layoutParams)
        activity.addContentView(cvv,layoutParams)
        val list = mutableListOf<Label>()
        list.add(expiry_date)
        list.add(cvv)

        val response = "string"
        RevealValueCallback(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                TestCase.assertEquals(response,exception.toString())
            }

        },list).onFailure(response)

    }
    //end RevealValueCallback




}

