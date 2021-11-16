package com.Skyflow

import Skyflow.*
import Skyflow.LogLevel
import Skyflow.Options
import Skyflow.utils.EventName
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_collect.*
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class CollectActivity : AppCompatActivity() {

    private val TAG = CollectActivity::class.qualifiedName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect)
        val tokenProvider = DemoTokenProvider()
        val skyflowConfiguration = Skyflow.Configuration(
            BuildConfig.VAULT_ID,
            BuildConfig.VAULT_URL,
            tokenProvider,
            Options(Skyflow.LogLevel.ERROR, Env.PROD)
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val padding = Skyflow.Padding(8, 8, 8, 8)
        val bstyle = Skyflow.Style(
            Color.parseColor("#403E6B"),
            10f,
            padding,
            null,
            R.font.roboto_light,
            Gravity.START,
            Color.parseColor("#403E6B")
        )
        val cstyle = Skyflow.Style(
            Color.GREEN,
            10f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.END,
            Color.GREEN
        )
        val fstyle = Skyflow.Style(
            Color.parseColor("#403E6B"),
            10f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.START,
            Color.GREEN
        )
        val estyle = Skyflow.Style(
            Color.YELLOW,
            10f,
            padding,
            4,
            R.font.roboto_light,
            Gravity.CENTER,
            Color.YELLOW
        )
        val istyle =
            Skyflow.Style(Color.RED, 15f, padding, 6, R.font.roboto_light, Gravity.START, Color.RED)
        val styles = Skyflow.Styles(bstyle, null, estyle, fstyle, istyle)


        var labelStyles = Styles(bstyle, null, estyle, fstyle, istyle)
        var base_error_styles =
            Style(null, null, padding, null, R.font.roboto_light, Gravity.START, Color.RED)
        val error_styles = Styles(base_error_styles)
        val cardNumberInput = Skyflow.CollectElementInput(
            "cards", "card_number", Skyflow.SkyflowElementType.CARD_NUMBER, styles, labelStyles,
            error_styles, "Card Number", "CardNumber"
        )
        val expiryDateInput = Skyflow.CollectElementInput(
            "cards", "expiry_date", SkyflowElementType.EXPIRATION_DATE,
            styles, labelStyles, error_styles, label = "expiry date", placeholder = "expiry date"
        )
        val nameInput = Skyflow.CollectElementInput(
            "cards",
            "fullname",
            Skyflow.SkyflowElementType.CARDHOLDER_NAME,
            styles,
            labelStyles,
            error_styles,
            "Full Name",
            "Full Name"
        )
        val cvvInput = Skyflow.CollectElementInput(
            "cards",
            "cvv",
            SkyflowElementType.CVV,
            styles,
            labelStyles,
            error_styles,
            "CVV",
            "CVV"
        )
        val options = CollectElementOptions(true)
        val cardNumber = collectContainer.create(this, cardNumberInput)
        val expirationDate = collectContainer.create(this, expiryDateInput)
        val name = collectContainer.create(this, nameInput, options)
        val cvv = collectContainer.create(this, cvvInput)
        
        cardNumber.on(EventName.FOCUS) { state ->
                    Log.d(TAG, "focus: sate $state")
        }

        cardNumber.on(EventName.BLUR) { state ->
            Log.d(TAG, "blur: sate $state")
        }

        cardNumber.on(EventName.CHANGE) { state ->
            Log.d(TAG, "change: sate $state")
        }

        cardNumber.on(EventName.READY) { state ->
            Log.d(TAG, "ready: sate $state")
        }

        val expiryDateInput1 = Skyflow.RevealElementInput(
            "reveal token",
            redaction = Skyflow.RedactionType.PLAIN_TEXT, styles, labelStyles, error_styles,
            label = "expire date", "mm/yyyy"
        )
        val revealContainer = skyflowClient.container(Skyflow.ContainerType.REVEAL)

        val parent = findViewById<LinearLayout>(R.id.parent)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(20, -20, 20, 0)
        cardNumber.layoutParams = lp
        expirationDate.layoutParams = lp
        name.layoutParams = lp
        cvv.layoutParams = lp
        parent.addView(name)
        parent.addView(cardNumber)
        parent.addView(expirationDate)
        parent.addView(cvv)

        submit.setOnClickListener {
            pureInsert()
            val additionalFields = JSONObject()
            val recordsArray = JSONArray()
            val record = JSONObject()
            record.put("table", "persons")
            val fieldsInAdditionalObject = JSONObject()
            record.put("fields", fieldsInAdditionalObject)
            //additionalFields.put("records", recordsArray)

            val dialog = AlertDialog.Builder(this).create()
            dialog.setMessage("please wait..")
            dialog.show()
            collectContainer.collect(object : Skyflow.Callback {
                override fun onSuccess(responseBody: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "collect success: $responseBody")
                    val jsonobj =
                        JSONObject(responseBody.toString()).getJSONArray("records").getJSONObject(0)
                    val fields = jsonobj.getJSONObject("fields")
                    val intent = Intent(this@CollectActivity, RevealActivity::class.java)
                    intent.putExtra("cardNumber", fields["card_number"].toString())
                    intent.putExtra("expiryDate", fields["expiry_date"].toString())
                    intent.putExtra("name", fields["fullname"].toString())
                    intent.putExtra("cvv", fields["cvv"].toString())
                    startActivity(intent)

                }

                override fun onFailure(exception: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "collect failure: ${exception.toString()}")
                }
            }, CollectOptions(true, additionalFields))
        }

    }


private fun pureInsert(){
    val tokenProvider = DemoTokenProvider()
    val skyflowConfiguration = Skyflow.Configuration(
        BuildConfig.VAULT_ID,
        BuildConfig.VAULT_URL,
        tokenProvider,
        Options(LogLevel.ERROR)
    )
    val skyflow = Skyflow.init(skyflowConfiguration)

    try{
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "persons")
        val fields = JSONObject()
        fields.put("cvv", "123")
        fields.put("card_number", "41111111111")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)

        skyflow.insert(records, Skyflow.InsertOptions(true), object : Callback {
            override fun onSuccess(responseBody: Any) {
                Log.d("insert", "success: $responseBody")
            }

            override fun onFailure(exception: Any) {
                Log.d(ContentValues.TAG, "failure: $exception")
            }

        })
    }catch (e: Exception){
        Log.d("TAG", "testingFunction: $e")
    }}



class DemoTokenProvider : Skyflow.TokenProvider {
    override fun getBearerToken(callback: Skyflow.Callback) {
        val url = BuildConfig.TOKEN_URL
        val request = okhttp3.Request.Builder().url(url).build()
        val okHttpClient = OkHttpClient()
        try {
            val thread = Thread {
                run {
                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful)
                            throw IOException("Unexpected code $response")
                        val accessTokenObject =
                            JSONObject(response.body!!.string().toString())
                        val accessToken = accessTokenObject["accessToken"]
//                        val accessToken = ""
                        callback.onSuccess("$accessToken")
                    }
                }
            }
            thread.start()
        } catch (exception: Exception) {
            callback.onFailure(exception)
        }
    }
}
}