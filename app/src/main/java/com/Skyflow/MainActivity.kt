package com.Skyflow

import Skyflow.*
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tokenProvider = DemoTokenProvider()
        val skyflowConfiguration = Skyflow.Configuration(
            getString(R.string.test_vault_id),
            getString(R.string.test_vault_url),
            tokenProvider
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val collectContainer =  skyflowClient.container(Skyflow.ContainerType.COLLECT)
        val revealContainer = skyflowClient.container(Skyflow.ContainerType.REVEAL)
        val padding = Skyflow.Padding(30,20,20,20)
        val bstyle = Skyflow.Style(Color.BLUE,30f,padding,4,R.font.roboto_light, Gravity.START, Color.BLUE)
        val cstyle = Skyflow.Style(Color.GREEN,30f,padding,4,R.font.roboto_light, Gravity.END, Color.GREEN)
        val fstyle = Skyflow.Style(Color.CYAN,30f,padding,4,R.font.roboto_light, Gravity.END, Color.CYAN)
        val estyle = Skyflow.Style(Color.YELLOW,30f,padding,4,R.font.roboto_light, Gravity.CENTER, Color.YELLOW)
        val istyle = Skyflow.Style(Color.RED,30f,padding,4,R.font.roboto_light, Gravity.START, Color.RED)

        val revealStyle = Style(null, null, padding, null, R.font.roboto_light, Gravity.START, Color.BLUE)

        val styles = Skyflow.Styles(revealStyle,cstyle,estyle,fstyle,istyle)
        val cardNumberInput = Skyflow.CollectElementInput("persons","cardNumber",Skyflow.SkyflowElementType.CARD_NUMBER, label = "card number")
        val expiryDateInput = Skyflow.CollectElementInput("persons","cardExpiration",SkyflowElementType.EXPIRATION_DATE,
            Styles(), "expiry date","expiry date")
        val cvvInput = Skyflow.CollectElementInput("persons","cvv",Skyflow.SkyflowElementType.CVV,styles,"cvv","cvv")
        val nameInput = Skyflow.CollectElementInput("persons","name.first_name",Skyflow.SkyflowElementType.CARDHOLDER_NAME, styles,"Name","name")

        val cardNumber = collectContainer.create(this, cardNumberInput)
        val expirationDate = collectContainer.create(this, expiryDateInput)
        val cvv = collectContainer.create(this, cvvInput)
        val name = collectContainer.create(this, nameInput)

        val reStyles = Styles(revealStyle)

        val parent = findViewById<LinearLayout>(R.id.parent)

        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(10, 10, 10, 10)
        cvv.layoutParams = lp
        val revealInput = Skyflow.RevealElementInput(
            "45012507-f72b-4f5c-9bf9-86b133bae719",
            redaction = Skyflow.RedactionType.PLAIN_TEXT,
            label =  "CVV"
        )
        val revealElement = revealContainer.create(this, revealInput, Skyflow.RevealElementOptions())
        revealElement.layoutParams = lp
        cardNumber.layoutParams = lp
        expirationDate.layoutParams=lp
        cvv.layoutParams= lp
        name.layoutParams = lp

        parent.addView(cardNumber)
        parent.addView(expirationDate)
        parent.addView(cvv)
        parent.addView(name)
        parent.addView(revealElement)

        val submit: Button = findViewById(R.id.submit)
        submit.setOnClickListener {
            pureSDKTest()
            collectContainer.collect(object: Skyflow.Callback {
                override fun onSuccess(responseBody: Any) {
                    Log.d(TAG, "collect success: $responseBody")
                }

                override fun onFailure(exception: Exception) {
                    Log.d(TAG, "collect failure: $exception")

                }
            })
            revealContainer.reveal(object: Skyflow.Callback {
                override fun onSuccess(responseBody: Any) {
                    Log.d(TAG, "reveal success: ${responseBody}")
                }

                override fun onFailure(exception: Exception) {
                    Log.d(TAG, "reveal failure: ${exception.printStackTrace()}")

                }})
            }

    }

    private fun pureSDKTest(){
        val tokenProvider = DemoTokenProvider()
        val skyflowConfiguration = Skyflow.Configuration(
            getString(R.string.test_vault_id),
            getString(R.string.test_vault_url),
            tokenProvider
        )
        val skyflow = Skyflow.init(skyflowConfiguration)

        try{
            val records = JSONObject()
            val recordsArray = JSONArray()
            val record = JSONObject()
            record.put("table", "persons")
            val fields = JSONObject()
            fields.put("cvv", "123")
            fields.put("cardNumber", "41111111111")
            record.put("fields", fields)
            recordsArray.put(record)
            records.put("records", recordsArray)


            val revealRecords = JSONObject()
            val revealRecordsArray = JSONArray()
            val recordObj = JSONObject()
            recordObj.put("id", "45012507-f72b-4f5c-9bf9-86b133bae719")
            recordObj.put("redaction", Skyflow.RedactionType.PLAIN_TEXT)
            val recordObj1 = JSONObject()
            recordObj1.put("id", "b6f3a872-1d92-4abc-84c3-018ee2401038")
            recordObj1.put("redaction", Skyflow.RedactionType.DEFAULT)
            revealRecordsArray.put(recordObj)
            revealRecordsArray.put(recordObj1)
            revealRecords.put("records", revealRecordsArray)
            skyflow.insert(records, Skyflow.InsertOptions(false), object : Skyflow.Callback {
                override fun onSuccess(responseBody: Any) {
                    Log.d(TAG, "insert success: $responseBody")
                }

                override fun onFailure(exception: Exception) {
                    Log.d(TAG, "insert failure: $exception")
                }

            })

            skyflow.get(revealRecords, Skyflow.RevealOptions(), object: Skyflow.Callback {
                override fun onSuccess(responseBody: Any) {
                    Log.d(TAG, "get success: $responseBody")
                }

                override fun onFailure(exception: Exception) {
                    Log.d(TAG, "get failure: $exception")
                }

            })
    }catch (e: Exception){
            Log.d("TAG", "testingFunction: $e")
    }}

}

class DemoTokenProvider: Skyflow.TokenProvider {
    override fun getBearerToken(callback: Skyflow.Callback) {
        val url = "http://10.0.2.2:8000/js/analystToken"
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

