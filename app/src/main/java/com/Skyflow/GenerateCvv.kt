package com.Skyflow

import Skyflow.Callback
import Skyflow.GatewayConfiguration
import Skyflow.RequestMethod
import Skyflow.create
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_generate_cvv.*
import org.json.JSONObject

class GenerateCvv : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_cvv)

        val skyflowConfiguration = Skyflow.Configuration(
            BuildConfig.VAULT_ID,
            BuildConfig.VAULT_URL,
            MainActivity.DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val cardNumberInput = Skyflow.RevealElementInput(
            "your token",
            redaction = Skyflow.RedactionType.PLAIN_TEXT,
            label =  "card number",
        )
        val cvvInput = Skyflow.RevealElementInput(
            label =  "cvv",altText = "Cvv not generated"
        )

        val revealContainer = skyflowClient.container(Skyflow.ContainerType.REVEAL)
        val cardNumber = revealContainer.create(this, cardNumberInput)
        val cvv = revealContainer.create(this,cvvInput)
        val parent = findViewById<LinearLayout>(R.id.parent1)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(20,-20, 20, 0)
        cardNumber.layoutParams = lp
        cvv.layoutParams = lp
        parent.addView(cardNumber)
        parent.addView(cvv)

        submit.setOnClickListener {

            val requestBody = JSONObject()
            val expire = JSONObject()
            expire.put("mm","12")
            expire.put("yy","22")
            requestBody.put("expirationDate",expire)
            val responseBody = JSONObject()
            val cvvResponse = JSONObject()
            cvvResponse.put("cvv2",cvv)
            responseBody.put("resource",cvvResponse)
            val quertParams = JSONObject()
            val pathparams = JSONObject()
            pathparams.put("cardNumber",cardNumber)
            val requestHeader = JSONObject()
            requestHeader.put("Authorization",BuildConfig.GATEWAY_TOKEN)
            val url = BuildConfig.GATEWAY_GENERATE_CVV_URL  // eg:  url.../{cardNumber}/...
            val bodyForgateWay = GatewayConfiguration(gatewayURL = url,requestHeader = requestHeader,pathParams = pathparams,methodName = RequestMethod.POST,requestBody = requestBody, responseBody =  responseBody,queryParams = quertParams)
            skyflowClient.invokeGateway(bodyForgateWay,object : Callback
            {
                override fun onSuccess(responseBody: Any) {
                    Log.d("gateway success",responseBody.toString())
                }

                override fun onFailure(exception: Exception) {
                    Log.d("gateway failure",exception.toString())
                }

            })
        }



    }
}