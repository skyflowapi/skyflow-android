package com.Skyflow

import Skyflow.*
import Skyflow.composable.collect
import Skyflow.composable.create
import Skyflow.composable.getComposableLayout
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

class UpdateComposableActivity : AppCompatActivity() {

    private val TAG = UpdateComposableActivity::class.qualifiedName
    private lateinit var binding: ActivityCollectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Step 1: Initialize Skyflow Client
        val tokenProvider = CollectActivity.DemoTokenProvider()
        val skyflowConfiguration = Configuration(
            vaultID = "<VAULT_ID>",  //  Replace with your Vault ID
            vaultURL = "<VAULT_URL>",  //  Replace with your Vault URL
            tokenProvider = tokenProvider,
            Options(LogLevel.DEBUG, Env.PROD)
        )
        val skyflowClient = init(skyflowConfiguration)

        // Step 2: Create a Composable Container with Layout Configuration
        // Layout: [1, 1, 3] means:
        // - Row 1: 1 element (cardholder name)
        // - Row 2: 1 element (card number)
        // - Row 3: 3 elements (expiry month, expiry year, CVV)
        val composableContainer = skyflowClient.container(
            ContainerType.COMPOSABLE,
            this,
            ContainerOptions(arrayOf(1, 1, 3))
        )

        // Step 3: Define Styles for Elements
        val padding = Padding(8, 8, 8, 8)
        val margin = Margin(0, 0, 0, 0)
        
        val baseStyle = Style(
            borderColor = Color.TRANSPARENT,
            cornerRadius = 4f,
            padding = padding,
            borderWidth = 3,
            font = R.font.roboto_light,
            textAlignment = Gravity.START,
            textColor = Color.parseColor("#403E6B"),
            margin = margin
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
                textColor = Color.RED,
                margin = margin
            )
        )

        // Step 4: Create Composable Elements with skyflowID for UPDATE
        
        // IMPORTANT: Replace this with an actual skyflow_id from your vault
        val existingSkyflowId = "<SKYFLOW_ID>"  // Replace with actual skyflow_id
        
        // Element 1: Update Cardholder Name (Row 1)
        val nameInput = CollectElementInput(
            table = "cards",
            column = "cardholder_name",
            type = SkyflowElementType.CARDHOLDER_NAME,
            inputStyles = inputStyles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Cardholder Name",
            placeholder = "John Doe",
            skyflowID = existingSkyflowId  // Providing skyflowID makes this an UPDATE operation
        )
        val nameElement = composableContainer.create(this, nameInput, CollectElementOptions(required = true))

        // Element 2: Update Card Number (Row 2)
        val cardNumberInput = CollectElementInput(
            table = "cards",
            column = "card_number",
            type = SkyflowElementType.CARD_NUMBER,
            inputStyles = inputStyles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Card Number",
            placeholder = "XXXX XXXX XXXX XXXX",
            skyflowID = existingSkyflowId  // Same skyflowID - will be merged with name update
        )
        val cardNumberElement = composableContainer.create(
            this,
            cardNumberInput,
            CollectElementOptions(required = true, enableCardIcon = true)
        )

        // Element 3: Update Expiry Month (Row 3, Position 1)
        val expiryMonthInput = CollectElementInput(
            table = "cards",
            column = "expiry_month",
            type = SkyflowElementType.EXPIRATION_MONTH,
            inputStyles = inputStyles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Expiry Month",
            placeholder = "MM",
            skyflowID = existingSkyflowId  // Same skyflowID - will be merged with above updates
        )
        val expiryMonthElement = composableContainer.create(this, expiryMonthInput, CollectElementOptions(required = true))

        // Element 4: Update Expiry Year (Row 3, Position 2)
        val expiryYearInput = CollectElementInput(
            table = "cards",
            column = "expiry_year",
            type = SkyflowElementType.EXPIRATION_YEAR,
            inputStyles = inputStyles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Expiry Year",
            placeholder = "YY",
            skyflowID = existingSkyflowId  // Same skyflowID - will be merged with above updates
        )
        val expiryYearElement = composableContainer.create(this, expiryYearInput, CollectElementOptions(required = true, format = "yy"))

        // Element 5: Update CVV (Row 3, Position 3)
        val cvvInput = CollectElementInput(
            table = "cards",
            column = "cvv",
            type = SkyflowElementType.CVV,
            inputStyles = inputStyles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "CVV",
            placeholder = "***",
            skyflowID = existingSkyflowId  // Same skyflowID - will be merged with above updates
        )
        val cvvElement = composableContainer.create(this, cvvInput, CollectElementOptions(required = true))

        // Step 5: Mount Composable Layout to Screen
        val parent = findViewById<LinearLayout>(R.id.parent)
        try {
            val composableLayout = composableContainer.getComposableLayout()
            parent.addView(composableLayout)
        } catch (error: Exception) {
            Log.e(TAG, "Error mounting composable layout: $error")
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Failed to create composable layout: ${error.message}")
                .setPositiveButton("OK", null)
                .show()
        }

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
                        put("country", "USA")  // Another non-PCI field to update
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

            composableContainer.collect(object : Callback {
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

        // Step 7: Clear All Elements
        binding.clear.setOnClickListener {
            val elements = listOf(
                nameElement,
                cardNumberElement,
                expiryMonthElement,
                expiryYearElement,
                cvvElement
            )
            
            for (element in elements) {
                element.unmount()
            }
        }
    }
}
