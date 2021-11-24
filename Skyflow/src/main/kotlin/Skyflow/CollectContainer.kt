package Skyflow

import Skyflow.collect.client.CollectRequestBody
import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import Skyflow.utils.Utils
import android.content.Context
import com.Skyflow.collect.elements.validations.SkyflowValidationError
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONObject


open class CollectContainer : ContainerProtocol {

}

private val tag = CollectContainer::class.qualifiedName


fun Container<CollectContainer>.create(context: Context, input : CollectElementInput, options : CollectElementOptions = CollectElementOptions()) : TextField
{
    Logger.info(tag, Messages.CREATED_COLLECT_ELEMENT.getMessage(input.label), configuration.options.logLevel)
    val collectElement = TextField(context, configuration.options)
    collectElement.setupField(input,options)
    elements.add(collectElement)
    return collectElement
}

fun Container<CollectContainer>.collect(callback: Callback, options: CollectOptions? = CollectOptions()){
    
    if(apiClient.vaultURL.isEmpty() || apiClient.vaultURL.equals("/v1/vaults/"))
    {
        val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL, tag, configuration.options.logLevel)
        callback.onFailure(error)
    }
    else if(apiClient.vaultId.isEmpty())
    {
        val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID, tag, configuration.options.logLevel)
        callback.onFailure(error)
    }
    else {

        val isUrlValid = Utils.checkUrl(apiClient.vaultURL)
        if (isUrlValid) {
            var errors = ""
            Logger.info(tag, Messages.VALIDATE_COLLECT_RECORDS.getMessage(), configuration.options.logLevel)
            for (element in this.elements) {
                if (element.isAttachedToWindow()) {
                    when {
                        element.collectInput.table.equals(null) -> {
                            callback.onFailure(SkyflowError(SkyflowErrorCode.MISSING_TABLE, tag, configuration.options.logLevel))
                            return
                        }
                        element.collectInput.column.equals(null) -> {
                            callback.onFailure(SkyflowError(SkyflowErrorCode.MISSING_COLUMN, tag, configuration.options.logLevel))
                            return
                        }
                        element.collectInput.table!!.isEmpty() -> {
                            callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_TABLE_NAME, tag, configuration.options.logLevel))
                            return
                        }
                        element.collectInput.column!!.isEmpty() -> {
                            callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_COLUMN_NAME, tag, configuration.options.logLevel))
                            return
                        }
                        else -> {
                            val state = element.getState()
                            val error = state["validationError"]
                            if ((state["isRequired"] as Boolean) && (state["isEmpty"] as Boolean)) {
                                errors += element.columnName + " is empty" + "\n"
                            }
                            if (!(state["isValid"] as Boolean)) {
                                errors += "for " + element.columnName + " " + (error as String) + "\n"
                            }
                        }
                    }
                } else {
                    //callback.onFailure(Exception("Element with label ${element.collectInput.label} is not attached to window"))
                    val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, configuration.options.logLevel, arrayOf(element.columnName))
                    callback.onFailure(error)
                    return
                }
            }
            if (errors != "") {
                val error = SkyflowError(SkyflowErrorCode.INVALID_INPUT, tag, configuration.options.logLevel, arrayOf(errors))
                callback.onFailure(error)
                return
            }
            val records = CollectRequestBody.createRequestBody(this.elements,
                options!!.additionalFields,
                callback, configuration.options.logLevel)
            if (!records.isEmpty() || !records.equals("")) {
                val insertOptions = InsertOptions(options.token)
                this.apiClient.post(JSONObject(records), callback, insertOptions)
            }
        } else {
            val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL, tag, configuration.options.logLevel, arrayOf(apiClient.vaultURL))
            callback.onFailure(error)
        }
    }
}