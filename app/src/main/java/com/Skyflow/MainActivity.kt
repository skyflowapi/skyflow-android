package com.Skyflow

import Skyflow.*
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.error

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tokenProvider = DemoTokenProvider()
        val skyflowConfiguration = Skyflow.Configuration(
            BuildConfig.VAULT_ID,
            BuildConfig.VAULT_URL,
            tokenProvider
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val collectContainer = skyflowClient.container(Skyflow.ContainerType.COLLECT)
        val padding = Skyflow.Padding(20, 20, 20, 20)
        val bstyle = Skyflow.Style(Color.parseColor("#403E6B"), 10f, padding, null, R.font.roboto_light, Gravity.START, Color.parseColor("#403E6B"))
        val cstyle = Skyflow.Style(Color.GREEN, 10f, padding, 6, R.font.roboto_light, Gravity.END, Color.GREEN)
        val fstyle = Skyflow.Style(Color.parseColor("#403E6B"), 10f, padding, 6, R.font.roboto_light, Gravity.END, Color.GREEN)
        val estyle = Skyflow.Style(Color.YELLOW, 10f, padding, 4, R.font.roboto_light, Gravity.CENTER, Color.YELLOW)
        val istyle = Skyflow.Style(Color.RED, 15f, padding, 6, R.font.roboto_light, Gravity.START, Color.RED)
        val styles = Skyflow.Styles(bstyle , null, estyle, fstyle,istyle)


        val labelStyles = Styles(bstyle , null, estyle, fstyle, istyle)
        val base_error_styles = Style(null, null, padding, null, R.font.roboto_light, Gravity.END, Color.RED)
        val error_styles = Styles(base_error_styles)
        val cardNumberInput = Skyflow.CollectElementInput("cards", "card_number", Skyflow.SkyflowElementType.CARD_NUMBER,styles,labelStyles,
            error_styles, "Card Number","Card Number")
        val expiryDateInput = Skyflow.CollectElementInput("cards", "expiry_date", SkyflowElementType.EXPIRATION_DATE,
                                styles, labelStyles,error_styles, label = "expiry date", placeholder = "expiry date")
        val nameInput = Skyflow.CollectElementInput("cards", "fullname", Skyflow.SkyflowElementType.CARDHOLDER_NAME, styles, labelStyles, error_styles,
                            "Full Name", "Full Name")
        val cvvInput = Skyflow.CollectElementInput("cards", "cvv", SkyflowElementType.CVV, styles, labelStyles, error_styles, "CVV", "CVV")
        val options = CollectElementOptions(true)
        val cardNumber = collectContainer.create(this, cardNumberInput)
        val expirationDate = collectContainer.create(this, expiryDateInput)
        //val name = collectContainer.create(this, nameInput,options)
        val cvv = collectContainer.create(this, cvvInput)


        val parent = findViewById<LinearLayout>(R.id.parent)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(20,-20, 20, 0)
        cardNumber.layoutParams = lp
        expirationDate.layoutParams = lp
        //name.layoutParams = lp
        cvv.layoutParams = lp

        //parent.addView(name)
        parent.addView(cardNumber)
        parent.addView(expirationDate)
        parent.addView(cvv)

        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        val fields = JSONObject()
        fields.put("fullname", JSONObject())
        fields.put("test_int",111)
        //fields.put("name.lastname","last")
        fields.put("expiry_date", "11/34")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)



        submit.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()
            dialog.setMessage("please wait..")
            dialog.show()
            collectContainer.collect(object : Skyflow.Callback {
                override fun onSuccess(responseBody: Any) {
                    dialog.dismiss()
                    Log.d(TAG, "collect success: $responseBody")
                    val jsonobj = JSONObject(responseBody.toString()).getJSONArray("records").getJSONObject(0)
                    val fields =  jsonobj.getJSONObject("fields")
                    val intent = Intent(this@MainActivity,RevealActivity::class.java)
                    intent.putExtra("cardNumber",fields["card_number"].toString())
                    intent.putExtra("expiryDate",fields["expiry_date"].toString())
                    intent.putExtra("name",fields["fullname"].toString())
                    intent.putExtra("cvv", fields["cvv"].toString())
                    startActivity(intent)

                }
                override fun onFailure(exception: Exception) {
                    dialog.dismiss()
                   // error.text = exception.message.toString()
                    Log.d(TAG, "collect failure: ${exception.message.toString()}")
                }
            },CollectOptions(true,records))
        }
    }

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
                                JSONObject(response.body()!!.string().toString())
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

