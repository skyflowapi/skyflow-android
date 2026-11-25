package Skyflow.collect.client

import Skyflow.*
import Skyflow.Callback
import Skyflow.core.APIClient
import Skyflow.core.file.FileUploadRequestBody
import Skyflow.core.file.FileUploadValidator
import Skyflow.core.file.SelectedFile
import Skyflow.utils.Utils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Uploads every selected file to `POST {vaultURL}/v2/vaults/{vaultID}/files/upload` (one multipart
 * request per file) and aggregates the per-file outcomes. Ported from skyflow-js's upload paths.
 *
 * Two flows, chosen per element:
 *  - **Direct** (all [FileElementType.FILE_INPUT], and [FileElementType.MULTI_FILE_INPUT] when no
 *    `metaData` is given): upload each file straight to the vault, which assigns/uses the record id.
 *  - **Insert-then-upload** ([FileElementType.MULTI_FILE_INPUT] with `metaData`): first insert one
 *    record per file (`metaData` as fields) via `POST {vaultURL}/v1/vaults/{vaultID}/{tableName}`,
 *    then upload file[i] attached to inserted record skyflow_id[i].
 *
 * File bytes are streamed from the `content://` Uri (see [FileUploadRequestBody]) — nothing is
 * buffered or copied to disk.
 *
 * Resolution (matches skyflow-js): all succeed → [Callback.onSuccess] `{ fileUploadResponse }`;
 * total failure → [Callback.onFailure] `{ errorResponse }`; partial failure → `onFailure`
 * `{ fileUploadResponse, errorResponse }`.
 */
