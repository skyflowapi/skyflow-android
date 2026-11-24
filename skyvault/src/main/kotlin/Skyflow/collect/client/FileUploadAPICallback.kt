package Skyflow.collect.client

import Skyflow.*
import Skyflow.Callback
import Skyflow.core.APIClient
import Skyflow.core.file.FileUploadRequestBody
import Skyflow.core.file.SelectedFile
import Skyflow.utils.Utils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

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
) : Callback {

    private val okHttpClient = OkHttpClient()
    private val tag = FileUploadAPICallback::class.qualifiedName

    private val successes = JSONArray()
    private val errors = JSONArray()
    private val lock = Any()
    private lateinit var remaining: AtomicInteger
    private lateinit var authHeader: String

    private val useMetaData: Boolean get() = metaData != null && metaData.length() > 0

    override fun onSuccess(responseBody: Any) {
        authHeader = responseBody.toString()

        // Count every file up front so the completion latch is exact across the mixed
        // direct-upload and insert-then-upload paths.
        val totalFiles = elements.sumOf { it.getSelectedFiles().size }
        if (totalFiles == 0) {
            finish() // only optional elements with no selection — nothing to upload
            return
        }
        remaining = AtomicInteger(totalFiles)

        for (element in elements) {
            val files = element.getSelectedFiles()
            if (files.isEmpty()) continue
            if (element.isMultiple && useMetaData) {
                insertThenUpload(element, files)
            } else {
                for (file in files) uploadOne(element, file, skyflowIdOverride = null)
            }
        }
    }

    override fun onFailure(exception: Any) {
        callback.onFailure(exception)
    }

    // --- Multi-file: insert N records, then upload each file with its new skyflow_id ---

    private fun insertThenUpload(element: FileInputElement, files: List<SelectedFile>) {
        val request = try {
            buildInsertRequest(element, files.size)
        } catch (e: Exception) {
            failFiles(files, 400, messageOf(e))
            return
        }
        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                failFiles(files, 500, e.message ?: "network error")
            }

            override fun onResponse(call: Call, response: Response) {
                val ids = try { parseSkyflowIds(response) } catch (e: Exception) { emptyList() }
                if (ids.size < files.size) {
                    failFiles(files, 500, "Insert for file upload did not return enough record ids.")
                    return
                }
                files.forEachIndexed { i, file -> uploadOne(element, file, ids[i]) }
            }
        })
    }

    private fun buildInsertRequest(element: FileInputElement, count: Int): Request {
        val base = apiClient.vaultURL.trimEnd('/')
        val url = "$base/v1/vaults/${apiClient.vaultId}/${element.tableName}"
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

    private fun parseSkyflowIds(response: Response): List<String> {
        response.use {
            if (!response.isSuccessful) return emptyList()
            val bodyStr = response.body?.string() ?: return emptyList()
            val records = JSONObject(bodyStr).optJSONArray("records") ?: return emptyList()
            val ids = mutableListOf<String>()
            for (i in 0 until records.length()) {
                val id = records.getJSONObject(i).optString("skyflow_id", "")
                if (id.isNotEmpty()) ids.add(id)
            }
            return ids
        }
    }

    // --- Direct per-file upload ---

    private fun uploadOne(element: FileInputElement, file: SelectedFile, skyflowIdOverride: String?) {
        val request = try {
            buildUploadRequest(element, file, skyflowIdOverride)
        } catch (e: Exception) {
            recordError(file.name, 400, messageOf(e))
            return
        }
        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                recordError(file.name, 500, e.message ?: "network error")
            }

            override fun onResponse(call: Call, response: Response) {
                handleUploadResponse(response, element, file)
            }
        })
    }

    private fun buildUploadRequest(
        element: FileInputElement,
        file: SelectedFile,
        skyflowIdOverride: String?,
    ): Request {
        val base = apiClient.vaultURL.trimEnd('/')
        val url = "$base/v2/vaults/${apiClient.vaultId}/files/upload"

        // preserveFileName = false → send a generated name so any PII in the file name never reaches
        // the vault (matches skyflow-js generateUploadFileName).
        val sentName =
            if (element.fileOptions.preserveFileName) file.name
            else "${UUID.randomUUID()}" + if (file.extension.isNotEmpty()) ".${file.extension}" else ""

        val fileBody = FileUploadRequestBody(
            element.context.contentResolver, file.uri, file.mimeType, file.sizeBytes
        )

        val bodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("columnName", element.columnName)
            .addFormDataPart("tableName", element.tableName)
            .addFormDataPart("file", sentName, fileBody)

        val skyflowId = skyflowIdOverride ?: element.skyflowID
        if (!skyflowId.isNullOrEmpty()) bodyBuilder.addFormDataPart("skyflowID", skyflowId)
        if (element.fileOptions.returnFileMetadata) bodyBuilder.addFormDataPart("returnFileMetadata", "true")

        return Request.Builder()
            .method("POST", bodyBuilder.build())
            .addHeader("Authorization", authHeader)
            .addHeader("sky-metadata", "${Utils.fetchMetrics()}")
            .url(url)
            .build()
    }

    private fun handleUploadResponse(response: Response, element: FileInputElement, file: SelectedFile) {
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
                    recordSuccess(if (entry.length() > 0) entry else parsed)
                } else {
                    val bodyStr = response.body?.string() ?: ""
                    val message = try {
                        JSONObject(bodyStr).getJSONObject("error").getString("message")
                    } catch (e: Exception) {
                        bodyStr
                    }
                    recordError(file.name, response.code, Utils.appendRequestId(message, requestId))
                }
            } catch (e: Exception) {
                recordError(file.name, 500, e.message ?: "error")
            }
        }
    }

    // --- Aggregation ---

    private fun recordSuccess(entry: JSONObject) {
        synchronized(lock) { successes.put(entry) }
        complete()
    }

    private fun recordError(fileName: String, code: Int, description: String) {
        synchronized(lock) { errors.put(errorJson(fileName, code, description)) }
        complete()
    }

    private fun failFiles(files: List<SelectedFile>, code: Int, description: String) {
        synchronized(lock) { for (f in files) errors.put(errorJson(f.name, code, description)) }
        repeat(files.size) { complete() }
    }

    private fun complete() {
        if (remaining.decrementAndGet() == 0) finish()
    }

    private fun finish() {
        val result = JSONObject()
        synchronized(lock) {
            if (errors.length() == 0) {
                result.put("fileUploadResponse", successes)
                callback.onSuccess(result)
            } else {
                if (successes.length() > 0) result.put("fileUploadResponse", successes)
                result.put("errorResponse", errors)
                callback.onFailure(result)
            }
        }
    }

    private fun errorJson(fileName: String, code: Int, description: String): JSONObject =
        JSONObject().put(
            "error",
            JSONObject().put("code", code).put("description", description).put("fileName", fileName)
        )

    private fun messageOf(e: Exception): String =
        (e as? SkyflowError)?.getErrorMessage() ?: e.message ?: "error"
}
