package com.skyflow_android.collect.client

import android.content.Context
import com.skyflow_android.collect.elements.CollectElementInput
import com.skyflow_android.collect.elements.CollectElementOptions
import com.skyflow_android.collect.elements.SkyflowTextField
import com.skyflow_android.core.container.Container
import com.skyflow_android.core.container.ContainerProtocol
import com.skyflow_android.core.protocol.SkyflowCallback

class CollectContainer : ContainerProtocol {

}


fun Container<CollectContainer>.create(context: Context, input : CollectElementInput, options : CollectElementOptions = CollectElementOptions()) : SkyflowTextField
{
    val collectElement = SkyflowTextField(context)
    collectElement.setupField(input,options)
    elements.add(collectElement)
    return collectElement
}

fun Container<CollectContainer>.collect(callback: SkyflowCallback, options: InsertOptions? = InsertOptions()){

    var errors = ""
    for (element in this.elements)
    {
        val state = element.getState()
        val error = state["validationErrors"]
        if((state["isRequired"] as Boolean) && (state["isEmpty"] as Boolean))
        {
            errors += element.columnName+" is empty"+"\n"
        }
        if(!(state["isValid"] as Boolean))
        {
            errors += "for " + element.columnName + " " + (error as String) + "\n"
        }
    }
    if(errors != "")
    {
        callback.failure(Exception("error: ", Throwable(errors)))
        return
    }
    val records = CollectRequestBody.createRequestBody(this.elements)
    this.skyflow.insert(records, options,callback)
}
