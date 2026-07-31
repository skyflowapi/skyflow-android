package Skyflow

import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import android.content.Context
import com.Skyflow.core.container.ContainerProtocol
import Skyflow.reveal.FlowDBRevealRequestBody
import Skyflow.reveal.RevealValueCallback
import Skyflow.utils.Utils
import Skyflow.utils.Utils.Companion.checkIfElementsMounted
import java.lang.Exception
import java.util.*

class RevealContainer : ContainerProtocol {
    private val tag = RevealContainer::class.qualifiedName
}

private val tag = RevealContainer::class.qualifiedName

fun Container<RevealContainer>.create(
    context: Context,
    input: RevealElementInput,
    options: RevealElementOptions = RevealElementOptions()
): Label {
    Logger.info(
        tag,
        Messages.CREATED_REVEAL_ELEMENT.getMessage(input.label),
        configuration.options.logLevel
    )

    val revealElement = Label(context)
    revealElement.setupField(input, options)
    revealElements.add(revealElement)

    val uuid = UUID.randomUUID().toString()
    client.elementMap.put(uuid, revealElement)
    revealElement.uuid = uuid

    return revealElement
}

internal fun Container<RevealContainer>.reveal(
    callback: Callback,
    options: RevealOptions? = RevealOptions()
) {
    try {
        validateElements()
        Logger.info(
            tag,
            Messages.VALIDATE_REVEAL_RECORDS.getMessage(),
            configuration.options.logLevel
        )
        get(callback, options)
    } catch (e: Exception) {
        callback.onFailure(Utils.constructError(e))
    }
}

internal fun Container<RevealContainer>.validateElements() {
    for (element in this.revealElements) {
        val token = element.revealInput.token
        if (!checkIfElementsMounted(element)) {
            throw SkyflowInternalError(
                SkyflowErrorCode.ELEMENT_NOT_MOUNTED_REVEAL, tag, configuration.options.logLevel,
                arrayOf(element.revealInput.label)
            )
        }

        if (element.isTokenNull) {
            throw SkyflowInternalError(
                SkyflowErrorCode.TOKEN_KEY_NOT_FOUND_REVEAL, tag, configuration.options.logLevel,
            )
        } else if (token!!.isEmpty()) {
            throw SkyflowInternalError(
                SkyflowErrorCode.EMPTY_TOKEN_REVEAL, tag, configuration.options.logLevel
            )
        } else if (element.isError) {
            throw SkyflowInternalError(
                SkyflowErrorCode.ERROR_STATE_REVEAL, tag, configuration.options.logLevel,
                arrayOf("${element.error.text}")
            )
        }
    }
}

fun Container<RevealContainer>.reveal(callback: RevealCallback, options: RevealOptions? = RevealOptions()) {
    val adapter = object : Callback {
        override fun onSuccess(responseBody: Any) {
            callback.onSuccess(RevealResponse.fromJson(responseBody.toString()))
        }
        override fun onFailure(exception: Any) {
            callback.onFailure(SkyflowError.fromJson(exception.toString()))
        }
    }
    reveal(adapter, options)
}

internal fun Container<RevealContainer>.get(callback: Callback, options: RevealOptions?) {
    val revealValueCallback = RevealValueCallback(
        callback,
        this.revealElements,
        configuration.options.logLevel
    )
    val requestBody = FlowDBRevealRequestBody.buildRequestBody(
        configuration.vaultID,
        this.revealElements,
        options
    )
    this.client.apiClient.get(requestBody, revealValueCallback)
}