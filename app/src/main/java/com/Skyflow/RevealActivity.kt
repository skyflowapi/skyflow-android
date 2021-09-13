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

        //tview.text = card_number+"\n"+expiry_date+"\n"+name
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(10, 10, 10, 10)

        val padding = Skyflow.Padding(20, 20, 20, 20)
        val bstyle = Skyflow.Style(Color.parseColor("#403E6B"), 10f, padding, 6, R.font.roboto_light, Gravity.START, Color.parseColor("#403E6B"))
        val styles = Skyflow.Styles(bstyle)
        val labelStyles = Styles(bstyle)

        val cardNumberInput = Skyflow.RevealElementInput(
            card_number.toString(),
            redaction = Skyflow.RedactionType.PLAIN_TEXT,styles,labelStyles,
            label =  "card number"
        )

        val expiryDateInput = Skyflow.RevealElementInput(
            expiry_date.toString(),
            redaction = Skyflow.RedactionType.PLAIN_TEXT,styles,labelStyles,
            label =  "expire date","<Redacted expire date token>"
        )

        val fullNameInput = Skyflow.RevealElementInput(
            name.toString(),
            redaction = Skyflow.RedactionType.PLAIN_TEXT,styles,labelStyles,
            label =  "Name","Redacted fullname token>"
        )

        val tokenProvider = MainActivity.DemoTokenProvider()
        val skyflowConfiguration = Skyflow.Configuration(
            BuildConfig.VAULT_ID,
            BuildConfig.VAULT_URL,
            tokenProvider
        )

        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val revealContainer = skyflowClient.container(Skyflow.ContainerType.REVEAL)

        val cardnumber = revealContainer.create(this, cardNumberInput, Skyflow.RevealElementOptions())
        val expiry = revealContainer.create(this, expiryDateInput, Skyflow.RevealElementOptions())
        val fullname = revealContainer.create(this, fullNameInput, Skyflow.RevealElementOptions())

        cardnumber.layoutParams = lp
        expiry.layoutParams = lp
        fullname.layoutParams = lp

        linear_parent.addView(cardnumber)
        linear_parent.addView(expiry)
        linear_parent.addView(fullname)

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
