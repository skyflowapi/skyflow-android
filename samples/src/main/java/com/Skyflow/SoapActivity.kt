package com.Skyflow

import Skyflow.*
import Skyflow.soap.SoapConnectionConfig
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_soap.*

class SoapActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soap)

        val skyflowConfiguration = Skyflow.Configuration(
            BuildConfig.VAULT_ID,
            BuildConfig.VAULT_URL,
            CollectActivity.DemoTokenProvider(),
            Options(LogLevel.ERROR, Env.PROD)
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val collectInput1 = Skyflow.CollectElementInput(
             null, null, Skyflow.SkyflowElementType.INPUT_FIELD, label = "input-1", placeholder = "enter"
        )
        val collectInput2 = Skyflow.CollectElementInput(
            null, null, SkyflowElementType.INPUT_FIELD,
            label = "input-2", placeholder = "enter"
        )
        val revealInput1 = CollectElementInput(
            null, null,SkyflowElementType.INPUT_FIELD,
            label = "output"
        )

        val revealInput2 = RevealElementInput(
            null, null,
            label = "output"
        )
        val input1 = collectContainer.create(this, collectInput1, CollectElementOptions(enableCardIcon = false))
        val input2 = collectContainer.create(this, collectInput2)
        val output1 = collectContainer.create(this,revealInput1)
        val output2 = revealContainer.create(this,revealInput2)
        val parent = findViewById<LinearLayout>(R.id.parent)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(20, -20, 20, 0)
        input1.layoutParams = lp
        input2.layoutParams = lp
        output1.layoutParams = lp
        output2.layoutParams = lp
        parent.addView(input1)
        parent.addView(input2)
        parent.addView(output1)
        parent.addView(output2)
        submit.setOnClickListener {
            val connectionUrl = "https://url.com"
            val httpHeaders = HashMap<String, String>()
            httpHeaders.put("SOAPAction", "")
            val requestBody = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                    <soapenv:Header>
                       <skyflow>
                           ${input1.getID()}
                        </skyflow>
                        </soapenv:Header>
                    <soapenv:Body>
                       <value>
                       <skyflow>
                          ${input2.getID()}
                          </skyflow>
                       </value>
                    </soapenv:Body>
                </soapenv:Envelope>

            """.trimIndent()
            val responseBody = """
                 <s:Envelope>
                <s:Header>
                     <Value>
                           <skyflow>
                                  ${output2.getID()}
                           </skyflow>
                      </Value>
                </s:Header>
                <s:Body>
                   <skyflow>
                       ${output1.getID()}
                  </skyflow>
                </s:Body>
            </s:Envelope>
            """.trimIndent()
            val soapConnectionConfig =
                SoapConnectionConfig(connectionUrl, httpHeaders, requestBody, responseBody)
            skyflowClient.invokeSoapConnection(soapConnectionConfig, object : Callback {
                override fun onSuccess(responseBody: Any) {
                    Log.d("result:", responseBody.toString())
                }

                override fun onFailure(exception: Any) {
                    Log.d("exception:", exception.toString())
                    var error = exception as SkyflowError
                    var xml = error.getXml()
                    if(xml.isNotEmpty()){
                        Log.d("error from server",xml)
                    }
                }

            })

        }
    }



}