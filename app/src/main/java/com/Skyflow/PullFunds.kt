package com.Skyflow

import Skyflow.*
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_generate_cvv.*
import org.json.JSONObject

class PullFunds : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_cvv)

        val skyflowConfiguration = Skyflow.Configuration(
            BuildConfig.VAULT_ID,
            BuildConfig.VAULT_URL,
            MainActivity.DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)

        val cardNumberInput = Skyflow.CollectElementInput(
            table = "",column = "", SkyflowElementType.CARD_NUMBER, label="Card Number", placeholder = "Please enter card number"
        )

        val cvvElementInput = Skyflow.CollectElementInput(table="", column = "", SkyflowElementType.CVV, label = "cvv", placeholder = "please enter cvv")


        val collectContainer = skyflowClient.container(Skyflow.ContainerType.COLLECT)
        val cardNumber = collectContainer.create(this, cardNumberInput)
        val cvv = collectContainer.create(this, cvvElementInput)

        val revealContainer = skyflowClient.container(Skyflow.ContainerType.REVEAL)
        val approvalCodeElement = Skyflow.RevealElementInput(
            "",
            redaction = Skyflow.RedactionType.PLAIN_TEXT,
            label =  "approval code",altText = "Approval Code"
        )
        val approvalCode = revealContainer.create(this, approvalCodeElement)

//        val cvv = revealContainer.create(this,cvvInput)

        val parent = findViewById<LinearLayout>(R.id.parent1)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(20,-20, 20, 0)
        cardNumber.layoutParams = lp
        approvalCode.layoutParams = lp
        cvv.layoutParams = lp
        parent.addView(cardNumber)
        parent.addView(cvv)
        parent.addView(approvalCode)

        val requestbodyString = "{\n" +
                // json string
                "}"

        val reqBodyObj = JSONObject(requestbodyString)

        submit.setOnClickListener {

            val requestBody = JSONObject()
            reqBodyObj.put("senderPrimaryAccountNumber", cardNumber)
            reqBodyObj.put("cardCvv2Value", cvv)
            val responseBody = JSONObject()
            responseBody.put("approvalCode",approvalCode)
            val queryParams = JSONObject()
            val pathparams = JSONObject()
            val requestHeader = JSONObject()
            requestHeader.put("Accept", "application/json")
            requestHeader.put("Authorization",BuildConfig.GATEWAY_TOKEN)
            val url = BuildConfig.GATEWAY_URL_PULL_FUNDS
            val bodyForGateway = GatewayConfiguration(gatewayURL = url,requestHeader = requestHeader,pathParams = pathparams,methodName = RequestMethod.POST, requestBody=reqBodyObj, responseBody =  responseBody,queryParams = queryParams)
            skyflowClient.invokeGateway(bodyForGateway,object : Callback
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