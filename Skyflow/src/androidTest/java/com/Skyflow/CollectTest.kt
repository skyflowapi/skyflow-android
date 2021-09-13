package com.Skyflow

import Skyflow.*
import Skyflow.core.elements.state.StateforText
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
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
    fun testWithEmptyAdditionalFields() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("cards", "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number"
        )
        val collectInput2 = CollectElementInput("cards",
            "expiry_date",
            SkyflowElementType.EXPIRATION_DATE,
            label = "expire date")
        val collectInput3 = CollectElementInput("cards",
            "fullname",
            SkyflowElementType.CARDHOLDER_NAME,
            label = "name")
        val card_number = container.create(context, collectInput1, options)
        val expire = container.create(context, collectInput2, options)
        val name = container.create(context, collectInput3, options)

        card_number.inputField.setText("4111111111111111")
        expire.inputField.setText("11/22")
        name.inputField.setText("santhosh")
        card_number.state = StateforText(card_number)
        expire.state = StateforText(expire)
        name.state = StateforText(name)
        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {
                Log.d("response is: ", responseBody.toString())
                val jsonobj =
                    JSONObject(responseBody.toString()).getJSONArray("records").getJSONObject(0)
                val fields = jsonobj.getJSONObject("fields")
                val table = jsonobj.getString("table")
                assertEquals(table, "cards")
                assertNotNull(fields["card_number"].toString())
                assertNotNull(fields["expiry_date"].toString())
                assertNotNull(fields["fullname"].toString())
                /* Log.d("card_number",fields["card_number"].toString())
                 Log.d("expire date",fields["expiry_date"].toString())
                 Log.d("name",fields["fullname"].toString())*/
            }

            override fun onFailure(exception: Exception) {
                //   Log.d("error is:",exception.message.toString())
            }


        }, CollectOptions())
    }

    @Test
    fun testWithInvalidAdditionalFields() {

        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        val field = JSONObject()
        field.put("fullname", "santhosh yennam")
        //fields.put("cardNumber", "41111111111")
        record.put("fields", field)
        recordsArray.put(record)
        records.put("records", recordsArray)

        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("cards", "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number"
        )
        val collectInput2 = CollectElementInput("cards",
            "expiry_date",
            SkyflowElementType.EXPIRATION_DATE,
            label = "expire date")
        val collectInput3 = CollectElementInput("cards",
            "fullname",
            SkyflowElementType.CARDHOLDER_NAME,
            label = "name")
        val card_number = container.create(context, collectInput1, options)
        val expire = container.create(context, collectInput2, options)
        val name = container.create(context, collectInput3, options)

        card_number.inputField.setText("4111111111111111")
        expire.inputField.setText("11/22")
        name.inputField.setText("santhosh")
        card_number.state = StateforText(card_number)
        expire.state = StateforText(expire)
        name.state = StateforText(name)
        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {
                Log.d("response is: ", responseBody.toString())
                val jsonobj =
                    JSONObject(responseBody.toString()).getJSONArray("records").getJSONObject(0)
                val fields = jsonobj.getJSONObject("fields")
                val table = jsonobj.getString("table")
                assertEquals(table, "cards")
                assertNotNull(fields["card_number"].toString())
                assertNotNull(fields["expiry_date"].toString())
                assertNotNull(fields["fullname"].toString())
                /* Log.d("card_number",fields["card_number"].toString())
                 Log.d("expire date",fields["expiry_date"].toString())
                 Log.d("name",fields["fullname"].toString())*/
            }

            override fun onFailure(exception: Exception) {
                //   Log.d("error is:",exception.message.toString())
            }


        },CollectOptions(true,records))
    }

    @Test
    fun testSameColumnName()
    {
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        val field = JSONObject()
        field.put("expiry_date", "11/23")
        //fields.put("cardNumber", "41111111111")
        record.put("fields", field)
        recordsArray.put(record)
        records.put("records", recordsArray)

        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("cards", "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number"
        )
        val collectInput2 = CollectElementInput("cards",
            "expiry_date",
            SkyflowElementType.EXPIRATION_DATE,
            label = "expire date")
        val collectInput3 = CollectElementInput("cards",
            "fullname",
            SkyflowElementType.CARDHOLDER_NAME,
            label = "name")
        val card_number = container.create(context, collectInput1, options)
        val expire = container.create(context, collectInput2, options)
        val name = container.create(context, collectInput3, options)

        card_number.inputField.setText("4111111111111111")
        expire.inputField.setText("11/22")
        name.inputField.setText("santhosh")
        card_number.state = StateforText(card_number)
        expire.state = StateforText(expire)
        name.state = StateforText(name)
        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {
                Log.d("response is: ", responseBody.toString())
                val jsonobj =
                    JSONObject(responseBody.toString()).getJSONArray("records").getJSONObject(0)
                val fields = jsonobj.getJSONObject("fields")
                val table = jsonobj.getString("table")
                assertEquals(table, "cards")
                assertNotNull(fields["card_number"].toString())
                assertNotNull(fields["expiry_date"].toString())
                assertNotNull(fields["fullname"].toString())
                /* Log.d("card_number",fields["card_number"].toString())
                 Log.d("expire date",fields["expiry_date"].toString())
                 Log.d("name",fields["fullname"].toString())*/
            }

            override fun onFailure(exception: Exception) {
                //   Log.d("error is:",exception.message.toString())
            }


        }, CollectOptions(true,records))
    }

    @Test
    fun testNestedJsonInAdditionalFields()
    {

    }

    @Test
    fun testNestedJsonInAddtionalFieldsSameAsNormalColumnInCollectElement()
    {

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