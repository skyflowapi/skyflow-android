package com.Skyflow

import Skyflow.*
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import androidx.test.core.app.ApplicationProvider
import com.skyflow_android.R
import junit.framework.Assert.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RevealTest {

    lateinit var skyflow : Client
    lateinit var context: Context
    @Before
    fun setup()
    {
        var configuration = Configuration( "ffe21f44f68a4ae3b4fe55ee7f0a85d6",
            "https://na1.area51.vault.skyflowapis.com<",
            DemoTokenProvider())
        skyflow = Client(configuration)
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun testRevealElementInput()
    {
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4, R.font.roboto_light, Gravity.START, Color.BLUE)
        val styles = Styles(bstyle)
        val revealInput = RevealElementInput("2429-2390-5964-3689",RedactionType.DEFAULT,styles,"card number",)
        assertEquals(revealInput.id,"2429-2390-5964-3689")
        assertEquals(revealInput.redaction,RedactionType.DEFAULT)
        assertEquals(revealInput.label,"card number")
    }

    @Test
    fun testCreateSkyflowRevealContainer()
    {
        val container = skyflow.container(ContainerType.REVEAL)
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4, R.font.roboto_light, Gravity.START, Color.BLUE)
        val styles = Styles(bstyle)
        val revealInput = RevealElementInput("2429-2390-5964-3689",RedactionType.DEFAULT,styles,"card number")
        val cardNumber = container.create(context,revealInput, RevealElementOptions())

        assertEquals(cardNumber.placeholder.text.toString(),"2429-2390-5964-3689")
    }

    @Test
    fun testCheckRevealElementsArray()
    {
        val container = skyflow.container(ContainerType.REVEAL)
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4, R.font.roboto_light, Gravity.START, Color.BLUE)
        val styles = Styles(bstyle)
        val revealInput = RevealElementInput("2429-2390-5964-3689",RedactionType.DEFAULT,styles,"card number")
        val cardNumber = container.create(context,revealInput, RevealElementOptions())

        assertEquals(container.revealElements.count(),1)
        assertEquals(container.revealElements[0].revealInput.id,cardNumber.revealInput.id)
        assertNotNull(container.revealElements[0].revealInput.id)
    }

    @Test
    fun testPureGet()
    {
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("id", "51b1406a-0a30-49bf-b303-0eef66bd502d")
        recordObj.put("redaction", Skyflow.RedactionType.PLAIN_TEXT)
        val recordObj1 = JSONObject()
        recordObj1.put("id", "b6f3a872-1d92-4abc-84c3-018ee2401038")
        recordObj1.put("redaction", Skyflow.RedactionType.DEFAULT)
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        skyflow.get(revealRecords, Skyflow.RevealOptions(), object: Skyflow.Callback {
            override fun onSuccess(responseBody: Any) {
                //Log.d("get success:", " $responseBody")
                val successRecord_id = JSONObject(responseBody.toString()).getJSONArray("records").getJSONObject(0).getString("id")
                val failedRecord_description = JSONObject(responseBody.toString()).getJSONArray("errors").getJSONObject(0).getJSONObject("error").getString("description")
                assertEquals(successRecord_id,"51b1406a-0a30-49bf-b303-0eef66bd502d")
                assertTrue(failedRecord_description.toString().contains("Tokens not found for [b6f3a872-1d92-4abc-84c3-018ee2401038]"))
            }
            override fun onFailure(exception: Exception) {
                //Log.d("get failure:", " $exception")
            }
        })

    }

    @Test
    fun testGetWithInvalidToken()
    {
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4,R.font.roboto_light, Gravity.START, Color.BLUE)
        val styles = Styles(bstyle)
        val revealInput = RevealElementInput(
            "412b820b-9b8b-407a-b313-1e2fb83664",RedactionType.PLAIN_TEXT,
            styles, "name",
        )
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealElement = revealContainer.create(context, revealInput, RevealElementOptions())

        revealContainer.reveal(object: Skyflow.Callback {
            override fun onSuccess(responseBody: Any) {
                //Log.d("", "success reveal: ${responseBody}")
                val jsonobj = JSONObject(responseBody.toString()).getJSONArray("errors").getJSONObject(0).getJSONObject("error")
                //Log.d("error",jsonobj.toString())
                assertTrue(jsonobj.getString("description").toString().contains("Tokens not found for [412b820b-9b8b-407a-b313-1e2fb83664]"))

            }
            override fun onFailure(exception: Exception) {
               // Log.d("", "failure reveal: $exception")
            }})
    }

    @Test
    fun testValidAndInvalidToken() //contains one valid token and one invalid token for pure reveal
    {
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("id", "51b1406a-0a30-49bf-b303-0eef66bd502d")
        recordObj.put("redaction", Skyflow.RedactionType.PLAIN_TEXT)
        val recordObj1 = JSONObject()
        recordObj1.put("id", "b6f3a872-1d92-4abc-84c3-018ee2401038")
        recordObj1.put("redaction", Skyflow.RedactionType.DEFAULT)
        revealRecordsArray.put(recordObj)
        revealRecordsArray.put(recordObj1)
        revealRecords.put("records", revealRecordsArray)
        skyflow.get(revealRecords, Skyflow.RevealOptions(), object: Skyflow.Callback {
            override fun onSuccess(responseBody: Any) {
                val successRecord = JSONObject(responseBody.toString()).getJSONArray("records").getJSONObject(0).getString("id")
                val failedRecord_description = JSONObject(responseBody.toString()).getJSONArray("errors").getJSONObject(0).getJSONObject("error").getString("description")
                assertEquals(successRecord,"51b1406a-0a30-49bf-b303-0eef66bd502d")
                assertEquals(failedRecord_description,"Tokens not found for [b6f3a872-1d92-4abc-84c3-018ee2401038]")

            }

            override fun onFailure(exception: Exception) {
            }

        })
    }



    @Test
    fun testRevealContainersReveal()
    {
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4,R.font.roboto_light, Gravity.START, Color.BLUE)
        val styles = Styles(bstyle)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            styles, "expire_date",

        )
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealElement = revealContainer.create(context, revealInput, Skyflow.RevealElementOptions())
        val realValue = "11/32"
        revealContainer.reveal(object: Callback {
            override fun onSuccess(responseBody: Any) {
                assertEquals(revealElement.placeholder.text.toString(),realValue)
            }
            override fun onFailure(exception: Exception) {
                //Log.d("", "failure reveal: $exception")
            }})
    }

    @Test
    fun testEmptyStyleForLabel()
    {
        val styles = Styles()
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,styles,
          "expire_date"
        )
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealElement = revealContainer.create(context, revealInput, Skyflow.RevealElementOptions())

    }

    @Test
    fun testNullStyleForLabel() //unable to pass null to style in Styles constructor
    {
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4,R.font.roboto_light, Gravity.START, Color.BLUE)
        //val styles = Styles(null)
        val styles = Styles(bstyle)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,styles,
            "expire_date"
        )
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealElement = revealContainer.create(context, revealInput, Skyflow.RevealElementOptions())


    }

    @Test
    fun testEmptyStylesForLabel()
    {
         val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "expire_date"
            )
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealElement = revealContainer.create(context, revealInput, Skyflow.RevealElementOptions())

    }

    @Test
    fun testNullStylesForLabel()  //unable to pass null for styles in RevealElementInput
    {
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4,R.font.roboto_light, Gravity.START, Color.BLUE)
        val styles = Styles(bstyle)
        /*val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            null, "expire_date",

            )*/
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            styles, "expire_date",

            )
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val revealElement = revealContainer.create(context, revealInput, Skyflow.RevealElementOptions())

    }


    @Test
    fun testWithWrongVaultURLWithLabel()
    {
        val skyflowConfiguration = Configuration(
            "b359c43f1b844ff4bea0f098d2c091",
            "https://sb1.area51.vault.skyflapis.tech",
            DemoTokenProvider()
        )
        val skyflow = Client(skyflowConfiguration)
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4,R.font.roboto_light, Gravity.START, Color.BLUE)
        val styles = Styles(bstyle)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            styles, "expire_date"

        )
        val revealElement = revealContainer.create(context, revealInput, Skyflow.RevealElementOptions())
        revealContainer.reveal(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Exception) {
                //Log.d("failure:", "${exception.message.toString()}")
                //Assert.assertTrue(exception.message.toString()
                 //   .contains("No address associated with hostname"))
                val error_message = exception.message.toString()
                assertTrue(error_message.contains("Reveal elements failed"))
            }

        })
    }

    @Test
    fun testWithInvalidVaultIDWithLabel()
    {
        val skyflowConfiguration = Configuration(
            "ff",
            "https://sb1.area51.vault.skyflowapis.tech",
            DemoTokenProvider()
        )
        val skyflow = Client(skyflowConfiguration)
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4,R.font.roboto_light, Gravity.START, Color.BLUE)
        val styles = Styles(bstyle)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            styles, "expire_date"
        )
        val revealElement = revealContainer.create(context, revealInput, Skyflow.RevealElementOptions())
        revealContainer.reveal(object: Callback {
            override fun onSuccess(responseBody: Any) {
                val jsonobj = JSONObject(responseBody.toString()).getJSONArray("errors").getJSONObject(0).getJSONObject("error")
                assertTrue(jsonobj.getString("description").toString().contains("document does not exist"))
            }
            override fun onFailure(exception: Exception) {
                //assertEquals(exception.message.toString(),"No value for records")
               /* val jsonobj = JSONObject(exception.message.toString()).getJSONObject("error")
                Assert.assertTrue(jsonobj.getString("message").toString()
                    .contains("document does not exist"))*/
            }})
    }

    @Test
    fun testEmptyVaultIDWithLabel()
    {
        val skyflowConfiguration = Configuration(
            "",
            "https://sb1.area51.vault.skyflowapis.tech",
            DemoTokenProvider()
        )
        val skyflow = Client(skyflowConfiguration)
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4,R.font.roboto_light, Gravity.START, Color.BLUE)
        val styles = Styles(bstyle)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            styles, "expire_date"
        )
        val revealElement = revealContainer.create(context, revealInput, Skyflow.RevealElementOptions())
        revealContainer.reveal(object: Callback {
            override fun onSuccess(responseBody: Any) {
                val jsonobj = JSONObject(responseBody.toString()).getJSONArray("errors").getJSONObject(0).getJSONObject("error")
                assertTrue(jsonobj.getString("description").toString().contains("unable to retrieve vault mapping for vault ID"))
            }
            override fun onFailure(exception: Exception) {
                //assertEquals(exception.message.toString(),"No value for records")
                /* val jsonobj = JSONObject(exception.message.toString()).getJSONObject("error")
                 Assert.assertTrue(jsonobj.getString("message").toString()
                     .contains("document does not exist"))*/
            }})
    }

    @Test
    fun testEmptyVaultURLWithLabel()
    {
        val skyflowConfiguration = Configuration(
            "b359c43f1b844ff4bea0f098d2c091",
            "",
            DemoTokenProvider()
        )
        val skyflow = Client(skyflowConfiguration)
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val padding = Padding(30,20,20,20)
        val bstyle = Style(Color.BLUE,30f,padding,4,R.font.roboto_light, Gravity.START, Color.BLUE)
        val styles = Styles(bstyle)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            styles, "expire_date"
        )
        val revealElement = revealContainer.create(context, revealInput, Skyflow.RevealElementOptions())
        revealContainer.reveal(object: Callback {
            override fun onSuccess(responseBody: Any) {
                     }
            override fun onFailure(exception: Exception) {
                Assert.assertEquals(exception.message, toString(), "Bad or missing url")
            }})
    }

    @Test
    fun testInvalidVaultID()
    {
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("id", "51b1406a-0a30-49bf-b303-0eef66bd502d")
        recordObj.put("redaction", Skyflow.RedactionType.PLAIN_TEXT)
        val recordObj1 = JSONObject()
        recordObj1.put("id", "b6f3a872-1d92-4abc-84c3-018ee2401038")
        recordObj1.put("redaction", Skyflow.RedactionType.DEFAULT)
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        skyflow.get(revealRecords, Skyflow.RevealOptions(), object: Skyflow.Callback {
            override fun onSuccess(responseBody: Any) {
                val jsonobj = JSONObject(responseBody.toString()).getJSONArray("errors").getJSONObject(0).getJSONObject("error")
                assertTrue(jsonobj.getString("description").toString().contains("document does not exist"))
            }
            override fun onFailure(exception: Exception) {
                //assertEquals(exception.message.toString(),"No value for records")
                /* val jsonobj = JSONObject(exception.message.toString()).getJSONObject("error")
                 Assert.assertTrue(jsonobj.getString("message").toString()
                     .contains("document does not exist"))*/
            }})


    }

    @Test
    fun testInvalidVaultURL()
    {
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("id", "51b1406a-0a30-49bf-b303-0eef66bd502d")
        recordObj.put("redaction", Skyflow.RedactionType.PLAIN_TEXT)
        val recordObj1 = JSONObject()
        recordObj1.put("id", "b6f3a872-1d92-4abc-84c3-018ee2401038")
        recordObj1.put("redaction", Skyflow.RedactionType.DEFAULT)
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        skyflow.get(revealRecords, Skyflow.RevealOptions(), object: Skyflow.Callback {
            override fun onSuccess(responseBody: Any) {
                  }
            override fun onFailure(exception: Exception) {
                //Log.d("failure:", "${exception.message.toString()}")
                //Assert.assertTrue(exception.message.toString()
                //   .contains("No address associated with hostname"))
                val error_message = exception.message.toString()
                assertTrue(error_message.contains("Reveal elements failed"))
            }
        })

    }

    @Test
    fun testEmptyVaultID()
    {
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("id", "51b1406a-0a30-49bf-b303-0eef66bd502d")
        recordObj.put("redaction", Skyflow.RedactionType.PLAIN_TEXT)
        val recordObj1 = JSONObject()
        recordObj1.put("id", "b6f3a872-1d92-4abc-84c3-018ee2401038")
        recordObj1.put("redaction", Skyflow.RedactionType.DEFAULT)
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        skyflow.get(revealRecords, Skyflow.RevealOptions(), object: Skyflow.Callback {
            override fun onSuccess(responseBody: Any) {
                val jsonobj = JSONObject(responseBody.toString()).getJSONArray("errors").getJSONObject(0).getJSONObject("error")
                assertTrue(jsonobj.getString("description").toString().contains("unable to retrieve vault mapping for vault ID"))
            }
            override fun onFailure(exception: Exception) {
                //assertEquals(exception.message.toString(),"No value for records")
                /* val jsonobj = JSONObject(exception.message.toString()).getJSONObject("error")
                 Assert.assertTrue(jsonobj.getString("message").toString()
                     .contains("document does not exist"))*/
            }})


    }

    @Test
    fun testEmptyVaultURL()
    {
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("id", "51b1406a-0a30-49bf-b303-0eef66bd502d")
        recordObj.put("redaction", Skyflow.RedactionType.PLAIN_TEXT)
        val recordObj1 = JSONObject()
        recordObj1.put("id", "b6f3a872-1d92-4abc-84c3-018ee2401038")
        recordObj1.put("redaction", Skyflow.RedactionType.DEFAULT)
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        skyflow.get(revealRecords, Skyflow.RevealOptions(), object: Skyflow.Callback {
            override fun onSuccess(responseBody: Any) {
                 }
            override fun onFailure(exception: Exception) {
                Assert.assertEquals(exception.message, toString(), "Bad or missing url")

            }
        })

    }
}