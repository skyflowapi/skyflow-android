package com.Skyflow

import Skyflow.*
import com.Skyflow.BuildConfig
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.Skyflow.databinding.ActivityCollectBinding

class UpdateCollectActivity : AppCompatActivity() {

    private val TAG = UpdateCollectActivity::class.qualifiedName
    private lateinit var binding: ActivityCollectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val skyflowClient = init(Configuration(
            vaultID = BuildConfig.VAULT_ID,
            vaultURL = BuildConfig.VAULT_URL,
            tokenProvider = CollectActivity.DemoTokenProvider()
        ))

        val collectContainer = skyflowClient.container(ContainerType.COLLECT)

        val padding = Padding(8, 8, 8, 8)
        val baseStyle = Style(
            borderColor = Color.parseColor("#403E6B"),
            cornerRadius = 10f,
            padding = padding,
            font = R.font.roboto_light,
            textAlignment = Gravity.START,
            textColor = Color.parseColor("#403E6B")
        )
        val inputStyles = Styles(
            base = baseStyle,
            complete = Style(borderColor = Color.GREEN),
            focus = Style(borderColor = Color.BLUE),
            invalid = Style(borderColor = Color.RED)
        )
        val errorStyles = Styles(
            base = Style(padding = padding, font = R.font.roboto_light, textAlignment = Gravity.START, textColor = Color.RED)
        )

        val skyflowId = BuildConfig.SKYFLOW_ID

        val cardNumberInput = CollectElementInput(
            tableName = BuildConfig.TABLE_NAME,
            column = BuildConfig.COLUMN_NAME,
            type = SkyflowElementType.CARD_NUMBER,
            inputStyles = inputStyles,
            errorTextStyles = errorStyles,
            label = "Card Number",
            placeholder = "Card Number",
            skyflowId = skyflowId
        )
        val nameInput = CollectElementInput(
            tableName = BuildConfig.TABLE_NAME,
            column = BuildConfig.COLUMN_NAME,
            type = SkyflowElementType.CARDHOLDER_NAME,
            inputStyles = inputStyles,
            errorTextStyles = errorStyles,
            label = "Cardholder Name",
            placeholder = "Cardholder Name",
            skyflowId = skyflowId
        )
        val expiryInput = CollectElementInput(
            tableName = BuildConfig.TABLE_NAME,
            column = BuildConfig.COLUMN_NAME,
            type = SkyflowElementType.EXPIRATION_DATE,
            inputStyles = inputStyles,
            errorTextStyles = errorStyles,
            label = "Expiry Date",
            placeholder = "MM/YY",
            skyflowId = skyflowId
        )

        val cardNumber = collectContainer.create(this, cardNumberInput, CollectElementOptions(required = true, enableCardIcon = true))
        val name = collectContainer.create(this, nameInput, CollectElementOptions(required = true))
        val expiry = collectContainer.create(this, expiryInput, CollectElementOptions(required = true, format = "mm/yy"))

        val parent = findViewById<LinearLayout>(R.id.parent)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(20, 20, 20, 0)
        cardNumber.layoutParams = lp
        name.layoutParams = lp
        expiry.layoutParams = lp
        parent.addView(cardNumber)
        parent.addView(name)
        parent.addView(expiry)

        binding.submit.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()
            dialog.setMessage("Updating records...")
            dialog.show()
            collectContainer.collect(object : CollectCallback {
                override fun onSuccess(response: CollectResponse) {
                    dialog.dismiss()
                    response.records.forEach { record ->
                        if (record.httpCode == 200) Log.d(TAG, "update success: ${record.tokens}")
                        else Log.d(TAG, "update error [${record.httpCode}]: ${record.error}")
                    }
                }
                override fun onFailure(error: SkyflowError) {
                    dialog.dismiss()
                    Log.d(TAG, "update failure: ${error.message}")
                }
            })
        }

        binding.btnUpsert.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()
            dialog.setMessage("Updating records...")
            dialog.show()
            val options = CollectOptions(
                additionalFields = AdditionalFields(
                    records = listOf(
                        AdditionalFieldsRecord(
                            tableName = BuildConfig.TABLE_NAME,
                            data = mapOf(BuildConfig.COLUMN to BuildConfig.VALUE),
                            skyflowId = skyflowId
                        )
                    )
                )
            )
            collectContainer.collect(object : CollectCallback {
                override fun onSuccess(response: CollectResponse) {
                    dialog.dismiss()
                    response.records.forEach { record ->
                        if (record.httpCode == 200) Log.d(TAG, "update success: ${record.tokens}")
                        else Log.d(TAG, "update error [${record.httpCode}]: ${record.error}")
                    }
                }
                override fun onFailure(error: SkyflowError) {
                    dialog.dismiss()
                    Log.d(TAG, "update failure: ${error.message}")
                }
            }, options)
        }

        binding.clear.setOnClickListener {
            cardNumber.unmount()
            name.unmount()
            expiry.unmount()
        }
    }
}
