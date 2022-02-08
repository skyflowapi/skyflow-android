package Skyflow

import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import android.content.Context
import com.Skyflow.core.container.ContainerProtocol
import Skyflow.reveal.RevealRequestBody
import Skyflow.reveal.RevealValueCallback
import Skyflow.utils.Utils
import Skyflow.utils.Utils.Companion.checkIfElementsMounted
import org.json.JSONObject
import java.lang.Exception
import java.util.*

class RevealContainer: ContainerProtocol
{
    private val tag = RevealContainer::class.qualifiedName
}

private val tag = RevealContainer::class.qualifiedName

fun Container<RevealContainer>.create(context: Context, input : RevealElementInput, options : RevealElementOptions = RevealElementOptions()) : Label
{
    Logger.info(tag, Messages.CREATED_REVEAL_ELEMENT.getMessage(input.label), configuration.options.logLevel)
    val revealElement = Label(context)
    revealElement.setupField(input,options)
    revealElements.add(revealElement)
    val uuid = UUID.randomUUID().toString()
    client.elementMap.put(uuid,revealElement)
    revealElement.uuid = uuid
    return revealElement
}

fun Container<RevealContainer>.reveal(callback: Callback, options: RevealOptions? = RevealOptions())
{
    try {
        Utils.checkVaultDetails(client.configuration)
        validateElements()
        Logger.info(tag, Messages.VALIDATE_REVEAL_RECORDS.getMessage(), configuration.options.logLevel)
        get(callback,options)
    }
    catch (e: Exception)
    {
        callback.onFailure(Utils.constructError(e))
    }
}

internal fun Container<RevealContainer>.validateElements()
{
    for (element in this.revealElements) {
        val token = element.revealInput.token
        if(!checkIfElementsMounted(element))
        {
            throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, configuration.options.logLevel, arrayOf(element.revealInput.label))
        }
        if (element.isTokenNull) {
            throw SkyflowError(SkyflowErrorCode.MISSING_TOKEN, tag, configuration.options.logLevel)
        }  else if (token!!.isEmpty()) {
            throw SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID, tag, configuration.options.logLevel)
        }
        else if(element.isError)
        {
            throw SkyflowError(SkyflowErrorCode.INVALID_INPUT, tag, configuration.options.logLevel, arrayOf("${element.error.text}"))
        }

    }
}
internal fun Container<RevealContainer>.get(callback: Callback, options: RevealOptions?)
{
    val revealValueCallback = RevealValueCallback(callback, this.revealElements,configuration.options.logLevel)
    val records = JSONObject(RevealRequestBody.createRequestBody(this.revealElements))
    this.client.apiClient.get(records, revealValueCallback)
}