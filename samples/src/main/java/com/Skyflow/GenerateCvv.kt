package com.Skyflow


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_generate_cvv.*
import org.json.JSONObject
import Skyflow.*

class GenerateCvv : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_cvv)

        val skyflowConfiguration = Configuration(
            "VAULT_ID",
            "VAULT_URL",
            PullFunds.DemoTokenProvider()
        )
        val skyflowClient = init(skyflowConfiguration)

        val cardNumberInput = RevealElementInput(
            "CARD_NUMBER_TOKEN",
            redaction = RedactionType.PLAIN_TEXT,
            label =  "card number",
        )
        val cvvInput = RevealElementInput(
            label =  "cvv",altText = "Cvv not generated",token = "1234"
        )

        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
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
            val queryParams = JSONObject()
            val pathParams = JSONObject()
            pathParams.put("cardNumber",cardNumber)
            val requestHeader = JSONObject()
            requestHeader.put("Authorization","GATEWAY_TOKEN")
            val url = "GATEWAY_CVV_GEN_URL"  // eg:  url.../{cardNumber}/...
            val gatewayRequestBody = ConnectionConfig(connectionURL = url,requestHeader = requestHeader,pathParams = pathParams,methodName = RequestMethod.POST,requestBody = requestBody, responseBody =  responseBody,queryParams = queryParams)
            skyflowClient.invokeConnection(gatewayRequestBody,object : Callback
            {
                override fun onSuccess(responseBody: Any) {
                    Log.d("gateway success",responseBody.toString())
                }

                override fun onFailure(exception: Any) {
                    Log.d("gateway failure",exception.toString())
                }

            })
        }



    }
}