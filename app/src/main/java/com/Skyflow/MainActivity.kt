package com.Skyflow

import Skyflow.Callback
import Skyflow.collect
import Skyflow.create
import Skyflow.reveal
import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tokenProvider = DemoTokenProvider()
        val skyflowConfiguration = Skyflow.Configuration(
            "https://na1.area51.vault.skyflowapis.com/v1/vaults/",
            "ffe21f44f68a4ae3b4fe55ee7f0a85d6",
            tokenProvider
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val collectContainer =  skyflowClient.container(Skyflow.ContainerType.COLLECT)
        val revealContainer = skyflowClient.container(Skyflow.ContainerType.REVEAL)
        pureSDKTest()
        val padding = Skyflow.Padding(30,20,20,20)
        val bstyle = Skyflow.Style(Color.BLUE,30f,padding,4,R.font.roboto_light, Gravity.START, Color.BLUE)
        val cstyle = Skyflow.Style(Color.GREEN,30f,padding,4,R.font.roboto_light, Gravity.END, Color.GREEN)
        val fstyle = Skyflow.Style(Color.CYAN,30f,padding,4,R.font.roboto_light, Gravity.END, Color.CYAN)
        val estyle = Skyflow.Style(Color.YELLOW,30f,padding,4,R.font.roboto_light, Gravity.CENTER, Color.YELLOW)
        val istyle = Skyflow.Style(Color.RED,30f,padding,4,R.font.roboto_light, Gravity.START, Color.RED)

        val styles = Skyflow.Styles(bstyle,cstyle,estyle,fstyle,istyle)
        val cardNumberInput = Skyflow.CollectElementInput("persons","cardNumber",styles,"","card number",
            Skyflow.SkyflowElementType.CARD_NUMBER)
        val expiryDateInput = Skyflow.CollectElementInput("persons","cardExpiration",styles,"expiry date","expiry date",
            Skyflow.SkyflowElementType.EXPIRATION_DATE)
        val cvvInput = Skyflow.CollectElementInput("persons","cvv",styles,"","cvv",
            Skyflow.SkyflowElementType.CVV)
        val nameInput = Skyflow.CollectElementInput("persons","name.first_name",styles,"","name",
            Skyflow.SkyflowElementType.CARDHOLDER_NAME)

        val cardNumber = collectContainer.create(this, cardNumberInput)
        val expirationDate = collectContainer.create(this, expiryDateInput)
        val cvv = collectContainer.create(this, cvvInput)
        val name = collectContainer.create(this, nameInput)


        val parent = findViewById<LinearLayout>(R.id.parent)


        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(10, 10, 10, 10)
        cvv.layoutParams = lp
        val revealInput = Skyflow.RevealElementInput("45012507-f72b-4f5c-9bf9-86b133bae719", styles, "CVV", Skyflow.RedactionType.PLAIN_TEXT)
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
            collectContainer.collect(object: Callback {
                override fun onSuccess(responseBody: String) {
                    Log.d(TAG, "success: $responseBody")
                }

                override fun onFailure(exception: Exception?) {
                    Log.d(TAG, "failure: $exception")

                }
            })
            revealContainer.reveal(object: Callback {
                override fun onSuccess(responseBody: String) {
                    Log.d(TAG, "success reveal: $responseBody")
                }

                override fun onFailure(exception: Exception?) {
                    Log.d(TAG, "failure reveal: $exception")

                }})
            }

    }

    private fun pureSDKTest(){
        Log.d("enter", "testingFunction: called")
        val tokenProvider = DemoTokenProvider()
        val skyflowConfiguration = Skyflow.Configuration(
            "https://na1.area51.vault.skyflowapis.com/v1/vaults/",
            "ffe21f44f68a4ae3b4fe55ee7f0a85d6",
            tokenProvider
        )
        val skyflow = Skyflow.init(skyflowConfiguration)

        try{
            val records = """{"records" : [{"table": "persons", "fields": {"cvv": "123", "cardExpiration":"1221",
                         "cardNumber": "1232132132311231", "name": {"first_name": "Bob"}}},
                        {"table": "persons", "fields": {"cvv": "123", "cardExpiration":"1221","cardNumber": "1232132132311231",
                        "name": {"first_name": "Bobb"}}}]}"""

            val relRecods = """{"records": [{
            "id": "45012507-f72b-4f5c-9bf9-86b133bae719",
            "redaction": "PLAIN_TEXT"
        },{
            "id": "b6f3a872-1d92-4abc-84c3-018ee2401038",
            "redaction": "DEFAULT"
        }
        ]}"""


            skyflow.insert(records, Skyflow.InsertOptions(false), object : Callback {
                override fun onSuccess(responseBody: String) {
                    Log.d(TAG, "success: $responseBody")
                }

                override fun onFailure(exception: Exception?) {
                    Log.d(TAG, "failure: $exception")
                }

            })

            skyflow.get(relRecods, Skyflow.RevealOptions(), object: Callback {
                override fun onSuccess(responseBody: String) {
                    Log.d(TAG, "Reveal success: $responseBody")
                }

                override fun onFailure(exception: Exception?) {
                    Log.d(TAG, "Reveal failure: $exception")
                }

            })
//            Log.d("result", "testingFunction: " + obj1.toString())
    }catch (e: Exception){
            Log.d("TAG", "testingFunction: $e")
    }}

}

class DemoTokenProvider: Skyflow.TokenProvider {
    override fun getAccessToken(callback: Callback) {
        val url = "http://10.0.2.2:8000/js/analystToken"
        val request = okhttp3.Request.Builder().url(url).build()

        val okHttpClient = OkHttpClient()
        try {
            val thread = Thread {
                run {
                    try {
                        val call: Call = okHttpClient.newCall(request)
                        val response: Response = call.execute()
                        val accessTokenObject = JSONObject(response.body()!!.string())
                        val accessToken = accessTokenObject["accessToken"]
                        val tokenType = accessTokenObject["tokenType"]
                        callback.onSuccess("$tokenType $accessToken")
                    } catch (e: IOException) {
                        callback.onFailure(e)
                    }
                }
            }
            thread.start()
        }catch (e: Exception){
            callback.onFailure(e)
        }
    }
}

