package com.Skyflow

import Skyflow.*
import Skyflow.LogLevel
import Skyflow.Options
import Skyflow.collect.elements.utils.CardType
import Skyflow.utils.EventName
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import com.Skyflow.collect.elements.validations.LengthMatchRule
import com.Skyflow.collect.elements.validations.ValidationSet
import com.Skyflow.utils.CustomStyles
import kotlinx.android.synthetic.main.activity_collect.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class CardBrandChoiceActivity : AppCompatActivity() {

    private val TAG = CollectActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect)
        val tokenProvider = CollectActivity.DemoTokenProvider()
        val skyflowConfiguration = Configuration(
            "<VAULT_ID>",
            "<VAULT_URL>",
            tokenProvider,
            Options(LogLevel.ERROR, Env.PROD)
        )
        val skyflowClient = init(skyflowConfiguration)
        val collectContainer = skyflowClient.container(ContainerType.COLLECT)

        val styles = CustomStyles.getInputStyles()
        val labelStyles = CustomStyles.getLabelStyles()
        val errorStyles = CustomStyles.getErrorStyles()

        val validationSet = ValidationSet()
        validationSet.add(LengthMatchRule(2, 20, "not valid"))

        val cardNumberInput = CollectElementInput(
            table = "<TABLE_NAME>",
            column = "<COLUMN_NAME>",
            SkyflowElementType.CARD_NUMBER,
            inputStyles = styles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Card Number",
            placeholder = "CardNumber"
        )

        val expiryDateInput = CollectElementInput(
            table = "<TABLE_NAME>",
            column = "<COLUMN_NAME>",
            SkyflowElementType.EXPIRATION_DATE,
            inputStyles = styles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "expiry date",
            placeholder = "expiry date"
        )

        val nameInput = CollectElementInput(
            table = "<TABLE_NAME>",
            column = "<COLUMN_NAME>",
            SkyflowElementType.CARDHOLDER_NAME,
            inputStyles = styles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Full Name",
            placeholder = "Full Name",
            validations = validationSet
        )

        val cvvInput = CollectElementInput(
            table = "<TABLE_NAME>",
            column = "<COLUMN_NAME>",
            type = SkyflowElementType.CVV,
            inputStyles = styles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            placeholder = "CVV",
            altText = "CVV"
        )

        val options = CollectElementOptions(true)
        val cardNumber = collectContainer.create(
            this, cardNumberInput, CollectElementOptions(required = true, enableCopy = true)
        )

        val expirationDate = collectContainer.create(
            this, expiryDateInput, CollectElementOptions(format = "yyyy/mm")
        )

        val name = collectContainer.create(this, nameInput, options)
        val cvv = collectContainer.create(this, cvvInput)

        cardNumber.on(EventName.FOCUS) { state ->
            Log.d(TAG, "focus: state $state")
        }

        cardNumber.on(EventName.BLUR) { state ->
            Log.d(TAG, "blur: state $state")
        }

        var scheme = arrayOf<CardType>()
        cardNumber.on(EventName.CHANGE) { state ->
            Log.d(TAG, "change: state $state")
            val value = state.getString("value")
            if (value.length < 8 && scheme.isNotEmpty()) {
                scheme = arrayOf()
                cardNumber.update(CollectElementOptions(cardMetadata = CardMetadata(scheme)))
            } else if (value.length >= 8 && scheme.isEmpty()) {
                binLookup(value, object : Callback {
                    override fun onSuccess(responseBody: Any) {
                        scheme = getCardSchemes(responseBody as JSONArray)
                        runOnUiThread(kotlinx.coroutines.Runnable {
                            cardNumber.update(
                                CollectElementOptions(cardMetadata = CardMetadata(scheme))
                            )
                        })
                    }

                    override fun onFailure(exception: Any) {
                        println(exception)
                    }
                })
            }
        }

        cardNumber.on(EventName.READY) { state ->
            Log.d(TAG, "ready: state $state")
        }

        val parent = findViewById<LinearLayout>(R.id.parent)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(20, -20, 20, 0)
        cardNumber.layoutParams = lp
        expirationDate.layoutParams = lp
        name.layoutParams = lp
        cvv.layoutParams = lp

        parent.addView(name)
        parent.addView(cardNumber)
        parent.addView(expirationDate)
        parent.addView(cvv)

        submit.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()
            dialog.setMessage("please wait..")
            dialog.show()
            collectContainer.collect(object : Callback {
                override fun onSuccess(responseBody: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "collect success: $responseBody")
                }

                override fun onFailure(exception: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "collect failure: ${(exception as Exception).message}")
                }
            }, CollectOptions(true))
        }

        clear.setOnClickListener {
            clearFields(mutableListOf(cardNumber, cvv, name, expirationDate))
        }

    }

    private fun binLookup(binValue: String, callback: Callback) {
        val jsonBody = JSONObject().put("BIN", binValue)
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val url = "https://<VAULT_URL>/v1/card_lookup"
        val request = okhttp3.Request.Builder()
            .url(url)
            .method("POST", requestBody)
            .addHeader("X-skyflow-authorization", "<BEARER_TOKEN>")
            .build()

        val okHttpClient = OkHttpClient()
        try {
            val thread = Thread {
                run {

                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw IOException("Request Failed $response")
                        }
                        val responseObject = JSONObject(response.body?.string().toString())
                        callback.onSuccess(responseObject.getJSONArray("cards_data"))
                    }
                }
            }
            thread.start()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun getCardSchemes(cardData: JSONArray): Array<CardType> {
        var cardSchemes: Array<CardType> = arrayOf()
        for (i in 0 until cardData.length()) {
            val cardObject = cardData.getJSONObject(i)
            when (cardObject.getString("card_scheme")) {
                "CARTES BANCAIRES" -> cardSchemes = cardSchemes.plus(CardType.CARTES_BANCAIRES)
                "MASTERCARD" -> cardSchemes = cardSchemes.plus(CardType.MASTERCARD)
                "VISA" -> cardSchemes = cardSchemes.plus(CardType.VISA)
            }
        }
        return cardSchemes
    }

    //reset elements to initial state
    private fun clearFields(elements: List<TextField>) {
        for (element in elements) {
            element.unmount()
        }
    }
}