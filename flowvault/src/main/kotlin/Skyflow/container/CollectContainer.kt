package Skyflow

import Skyflow.collect.client.CVVMap
import Skyflow.collect.client.FlowDBCollectRequestBody
import Skyflow.collect.client.FlowDBMixedAPICallback
import org.json.JSONObject
import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import Skyflow.utils.Utils
import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.*

// NOTE: `open class CollectContainer : ContainerProtocol` (the container marker) lives in common
// (ContainerClasses.kt) so both products share it. Only the v2 extension functions live here.

private val tag = CollectContainer::class.qualifiedName

// Public signature unchanged; the shared body lives in common (createElement).
fun Container<CollectContainer>.create(
    context: Context,
    input: CollectElementInput,
    options: CollectElementOptions = CollectElementOptions()
): TextField = createElement(context, input, options)

internal fun Container<CollectContainer>.collect(callback: Callback, options: CollectOptions? = CollectOptions()) {
    try {
        validateVaultConfig()
        Logger.info(tag, Messages.VALIDATE_COLLECT_RECORDS.getMessage(), configuration.options.logLevel)
        validateElements()
        FlowDBCollectRequestBody.validateAdditionalFields(options?.additionalFields, configuration.options.logLevel)
        post(callback, options)
    } catch (e: Exception) {
        callback.onFailure(Utils.constructErrorResponse(e))
    }
}

internal fun Container<CollectContainer>.validateVaultConfig() {
    if (configuration.vaultID.isEmpty()) {
        throw SkyflowInternalError(SkyflowErrorCode.EMPTY_VAULT_ID, tag, configuration.options.logLevel)
    }
    if (configuration.vaultURL.isEmpty()) {
        throw SkyflowInternalError(SkyflowErrorCode.EMPTY_VAULT_URL, tag, configuration.options.logLevel)
    }
    if (!Utils.checkUrl(configuration.vaultURL)) {
        throw SkyflowInternalError(SkyflowErrorCode.INVALID_VAULT_URL, tag, configuration.options.logLevel)
    }
}

// validateElements / validateElement moved to common (BaseCollectContainer.kt) — shared with v1.

internal fun Container<CollectContainer>.post(callback: Callback, options: CollectOptions?) {
    val collectOptions = options ?: CollectOptions()
    val updateElements = collectElements.filter { !it.skyflowId.isNullOrEmpty() }
    val insertElements = collectElements.filter { it.skyflowId.isNullOrEmpty() }

    val additionalUpdates = collectOptions.additionalFields?.records
        ?.filter { !it.skyflowId.isNullOrEmpty() } ?: emptyList()
    val additionalInserts = collectOptions.additionalFields?.records
        ?.filter { it.skyflowId.isNullOrEmpty() } ?: emptyList()

    if (updateElements.isNotEmpty() || additionalUpdates.isNotEmpty()) {
        // ONE combined update body: element-update columns + additionalFields update data are merged
        // by (tableName, skyflowId) into a single record per record id (matching v1). See helper.
        val combinedUpdateBody = FlowDBCollectRequestBody.buildCombinedUpdateBody(
            configuration.vaultID, updateElements, additionalUpdates, configuration.options.logLevel
        )

        val insertBody: JSONObject? = if (insertElements.isNotEmpty() || additionalInserts.isNotEmpty()) {
            val insertOptions = CollectOptions(
                upsert = collectOptions.upsert?.filter { opt -> insertElements.any { it.tableName == opt.tableName } },
                additionalFields = if (additionalInserts.isEmpty()) null else AdditionalFields(additionalInserts)
            )
            FlowDBCollectRequestBody.buildRequestBody(
                configuration.vaultID, insertElements.toMutableList(),
                insertOptions, configuration.options.logLevel
            )
        } else null

        val mixedCallback = FlowDBMixedAPICallback(
            (client as Client).apiClient, combinedUpdateBody, insertBody, callback, collectOptions,
            configuration.options.logLevel, CVVMap.capture(collectElements)
        )
        (client as Client).apiClient.getAccessToken(mixedCallback)
        return
    }

    val insertOptions = CollectOptions(
        upsert = collectOptions.upsert,
        additionalFields = if (additionalInserts.isEmpty()) null else AdditionalFields(additionalInserts)
    )
    val requestBody = FlowDBCollectRequestBody.buildRequestBody(
        configuration.vaultID,
        this.collectElements,
        insertOptions,
        configuration.options.logLevel
    )
    (this.client as Client).apiClient.post(requestBody, callback, collectOptions, cvvMap = CVVMap.capture(this.collectElements))
}

fun Container<CollectContainer>.collect(callback: CollectCallback, options: CollectOptions? = CollectOptions()) {
    val logLevel = configuration.options.logLevel
    // Deliver the app's CollectCallback on the main thread (see CollectCallback doc): apps can touch
    // Views directly, it matches the SDK's own main-thread element updates, and an exception in the
    // app's handler surfaces as a normal main-thread crash instead of killing the OkHttp thread.
    val mainHandler = Handler(Looper.getMainLooper())
    val adapter = object : Callback {
        override fun onSuccess(responseBody: Any) {
            val parsed = try {
                CollectResponse.fromJsonOrThrow(responseBody.toString(), logLevel)
            } catch (e: Exception) {
                // A success-path response the SDK cannot decode must NOT be reported as an empty
                // success — surface it as a failure (and log) so the app knows something went wrong.
                Logger.error("CollectContainer", "Unable to parse collect response: ${e.message}", logLevel)
                mainHandler.post { callback.onFailure(SkyflowError(null, 500, "Unable to parse response from server", null, emptyList())) }
                return
            }
            mainHandler.post { callback.onSuccess(parsed) }
        }
        override fun onFailure(exception: Any) {
            val error = SkyflowError.fromJson(exception.toString())
            mainHandler.post { callback.onFailure(error) }
        }
    }
    collect(adapter, options)
}
