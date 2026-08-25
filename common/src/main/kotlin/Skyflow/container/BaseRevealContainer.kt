package Skyflow

import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import Skyflow.utils.Utils.Companion.checkIfElementsMounted
import android.content.Context
import java.util.*

private val tag = RevealContainer::class.qualifiedName

internal fun Container<RevealContainer>.createLabel(
    context: Context,
    input: BaseRevealElementInput,
    options: RevealElementOptions
): Label {
    Logger.info(tag, Messages.CREATED_REVEAL_ELEMENT.getMessage(input.label), configuration.options.logLevel)
    val revealElement = Label(context)
    revealElement.setupField(input, options)
    revealElements.add(revealElement)
    val uuid = UUID.randomUUID().toString()
    client.elementMap.put(uuid, revealElement)
    revealElement.uuid = uuid
    return revealElement
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
