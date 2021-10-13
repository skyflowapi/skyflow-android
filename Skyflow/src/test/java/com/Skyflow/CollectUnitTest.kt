package com.Skyflow

import Skyflow.*
import Skyflow.core.elements.state.StateforText
import android.app.Activity
import android.content.res.Resources
import android.graphics.Color
import android.os.Handler
import android.os.Looper.getMainLooper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import com.skyflow_android.R
import junit.framework.Assert.assertEquals
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import java.io.IOException
import android.os.Looper
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.skyflow_android.BuildConfig
import org.robolectric.annotation.LooperMode


@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class CollectTest {
        lateinit var skyflow : Client
        lateinit var baseStyle: Style
        lateinit var completeStyle: Style
        lateinit var focusStyle: Style
        lateinit var invalidStyle: Style
        lateinit var emptyStyle: Style
        lateinit var styles : Styles
        private lateinit var activityController: ActivityController<Activity>
        private lateinit var activity: Activity

        @Before
        fun setup()
        {
            val configuration = Configuration(
                "b359c43f1b844ff4bea0f098",
                "https://sb1.area51.vault.skyflowapis.tech",
                DemoTokenProvider1()
            )
            skyflow = Client(configuration)
            val padding = Skyflow.Padding(8, 8, 8, 8)
            baseStyle = Skyflow.Style(
                Color.parseColor("#403E6B"),
                10f,
                padding,
                null,
                R.font.roboto_light,
                Gravity.START,
                Color.parseColor("#403E6B")
            )
            completeStyle = Skyflow.Style(
                Color.GREEN,
                10f,
                padding,
                6,
                R.font.roboto_light,
                Gravity.END,
                Color.GREEN
            )
            focusStyle = Skyflow.Style(
                Color.parseColor("#403E6B"),
                10f,
                padding,
                6,
                R.font.roboto_light,
                Gravity.END,
                Color.GREEN
            )
            emptyStyle = Skyflow.Style(
                Color.YELLOW,
                10f,
                padding,
                4,
                R.font.roboto_light,
                Gravity.CENTER,
                Color.YELLOW
            )
            invalidStyle = Skyflow.Style(Color.RED, 15f, padding, 6, R.font.roboto_light, Gravity.START, Color.RED)
            styles = Skyflow.Styles(baseStyle, completeStyle, emptyStyle, focusStyle, invalidStyle)
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()
        }

        @Test
        fun testInvalidInput() {
            val container = skyflow.container(ContainerType.COLLECT)
            val options = CollectElementOptions(true)
            val collectInput1 = CollectElementInput(activity.resources.getString(R.string.bt_cvc),"card_number",
                SkyflowElementType.CARD_NUMBER,label = "card number"
            )
            val collectInput2 = CollectElementInput("cards","cvv",SkyflowElementType.CVV, label = "cvv")
            var card_number : TextField? = null
            var cvv : TextField ?= null

            Handler(getMainLooper()).post { card_number = container.create(activity,collectInput1, options) }
            Handler(getMainLooper()).post { cvv = container.create(activity,collectInput2, options) }
               val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            shadowOf(getMainLooper()).idle()

            card_number!!.inputField.setText("4111 1111 1111 1111")
            cvv!!.inputField.setText("2")
            activity.addContentView(card_number,layoutParams)
            activity.addContentView(cvv,layoutParams)
            card_number!!.state = StateforText(card_number!!)
            cvv!!.state = StateforText(cvv!!)

            container.collect(object : Callback
            {
                override fun onSuccess(responseBody: Any) {

                }

                override fun onFailure(exception: Any) {
                    Log.d("card",(card_number as TextField).getValue())
                    Log.d("cvv",(cvv as TextField).getValue())
                    val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_INPUT,params = arrayOf("for cvv [INVALID_LENGTH_MATCH]"))
                    assertEquals((exception as SkyflowError).message.trim(),skyflowError.getErrorMessage().trim())
                }
            })
        }
}


class DemoTokenProvider1: TokenProvider {
    override fun getBearerToken(callback: Skyflow.Callback) {
        val url = "https://go-server.skyflow.dev/sa-token/b359c43f1b844ff4bea0f098d2c0"
        val request = okhttp3.Request.Builder().url(url).build()
        val okHttpClient = OkHttpClient()
        try {
            val thread = Thread {
                run {
                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful)
                            throw IOException("Unexpected code $response")
                        //  val accessTokenObject = JSONObject(response.body()!!.string().toString())
                        //  val accessToken = accessTokenObject["accessToken"]
                        val accessToken = ""
                        callback.onSuccess("$accessToken")
                    }
                }
            }
        }
        catch (e:Exception)
        {

        }
    }
}