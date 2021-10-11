package Skyflow

import Skyflow.collect.client.CollectRequestBody
import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import Skyflow.utils.Utils
import android.content.Context
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONObject


class CollectContainer : ContainerProtocol {

}

private val tag = CollectContainer::class.qualifiedName


fun Container<CollectContainer>.create(context: Context, input : CollectElementInput, options : CollectElementOptions = CollectElementOptions()) : TextField
{
    Logger.info(tag, Messages.CREATED_COLLECT_ELEMENT.getMessage(input.label), configuration.options.logLevel)
    val collectElement = TextField(context)
    collectElement.setupField(input,options)
    elements.add(collectElement)
    return collectElement
}

fun Container<CollectContainer>.collect(callback: Callback, options: CollectOptions? = CollectOptions()){
    val isUrlValid = Utils.checkUrl(apiClient.vaultURL, configuration.options.logLevel, tag)
    if(isUrlValid) {
        var errors = ""
        Logger.info(tag, Messages.VALIDATE_COLLECT_RECORDS.getMessage(), configuration.options.logLevel)
        for (element in this.elements) {
            if(element.isAttachedToWindow()) {
                if (element.collectInput.table.equals(null)) {
                    callback.onFailure(Exception("invalid table name"))
                    return
                } else if (element.collectInput.column.equals(null)) {
                    callback.onFailure(Exception("invalid column name"))
                    return
                }
                val state = element.getState()
                val error = state["validationErrors"]
                if ((state["isRequired"] as Boolean) && (state["isEmpty"] as Boolean)) {
                    errors += element.columnName + " is empty" + "\n"
                }
                if (!(state["isValid"] as Boolean)) {
                    errors += "for " + element.columnName + " " + (error as String) + "\n"
                }
            }
            else {
                callback.onFailure(Exception("Element with label ${element.collectInput.label} is not attached to window"))
                return
            }
        }
        if (errors != "") {
            callback.onFailure(Exception(errors))
            return
        }
        val records = CollectRequestBody.createRequestBody(this.elements,
            options!!.additionalFields,
            callback)
        if (!records.isEmpty() || !records.equals(""))
        {
            val insertOptions = InsertOptions(options.token)
            this.apiClient.post(JSONObject(records), callback, insertOptions)
        }
    }
    else
    {
        callback.onFailure(Exception("Url is not valid/not secure"))
    }
}