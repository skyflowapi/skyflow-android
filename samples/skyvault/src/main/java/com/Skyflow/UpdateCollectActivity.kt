package com.Skyflow

import Skyflow.*
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.Skyflow.databinding.ActivityCollectBinding
import org.json.JSONArray
import org.json.JSONObject


class UpdateCollectActivity : AppCompatActivity() {

    private val TAG = UpdateCollectActivity::class.qualifiedName
    private lateinit var binding: ActivityCollectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Step 1: Initialize Skyflow Client
        val tokenProvider = CollectActivity.DemoTokenProvider()
        val skyflowConfiguration = Configuration(
            vaultID = "<VAULT_ID>",  
            vaultURL = "<VAULT_URL>",  
            tokenProvider = tokenProvider,
            Options(LogLevel.DEBUG, Env.PROD)
        )
        val skyflowClient = init(skyflowConfiguration)

        // Step 2: Create a Collect Container
        val collectContainer = skyflowClient.container(ContainerType.COLLECT)

        // Step 3: Define Styles for Elements
        val padding = Padding(8, 8, 8, 8)
        val baseStyle = Style(
            borderColor = Color.parseColor("#403E6B"),
            cornerRadius = 10f,
            padding = padding,
            font = R.font.roboto_light,
            textAlignment = Gravity.START,
            textColor = Color.parseColor("#403E6B")
        )
        val completeStyle = Style(borderColor = Color.GREEN)
        val focusStyle = Style(borderColor = Color.BLUE)
        val invalidStyle = Style(borderColor = Color.RED)
        
        val inputStyles = Styles(
            base = baseStyle,
            complete = completeStyle,
            focus = focusStyle,
            invalid = invalidStyle
        )
        val labelStyles = Styles(base = baseStyle)
        val errorStyles = Styles(
            base = Style(
                padding = padding,
                font = R.font.roboto_light,
                textAlignment = Gravity.START,
                textColor = Color.RED
            )
        )

        // Step 4: Create Collect Elements with skyflowID for UPDATE
        
        // IMPORTANT: Replace this with an actual skyflow_id from your vault
        val existingSkyflowId = "<SKYFLOW_ID>"  // Replace with actual skyflow_id
        
        // Example 1: Update Card Number (with skyflowID = UPDATE operation)
        val cardNumberInput = CollectElementInput(
            table = "cards",
            column = "card_number",
            type = SkyflowElementType.CARD_NUMBER,
            inputStyles = inputStyles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Card Number",
            placeholder = "XXXX XXXX XXXX XXXX",
            skyflowID = existingSkyflowId  // Providing skyflowID makes this an UPDATE operation
        )
        val cardNumberElement = collectContainer.create(
            context = this,
            input = cardNumberInput,
            options = CollectElementOptions(required = true, enableCardIcon = true)
        )

        // Example 2: Update Cardholder Name (same skyflowID = will be merged in single update call)
        val nameInput = CollectElementInput(
            table = "cards",
            column = "cardholder_name",
            type = SkyflowElementType.CARDHOLDER_NAME,
            inputStyles = inputStyles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Cardholder Name",
            placeholder = "John Doe",
            skyflowID = existingSkyflowId  // Same skyflowID - will be merged with card_number update
        )
        val nameElement = collectContainer.create(
            context = this,
            input = nameInput,
            options = CollectElementOptions(required = true)
        )

        // Example 3: Update Expiry Date (same skyflowID = will be merged in single update call)
        val expiryInput = CollectElementInput(
            table = "cards",
            column = "expiry_date",
            type = SkyflowElementType.EXPIRATION_DATE,
            inputStyles = inputStyles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Expiry Date",
            placeholder = "MM/YY",
            skyflowID = existingSkyflowId  // Same skyflowID - will be merged with above updates
        )
        val expiryElement = collectContainer.create(
            context = this,
            input = expiryInput,
            options = CollectElementOptions(required = true, format = "mm/yy")
        )

        // Step 5: Mount Elements to Screen
        val parent = findViewById<LinearLayout>(R.id.parent)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(20, 20, 20, 0)

        cardNumberElement.layoutParams = layoutParams
        nameElement.layoutParams = layoutParams
        expiryElement.layoutParams = layoutParams

        parent.addView(cardNumberElement)
        parent.addView(nameElement)
        parent.addView(expiryElement)

        // Step 6: Collect (Update) Data on Submit
        binding.submit.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()
            dialog.setMessage("Updating records...")
            dialog.show()

            // Optional: Add additional non-PCI fields to update
            val additionalFields = JSONObject().apply {
                val recordsArray = JSONArray()
                
                // Update additional non-PCI field in the same record
                val additionalRecord = JSONObject().apply {
                    put("table", "cards")
                    val fields = JSONObject().apply {
                        put("customer_id", "CUST_12345")  // Non-PCI field to update
                        put("skyflowID", existingSkyflowId)  // Must match element skyflowID
                    }
                    put("fields", fields)
                }
                recordsArray.put(additionalRecord)
                put("records", recordsArray)
            }

            // Optional: Upsert configuration
            val upsertArray = JSONArray().apply {
                val upsertColumn = JSONObject().apply {
                    put("table", "cards")
                    put("column", "card_number")  // Unique column for upsert
                }
                put(upsertColumn)
            }

            val collectOptions = CollectOptions(
                token = true,  // Return tokens for updated fields
                additionalFields = additionalFields,
                upsert = upsertArray
            )

            collectContainer.collect(object : Callback {
                override fun onSuccess(responseBody: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "update success: $responseBody")
                }

                override fun onFailure(exception: Any) {
                    Log.d(TAG, "update failure: ${(exception as Exception).message}")
                    dialog.dismiss()
                }
            }, collectOptions)
        }

        // Step 7: Clear Elements
        binding.clear.setOnClickListener {
            cardNumberElement.unmount()
            nameElement.unmount()
            expiryElement.unmount()
        }
    }
}
