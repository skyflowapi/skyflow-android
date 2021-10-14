package com.Skyflow

import Skyflow.*
import Skyflow.core.elements.state.StateforText
import android.app.Activity
import android.view.ViewGroup
import com.skyflow_android.R
import junit.framework.Assert.assertEquals
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import java.io.IOException
import android.util.Log
import junit.framework.Assert


@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class CollectTest {
        lateinit var skyflow : Client
        private lateinit var activityController: ActivityController<Activity>
        private lateinit var activity: Activity
        lateinit var layoutParams : ViewGroup.LayoutParams
        @Before
        fun setup()
        {
            val configuration = Configuration(
                "b359c43f1b844ff4bea0f098",
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
    fun testCreateSkyflowElement(){
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111")
        activity.addContentView(card_number,layoutParams)
        card_number.inputField.setText("4111 1111 1111 1111")
        assertEquals(card_number.getValue(),"4111 1111 1111 1111")
    }

    @Test
    fun testValidValueSkyflowElement()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")

        val state = StateforText(card_number).getInternalState()
        Assert.assertTrue(state["isValid"] as Boolean)

    }

    @Test
    fun testEmptyStateForSkyflowElement()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")

        val state = StateforText(card_number).getInternalState()
        Assert.assertFalse(state["isEmpty"] as Boolean)
    }

    @Test
    fun testNonEmptyStateforSkyflowElement()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput) as? TextField

        val state = StateforText(card_number!!).getInternalState()
        Assert.assertTrue(state["isEmpty"] as Boolean)
    }

    @Test
    fun testInvalidValueSkyflowElement()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput) as? TextField
        card_number!!.inputField.setText("4111")


        val state = StateforText(card_number).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean)

    }

    @Test
    fun testCheckElementsArray()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        assertEquals(container.elements.count(),1)
        Assert.assertTrue(container.elements[0].fieldType == SkyflowElementType.CARD_NUMBER)
    }

    @Test
    fun testElementNotMounted()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(activity.resources.getString(R.string.bt_cvc),"card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val collectInput2 = CollectElementInput("cards","cvv",SkyflowElementType.CVV, label = "cvv")
        val card_number : TextField = container.create(activity,collectInput1, options)
        val cvv : TextField = container.create(activity,collectInput2, options)

        card_number.inputField.setText("4111 1111 1111 1111")
        cvv.inputField.setText("2")
        card_number.state = StateforText(card_number)
        cvv.state = StateforText(cvv)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(card_number.columnName))
                assertEquals((exception as SkyflowError).message.trim(),skyflowError.getErrorMessage().trim())
            }
        })
    }

        @Test
        fun testContainerInsertMixedInvalidInput() {
            val container = skyflow.container(ContainerType.COLLECT)
            val options = CollectElementOptions(true)
            val collectInput1 = CollectElementInput(activity.resources.getString(R.string.bt_cvc),"card_number",
                SkyflowElementType.CARD_NUMBER,label = "card number"
            )
            val collectInput2 = CollectElementInput("cards","cvv",SkyflowElementType.CVV, label = "cvv")
            val card_number : TextField = container.create(activity,collectInput1, options)
            val cvv : TextField = container.create(activity,collectInput2, options)

            card_number.inputField.setText("4111 1111 1111 1111")
            cvv.inputField.setText("2")
            activity.addContentView(card_number,layoutParams)
            activity.addContentView(cvv,layoutParams)
            card_number.state = StateforText(card_number)
            cvv.state = StateforText(cvv)

            container.collect(object : Callback
            {
                override fun onSuccess(responseBody: Any) {

                }

                override fun onFailure(exception: Any) {
                    val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_INPUT,params = arrayOf("for cvv [INVALID_LENGTH_MATCH]"))
                    assertEquals((exception as SkyflowError).message.trim(),skyflowError.getErrorMessage().trim())
                }
            })
        }


    @Test
    fun testContainerInsertIsRequiredAndEmpty() //to do
    {

        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val collectInput2 = CollectElementInput("cards","cvv",SkyflowElementType.CVV, label = "cvv")
        val card_number = container.create(activity,collectInput1, options)
        val cvv = container.create(activity,collectInput2,options)
        activity.addContentView(card_number,layoutParams)
        activity.addContentView(cvv,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_INPUT,params = arrayOf("card_number is empty\n" +
                        "cvv is empty"))
                assertEquals((exception as SkyflowError).message.trim(),skyflowError.getErrorMessage().trim())
            }
        })
    }

    @Test
    fun testEmptyVaultIDWithSkyflowElement()
    {
        val skyflowConfiguration = Configuration( "",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider())
        val skyflow = Client(skyflowConfiguration)
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val card_number = container.create(activity,collectInput1, options)
        card_number.inputField.setText("4111111111111111")
        card_number.state = StateforText(card_number)
        activity.addContentView(card_number,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                assertEquals((exception as SkyflowError).message,SkyflowErrorCode.EMPTY_VAULT_ID.getMessage())
            }
        })

    }

    @Test
    fun testEmptyVaultURLWithSkyflowElement()
    {
        val configuration = Configuration( "b359c43f1b844ff4bea0f098d2c09",
            "",
            AccessTokenProvider())
        val skyflow = Client(configuration)
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val collectInput2 = CollectElementInput("cards","expiry_date",SkyflowElementType.EXPIRATION_DATE, label = "expire date")
        val card_number = container.create(activity,collectInput1, options)
        val expire = container.create(activity,collectInput2,options)

        card_number.inputField.setText("4111111111111111")
        expire.inputField.setText("11/22")
        card_number.state = StateforText(card_number)
        expire.state = StateforText(expire)
        activity.addContentView(card_number,layoutParams)
        activity.addContentView(expire,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                assertEquals((exception as SkyflowError).message,SkyflowErrorCode.EMPTY_VAULT_URL.getMessage())
            }


        })
    }

    @Test
    fun testInvalidVaultURLWithSkyflowElement()
    {
        val configuration = Configuration( "b359c43f1b844ff4bea0f098d2c09",
            "http://www.google.com",
            AccessTokenProvider())
        val skyflow = Client(configuration)
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val collectInput2 = CollectElementInput("cards","expiry_date",SkyflowElementType.EXPIRATION_DATE, label = "expire date")

        val card_number = container.create(activity,collectInput1, options)
        val expire = container.create(activity,collectInput2,options)

        card_number.inputField.setText("4111111111111111")
        expire.inputField.setText("11/22")
        card_number.state = StateforText(card_number)
        expire.state = StateforText(expire)
        activity.addContentView(card_number,layoutParams)
        activity.addContentView(expire,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL,params = arrayOf(configuration.vaultURL))
                assertEquals((exception as SkyflowError).message,skyflowError.getErrorMessage())
            }


        })
    }
    @Test
    fun testNullTableName()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(null,"card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                assertEquals((exception as SkyflowError).message,SkyflowErrorCode.MISSING_TABLE.getMessage())
            }


        })
    }
    @Test
    fun testEmptyTableName()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30,20,20,20)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                Log.d("exc",(exception as SkyflowError).message)
                assertEquals((exception as SkyflowError).message,SkyflowErrorCode.EMPTY_TABLE_NAME.getMessage())
            }


        })
    }
    @Test
    fun nullColumnName()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards",null,
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                assertEquals((exception as SkyflowError).message,SkyflowErrorCode.MISSING_COLUMN.getMessage())
            }


        })

    }

    @Test
    fun emptyColumnName()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30,20,20,20)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                assertEquals((exception as SkyflowError).message,SkyflowErrorCode.EMPTY_COLUMN_NAME.getMessage())
            }


        })
    }
}


class AccessTokenProvider: TokenProvider {
    override fun getBearerToken(callback: Skyflow.Callback) {
        val url = "https://go-server.skyflow.dev/sa-token/b359c43f1b844ff4bea0f098d2c0"
        val request = okhttp3.Request.Builder().url(url).build()
        val okHttpClient = OkHttpClient()
        try {
            val thread = Thread {
                run {
                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful)
                            throw IOException("Unexpected code $response")
                        //  val accessTokenObject = JSONObject(response.body()!!.string().toString())
                        //  val accessToken = accessTokenObject["accessToken"]
                        val accessToken = ""
                        callback.onSuccess("$accessToken")
                    }
                }
            }
        }
        catch (e:Exception)
        {

        }
    }
}