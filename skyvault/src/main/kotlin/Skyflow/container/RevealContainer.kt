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

// RevealContainer (marker class) lives in core; these are the legacy (v1) container operations.
private val tag = RevealContainer::class.qualifiedName

// Public v1 signature unchanged; the shared body lives in common (createLabel).
fun Container<RevealContainer>.create(
    context: Context,
    input: RevealElementInput,
    options: RevealElementOptions = RevealElementOptions()
): Label = createLabel(context, input, options)

fun Container<RevealContainer>.reveal(
    callback: Callback,
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

// validateElements moved to common (BaseRevealContainer.kt) — shared with v2.

internal fun Container<RevealContainer>.get(callback: Callback, options: RevealOptions?) {
    val revealValueCallback = RevealValueCallback(
        callback,
        this.revealElements,
        configuration.options.logLevel
    )
    val records = RevealRequestBody.createRequestBody(this.revealElements)
    (this.client as Client).apiClient.get(records, revealValueCallback)
}