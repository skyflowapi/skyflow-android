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
import java.lang.Exception
import java.util.*

@Description("This is the description for Reveal Container class")
class RevealContainer : ContainerProtocol {
    private val tag = RevealContainer::class.qualifiedName
}

private val tag = RevealContainer::class.qualifiedName

@Description("This is description for create function")
fun Container<RevealContainer>.create(
    @Description("Description for context param")
    context: Context,
    @Description("Description for input param")
    input: RevealElementInput,
    @Description("Description for options param")
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

@Description("This is description for reveal function")
fun Container<RevealContainer>.reveal(
    @Description("Description for callback param")
    callback: Callback,
    @Description("Description for options param")
    options: RevealOptions? = RevealOptions()
) {
    try {
        Utils.checkVaultDetails(client.configuration)
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
            throw SkyflowError(
                SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                tag,
                configuration.options.logLevel,
                arrayOf(element.revealInput.label)
            )
        }

        if (element.isTokenNull) {
            throw SkyflowError(SkyflowErrorCode.MISSING_TOKEN, tag, configuration.options.logLevel)
        } else if (token!!.isEmpty()) {
            throw SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID, tag, configuration.options.logLevel)
        } else if (element.isError) {
            throw SkyflowError(
                SkyflowErrorCode.INVALID_INPUT,
                tag,
                configuration.options.logLevel,
                arrayOf("${element.error.text}")
            )
        }
    }
}

internal fun Container<RevealContainer>.get(callback: Callback, options: RevealOptions?) {
    val revealValueCallback = RevealValueCallback(
        callback,
        this.revealElements,
        configuration.options.logLevel
    )
    val records = RevealRequestBody.createRequestBody(this.revealElements)
    this.client.apiClient.get(records, revealValueCallback)
}