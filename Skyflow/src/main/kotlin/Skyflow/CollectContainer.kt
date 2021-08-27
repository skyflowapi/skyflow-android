package Skyflow

import Skyflow.collect.client.CollectRequestBody
import android.content.Context
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONObject


class CollectContainer : ContainerProtocol {

}


fun Container<CollectContainer>.create(context: Context, input : CollectElementInput, options : CollectElementOptions = CollectElementOptions()) : TextField
{
    val collectElement = TextField(context)
    collectElement.setupField(input,options)
    elements.add(collectElement)
    return collectElement
}

fun Container<CollectContainer>.collect(callback: Callback, options: InsertOptions? = InsertOptions()){

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
        callback.onFailure(Exception("error: ", Throwable(errors)))
        return
    }
    val records = CollectRequestBody.createRequestBody(this.elements)
    this.client.insert(JSONObject(records), options,callback)
}
