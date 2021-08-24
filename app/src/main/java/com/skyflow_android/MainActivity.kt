package com.skyflow_android

import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.skyflow_android.collect.client.InsertOptions
import com.skyflow_android.collect.client.collect
import com.skyflow_android.collect.client.create
import com.skyflow_android.collect.elements.CollectElementInput
import com.skyflow_android.collect.elements.SkyflowElementType
import com.skyflow_android.collect.elements.core.styles.SkyflowStyles
import com.skyflow_android.core.Skyflow
import com.skyflow_android.core.SkyflowConfiguration
import com.skyflow_android.core.container.ContainerTypes
import com.skyflow_android.core.elements.Padding
import com.skyflow_android.core.elements.styles.SkyflowStyle
import com.skyflow_android.core.protocol.SkyflowCallback
import com.skyflow_android.core.protocol.TokenProvider
import com.skyflow_android.reveal.client.*
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tokenProvider = DemoTokenProvider()
        val skyflowConfiguration = SkyflowConfiguration("https://na1.area51.vault.skyflowapis.com/v1/vaults/", "ffe21f44f68a4ae3b4fe55ee7f0a85d6", tokenProvider)
        val skyflow:Skyflow = Skyflow(skyflowConfiguration)
        val container =  skyflow.container(ContainerTypes.COLLECT)
        val revealContainer = skyflow.container(ContainerTypes.REVEAL)
        pureSDKTest()
        val padding = Padding(30,20,20,20)
        val bstyle = SkyflowStyle(Color.BLUE,30f,padding,4,R.font.roboto_light, Gravity.START, Color.BLUE)
        val cstyle = SkyflowStyle(Color.GREEN,30f,padding,4,R.font.roboto_light, Gravity.END, Color.GREEN)
        val fstyle = SkyflowStyle(Color.CYAN,30f,padding,4,R.font.roboto_light, Gravity.END, Color.CYAN)
        val estyle = SkyflowStyle(Color.YELLOW,30f,padding,4,R.font.roboto_light, Gravity.CENTER, Color.YELLOW)
        val istyle = SkyflowStyle(Color.RED,30f,padding,4,R.font.roboto_light, Gravity.START, Color.RED)

        val styles = SkyflowStyles(bstyle,cstyle,estyle,fstyle,istyle)
        val cardNumberInput = CollectElementInput("persons","cardNumber",styles,"","card number",
            SkyflowElementType.CARDNUMBER)
        val expiryDateInput = CollectElementInput("persons","cardExpiration",styles,"expiry date","expiry date",
            SkyflowElementType.EXPIRATIONDATE)
        val cvvInput = CollectElementInput("persons","cvv",styles,"","cvv",
            SkyflowElementType.CVV)
        val nameInput = CollectElementInput("persons","name.first_name",styles,"","name",
            SkyflowElementType.CARDHOLDERNAME)

        val cardNumber = container.create(this, cardNumberInput)
        val expirationDate = container.create(this, expiryDateInput)
        val cvv = container.create(this, cvvInput)
        val name = container.create(this, nameInput)

        val parent = findViewById<LinearLayout>(R.id.parent)

        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(10, 10, 10, 10)
        cvv.layoutParams = lp
        val revealInput = RevealElementInput("45012507-f72b-4f5c-9bf9-86b133bae719", styles, "CVV", "PLAIN_TEXT")
        val revealElement = revealContainer.create(this, revealInput, RevealElementOptions())
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
            container.collect(object: SkyflowCallback{
                override fun success(responseBody: String) {
                    Log.d(TAG, "success: $responseBody")
                }

                override fun failure(exception: Exception?) {
                    Log.d(TAG, "failure: $exception")

                }
            })
            revealContainer.reveal(object: SkyflowCallback{
                override fun success(responseBody: String) {
                    Log.d(TAG, "success reveal: $responseBody")
                }

                override fun failure(exception: Exception?) {
                    Log.d(TAG, "failure reveal: $exception")

                }})
            }

    }

    private fun pureSDKTest(){
        Log.d("enter", "testingFunction: called")
        val tokenProvider = DemoTokenProvider()
        val skyflowConfiguration = SkyflowConfiguration("https://na1.area51.vault.skyflowapis.com/v1/vaults/", "ffe21f44f68a4ae3b4fe55ee7f0a85d6", tokenProvider)
        val skyflow:Skyflow = Skyflow(skyflowConfiguration)

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


            skyflow.insert(records, InsertOptions(false), object : SkyflowCallback{
                override fun success(responseBody: String) {
                    Log.d(TAG, "success: $responseBody")
                }

                override fun failure(exception: Exception?) {
                    Log.d(TAG, "failure: $exception")
                }

            })

            skyflow.get(relRecods, RevealOptions(), object: SkyflowCallback{
                override fun success(responseBody: String) {
                    Log.d(TAG, "Reveal success: $responseBody")
                }

                override fun failure(exception: Exception?) {
                    Log.d(TAG, "Reveal failure: $exception")
                }

            })
//            Log.d("result", "testingFunction: " + obj1.toString())
    }catch (e: Exception){
            Log.d("TAG", "testingFunction: $e")
    }}

}

class DemoTokenProvider: TokenProvider{
    override fun getAccessToken(callback: SkyflowCallback) {
        val url = "http://10.0.2.2:8000/js/analystToken"
        val request = Request.Builder().url(url).build()
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
                        callback.success("$tokenType $accessToken")
                    } catch (e: IOException) {
                        callback.failure(e)
                    }
                }
            }
            thread.start()
        }catch (e: Exception){
            callback.failure(e)
        }
    }
}

