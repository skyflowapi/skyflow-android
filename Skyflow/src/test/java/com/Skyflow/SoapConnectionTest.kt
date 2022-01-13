package com.Skyflow

import Skyflow.*
import Skyflow.soap.SoapApiCallback
import Skyflow.soap.SoapConnectionConfig
import Skyflow.soap.SoapValueCallback
import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
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
            tokenProvider = AccessTokenProvider()
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
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_REQUEST_XML, params = arrayOf("Content is not allowed in prolog."))
                assertEquals(skyflowError.getInternalErrorMessage(),(exception as SkyflowError).getInternalErrorMessage())
            }

        })
    }

    @Test
    fun emptyRequest()
    {
        val requestBody = ""
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        skyflow.invokeSoapConnection(soapConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_REQUEST_XML)
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
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RESPONSE_XML, params = arrayOf("Content is not allowed in prolog."))
                assertEquals(skyflowError.getInternalErrorMessage().trim(),(exception as SkyflowError).getInternalErrorMessage().trim())
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
    fun emptyConnectionUrl()
    {
        val connectionUrl = ""
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        skyflow.invokeSoapConnection(soapConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_CONNECTION_URL)
                assertEquals(skyflowError.getInternalErrorMessage(),(exception as SkyflowError).getInternalErrorMessage())
            }

        })
    }
    // soap callback

    @Test
    fun invalidIdInRequest()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val clientNodeIdInput = Skyflow.RevealElementInput(
            null, null, label = "Client Node Id", altText = "enter Client node id"
        )
        val clientNodeId = revealContainer.create(activity, clientNodeIdInput)
        val requestBody = "<skyflow>1234</skyflow>"
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        val callback = SoapApiCallback(soapConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_ID_IN_REQUEST_XML, params = arrayOf("1234"))
                assertEquals(skyflowError.getInternalErrorMessage(),(exception as SkyflowError).getInternalErrorMessage())
            }

        },LogLevel.ERROR, client = skyflow )
       callback.onSuccess("token")
    }

    @Test
    fun emptyIdInRequest()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val clientNodeIdInput = Skyflow.RevealElementInput(
            null, null, label = "Client Node Id", altText = "enter Client node id"
        )
        val clientNodeId = revealContainer.create(activity, clientNodeIdInput)
        val requestBody = "<skyflow></skyflow>"
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        val callback = SoapApiCallback(soapConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_ID_IN_REQUEST_XML)
                assertEquals(skyflowError.getInternalErrorMessage(),(exception as SkyflowError).getInternalErrorMessage())
            }

        },LogLevel.ERROR, client = skyflow )
        callback.onSuccess("token")
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
        val callback = SoapApiCallback(soapConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, params = arrayOf(clientNodeIdInput.label))
                assertEquals(skyflowError.getInternalErrorMessage(),(exception as SkyflowError).getInternalErrorMessage())
            }

        },LogLevel.ERROR, client = skyflow )
        callback.onSuccess("token")
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
        val callback = SoapApiCallback(soapConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, params = arrayOf(clientNodeIdInput.label))
                assertEquals(skyflowError.getInternalErrorMessage(),(exception as SkyflowError).getInternalErrorMessage())
            }

        },LogLevel.ERROR, client = skyflow )
        callback.onSuccess("token")
    }

    @Test
    fun testCatchBlock()
    {
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        val callback = SoapApiCallback(soapConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, params = arrayOf("Index: 1, Size: 1"))
                assertEquals(skyflowError.getInternalErrorMessage(),(exception as SkyflowError).getInternalErrorMessage())
            }

        },LogLevel.ERROR, client = skyflow )
        callback.onSuccess(true) //execute catch block
    }

    //soap value callback

    @Test
    fun TestNormalResponse()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val clientNodeIdInput = Skyflow.RevealElementInput(
            null, null, label = "Client Node Id", altText = "enter Client node id"
        )
        val clientNodeId = revealContainer.create(activity, clientNodeIdInput)
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        val callback = SoapValueCallback(skyflow,soapConfiguration,DemoCallback(),LogLevel.ERROR)
        callback.onSuccess( """
                        <soapenv:Envelope>
                    <soapenv:Body>
                    <GenerateCVV>
                    <CVV><Skyflow>${clientNodeId.getID()}</Skyflow></CVV>
                    </GenerateCVV>
                    </soapenv:Body>
                    </soapenv:Envelope>
        """.trimIndent())
    }

    @Test
    fun testGetXmlDocumentFunction()
    {
        val actualXml = """
                <soapenv:Envelope>
                    <soapenv:Body>
                    <GenerateCVV>
                    <CVV><Skyflow>{clientNodeId.getID()}</Skyflow></CVV>
                    </GenerateCVV>
                    </soapenv:Body>
                    </soapenv:Envelope>
        """.trimIndent()
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        val callback = SoapValueCallback(skyflow,soapConfiguration,DemoCallback(),LogLevel.ERROR)
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val actualXmlDocument: Document = builder.parse(InputSource(StringReader(actualXml)))
        assertEquals(actualXml,callback.getXmlFromDocument(actualXmlDocument))
    }

    @Test
    fun testConstructLookup()
    {
        val actualXml = """
                <soapenv:Envelope>
                    <soapenv:Body>
                    <GenerateCVV>
                    <CVV>
                        <Skyflow>
                        {clientNodeId.getID()}
                        </Skyflow>
                    </CVV>
                    </GenerateCVV>
                    </soapenv:Body>
                    </soapenv:Envelope>
        """.trimIndent()
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        val callback = SoapValueCallback(skyflow,soapConfiguration,DemoCallback(),LogLevel.ERROR)
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val actualXmlDocument: Document = builder.parse(InputSource(StringReader(actualXml)))
        callback.constructLookup(actualXmlDocument.documentElement,"")
        assertEquals("{soapenv:Envelope.soapenv:Body.GenerateCVV=[{isFound=false, values={CVV={clientNodeId.getID()}}}]}".trim(),callback.lookup.toString().trim())
    }


    @Test
    fun testAddLookupEntry()
    {
        val actualXml = """
                <soapenv:Envelope>
                    <soapenv:Body>
                    <GenerateCVV>
                    <CVV>
                        <Skyflow>
                        {clientNodeId.getID()}
                        </Skyflow>
                    </CVV>
                    </GenerateCVV>
                    </soapenv:Body>
                    </soapenv:Envelope>
        """.trimIndent()
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        val callback = SoapValueCallback(skyflow,soapConfiguration,DemoCallback(),LogLevel.ERROR)
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val actualXmlDocument: Document = builder.parse(InputSource(StringReader(actualXml)))
        callback.addLookupEntry(actualXmlDocument.documentElement,"","")
        assertEquals("{values={soapenv:Body.GenerateCVV.CVV={clientNodeId.getID()}}}".trim(),callback.lookupEntry.toString().trim())
    }

    @Test
    fun testCheckifMapIsSubSet()
    {
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        val callback = SoapValueCallback(skyflow,soapConfiguration,DemoCallback(),LogLevel.ERROR)
        val submap = HashMap<String,String>()
        submap.put("x","123")
        val map = HashMap<String,String>()
        map["y"] = "123"
        assertFalse(callback.checkIfMapIsSubset(submap,map,true))
    }

    @Test
    fun testCheckifMapIsSubSet2()
    {
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        val callback = SoapValueCallback(skyflow,soapConfiguration,DemoCallback(),LogLevel.ERROR)
        val submap = HashMap<String,String>()
        submap.put("x","123")
        val map = HashMap<String,String>()
        map["x"] = "123"
        assertTrue(callback.checkIfMapIsSubset(submap,map,true))
    }

    @Test
    fun testConstructMap()
    {
        val userXml = """
                <soapenv:Envelope>
                    <soapenv:Body>
                    <GenerateCVV>
                    <CVV>
                        <Skyflow>
                        id
                        </Skyflow>
                    </CVV>
                    </GenerateCVV>
                    </soapenv:Body>
                    </soapenv:Envelope>
        """.trimIndent()
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        val callback = SoapValueCallback(skyflow,soapConfiguration,DemoCallback(),LogLevel.ERROR)
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val userDoc: Document = builder.parse(InputSource(StringReader(userXml)))
        callback.constructMap(userDoc.documentElement,"",true)
        assertEquals("{soapenv:Body.GenerateCVV.CVV.Skyflow=id}".trim(),callback.tempMap.toString().trim())
    }

    @Test
    fun testNewHelper()
    {
        val responseBody = """
                <soapenv:Envelope>
                    <soapenv:Body>
                    <GenerateCVV>
                    <CVV>
                        <Skyflow>
                        id
                        </Skyflow>
                    </CVV>
                    </GenerateCVV>
                    </soapenv:Body>
                    </soapenv:Envelope>
        """.trimIndent()
        val actualXml = """
                      <soapenv:Envelope>
                    <soapenv:Body>
                    <GenerateCVV>
                    <CVV>
                        <Skyflow>
                        123
                        </Skyflow>
                    </CVV>
                    </GenerateCVV>
                    </soapenv:Body>
                    </soapenv:Envelope>
        """.trimIndent()
        val soapConfiguration = SoapConnectionConfig(connectionUrl,httpHeaders,requestBody,responseBody)
        val callback = SoapValueCallback(skyflow,soapConfiguration,DemoCallback(),LogLevel.ERROR)
        callback.onSuccess(actualXml)
        assertEquals("{}",callback.actualValues.toString())
    }
}
class DemoCallback : Callback {
    override fun onSuccess(responseBody: Any) {
    }

    override fun onFailure(exception: Any) {
    }
}