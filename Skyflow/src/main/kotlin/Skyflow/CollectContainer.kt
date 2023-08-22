package Skyflow

import Skyflow.collect.client.CollectRequestBody
import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import Skyflow.utils.Utils
import android.content.Context
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONObject
import java.util.*

@Description("Contains all the Collect Elements.")
open class CollectContainer : ContainerProtocol {

}

private val tag = CollectContainer::class.qualifiedName


@Description("Creates a Collect Element.")
fun Container<CollectContainer>.create(
    @Description("Takes an Android Context object.")
    context: Context,
    @Description("Configuration for a Collect Element.")
    input : CollectElementInput,
    @Description("Additional options for a Collect Element.")
    options : CollectElementOptions = CollectElementOptions()
) : TextField
{
    Logger.info(tag, Messages.CREATED_COLLECT_ELEMENT.getMessage(input.label), configuration.options.logLevel)
    val collectElement = TextField(context, configuration.options)
    collectElement.setupField(input,options)
    collectElements.add(collectElement)
    val uuid = UUID.randomUUID().toString()
    client.elementMap.put(uuid,collectElement)
    collectElement.uuid = uuid
    return collectElement
}

@Description("Collects data and inserts it into a vault.")
fun Container<CollectContainer>.collect(
    @Description("Implementation of Skyflow.Callback.")
    callback: Callback,
    @Description("Additional collect options.")
    options: CollectOptions? = CollectOptions()
){
    try {
        Utils.checkVaultDetails(client.configuration)
        Logger.info(tag, Messages.VALIDATE_COLLECT_RECORDS.getMessage(), configuration.options.logLevel)
        validateElements()
        post(callback,options)
    }
    catch (e:Exception)
    {
        callback.onFailure(e)
    }
}
internal fun Container<CollectContainer>.validateElements() {
    var errors = ""
    for (element in this.collectElements) {
        errors = validateElement(element,errors)
    }
    if (errors != "") {
        throw SkyflowError(SkyflowErrorCode.INVALID_INPUT, tag, configuration.options.logLevel, arrayOf(errors))
    }
}

internal fun Container<CollectContainer>.validateElement(element: TextField,err:String) : String
{
    var errorOnElement = err
    if (!element.isAttachedToWindow()) {
        throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
            tag,
            configuration.options.logLevel,
            arrayOf(element.columnName))
    }
    when {
        element.collectInput.table.equals(null) -> {
            throw SkyflowError(SkyflowErrorCode.MISSING_TABLE_IN_ELEMENT,
                tag,
                configuration.options.logLevel,
                arrayOf(element.fieldType.toString()))
        }
        element.collectInput.column.equals(null) -> {
            throw SkyflowError(SkyflowErrorCode.MISSING_COLUMN,
                tag,
                configuration.options.logLevel,
                arrayOf(element.fieldType.toString()))
        }
        element.collectInput.table!!.isEmpty() -> {
            throw SkyflowError(SkyflowErrorCode.ELEMENT_EMPTY_TABLE_NAME,
                tag,
                configuration.options.logLevel,
                arrayOf(element.fieldType.toString()))
        }
        element.collectInput.column!!.isEmpty() -> {
            throw SkyflowError(SkyflowErrorCode.EMPTY_COLUMN_NAME,
                tag,
                configuration.options.logLevel,
                arrayOf(element.fieldType.toString()))
        }
        else -> {
            val state = element.getState()
            val error = state["validationError"]
            if (!(state["isValid"] as Boolean)) {
                element.invalidTextField()
                errorOnElement += "for " + element.columnName + " " + (error as String) + "\n"
            }
        }
    }
    return errorOnElement
}
internal fun Container<CollectContainer>.post(callback:Callback,options: CollectOptions?)
{
    val records = CollectRequestBody.createRequestBody(this.collectElements, options!!.additionalFields, configuration.options.logLevel)
    val insertOptions = InsertOptions(options.token,options.upsert)
    this.client.apiClient.post(JSONObject(records), callback, insertOptions)
}

