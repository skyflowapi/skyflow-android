package com.Skyflow

import Skyflow.*
import Skyflow.core.Logger
import android.app.Activity
import android.view.ViewGroup
import junit.framework.Assert.*
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
            label =  "expire_date"

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


}

