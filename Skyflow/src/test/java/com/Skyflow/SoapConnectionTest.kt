package com.Skyflow

import Skyflow.*
import Skyflow.soap.SoapConnectionConfig
import android.app.Activity
import android.view.ViewGroup
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import kotlin.collections.HashMap

@RunWith(RobolectricTestRunner::class)
class SoapConnectionTest {
    lateinit var skyflow : Client
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity
    lateinit var layoutParams: ViewGroup.LayoutParams

    var connectionUrl = "https://skyflow.com"
    var httpHeaders = HashMap<String, String>()
    var requestBody = ""
    var responseBody = ""
    @Before
    fun setup()
    {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098d2c09",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        skyflow = Client(configuration)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()

    }

    @Test
    fun inValidRequest()
    {
        val requestBody = "yyy"
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        skyflow.invokeSoapConnection(soapConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_REQUEST_XML)
                assertEquals(skyflowError.getInternalErrorMessage(),(exception as SkyflowError).getInternalErrorMessage())
            }

        })
    }

    @Test
    fun inValidResponse()
    {
        val requestBody = "<a></a>"
        val responseBody = "yyy"
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        skyflow.invokeSoapConnection(soapConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RESPONSE_XML)
                assertEquals(skyflowError.getInternalErrorMessage(),(exception as SkyflowError).getInternalErrorMessage())
            }

        })
    }

    @Test
    fun invalidConnectionUrl()
    {
        val connectionUrl = "skyflow.com"
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        skyflow.invokeSoapConnection(soapConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_CONNECTION_URL, params = arrayOf(connectionUrl))
                assertEquals(skyflowError.getInternalErrorMessage(),(exception as SkyflowError).getInternalErrorMessage())
            }

        })
    }

    @Test
    fun textFieldNotMountedInRequest()
    {
        val collectContainer = skyflow.container(ContainerType.COLLECT)
        val clientNodeIdInput = Skyflow.CollectElementInput(
            null, null, Skyflow.SkyflowElementType.INPUT_FIELD, label = "Client Node Id", placeholder = "enter Client node id"
        )
        val clientNodeId = collectContainer.create(activity, clientNodeIdInput, CollectElementOptions(enableCardIcon = false))
        val requestBody = "<skyflow>${clientNodeId.getID()}</skyflow>"
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        skyflow.invokeSoapConnection(soapConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, params = arrayOf(clientNodeId.collectInput.label))
                assertEquals(skyflowError.getInternalErrorMessage(),(exception as SkyflowError).getInternalErrorMessage())
            }

        })
    }

    @Test
    fun labelNotMountedInResponse()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val clientNodeIdInput = Skyflow.RevealElementInput(
            null, null, label = "Client Node Id", altText = "enter Client node id"
        )
        val clientNodeId = revealContainer.create(activity, clientNodeIdInput)
        val requestBody = "<skyflow>${clientNodeId.getID()}</skyflow>"
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        skyflow.invokeSoapConnection(soapConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, params = arrayOf(clientNodeId.revealInput.label))
                assertEquals(skyflowError.getInternalErrorMessage(),(exception as SkyflowError).getInternalErrorMessage())
            }

        })
    }
}