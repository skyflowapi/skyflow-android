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

// CollectContainer (marker class) lives in core; these are the legacy (v1) container operations.
private val tag = CollectContainer::class.qualifiedName

// Public v1 signature unchanged; the shared body lives in common (createElement).
fun Container<CollectContainer>.create(
    context: Context,
    input: CollectElementInput,
    options: CollectElementOptions = CollectElementOptions()
): TextField = createElement(context, input, options)

fun Container<CollectContainer>.collect(callback: Callback, options: CollectOptions? = CollectOptions()){
    try {
        Utils.checkVaultDetails(client.configuration)
        Logger.info(tag, Messages.VALIDATE_COLLECT_RECORDS.getMessage(), configuration.options.logLevel)
        validateElements()
        post(callback,options)
    }
    catch (e:Exception)
    {
        callback.onFailure(Utils.constructErrorResponse(e))
    }
}
// validateElements / validateElement moved to common (BaseCollectContainer.kt) — shared with v2.

internal fun Container<CollectContainer>.post(callback:Callback,options: CollectOptions?)
{
    // Separate insert and update elements/records
    val (insertElements, insertAdditionalFields, updateRecords) = Skyflow.collect.client.CollectRequestBody.separateInsertAndUpdateRecords(
        this.collectElements,
        options?.additionalFields,
        configuration.options.logLevel
    )
    
    val hasInsertData = insertElements.isNotEmpty() || insertAdditionalFields != null
    val hasUpdateRecords = updateRecords.isNotEmpty()
    
    if (hasInsertData && hasUpdateRecords) {
        // Mixed case: both insert and update
        val insertRecordsJson = if (insertElements.isNotEmpty()) {
            JSONObject(Skyflow.collect.client.CollectRequestBody.createRequestBody(
                insertElements, 
                insertAdditionalFields, 
                configuration.options.logLevel
            ))
        } else {
            insertAdditionalFields
        }
        
        val insertOptions = InsertOptions(options?.token ?: true, options?.upsert)
        (this.client as Client).apiClient.postWithUpdate(insertRecordsJson, updateRecords, callback, insertOptions)
    } else if (hasUpdateRecords) {
        // Only update records
        val insertOptions = InsertOptions(options?.token ?: true, options?.upsert)
        (this.client as Client).apiClient.postWithUpdate(null, updateRecords, callback, insertOptions)
    } else {
        // Only insert records
        val records = Skyflow.collect.client.CollectRequestBody.createRequestBody(
            this.collectElements, 
            insertAdditionalFields, 
            configuration.options.logLevel
        )
        val insertOptions = InsertOptions(options?.token ?: true, options?.upsert)
        (this.client as Client).apiClient.post(JSONObject(records), callback, insertOptions)
    }
}

