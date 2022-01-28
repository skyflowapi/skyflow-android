package com.Skyflow

import Skyflow.*
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_generate_cvv.*
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.IOException

class PullFunds : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_cvv)

        val skyflowConfiguration = Configuration(
            "VAULT_ID",
            "VAULT_URL",
            DemoTokenProvider()
        )
        val skyflowClient = init(skyflowConfiguration)

        val cardNumberInput = CollectElementInput(
            table = "",column = "", SkyflowElementType.CARD_NUMBER, label="Card Number", placeholder = "Please enter card number"
        )

        val cvvElementInput = CollectElementInput(table="", column = "", SkyflowElementType.CVV, label = "cvv", placeholder = "please enter cvv")

        val expiryDateInput = RevealElementInput(token="EXPIRATION_DATE_TOKEN", label = "Expiration Date")


        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val cardNumber = collectContainer.create(this, cardNumberInput, CollectElementOptions(true))
        val cvv = collectContainer.create(this, cvvElementInput)

        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val expiryDate = revealContainer.create(this, expiryDateInput)
        val approvalCodeElement = RevealElementInput(
            "1234",
            redaction = RedactionType.PLAIN_TEXT,
            label =  "approval code",altText = "Approval Code"
        )
        val approvalCode = revealContainer.create(this, approvalCodeElement)

        val amountField = EditText(this)
        amountField.hint = "Amount"
        submit.text = "Complete Payment"


        val parent = findViewById<LinearLayout>(R.id.parent1)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        val lp1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(20,-20, 20, 0)
        lp1.setMargins(20, 20, 20, 20)
        cardNumber.layoutParams = lp
        approvalCode.layoutParams = lp
        cvv.layoutParams = lp
        amountField.layoutParams = lp1
        parent.addView(amountField)
        parent.addView(cardNumber)
        parent.addView(cvv)
        parent.addView(expiryDate)

        val requestBodyString = "{\n" +
                "  \"surcharge\": \"11.99\",\n" +
                "  \"localTransactionDateTime\": \"2021-10-04T23:33:06\",\n" +
                "  \"cpsAuthorizationCharacteristicsIndicator\": \"Y\",\n" +
                "  \"riskAssessmentData\": {\n" +
                "    \"traExemptionIndicator\": true,\n" +
                "    \"trustedMerchantExemptionIndicator\": true,\n" +
                "    \"scpExemptionIndicator\": true,\n" +
                "    \"delegatedAuthenticationIndicator\": true,\n" +
                "    \"lowValueExemptionIndicator\": true\n" +
                "  },\n" +
                "  \"cardAcceptor\": {\n" +
                "    \"address\": {\n" +
                "      \"country\": \"USA\",\n" +
                "      \"zipCode\": \"94404\",\n" +
                "      \"county\": \"081\",\n" +
                "      \"state\": \"CA\"\n" +
                "    },\n" +
                "    \"idCode\": \"ABCD1234ABCD123\",\n" +
                "    \"name\": \"Visa Inc. USA-Foster City\",\n" +
                "    \"terminalId\": \"ABCD1234\"\n" +
                "  },\n" +
                "  \"acquirerCountryCode\": \"840\",\n" +
                "  \"acquiringBin\": \"408999\",\n" +
                "  \"senderCurrencyCode\": \"USD\",\n" +
                "  \"retrievalReferenceNumber\": \"330000550000\",\n" +
                "  \"addressVerificationData\": {\n" +
                "    \"street\": \"XYZ St\",\n" +
                "    \"postalCode\": \"12345\"\n" +
                "  },\n" +
                "  \"systemsTraceAuditNumber\": \"451001\",\n" +
                "  \"businessApplicationId\": \"AA\",\n" +
                "  \"settlementServiceIndicator\": \"9\",\n" +
                "  \"visaMerchantIdentifier\": \"73625198\",\n" +
                "  \"foreignExchangeFeeTransaction\": \"11.99\",\n" +
                "  \"nationalReimbursementFee\": \"11.22\"\n" +
                "}"

        val reqBodyObj = JSONObject(requestBodyString)

        submit.setOnClickListener {

            val requestBody = JSONObject()
            reqBodyObj.put("senderPrimaryAccountNumber", cardNumber)
            reqBodyObj.put("cardCvv2Value", cvv)
            reqBodyObj.put("senderCardExpiryDate", expiryDate)
            reqBodyObj.put("amount", amountField.text.toString())
            val responseBody = JSONObject()
            responseBody.put("approvalCode",approvalCode)
            val queryParams = JSONObject()
            val pathparams = JSONObject()
            val requestHeader = JSONObject()
            requestHeader.put("Accept", "application/json")
            requestHeader.put("Authorization",BuildConfig.GATEWAY_TOKEN)
            val url = "GATEWAY_URL_PULL_FUNDS_URL"
            val bodyForGateway = ConnectionConfig(connectionURL = url,requestHeader = requestHeader,pathParams = pathparams,methodName = RequestMethod.POST, requestBody=reqBodyObj, responseBody =  responseBody,queryParams = queryParams)
            skyflowClient.invokeConnection(bodyForGateway,object : Callback
            {
                override fun onSuccess(responseBody: Any) {
                    runOnUiThread(Runnable {
                        kotlin.run {
                            Toast.makeText(
                                this@PullFunds,
                                "Payment is successful with transaction id ${
                                    (responseBody as JSONObject).get("transactionIdentifier")
                                }",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                    Log.d("gateway success",responseBody.toString())
                }

                override fun onFailure(exception: Any) {
                    Log.d("gateway failure",exception.toString())
                }

            })
        }
    }

    class DemoTokenProvider : TokenProvider {
        override fun getBearerToken(callback: Callback) {
            val url = "TOKEN_LOCAL_URL"
            val request = okhttp3.Request.Builder().url(url).build()
            val okHttpClient = OkHttpClient()
            try {
                val thread = Thread {
                    run {
                        okHttpClient.newCall(request).execute().use { response ->
                            if (!response.isSuccessful)
                                throw IOException("Unexpected code $response")
                            val accessTokenObject =
                                JSONObject(response.body!!.string())
                            val accessToken = accessTokenObject["accessToken"]
                            callback.onSuccess("$accessToken")
                        }
                    }
                }
                thread.start()
            } catch (exception: Exception) {
                callback.onFailure(exception)
            }
        }
    }
}