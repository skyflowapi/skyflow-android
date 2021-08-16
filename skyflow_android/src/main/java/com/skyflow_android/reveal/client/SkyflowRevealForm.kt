package com.skyflowandroid.reveal.client

import com.skyflowandroid.reveal.elements.SkyflowOutputField
import org.json.JSONObject

class SkyflowRevealForm(
    private val skyflowRevealClient: SkyflowRevealClient
) {
    private val skyflowTokens: SkyflowTokens = SkyflowTokens()
    private val fields: HashMap<String, SkyflowOutputField> = HashMap()

    fun addField(field: SkyflowOutputField, value:String): SkyflowRevealForm {
        if(field.getFieldname()?.isNotEmpty()){
            skyflowTokens.put(value,field.getFieldname())
            fields[value] = field;

        }
        return this
    }

    fun reveal(redactionType:String) {
            skyflowRevealClient.reveal(skyflowTokens, redactionType, object : ApiCallback {
                override fun success(responseBody: String) {
                    val res = JSONObject(responseBody).getJSONArray("records");
                    for (i in 0 until res.length()){
                        val record = res.getJSONObject(i)
                        val recordFields = record.getJSONObject("fields")
                        val token = record.getString("token_id");
                        val field = fields[token]
                        field?.setText(recordFields.getString(skyflowTokens[token]))
                    }
                }
                override fun failure(exception: Exception?) {
                    println(exception)
                }

            })
    }

}