package com.Skyflow

import Skyflow.*
import Skyflow.core.elements.state.StateforText
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import androidx.test.core.app.ApplicationProvider
import com.skyflow_android.R
import okhttp3.Call
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException


class CollectTest {

    lateinit var skyflow : Client
    lateinit var context: Context
    @Before
    fun setup()
    {
        var configuration = Configuration(
            "b359c43f1b844ff4bea0f098d2c09193",
            "https://sb1.area51.vault.skyflowapis.tech",
            DemoTokenProvider()
        )
        skyflow = Client(configuration)
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun testPureInsert()
    {
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        val fields = JSONObject()
        fields.put("fullname", "san")
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        skyflow.insert(records, InsertOptions(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {
               // Log.d("response is: ",responseBody.toString())
                val jsonobj = JSONObject(responseBody.toString()).getJSONArray("records").getJSONObject(0)
                val fields =  jsonobj.getJSONObject("fields")
                val table =jsonobj.getString("table")
                assertEquals(table,"cards")
                assertNotNull(fields["card_number"].toString())
                assertNotNull(fields["expiry_date"].toString())
                assertNotNull(fields["fullname"].toString())
                /*Log.d("cardnumber",fields["card_number"].toString())
                Log.d("expire date",fields["expiry_date"].toString())
                Log.d("name",fields["fullname"].toString())*/
            }

            override fun onFailure(exception: Exception) {
               // Log.d("error is:",exception.message.toString())
            }

        })

    }
    @Test
    fun testInvalidVaultID()
    {
        var configuration = Configuration( "ff",
            "https://na1.area51.vault.skyflowapis.com<",
            DemoTokenProvider())
        val skyflow = Client(configuration)
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        val fields = JSONObject()
        fields.put("fullname", "san")
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        skyflow.insert(records, InsertOptions(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Exception) {
                val jsonobj = JSONObject(exception.message.toString()).getJSONObject("error")
                //Log.d("error",jsonobj.toString())
               // Log.d("message",
               //     jsonobj.getString("message").toString().contains("document does not exist").toString())

                assertTrue(jsonobj.getString("message").toString().contains("document does not exist"))

            }

        })
    }

