package Skyflow

import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import android.content.Context
import Skyflow.reveal.FlowDBRevealRequestBody
import Skyflow.reveal.RevealValueCallback
import Skyflow.utils.Utils
import Skyflow.utils.Utils.Companion.checkIfElementsMounted
import java.lang.Exception
import java.util.*

// NOTE: `class RevealContainer : ContainerProtocol` (the container marker) lives in common
// (ContainerClasses.kt) so both products share it. Only the v2 extension functions live here.

private val tag = RevealContainer::class.qualifiedName

// Public signature unchanged; the shared body lives in common (createLabel).
fun Container<RevealContainer>.create(
    context: Context,
    input: RevealElementInput,
    options: RevealElementOptions = RevealElementOptions()
): Label = createLabel(context, input, options)

internal fun Container<RevealContainer>.reveal(
    callback: Callback,
    options: RevealOptions? = RevealOptions()
) {
    try {
        // Fail fast on missing/invalid vault config BEFORE the bearer-token round-trip, matching
        // flowvault collect and legacy reveal (previously reveal failed opaquely at the network layer).
        Utils.checkVaultDetails(configuration)
        validateElements()
        Logger.info(
            tag,
            Messages.VALIDATE_REVEAL_RECORDS.getMessage(),
            configuration.options.logLevel
        )
        get(callback, options)
    } catch (e: Exception) {
        callback.onFailure(Utils.constructErrorResponse(e))
    }
}

// validateElements moved to common (BaseRevealContainer.kt) — shared with v1.

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
    (this.client as Client).apiClient.get(requestBody, revealValueCallback)
}
