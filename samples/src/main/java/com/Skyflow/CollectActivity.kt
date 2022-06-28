/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
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
import com.Skyflow.collect.elements.validations.LengthMatchRule
import com.Skyflow.collect.elements.validations.ValidationSet
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
        val skyflowConfiguration = Configuration(
            BuildConfig.VAULT_ID,
            BuildConfig.VAULT_URL,
            tokenProvider,
            Options(LogLevel.ERROR, Env.PROD)
        )
        val skyflowClient = init(skyflowConfiguration)
        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val padding = Padding(8, 8, 8, 8)
        val bstyle = Style(
            Color.parseColor("#403E6B"),
            10f,
            padding,
            null,
            R.font.roboto_light,
            Gravity.START,
            Color.parseColor("#403E6B")
        )
        val cstyle = Style(
            Color.GREEN,
            10f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.END,
            Color.GREEN
        )
        val fstyle = Style(
            Color.parseColor("#403E6B"),
            10f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.START,
            Color.GREEN
        )
        val estyle = Style(
            Color.YELLOW,
            10f,
            padding,
            4,
            R.font.roboto_light,
            Gravity.CENTER,
            Color.YELLOW
        )
        val istyle =
            Style(Color.RED, 15f, padding, 6, R.font.roboto_light, Gravity.START, Color.RED)
        val styles = Styles(bstyle, null, estyle, fstyle, istyle)


        val labelStyles = Styles(bstyle, null, estyle, fstyle, istyle)
        val baseErrorStyles =
            Style(null, null, padding, null, R.font.roboto_light, Gravity.START, Color.RED)
        val errorStyles = Styles(baseErrorStyles)
        val validationSet = ValidationSet()
        validationSet.add(LengthMatchRule(2,20,"not valid"))
        val cardNumberInput = CollectElementInput(
            "cards", "card_number", SkyflowElementType.CARD_NUMBER, styles, labelStyles,
            errorStyles, "Card Number", "CardNumber"
        )
        val expiryDateInput = CollectElementInput(
            "cards", "expiry_date", SkyflowElementType.EXPIRATION_DATE,
            styles, labelStyles, errorStyles, label = "expiry date", placeholder = "expiry date"
        )
        val nameInput = CollectElementInput(
            "cards",
            "fullname",
            SkyflowElementType.CARDHOLDER_NAME,
            styles,
            labelStyles,
            errorStyles,
            "Full Name",
            "Full Name",
            validations = validationSet
        )
        val cvvInput =CollectElementInput(
            table = "cards",
            column = "cvv",
            type = SkyflowElementType.CVV,
            inputStyles = styles,
             labelStyles = labelStyles,
            errorTextStyles = errorStyles,
            placeholder = "CVV",
            altText = "CVV"
        )
        val options = CollectElementOptions(true)
        val cardNumber = collectContainer.create(this, cardNumberInput, CollectElementOptions(enableCardIcon = false))
        val expirationDate = collectContainer.create(this, expiryDateInput,CollectElementOptions(format = "yyyy/mm"))
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
            record.put("table", "cards")
            val fieldsInAdditionalObject = JSONObject()
            fieldsInAdditionalObject.put("cvv", 123)
            record.put("fields", fieldsInAdditionalObject)
            recordsArray.put(record)
            additionalFields.put("records", recordsArray)

            val dialog = AlertDialog.Builder(this).create()
            dialog.setMessage("please wait..")
            dialog.show()
            collectContainer.collect(object : Callback {
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
                    Log.d(TAG, "collect failure: ${(exception as Exception).message}")
                }
            }, CollectOptions(true))
        }

        clear.setOnClickListener {
            clearFields(mutableListOf(cardNumber,cvv,name,expirationDate))
        }

    }


private fun pureInsert(){
    val tokenProvider = DemoTokenProvider()
    val skyflowConfiguration = Configuration(
        BuildConfig.VAULT_ID,
        BuildConfig.VAULT_URL,
        tokenProvider,
        Options(LogLevel.ERROR)
    )
    val skyflow = init(skyflowConfiguration)

    try{
        val records = JSONObject()
        val recordsArray = JSONArray()
//        val record = JSONObject()
//        record.put("table", "persons")
//        val fields = JSONObject()
//        fields.put("cvv", "123")
//        fields.put("card_number", "41111111111")
//        record.put("fields", fields)
//        recordsArray.put(record)
        records.put("records", recordsArray)

        skyflow.insert(records, InsertOptions(true), object : Callback {
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


    //reset elements to initial state
    fun clearFields(elements:List<TextField>)
    {
        for(element in elements)
        {
            element.unmount()
        }
    }



class DemoTokenProvider : TokenProvider {
    override fun getBearerToken(callback: Callback) {
        val url = "TOKEN_URL"
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