internal class FileUploadAPICallback(
    private val apiClient: APIClient,
    private val elements: List<FileInputElement>,
    private val metaData: JSONObject?,
    private val callback: Callback,
    private val logLevel: LogLevel,
    private val uploadOptions: FileUploadOptions? = null,
) : Callback {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().apply {
        val timeout = uploadOptions?.timeoutMs ?: 0L
        if (timeout > 0L) callTimeout(timeout, TimeUnit.MILLISECONDS)
    }.build()
    private val tag = FileUploadAPICallback::class.qualifiedName

    // Per-file result slots, indexed by input order so the response preserves it (matches
    // skyflow-js's Promise.allSettled, which keeps order). slotIsError[i] tags slot i as a failure.
    private val lock = Any()
    private lateinit var slots: Array<JSONObject?>
    private lateinit var slotIsError: BooleanArray
    private lateinit var remaining: AtomicInteger
    private lateinit var authHeader: String

    // Cancellation: every enqueued call is tracked; cancel() stops them all and flips `cancelled`.
    private val callLock = Any()
    private val activeCalls = mutableListOf<Call>()
    @Volatile private var cancelled = false

    // Progress: cumulative bytes written vs the total selected size across all files.
    private val bytesUploaded = AtomicLong(0L)
    private var totalBytes: Long = 0L

    private val useMetaData: Boolean get() = metaData != null && metaData.length() > 0

    // Configuration appends "/v1/vaults/" to vaultURL for the legacy v1 data APIs. The file-upload
    // endpoints build their own "/v2/vaults/..." and "/v1/vaults/..." paths, so they need the bare
    // vault origin — strip that suffix here to avoid a doubled "/v1/vaults/v2/vaults/..." path.
    private val vaultBase: String
        get() = apiClient.vaultURL.trimEnd('/').removeSuffix("/v1/vaults")

    override fun onSuccess(responseBody: Any) {
        authHeader = responseBody.toString()

        // Count every file up front so the completion latch is exact across the mixed
        // direct-upload and insert-then-upload paths, and so each file gets a stable input index.
        val totalFiles = elements.sumOf { it.getSelectedFiles().size }
        totalBytes = elements.sumOf { el -> el.getSelectedFiles().sumOf { maxOf(0L, it.sizeBytes) } }
        slots = arrayOfNulls(totalFiles)
        slotIsError = BooleanArray(totalFiles)
        if (totalFiles == 0) {
            finish() // only optional elements with no selection — nothing to upload
            return
        }
        remaining = AtomicInteger(totalFiles)

        var index = 0
        for (element in elements) {
            val files = element.getSelectedFiles()
            if (files.isEmpty()) continue
            if (element.isMultiple && useMetaData) {
                insertThenUpload(element, files, startIndex = index)
            } else {
                files.forEachIndexed { i, file ->
                    uploadOne(element, file, skyflowIdOverride = null, index = index + i)
                }
            }
            index += files.size
        }
    }

    override fun onFailure(exception: Any) {
        callback.onFailure(exception)
    }

    // --- Multi-file: insert N records, then upload each file with its new skyflow_id ---

    private fun insertThenUpload(element: FileInputElement, files: List<SelectedFile>, startIndex: Int) {
        val request = try {
            buildInsertRequest(element, files.size)
        } catch (e: Exception) {
            failFiles(files, startIndex, 400, messageOf(e))
            return
        }
        val call = okHttpClient.newCall(request)
        registerCall(call)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                failFiles(files, startIndex, 500, e.message ?: "network error")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val requestId = response.headers.get("x-request-id").toString()
                    val bodyStr = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        // Surface the vault's real insert error (matches skyflow-js), instead of the
                        // generic "did not return enough record ids" — e.g. an invalid-column 400.
                        val message = try {
                            val error = JSONObject(bodyStr).getJSONObject("error")
                            error.optString("description", error.optString("message", bodyStr))
                        } catch (e: Exception) {
                            bodyStr.ifEmpty { "Insert for file upload failed." }
                        }
                        failFiles(files, startIndex, response.code, Utils.appendRequestId(message, requestId))
                        return
                    }
                    val ids = try { parseSkyflowIds(bodyStr) } catch (e: Exception) { emptyList() }
                    if (ids.size < files.size) {
                        // Insert succeeded but returned too few record ids (matches skyflow-js's
                        // "No skyflow IDs returned from insert data").
                        failFiles(files, startIndex, 500, "Insert for file upload did not return enough record ids.")
                        return
                    }
                    files.forEachIndexed { i, file -> uploadOne(element, file, ids[i], startIndex + i) }
                }
            }
        })
    }

    private fun buildInsertRequest(element: FileInputElement, count: Int): Request {
        val url = "$vaultBase/v1/vaults/${apiClient.vaultId}/${element.tableName}"
        val records = JSONArray()
        repeat(count) {
            // Deep-copy metaData per record so records never share a mutable fields object.
            records.put(JSONObject().put("fields", JSONObject(metaData!!.toString())))
        }
        val body = JSONObject().put("records", records).put("tokenization", false)
        val requestBody = body.toString().toRequestBody("application/json".toMediaType())
        return Request.Builder()
            .method("POST", requestBody)
            .addHeader("Authorization", authHeader)
            .addHeader("sky-metadata", "${Utils.fetchMetrics()}")
            .url(url)
            .build()
    }

    private fun parseSkyflowIds(bodyStr: String): List<String> {
        val records = JSONObject(bodyStr).optJSONArray("records") ?: return emptyList()
        val ids = mutableListOf<String>()
        for (i in 0 until records.length()) {
            val id = records.getJSONObject(i).optString("skyflow_id", "")
            if (id.isNotEmpty()) ids.add(id)
        }
        return ids
    }

    // --- Direct per-file upload ---

    private fun uploadOne(
        element: FileInputElement,
        file: SelectedFile,
        skyflowIdOverride: String?,
        index: Int,
    ) {
        // stripImageMetadata: rewrite the photo to a temp cache file with EXIF removed, upload from
        // there, and delete it once the request finishes (null = not an image / disabled / failed).
        val tempFile = if (element.fileOptions.stripImageMetadata)
            ImageMetadataStripper.stripToTempFile(element.context, file) else null
        val request = try {
            buildUploadRequest(element, file, skyflowIdOverride, tempFile)
        } catch (e: Exception) {
            tempFile?.delete()
            recordError(index, file.name, 400, messageOf(e))
            return
        }
        val call = okHttpClient.newCall(request)
        registerCall(call)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                tempFile?.delete()
                recordError(index, file.name, 500, e.message ?: "network error")
            }

            override fun onResponse(call: Call, response: Response) {
                tempFile?.delete()
                handleUploadResponse(response, element, file, index)
            }
        })
    }

    private fun buildUploadRequest(
        element: FileInputElement,
        file: SelectedFile,
        skyflowIdOverride: String?,
        strippedFile: File?,
    ): Request {
        val url = "$vaultBase/v2/vaults/${apiClient.vaultId}/files/upload"

        // preserveFileName = false → send a generated name so any PII in the file name never reaches
        // the vault (matches skyflow-js generateUploadFileName).
        val sentName =
            if (element.fileOptions.preserveFileName) FileUploadValidator.sanitizeFileName(file.name)
            else "${UUID.randomUUID()}" + if (file.extension.isNotEmpty()) ".${file.extension}" else ""

        // Stream from the EXIF-stripped temp file when present, otherwise straight from the Uri.
        val baseBody: RequestBody = strippedFile?.asRequestBody(file.mimeType?.toMediaTypeOrNull())
            ?: FileUploadRequestBody(
                element.context.contentResolver, file.uri, file.mimeType, file.sizeBytes
            )
        // Count bytes for progress only when a listener is registered.
        val fileBody: RequestBody =
            if (uploadOptions?.progressListener != null) CountingRequestBody(baseBody) { reportProgress(it) }
            else baseBody

        val bodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("columnName", element.columnName)
            .addFormDataPart("tableName", element.tableName)
            .addFormDataPart("file", sentName, fileBody)

        // Single file may attach to an existing record via the element's skyflowID. Multi-file does
        // not use an element-level skyflowID (matches skyflow-js, which only reads skyflowID for the
        // single FILE_INPUT): each file creates a new record, or uses its inserted record id in the
        // metadata path (skyflowIdOverride).
        val skyflowId = skyflowIdOverride ?: if (element.isMultiple) null else element.skyflowID
        if (!skyflowId.isNullOrEmpty()) bodyBuilder.addFormDataPart("skyflowID", skyflowId)
        if (element.fileOptions.returnFileMetadata) bodyBuilder.addFormDataPart("returnFileMetadata", "true")

        return Request.Builder()
            .method("POST", bodyBuilder.build())
            .addHeader("Authorization", authHeader)
            .addHeader("sky-metadata", "${Utils.fetchMetrics()}")
            .url(url)
            .build()
    }

    private fun handleUploadResponse(
        response: Response,
        element: FileInputElement,
        file: SelectedFile,
        index: Int,
    ) {
        response.use {
            try {
                val requestId = response.headers.get("x-request-id").toString()
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    val parsed = try { JSONObject(bodyStr) } catch (e: Exception) { JSONObject() }
                    // Normalize to snake_case skyflow_id (skyflow-js constructUploadResponse).
                    val entry = JSONObject()
                    val skyflowId = parsed.optString("skyflow_id", parsed.optString("skyflowID", ""))
                    if (skyflowId.isNotEmpty()) entry.put("skyflow_id", skyflowId)
                    if (element.fileOptions.returnFileMetadata && parsed.has("fileMetadata")) {
                        entry.put("fileMetadata", parsed.get("fileMetadata"))
                    }
                    recordSuccess(index, if (entry.length() > 0) entry else parsed)
                } else {
                    val bodyStr = response.body?.string() ?: ""
                    val message = try {
                        JSONObject(bodyStr).getJSONObject("error").getString("message")
                    } catch (e: Exception) {
                        bodyStr
                    }
                    recordError(index, file.name, response.code, Utils.appendRequestId(message, requestId))
                }
            } catch (e: Exception) {
                recordError(index, file.name, 500, e.message ?: "error")
            }
        }
    }

    // --- Aggregation (per-file slots, assembled in input order at finish) ---

    private fun recordSuccess(index: Int, entry: JSONObject) {
        synchronized(lock) { slots[index] = entry; slotIsError[index] = false }
        complete()
    }

    private fun recordError(index: Int, fileName: String, code: Int, description: String) {
        synchronized(lock) { slots[index] = errorJson(fileName, code, description); slotIsError[index] = true }
        complete()
    }

    private fun failFiles(files: List<SelectedFile>, startIndex: Int, code: Int, description: String) {
        synchronized(lock) {
            files.forEachIndexed { i, f ->
                slots[startIndex + i] = errorJson(f.name, code, description)
                slotIsError[startIndex + i] = true
            }
        }
        repeat(files.size) { complete() }
    }

    private fun complete() {
        if (remaining.decrementAndGet() == 0) finish()
    }

    private fun finish() {
        val successes = JSONArray()
        val errors = JSONArray()
        synchronized(lock) {
            // Walk slots in input order so both lists keep the order files were given (matches
            // skyflow-js's Promise.allSettled).
            for (i in slots.indices) {
                val entry = slots[i] ?: continue
                if (slotIsError[i]) errors.put(entry) else successes.put(entry)
            }
        }
        val result = JSONObject()
        if (errors.length() == 0) {
            result.put("fileUploadResponse", successes)
            callback.onSuccess(result)
        } else {
            if (successes.length() > 0) result.put("fileUploadResponse", successes)
            result.put("errorResponse", errors)
            callback.onFailure(result)
        }
    }

    // --- Cancellation & progress ---

    /** Track an enqueued call so [cancel] can stop it; cancel it immediately if already cancelled. */
    private fun registerCall(call: Call) {
        synchronized(callLock) {
            if (cancelled) call.cancel() else activeCalls.add(call)
        }
    }

    /** Cancel every in-flight call. Pending files surface as cancellation errors via their onFailure. */
    fun cancel() {
        synchronized(callLock) {
            cancelled = true
            activeCalls.forEach { it.cancel() }
        }
    }

    private fun reportProgress(byteCount: Long) {
        val listener = uploadOptions?.progressListener ?: return
        listener.onProgress(bytesUploaded.addAndGet(byteCount), totalBytes)
    }

    private fun errorJson(fileName: String, code: Int, description: String): JSONObject =
        JSONObject().put(
            "error",
            JSONObject().put("code", code).put("description", description).put("fileName", fileName)
        )

    private fun messageOf(e: Exception): String =
        (e as? SkyflowError)?.getErrorMessage() ?: e.message ?: "error"
}
