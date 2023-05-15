package com.Skyflow

import Skyflow.*
import Skyflow.LogLevel
import Skyflow.Options
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_input_formatting.*
import org.json.JSONObject


class InputFormattingCollect : AppCompatActivity() {

    private val TAG = InputFormattingCollect::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_formatting)

        val tokenProvider = CollectActivity.DemoTokenProvider()
        val skyflowConfiguration = Configuration(
            "<VAULT_ID>",
            "<VAULT_URL>",
            tokenProvider,
            Options(LogLevel.DEBUG, Env.PROD)
        )
        val skyflowClient = init(skyflowConfiguration)

        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val padding = Padding(8, 8, 8, 8)

        val baseStyle = Style(
            Color.parseColor("#403E6B"),
            10f,
            padding,
            null,
            R.font.montserrat,
            Gravity.START,
            Color.parseColor("#403E6B")
        )

        val completeStyle = Style(
            Color.GREEN,
            10f,
            padding,
            6,
            R.font.montserrat,
            Gravity.END,
            Color.GREEN
        )

        val focusStyle = Style(
            Color.parseColor("#403E6B"),
            10f,
            padding,
            6,
            R.font.montserrat,
            Gravity.START,
            Color.GREEN
        )

        val errorStyle = Style(
            Color.YELLOW,
            10f,
            padding,
            4,
            R.font.montserrat,
            Gravity.CENTER,
            Color.YELLOW
        )

        val incompleteStyle = Style(
            Color.RED,
            15f,
            padding,
            6,
            R.font.montserrat,
            Gravity.START, Color.DKGRAY
        )

        val baseErrorStyles = Style(
            null,
            null,
            padding,
            null,
            R.font.montserrat,
            Gravity.START, Color.RED
        )

        val styles = Styles(baseStyle, completeStyle, errorStyle, focusStyle, incompleteStyle)
        val labelStyles = Styles(baseStyle, completeStyle, errorStyle, focusStyle, incompleteStyle)
        val errorStyles = Styles(baseErrorStyles)

        val cardNumberInput = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER,
            styles,
            labelStyles,
            errorStyles,
            "Card Number",
            "Card Number"
        )

        val expiryYearInput = CollectElementInput(
            "cards",
            "expiry_year",
            SkyflowElementType.EXPIRATION_YEAR,
            styles,
            labelStyles,
            errorStyles,
            "Expiry Year",
            "Expiry Year"
        )

        val expiryDateInput = CollectElementInput(
            "cards",
            "expiration_date",
            SkyflowElementType.EXPIRATION_DATE,
            styles,
            labelStyles,
            errorStyles,
            "Expiry Date",
            "Expiry Date"
        )

        val input = CollectElementInput(
            table = "cards",
            column = "zip_code",
            type = SkyflowElementType.INPUT_FIELD,
            styles,
            labelStyles,
            errorStyles,
            label = "Input Field",
            placeholder = "input field",
        )

        val parent = findViewById<LinearLayout>(R.id.linearLayout)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(20, -20, 20, 0)

        val cardNumber = collectContainer.create(
            this,
            cardNumberInput,
            CollectElementOptions(
                enableCardIcon = true,
                format = "XXXX-XXXX-XXXX-XXXX",
            )
        )

        val expirationYear = collectContainer.create(
            this,
            expiryYearInput,
            CollectElementOptions(format = "yyyy/mm")
        )

        val expirationDate = collectContainer.create(
            this,
            expiryDateInput,
            CollectElementOptions(format = "yyyy/mm")
        )

        val inputField = collectContainer.create(
            this, input, CollectElementOptions(
                format = "+91 XXX-XXX-XXXX",
//                translation = hashMapOf('X' to "[0-9]")
            )
        )

        var index = 0
        parent.addView(cardNumber, index++)
        parent.addView(expirationYear, index++)
        parent.addView(expirationDate, index++)
        parent.addView(inputField, index)

        submit.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()
            dialog.setMessage("please wait..")
            dialog.show()
            collectContainer.collect(object : Callback {
                override fun onSuccess(responseBody: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "collect success: $responseBody")

                    val jsonObject = JSONObject(responseBody.toString())
                        .getJSONArray("records")
                        .getJSONObject(0)
                    val fields = jsonObject.getJSONObject("fields")

                    val intent = Intent(
                        this@InputFormattingCollect,
                        InputFormattingReveal::class.java
                    )

                    intent.putExtra("cardNumber", fields["card_number"].toString())
                    intent.putExtra("expiryYear", fields["expiry_year"].toString())
                    intent.putExtra("expiryDate", fields["expiry_date"].toString())
                    intent.putExtra("inputField", fields["input_field"].toString())

                    startActivity(intent)
                }

                override fun onFailure(exception: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "collect failure: ${(exception as Exception).message}")
                }
            })
        }

        clear.setOnClickListener {
            clearFields(mutableListOf(cardNumber, expirationYear, expirationDate, inputField))
        }
    }

    //reset elements to initial state
    private fun clearFields(elements: List<TextField>) {
        for (element in elements) {
            element.unmount()
        }
    }
}