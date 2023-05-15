package com.Skyflow

import Skyflow.*
import Skyflow.LogLevel
import Skyflow.Options
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_reveal.*

class InputFormattingReveal : AppCompatActivity() {

    private val TAG = InputFormattingReveal::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reveal)

        val cardNumberToken = intent.getStringExtra("cardNumber")
        val yearToken = intent.getStringExtra("expiryYear")
        val dateToken = intent.getStringExtra("expiryDate")
        val inputFieldToken = intent.getStringExtra("inputField")

        val tokenProvider = CollectActivity.DemoTokenProvider()
        val skyflowConfiguration = Configuration(
            BuildConfig.VAULT_ID,
            BuildConfig.VAULT_URL,
            tokenProvider,
            Options(LogLevel.DEBUG, Env.PROD)
        )
        val skyflowClient = init(skyflowConfiguration)

        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
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

        val cardNumberInput = RevealElementInput(
            token = cardNumberToken,
            redaction = RedactionType.PLAIN_TEXT,
            inputStyles = styles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Card Number",
            altText = "Card Number"
        )

        val expiryYearInput = RevealElementInput(
            token = yearToken,
            redaction = RedactionType.PLAIN_TEXT,
            inputStyles = styles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Expiry Year",
            altText = "yyyy"
        )

        val expiryDateInput = RevealElementInput(
            token = dateToken,
            redaction = RedactionType.PLAIN_TEXT,
            inputStyles = styles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Expiry Date",
            altText = "mm/yy"
        )

        val input = RevealElementInput(
            token = inputFieldToken,
            redaction = RedactionType.PLAIN_TEXT,
            inputStyles = styles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Input Field",
            altText = "input field"
        )

        val cardNumber = revealContainer.create(
            this,
            cardNumberInput,
//            RevealElementOptions(
//                format = "XXXX XXXX XXXX XXXX",
//                translation = hashMapOf('X' to "[0-9]")
//            )
        )

        val expiryYear = revealContainer.create(this, expiryYearInput)

        val expiryDate = revealContainer.create(this, expiryDateInput)

        val inputField = revealContainer.create(
            this, input, RevealElementOptions(
//                format = "+91 (XXX) XXXX XXX",
//                translation = hashMapOf('X' to "[0-9]")
            )
        )

        val parent = findViewById<LinearLayout>(R.id.linear_parent)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(20, -20, 20, 0)

        cardNumber.layoutParams = lp
        inputField.layoutParams = lp

        var index = 0
        parent.addView(cardNumber, index++)
        parent.addView(expiryYear, index++)
        parent.addView(expiryDate, index++)
        parent.addView(inputField, index)

        val responseView = TextView(this)
        responseView.tag = "response_view"

        reveal.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()
            dialog.setMessage("please wait..")
            dialog.show()
            revealContainer.reveal(object : Callback {
                override fun onSuccess(responseBody: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "collect success: $responseBody")
                }

                override fun onFailure(exception: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "collect success: $exception")
                }
            })
        }
    }
}