    @Test
    fun testInvalidVaultURL()
    {
        var configuration = Configuration( "b359c43f1b844ff4bea0f098d2c09193",
            "https://na1.area51.vault.skyfwapis.com<",
            DemoTokenProvider())
        val skyflow = Client(configuration)
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        val fields = JSONObject()
        fields.put("fullname", "san")
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        skyflow.insert(records, InsertOptions(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Exception) {
                //Log.d("failure:", "${exception.message.toString()}")
                assertTrue(exception.message.toString().contains("No address associated with hostname"))
            }

        })
    }

    @Test
    fun testCreateSkyflowElement(){
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4, R.font.roboto_light, Gravity.START, Color.BLUE)
        val styles = Styles(bstyle)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("persons","cardNumber",
            SkyflowElementType.CARD_NUMBER,styles,"card number"
        )
        val cardNumber = container.create(context,collectInput, options) as? TextField
        cardNumber!!.inputField.setText("4111 1111 1111 1111")
        assertEquals(bstyle.borderColor,Color.BLUE)
        assertEquals(cardNumber.getOutput(),"4111 1111 1111 1111")


    }

    @Test
    fun testValidValueSkyflowElement()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4, R.font.roboto_light, Gravity.START, Color.BLUE)
        val styles = Styles(bstyle)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("persons","cardNumber",
            SkyflowElementType.CARD_NUMBER,styles,"card number"
        )
        val cardNumber = container.create(context,collectInput, options) as? TextField
        cardNumber!!.inputField.setText("4111 1111 1111 1111")

        val state = StateforText(cardNumber).getState()
        assertTrue(state["isValid"] as Boolean)

    }

    @Test
    fun testInvalidValueSkyflowElement()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4, R.font.roboto_light, Gravity.START, Color.BLUE)
        val styles = Styles(bstyle)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("persons","cardNumber",
            SkyflowElementType.CARD_NUMBER,styles,"card number"
        )
        val cardNumber = container.create(context,collectInput, options) as? TextField
        cardNumber!!.inputField.setText("4111")


        val state = StateforText(cardNumber).getState()
       /*  Log.d("text present is:",cardNumber.inputField.text.toString())
         Log.d("show : ",cardNumber.state.show())
         Log.d("isvalid:",(state["isValid"] as Boolean).toString())*/
         assertFalse(state["isValid"] as Boolean)

    }

    @Test
    fun testCheckElementsArray()
    {
        val container = skyflow.container(ContainerType.COLLECT)
         val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("persons","cardNumber",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val cardNumber = container.create(context,collectInput, options) as? TextField
        assertEquals(container.elements.count(),1)
        assertTrue(container.elements[0].fieldType == SkyflowElementType.CARD_NUMBER)
    }

    @Test
    fun testContainerInsertInvalidInput()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("persons","cardNumber",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val collectInput2 = CollectElementInput("persons","cvv",SkyflowElementType.CVV, label = "cvv")
        val cardNumber = container.create(context,collectInput1, options)
        cardNumber.inputField.setText("4111")
        cardNumber.state = StateforText(cardNumber)
        val callback = DemoApiCallback()
        container.collect(callback)

        assertEquals(callback.receivedResponse,"for cardNumber [INVALID_CARD_NUMBER]")
    }

    @Test
    fun testContainerInsertMixedInvalidInput()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("persons","cardNumber",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val collectInput2 = CollectElementInput("persons","cvv",SkyflowElementType.CVV, label = "cvv")
        val cardNumber = container.create(context,collectInput1, options)
        val cvv = container.create(context,collectInput2,options)
        cardNumber.inputField.setText("4111 1111 1111 1111")
        cvv.inputField.setText("2")
        cardNumber.state = StateforText(cardNumber)
        cvv.state = StateforText(cvv)
        val callback = DemoApiCallback()
        container.collect(callback)

        assertEquals(callback.receivedResponse,"for cvv [INVALID_LENGTH_MATCH]")


    }

    @Test
    fun testContainerInsertIsRequiredAndEmpty()
    {

        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("persons","cardNumber",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val collectInput2 = CollectElementInput("persons","cvv",SkyflowElementType.CVV, label = "cvv")
        val cardNumber = container.create(context,collectInput1, options)
        val cvv = container.create(context,collectInput2,options)
        val callback = DemoApiCallback()
        container.collect(callback)

        assertEquals(callback.receivedResponse,"cardNumber is empty\n" +
                "cvv is empty")
    }

    @Test
    fun testContainerInsert()   //IsRequiredAndNotEmpty also comes under it
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val collectInput2 = CollectElementInput("cards","expiry_date",SkyflowElementType.EXPIRATION_DATE, label = "expire date")
        val collectInput3 = CollectElementInput("cards","fullname",SkyflowElementType.CARDHOLDER_NAME, label = "name")
        val cardNumber = container.create(context,collectInput1, options)
        val expire = container.create(context,collectInput2,options)
        val name = container.create(context,collectInput3, options)

        cardNumber.inputField.setText("4111111111111111")
        expire.inputField.setText("11/22")
        name.inputField.setText("santhosh")
        cardNumber.state = StateforText(cardNumber)
        expire.state = StateforText(expire)
        name.state = StateforText(name)
        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
                Log.d("response is: ",responseBody.toString())
                val jsonobj = JSONObject(responseBody.toString()).getJSONArray("records").getJSONObject(0)
                val fields =  jsonobj.getJSONObject("fields")
                val table =jsonobj.getString("table")
                assertEquals(table,"cards")
                assertNotNull(fields["card_number"].toString())
                assertNotNull(fields["expiry_date"].toString())
                assertNotNull(fields["fullname"].toString())
               /* Log.d("cardnumber",fields["card_number"].toString())
                Log.d("expire date",fields["expiry_date"].toString())
                Log.d("name",fields["fullname"].toString())*/
            }

            override fun onFailure(exception: Exception) {
             //   Log.d("error is:",exception.message.toString())
            }


        })
    }

}



class DemoTokenProvider: TokenProvider {
    override fun getBearerToken(callback: Skyflow.Callback) {
        val url = "https://go-server.skyflow.dev/token"
        val request = okhttp3.Request.Builder().url(url).build()
        val okHttpClient = OkHttpClient()
        try {
            val thread = Thread {
                run {
                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful)
                            throw IOException("Unexpected code $response")
                        val accessTokenObject = JSONObject(response.body()!!.string().toString())
                        val accessToken = accessTokenObject["accessToken"]
                        callback.onSuccess("$accessToken")
                    }
                }
            }
            thread.start()
        }catch (exception:Exception){
            callback.onFailure(exception)
        }
    }
}


class DemoApiCallback : Callback
{
    var receivedResponse: String = ""

    override fun onSuccess(responseBody: Any) {
            Log.d("response: ", responseBody.toString())
            receivedResponse = responseBody.toString()
    }

    override fun onFailure(exception: Exception) {
        Log.d("error,",exception.message.toString())
        this.receivedResponse = exception.message.toString().trim()
    }

}
