package com.Skyflow

import Skyflow.*
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import com.Skyflow.databinding.ActivityRevealBinding

class RevealActivity : AppCompatActivity() {

    private val TAG = RevealActivity::class.qualifiedName
    private lateinit var binding: ActivityRevealBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRevealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val skyflowClient = init(Configuration(
            vaultID = "<VAULT_ID>",
            vaultURL = "<VAULT_URL>",
            tokenProvider = CollectActivity.DemoTokenProvider()
        ))

        val revealContainer = skyflowClient.container(ContainerType.REVEAL)

        val padding = Padding(10, 10, 10, 10)
        val baseStyle = Style(
            Color.parseColor("#403E6B"), 10f, padding, 6,
            R.font.roboto_light, Gravity.START, Color.parseColor("#403E6B")
        )
        val styles = Styles(baseStyle)

        val cardNumberInput = RevealElementInput(
            token = "<TOKEN_1>",
            inputStyles = styles,
            label = "Card Number",
            altText = "•••• •••• •••• ••••"
        )
        val cvvInput = RevealElementInput(
            token = "<TOKEN_2>",
            inputStyles = styles,
            label = "CVV",
            altText = "•••"
        )

        val linearParent = findViewById<LinearLayout>(R.id.linear_parent)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(10, 10, 10, 10)

        val cardNumber = revealContainer.create(this, cardNumberInput)
        val cvv = revealContainer.create(this, cvvInput)
        cardNumber.layoutParams = lp
        cvv.layoutParams = lp
        linearParent.addView(cardNumber)
        linearParent.addView(cvv)

        binding.reveal.setOnClickListener {
            revealContainer.reveal(object : RevealCallback {
                override fun onSuccess(response: RevealResponse) {
                    response.records.forEach { record ->
                        if (record.httpCode == 200) {
                            Log.d(TAG, "reveal success: token=${record.token}")
                        } else {
                            Log.d(TAG, "reveal error [${record.httpCode}]: ${record.error}")
                        }
                    }
                }
                override fun onFailure(error: SkyflowError) {
                    Log.d(TAG, "reveal failure: ${error.message}")
                }
            })
        }

        binding.btnRevealWithOptions.setOnClickListener {
            val options = RevealOptions(
                tokenGroupRedactions = listOf(
                    TokenGroupRedaction(
                        tokenGroupName = "<TOKEN_GROUP_NAME>",
                        redaction = "<REDACTION_TYPE>"
                    )
                )
            )
            revealContainer.reveal(object : RevealCallback {
                override fun onSuccess(response: RevealResponse) {
                    response.records.forEach { record ->
                        if (record.httpCode == 200) {
                            Log.d(TAG, "reveal success: token=${record.token}")
                        } else {
                            Log.d(TAG, "reveal error [${record.httpCode}]: ${record.error}")
                        }
                    }
                }
                override fun onFailure(error: SkyflowError) {
                    Log.d(TAG, "reveal failure: ${error.message}")
                }
            }, options)
        }
    }
}
