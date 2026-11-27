package Skyflow

import Skyflow.collect.client.FileSelection
import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.file.FileUploadValidator
import Skyflow.core.file.SelectedFile
import Skyflow.core.getMessage
import Skyflow.utils.Utils
import android.content.Context
import org.json.JSONObject
import java.util.UUID

// File-upload operations on the legacy (v1) COLLECT container. Kept in the skyvault module so the
// file types never reach flowvault. Mirrors the shape of CollectContainer.create/collect.
private val fileTag = CollectContainer::class.qualifiedName

/**
 * Create a file-collection element. Overloads [create] for file inputs; returns a mountable
 * [FileInputElement] that the host adds to its layout like any other element.
 */
fun Container<CollectContainer>.create(
    context: Context,
    input: FileElementInput,
    options: FileElementOptions = FileElementOptions()
): FileInputElement {
    Logger.info(
        fileTag,
        Messages.CREATED_COLLECT_ELEMENT.getMessage(input.label),
        configuration.options.logLevel
    )
    val element = FileInputElement(context, configuration.options.logLevel)
    element.setupFileField(input, options)
    fileElements.add(element)
    val uuid = UUID.randomUUID().toString()
    client.elementMap[uuid] = element
    element.uuid = uuid
    return element
}

/**
 * Upload every file selected across the container's file elements. Single entry point for both
 * [FileElementType.FILE_INPUT] (single) and [FileElementType.MULTI_FILE_INPUT] (multi) elements —
 * each selected file is uploaded directly and the vault assigns/uses the record id. (This unifies
 * skyflow-js's split between `container.uploadFiles()` and `element.uploadMultipleFiles()`.)
 *
 * Re-validates all file elements first and fails fast (no network request) if any is invalid. On
 * full success the callback receives `{ "fileUploadResponse": [...] }`; on total failure,
 * [Callback.onFailure] receives `{ "errorResponse": [...] }`; on partial failure, `onFailure`
 * receives both arrays.
 */
fun Container<CollectContainer>.uploadFiles(callback: Callback) {
    uploadFiles(metaData = null, callback = callback)
}

/**
 * Upload with per-record metadata. For every [FileElementType.MULTI_FILE_INPUT] element, the SDK
 * first inserts one record per selected file (`metaData` as the record fields) into the element's
 * table, then uploads each file attached to its new `skyflow_id` — mirroring skyflow-js's
 * `element.uploadMultipleFiles(metaData)`. [FileElementType.FILE_INPUT] elements ignore `metaData`
 * and upload directly, as in skyflow-js. Pass an empty object or use the no-arg overload to skip the
 * insert step entirely.
 */
fun Container<CollectContainer>.uploadFiles(
    metaData: JSONObject?,
    callback: Callback,
    uploadOptions: FileUploadOptions? = null,
) {
    try {
        Utils.checkVaultDetails(client.configuration)
        val elements = fileElements.filterIsInstance<FileInputElement>()
        if (elements.isEmpty()) {
            throw SkyflowInternalError(
                SkyflowErrorCode.NO_FILE_ELEMENTS, fileTag, configuration.options.logLevel
            )
        }
        // Snapshot each element's selection once, up front, then validate and upload that same
        // snapshot. The upload runs only after an async token fetch, so re-reading the live selection
        // there could otherwise upload files the user picked after (and so past) validation.
        val selections = elements.map { FileSelection(it, it.getSelectedFiles()) }
        for (selection in selections) {
            validateFileElement(selection.element, selection.files)
        }
        (this.client as Client).apiClient.uploadFiles(this, selections, metaData, callback, uploadOptions)
    } catch (e: Exception) {
        callback.onFailure(Utils.constructErrorResponse(e))
    }
}

/** Upload with [FileUploadOptions] (timeout / progress) and no per-record metadata. */
fun Container<CollectContainer>.uploadFiles(callback: Callback, uploadOptions: FileUploadOptions) {
    uploadFiles(metaData = null, callback = callback, uploadOptions = uploadOptions)
}

/**
 * Cancel the container's in-flight `uploadFiles(...)`, if any. Files that had not finished resolve as
 * cancellation errors in the upload callback.
 */
fun Container<CollectContainer>.cancelFileUploads() {
    (this.client as Client).apiClient.cancelFileUploads(this)
}

internal fun Container<CollectContainer>.validateFileElement(
    element: FileInputElement,
    files: List<SelectedFile>,
) {
    val logLevel = configuration.options.logLevel

    if (!element.isAttachedToWindow()) {
        throw SkyflowInternalError(
            SkyflowErrorCode.FILE_UPLOAD_ELEMENT_NOT_MOUNTED, fileTag, logLevel
        )
    }
    if (element.tableName.isEmpty()) {
        throw SkyflowInternalError(
            SkyflowErrorCode.MISSING_TABLE_IN_ELEMENT, fileTag, logLevel,
            arrayOf(element.fileInput.type.name)
        )
    }
    if (element.columnName.isEmpty()) {
        throw SkyflowInternalError(
            SkyflowErrorCode.MISSING_COLUMN, fileTag, logLevel,
            arrayOf(element.fileInput.type.name)
        )
    }

    FileUploadValidator.validateCount(
        count = files.size,
        required = element.isRequired,
        maxCount = element.effectiveMaxFileCount(),
        column = element.columnName,
        tag = fileTag,
        logLevel = logLevel,
    )

    val maxSize = element.effectiveMaxFileSizeBytes()
    for (file in files) {
        FileUploadValidator.validateFile(
            file = file,
            allowedFileType = element.fileOptions.allowedFileType,
            blockEmptyFiles = element.fileOptions.blockEmptyFiles,
            maxFileSizeBytes = maxSize,
            tag = fileTag,
            logLevel = logLevel,
        )
    }
}
