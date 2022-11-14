package com.Skyflow

import Skyflow.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import Skyflow.LogLevel
import Skyflow.Options
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_collect.*
import org.json.JSONArray
import org.json.JSONObject

class UpsertFeature : AppCompatActivity() {

    private val TAG = UpsertFeature::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect)
        val tokenProvider = CollectActivity.DemoTokenProvider()
        val skyflowConfiguration = Configuration(
            BuildConfig.VAULT_ID,
            BuildConfig.VAULT_URL,
            tokenProvider,
            Options(LogLevel.ERROR, Env.PROD)
        )
        val skyflowClient = init(skyflowConfiguration)
        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val padding = Padding(8, 8, 8, 8)
        val istyle =
            Style(Color.RED, 15f, padding, 6, R.font.roboto_light, Gravity.START, Color.RED)
        val styles = Styles(null, null, null, null, istyle)
        val labelStyles = Styles(null, null, null, null, istyle)
        val baseErrorStyles =
            Style(null, null, padding, null, R.font.roboto_light, Gravity.START, Color.RED)
        val errorStyles = Styles(baseErrorStyles)

        val cardNumberInput = CollectElementInput(
            "cards", "card_number", SkyflowElementType.CARD_NUMBER, styles, labelStyles,
            errorStyles, "Card Number", "CardNumber"
        )
        val expiryDateInput = CollectElementInput(
            "cards", "expiry_date", SkyflowElementType.EXPIRATION_DATE,
            styles, labelStyles, errorStyles, label = "expiry date", placeholder = "expiry date"
        )
        val nameInput = CollectElementInput(
            "cards",
            "fullname",
            SkyflowElementType.CARDHOLDER_NAME,
            styles,
            labelStyles,
            errorStyles,
            "Full Name",
            "Full Name",
        )
        val cvvInput = CollectElementInput(
            table = "cards",
            column = "cvv",
            type = SkyflowElementType.CVV,
            inputStyles = styles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            placeholder = "CVV",
            altText = "CVV"
        )
        val options = CollectElementOptions(true)
        val cardNumber = collectContainer.create(this,
            cardNumberInput,
            CollectElementOptions(enableCardIcon = true))
        val expirationDate = collectContainer.create(this,
            expiryDateInput,
            CollectElementOptions(format = "yyyy/mm"))
        val name = collectContainer.create(this, nameInput, options)
        val cvv = collectContainer.create(this, cvvInput)

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

            //Upsert options
            val upsertArray = JSONArray()
            val upsertColumn = JSONObject()
            upsertColumn.put("table", "cards")
            upsertColumn.put("column", "card_number")
            upsertArray.put(upsertColumn)

            collectContainer.collect(object : Callback {
                override fun onSuccess(responseBody: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "collect success: $responseBody")
                    val jsonobj =
                        JSONObject(responseBody.toString()).getJSONArray("records").getJSONObject(0)
                    val fields = jsonobj.getJSONObject("fields")
                    val intent = Intent(this@UpsertFeature, RevealActivity::class.java)
                    intent.putExtra("cardNumber", fields["card_number"].toString())
                    intent.putExtra("expiryDate", fields["expiry_date"].toString())
                    intent.putExtra("name", fields["fullname"].toString())
                    intent.putExtra("cvv", fields["cvv"].toString())
                    startActivity(intent)
                }

                override fun onFailure(exception: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "collect failure: ${(exception as Exception).message}")
                }
            }, CollectOptions(true, upsert =  upsertArray))
        }

        clear.setOnClickListener {
            clearFields(mutableListOf(cardNumber, cvv, name, expirationDate))
        }

    }
    //reset elements to initial state
    fun clearFields(elements: List<TextField>) {
        for (element in elements) {
            element.unmount()
        }
    }
}