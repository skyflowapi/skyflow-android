package com.Skyflow

import Skyflow.Styles
import Skyflow.create
import Skyflow.reveal
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_reveal.*

class RevealActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reveal)
        val card_number = intent.getStringExtra("cardNumber")
        val expiry_date = intent.getStringExtra("expiryDate")
        val name = intent.getStringExtra("name")
        val cvv_token = intent.getStringExtra("cvv")

        //tview.text = card_number+"\n"+expiry_date+"\n"+name
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(10, 10, 10, 10)

        val padding = Skyflow.Padding(10, 10, 10, 10)
        val bstyle = Skyflow.Style(Color.parseColor("#403E6B"), 10f, padding, 6, R.font.roboto_light, Gravity.START, Color.parseColor("#403E6B"))
        val istyle = Skyflow.Style(Color.RED, 15f, padding, 6, R.font.roboto_light, Gravity.START, Color.RED)
        val styles = Skyflow.Styles(bstyle,invalid = istyle)
        val labelStyles = Styles(bstyle)
        val base_error_style = Skyflow.Style(Color.RED, 10f, padding, 6, R.font.roboto_light, Gravity.START, Color.RED)
        val error_styles = Styles(base_error_style)
        val cardNumberInput = Skyflow.RevealElementInput(
            card_number.toString(),
            Skyflow.RedactionType.PLAIN_TEXT,styles,labelStyles,error_styles,
           "card number"
        )

        val expiryDateInput = Skyflow.RevealElementInput(
            expiry_date.toString(),
            redaction = Skyflow.RedactionType.PLAIN_TEXT,styles,labelStyles, error_styles,
            label =  "expire date","mm/yyyy"
        )

        val fullNameInput = Skyflow.RevealElementInput(
            name.toString(),
            redaction = Skyflow.RedactionType.PLAIN_TEXT,styles,labelStyles,error_styles,
            label =  "Name","Name"
        )

        val cvvElement = Skyflow.RevealElementInput(
            cvv_token.toString(),
            redaction = Skyflow.RedactionType.PLAIN_TEXT, styles, labelStyles,error_styles,
            label = "CVV", "***"
        )

        val tokenProvider = MainActivity.DemoTokenProvider()
        val skyflowConfiguration = Skyflow.Configuration(
            BuildConfig.VAULT_ID,
            BuildConfig.VAULT_URL,
            tokenProvider
        )

        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val revealContainer = skyflowClient.container(Skyflow.ContainerType.REVEAL)

        val cardnumber = revealContainer.create(this, cardNumberInput)
        val expiry = revealContainer.create(this, expiryDateInput)
        val fullname = revealContainer.create(this, fullNameInput)
        val cvv = revealContainer.create(this, cvvElement)

        cardnumber.layoutParams = lp
        expiry.layoutParams = lp
        fullname.layoutParams = lp
        cvv.layoutParams = lp

        linear_parent.addView(fullname)
        linear_parent.addView(cardnumber)
        linear_parent.addView(expiry)
        linear_parent.addView(cvv)


        reveal.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()
            dialog.setMessage("please wait..")
            dialog.show()
            revealContainer.reveal(object: Skyflow.Callback {
                override fun onSuccess(responseBody: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "reveal success: ${responseBody}")
                }

                override fun onFailure(exception: Exception) {
                    dialog.dismiss()
                    Log.d(TAG, "reveal failure: ${exception.printStackTrace()}")
                }})
        }
        }


    }
