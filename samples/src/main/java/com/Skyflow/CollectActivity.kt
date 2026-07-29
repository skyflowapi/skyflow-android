package com.Skyflow

import Skyflow.*
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import com.Skyflow.databinding.ActivityCollectBinding
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.IOException

class CollectActivity : AppCompatActivity() {

    private val TAG = CollectActivity::class.qualifiedName
    private lateinit var binding: ActivityCollectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val skyflowClient = init(Configuration(
            vaultID = "<VAULT_ID>",
            vaultURL = "<VAULT_URL>",
            tokenProvider = DemoTokenProvider()
        ))

        val container = skyflowClient.container(ContainerType.COLLECT)

        val padding = Padding(8, 8, 8, 8)
        val baseStyle = Style(
            Color.parseColor("#403E6B"), 10f, padding, null,
            R.font.roboto_light, Gravity.START, Color.parseColor("#403E6B")
        )
        val incompleteStyle = Style(Color.RED, 15f, padding, 6, R.font.roboto_light, Gravity.START, Color.RED)
        val errorStyle = Style(null, null, padding, null, R.font.roboto_light, Gravity.START, Color.RED)
        val styles = Styles(baseStyle, null, null, null, incompleteStyle)
        val errorStyles = Styles(errorStyle)

        val cardNumberInput = CollectElementInput(
            table = "<TABLE_NAME>",
            column = "<COLUMN_NAME>",
            type = SkyflowElementType.CARD_NUMBER,
            inputStyles = styles,
            errorTextStyles = errorStyles,
            label = "Card Number",
            placeholder = "Card Number"
        )
        val expiryInput = CollectElementInput(
            table = "<TABLE_NAME>",
            column = "<EXPIRY_COLUMN>",
            type = SkyflowElementType.EXPIRATION_DATE,
            inputStyles = styles,
            errorTextStyles = errorStyles,
            label = "Expiry Date",
            placeholder = "MM/YY"
        )

        val cardNumber = container.create(this, cardNumberInput)
        val expiry = container.create(this, expiryInput)

        val parent = findViewById<LinearLayout>(R.id.parent)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(20, -20, 20, 0)
        cardNumber.layoutParams = lp
        expiry.layoutParams = lp
        parent.addView(cardNumber)
        parent.addView(expiry)

        binding.submit.setOnClickListener {
            container.collect(object : CollectCallback {
                override fun onSuccess(response: CollectResponse) {
                    response.records.forEach { record ->
                        if (record.httpCode == 200) {
                            Log.d(TAG, "insert success: ${record.tokens}")
                        } else {
                            Log.d(TAG, "insert error [${record.httpCode}]: ${record.error}")
                        }
                    }
                }
                override fun onFailure(error: SkyflowError) {
                    Log.d(TAG, "collect failure: ${error.message}")
                }
            })
        }

        binding.btnUpsert.setOnClickListener {
            val options = CollectOptions(
                upsert = listOf(
                    UpsertOptions(
                        tableName = "<TABLE_NAME>",
                        updateType = UpdateType.UPDATE,
                        uniqueColumns = listOf("<UNIQUE_COLUMN>")
                    )
                )
            )
            container.collect(object : CollectCallback {
                override fun onSuccess(response: CollectResponse) {
                    response.records.forEach { record ->
                        if (record.httpCode == 200) {
                            Log.d(TAG, "upsert success: ${record.tokens}")
                        } else {
                            Log.d(TAG, "upsert error [${record.httpCode}]: ${record.error}")
                        }
                    }
                }
                override fun onFailure(error: SkyflowError) {
                    Log.d(TAG, "upsert failure: ${error.message}")
                }
            }, options)
        }

        binding.btnAdditionalFields.setOnClickListener {
            val options = CollectOptions(
                additionalFields = AdditionalFields(
                    records = listOf(
                        AdditionalFieldsRecord(
                            tableName = "<TABLE_NAME>",
                            data = mapOf("<COLUMN>" to "<VALUE>")
                        )
                    )
                )
            )
            container.collect(object : CollectCallback {
                override fun onSuccess(response: CollectResponse) {
                    response.records.forEach { record ->
                        if (record.httpCode == 200) {
                            Log.d(TAG, "insert success: ${record.tokens}")
                        } else {
                            Log.d(TAG, "insert error [${record.httpCode}]: ${record.error}")
                        }
                    }
                }
                override fun onFailure(error: SkyflowError) {
                    Log.d(TAG, "collect failure: ${error.message}")
                }
            }, options)
        }

        binding.clear.setOnClickListener {
            cardNumber.unmount()
            expiry.unmount()
        }
    }

    class DemoTokenProvider : TokenProvider {
        override fun getBearerToken(callback: Callback) {
            val url = "<TOKEN_URL>"
            val request = okhttp3.Request.Builder().url(url).build()
            val okHttpClient = OkHttpClient()
            try {
                val thread = Thread {
                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful)
                            throw IOException("Unexpected code $response")
                        val body = JSONObject(response.body!!.string())
                        callback.onSuccess("${body["accessToken"]}")
                    }
                }
                thread.start()
            } catch (exception: Exception) {
                callback.onFailure(exception)
            }
        }
    }
}
