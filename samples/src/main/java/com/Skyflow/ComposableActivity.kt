package com.Skyflow

import Skyflow.*
import Skyflow.collect.elements.validations.ElementValueMatchRule
import Skyflow.composable.*
import Skyflow.utils.EventName
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.Skyflow.collect.elements.validations.ValidationSet
import kotlinx.android.synthetic.main.activity_collect.*
import org.json.JSONArray
import org.json.JSONObject

class ComposableActivity : AppCompatActivity() {
    private val TAG = ComposableActivity::class.qualifiedName

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
        val composableContainer = skyflowClient.container(
            ContainerType.COMPOSABLE,
            this,
            ContainerOptions(arrayOf(1, 1, 3))
        )
        val parent = findViewById<LinearLayout>(R.id.parent)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, 20, 0, 0)

        val padding = Padding(8, 8, 8, 8)
        val bstyle = Style(
            Color.TRANSPARENT,
            4f,
            padding,
            3,
            R.font.roboto_light,
            Gravity.START,
            Color.parseColor("#403E6B"),
            margin = Margin(0, 0, 0, 0)
        )
        val bstyle1 = Style(
            Color.TRANSPARENT,
            0f,
            padding,
            0,
            R.font.roboto_light,
            Gravity.START,
            Color.parseColor("#403E6B"),
            530,
            margin = Margin(0, 0, 0, 0)
        )
        val bstyle2 = Style(
            Color.TRANSPARENT,
            0f,
            padding,
            0,
            R.font.roboto_light,
            Gravity.START,
            Color.parseColor("#403E6B"),
            350,
            margin = Margin(0, 0, 0, 0)
        )
        val bstyle3 = Style(
            Color.TRANSPARENT,
            0f,
            padding,
            0,
            R.font.roboto_light,
            Gravity.START,
            Color.parseColor("#403E6B"),
            220,
            margin = Margin(0, 0, 0, 0)
        )
        val cstyle = Style(
            Color.TRANSPARENT,
            0f,
            padding,
            0,
            R.font.roboto_light,
            Gravity.END,
            Color.GREEN,
            margin = Margin(0, 0, 0, 0)
        )
        val fstyle = Style(
            Color.TRANSPARENT,
            0f,
            padding,
            0,
            R.font.roboto_light,
            Gravity.START,
            Color.GREEN,
            margin = Margin(0, 0, 0, 0)
        )
        val estyle = Style(
            Color.TRANSPARENT,
            0f,
            padding,
            0,
            R.font.roboto_light,
            Gravity.START,
            Color.YELLOW,
            margin = Margin(0, 0, 0, 0)
        )
        val istyle = Style(
            Color.TRANSPARENT,
            0f,
            padding,
            0,
            R.font.roboto_light,
            Gravity.START,
            Color.RED,
            margin = Margin(0, 0, 0, 0)
        )
        val styles = Styles(bstyle, cstyle, estyle, fstyle, istyle)
        val cardNumberStyles = Styles(bstyle1, cstyle, estyle, fstyle, istyle)
        val expDateStyles = Styles(bstyle2, cstyle, estyle, fstyle, istyle)
        val cvvStyles = Styles(bstyle3, cstyle, estyle, fstyle, istyle)

        val labelStyles = Styles(bstyle, cstyle, estyle, fstyle, istyle)
        val baseErrorStyles = Style(
            null, null, padding,
            null, R.font.roboto_light, Gravity.START, Color.RED
        )
        val errorStyles = Styles(baseErrorStyles)
        val options = CollectElementOptions(true)

        val nameInput = CollectElementInput(
            table = "cards",
            column = "cardholder_name",
            type = SkyflowElementType.CARDHOLDER_NAME,
            inputStyles = styles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Full Name",
            placeholder = "Full Name",
        )
        val name = composableContainer.create(this, nameInput, options)

        val cardNumberInput = CollectElementInput(
            table = "cards",
            column = "card_number",
            type = SkyflowElementType.CARD_NUMBER,
            inputStyles = cardNumberStyles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Card Number",
            placeholder = "XXXX"
        )
        val cardNumber = composableContainer.create(
            this,
            cardNumberInput,
            CollectElementOptions(true, enableCardIcon = true)
        )

        val expiryMonthInput = CollectElementInput(
            table = "cards",
            column = "exp_month",
            type = SkyflowElementType.EXPIRATION_MONTH,
            inputStyles = expDateStyles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Expiry Month",
            placeholder = "mm"
        )
        val expMonth = composableContainer.create(this, expiryMonthInput, options)

        val cvvInput = CollectElementInput(
            table = "cards",
            column = "cvv",
            type = SkyflowElementType.CVV,
            inputStyles = cvvStyles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "CVV",
            placeholder = "****",
        )
        val cvv = composableContainer.create(this, cvvInput, options)

        val pinInput = CollectElementInput(
            table = "cards",
            column = "pin",
            type = SkyflowElementType.PIN,
            inputStyles = cvvStyles,
            labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            label = "Pin",
            placeholder = "****",
            validations = ValidationSet(
                mutableListOf(ElementValueMatchRule(cvv, "cvv and pin don't match"))
            )
        )
        val pin = composableContainer.create(this, pinInput, options)

        val elements = mutableListOf(name, cardNumber, expMonth, cvv, pin)

        for (element in elements) {
            element.on(EventName.READY) { state -> println("READY from activity $state") }
            element.on(EventName.FOCUS) { state -> println("FOCUS from activity $state") }
            element.on(EventName.CHANGE) { state -> println("CHANGE from activity $state") }
            element.on(EventName.BLUR) { state -> println("BLUR from activity $state") }
        }

        val lp1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val lp2 = LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT)
        val lp3 = LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT)

        lp1.setMargins(6, 20, 0, 0)
        lp2.setMargins(6, 20, 0, 0)
        lp3.setMargins(6, 20, 0, 0)

        composableContainer.on(EventName.SUBMIT) {
            println("submit event is being triggered!!")
        }

        try {
            val composableLayout = composableContainer.getComposableLayout()
            parent.addView(composableLayout)
        } catch (e: Exception) {
            println(e)
        }

        // Non-PCI use case fields
        val additionalFields = JSONObject()
        val recordsArray = JSONArray()

        val record = JSONObject()
        record.put("table", "persons")

        val fieldsInAdditionalObject = JSONObject()
        fieldsInAdditionalObject.put("gender", "MALE")

        record.put("fields", fieldsInAdditionalObject)
        recordsArray.put(record)

        additionalFields.put("records", recordsArray)

        // Upsert options
        val upsertArray = JSONArray()

        val upsertColumn = JSONObject()
        upsertColumn.put("table", "cards")
        upsertColumn.put("column", "cardNumber")

        upsertArray.put(upsertColumn)

        submit.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()
            dialog.setMessage("please wait..")
            dialog.show()
            composableContainer.collect(object : Callback {
                override fun onSuccess(responseBody: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "collect success: $responseBody")
                }

                override fun onFailure(exception: Any) {
                    Log.d(TAG, "collect failure: ${(exception as Exception).message}")
                    dialog.dismiss()
                }
            }, CollectOptions(true, additionalFields, upsertArray))
        }

        clear.setOnClickListener {
            for (element in elements) {
                element.unmount()
            }
        }
    }
}