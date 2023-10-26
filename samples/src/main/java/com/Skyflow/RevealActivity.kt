package com.Skyflow

import Skyflow.*
import Skyflow.get.GetOptions
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_reveal.*
import org.json.JSONArray
import org.json.JSONObject

class RevealActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reveal)

        val cardNumberToken = intent.getStringExtra("cardNumber")
        val expiryDateToken = intent.getStringExtra("expiryDate")
        val nameToken = intent.getStringExtra("name")
        val cvvToken = intent.getStringExtra("cvv")

        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(10, 10, 10, 10)

        val padding = Padding(10, 10, 10, 10)
        val bStyle = Style(
            Color.parseColor("#403E6B"),
            10f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.START,
            Color.parseColor("#403E6B")
        )
        val iStyle = Style(
            Color.RED,
            15f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.START,
            Color.RED
        )

        val styles = Styles(bStyle, invalid = iStyle)
        val labelStyles = Styles(bStyle)
        val baseErrorStyle = Style(
            Color.RED,
            10f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.START,
            Color.RED
        )
        val errorStyles = Styles(baseErrorStyle)
        val cardNumberInput = RevealElementInput(
            cardNumberToken.toString(),
            inputStyles = styles, labelStyles = labelStyles, errorTextStyles = errorStyles,
            label = "card number", altText = "41111"
        )

        val expiryDateInput = RevealElementInput(
            expiryDateToken.toString(),
            label = "expire date", altText = "mm/yyyy"
        )

        val fullNameInput = RevealElementInput(
            nameToken.toString(),
            inputStyles = styles, labelStyles = labelStyles, errorTextStyles = errorStyles,
            label = "name", altText = "name"
        )

        val cvvElement = RevealElementInput(
            cvvToken.toString(),
            inputStyles = styles, labelStyles = labelStyles, errorTextStyles = errorStyles,
            label = "CVV", altText = "***"
        )

        val linearParent = findViewById<LinearLayout>(R.id.linear_parent)

        val tokenProvider = CollectActivity.DemoTokenProvider()
        val skyflowConfiguration = Configuration(
            "VAULT_ID",
            "VAULT_URL",
            tokenProvider
        )

        val skyflowClient = init(skyflowConfiguration)
        val revealContainer = skyflowClient.container(ContainerType.REVEAL)

        val cardNumber = revealContainer.create(this, cardNumberInput)
        val expiry = revealContainer.create(this, expiryDateInput)
        val fullName = revealContainer.create(this, fullNameInput)
        val cvv = revealContainer.create(this, cvvElement)

        cardNumber.layoutParams = lp
        expiry.layoutParams = lp
        fullName.layoutParams = lp
        cvv.layoutParams = lp

        linearParent.addView(fullName)
        linearParent.addView(cardNumber)
        linearParent.addView(expiry)
        linearParent.addView(cvv)

        reveal.setOnClickListener {
            getByIds()
            detokenize()
            get()
            getTokens()

            val dialog = AlertDialog.Builder(this).create()
            dialog.setMessage("please wait..")
            dialog.show()
            revealContainer.reveal(object : Callback {
                override fun onSuccess(responseBody: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "reveal success: $responseBody")
                }

                override fun onFailure(exception: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "reveal failure: $exception")
                }
            })
        }
    }

    private fun getByIds() {
        val skyflowConfiguration = Configuration(
            "VAULT_ID",
            "VAULT_URL",
            CollectActivity.DemoTokenProvider()
        )
        val skyflowClient = init(skyflowConfiguration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        record.put("redaction", RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("<skyflow_id1>")
        skyflowIds.add("<skyflow_id2>")
        record.put("ids", skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records", recordsArray)
        skyflowClient.getById(records, object : Callback {
            override fun onSuccess(responseBody: Any) {
                Log.d("getById", responseBody.toString())
            }

            override fun onFailure(exception: Any) {
                Log.d("getById", exception.toString())
            }
        })
    }

    //pure reveal
    private fun detokenize() {
        val skyflowConfiguration = Configuration(
            "VAULT_ID",
            "VAULT_URL",
            CollectActivity.DemoTokenProvider()
        )
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "<token1>")
        val recordObj1 = JSONObject()
        recordObj1.put("token", "<token2>")
        revealRecordsArray.put(recordObj)
        revealRecordsArray.put(recordObj1)
        revealRecords.put("records", revealRecordsArray)
        val skyflowClient = init(skyflowConfiguration)
        skyflowClient.detokenize(records = revealRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
                Log.d("detokenize", "onSuccess: $responseBody")
            }

            override fun onFailure(exception: Any) {
                Log.d("detokenize", "onFailure: $exception")
            }
        })
    }

    private fun get() {
        val skyflowConfiguration = Configuration(
            "VAULT_ID",
            "VAULT_URL",
            CollectActivity.DemoTokenProvider()
        )
        val skyflowClient = init(skyflowConfiguration)

        val getRecords = JSONObject()
        val recordsArray = JSONArray()

        val idRecord = JSONObject()
        val ids = JSONArray()
        ids.put("<skyflow_id1>")
        ids.put("<skyflow_id2>")

        idRecord.put("table", "cards")
        idRecord.put("redaction", RedactionType.PLAIN_TEXT)
        idRecord.put("ids", ids)

        val columnRecord = JSONObject()
        val values = JSONArray()
        values.put("<value1>")
        values.put("<value2>")

        columnRecord.put("table", "cards")
        columnRecord.put("redaction", RedactionType.PLAIN_TEXT)
        columnRecord.put("columnName", "<column_name>")
        columnRecord.put("columnValues", values)

        recordsArray.put(idRecord)
        recordsArray.put(columnRecord)

        getRecords.put("records", recordsArray)

        skyflowClient.get(getRecords, GetOptions(), object : Callback {
            override fun onSuccess(responseBody: Any) {
                Log.d("get", "onSuccess: $responseBody")
            }

            override fun onFailure(exception: Any) {
                Log.d("get", "onFailure: $exception")
            }
        })
    }

    private fun getTokens() {
        val skyflowConfiguration = Configuration(
            "VAULT_ID",
            "VAULT_URL",
            CollectActivity.DemoTokenProvider()
        )
        val skyflowClient = init(skyflowConfiguration)

        val getRecords = JSONObject()
        val recordsArray = JSONArray()

        val idRecord1 = JSONObject()
        val ids1 = JSONArray()
        ids1.put("<skyflow_id1>")
        ids1.put("<skyflow_id2>")

        idRecord1.put("table", "cards")
        idRecord1.put("ids", ids1)

        val idRecord2 = JSONObject()
        val ids2 = JSONArray()
        ids2.put("<skyflow_id1>")
        ids2.put("<skyflow_id2>")

        idRecord2.put("table", "cards")
        idRecord2.put("ids", ids2)

        recordsArray.put(idRecord1)
        recordsArray.put(idRecord2)
        getRecords.put("records", recordsArray)

        skyflowClient.get(getRecords, GetOptions(true), object : Callback {
            override fun onSuccess(responseBody: Any) {
                Log.d("getTokens", "onSuccess: $responseBody")
            }

            override fun onFailure(exception: Any) {
                Log.d("getTokens", "onFailure: $exception")
            }
        })
    }

}