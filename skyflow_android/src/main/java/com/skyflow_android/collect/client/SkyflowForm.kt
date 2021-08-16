package com.skyflowandroid.collect.client

import android.view.View
import androidx.core.widget.doAfterTextChanged
import com.skyflowandroid.collect.elements.SkyflowInputField

class SkyflowForm(
    private val skyflowCollectClient: SkyflowCollectClient,
    private val tableName: String
    ) {
    private val skyflowRecord: SkyflowRecord = SkyflowRecord(tableName)
    private val fields: ArrayList<SkyflowInputField> = ArrayList()

    fun addTextField(field: SkyflowInputField): SkyflowForm {
        if(field.getFieldname().isNotEmpty()){
            skyflowRecord.put(field.getFieldname(),field.text.toString())
            field.doAfterTextChanged {
                skyflowRecord.put(field.getFieldname(), it.toString())
            }
            fields.add(field);
        }
        return this
    }

    fun addTextFields(fields: List<SkyflowInputField>): SkyflowForm {
        fields.map {
            this.addTextField(it);
        }
        return this
    }

    fun removeView(field: View) {
        fields.remove(field)
    }

    fun isValid() : Boolean
    {
        for ( field in fields)
        {
            if(!field.isValid())
                return false
        }
        return true
    }

    fun submit(callback: ApiCallback) {
        if (isValid()) {
            skyflowCollectClient.tokenize(skyflowRecord, callback)
        } else {
            callback.failure(Exception("Invalid input"))
        }
    }

}