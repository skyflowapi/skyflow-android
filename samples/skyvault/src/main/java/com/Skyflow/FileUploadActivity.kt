package com.Skyflow

import Skyflow.*
import Skyflow.LogLevel
import Skyflow.Options
import Skyflow.utils.EventName
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.Skyflow.databinding.ActivityFileUploadBinding
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.IOException

/**
 * Demonstrates the skyvault file-upload element. File elements use the same [CollectElementInput] and
 * [CollectElementOptions] as text fields (matching skyflow-js / iOS); `type` is [FileElementType] and
 * the element is created with [createFileElement]. There is one native default layout — a trigger
 * button that opens the picker, the selected files listed below with a per-file remove (✕), and an
 * error line; `inputStyles` / `labelStyles` / `errorTextStyles` style it. Apps that want their own
 * trigger call [FileInputElement.openPicker] from any control.
 *
 * - [FileElementType] — `FILE_INPUT` (single) and `MULTI_FILE_INPUT` (multiple).
 * - [FileSource] — `FILES`, `PHOTOS`, `CAMERA` (the element shows a source chooser when more than one
 *   is enabled, and hides `CAMERA` on devices without a camera).
 *
 * All elements live in one COLLECT container, so a single `container.uploadFiles(...)` uploads every
 * selected file. Replace `VAULT_ID` / `VAULT_URL` / `TOKEN_URL` to exercise a real upload; the picker,
 * remove (✕) and validation UI work without a vault.
 */
class FileUploadActivity : AppCompatActivity() {

    private val TAG = FileUploadActivity::class.qualifiedName
    private lateinit var binding: ActivityFileUploadBinding

    private lateinit var collectContainer: Container<CollectContainer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Initialize the client and a COLLECT container.
        val skyflowConfiguration = Configuration(
            "VAULT_ID",
            "VAULT_URL",
            DemoTokenProvider(),
            Options(LogLevel.ERROR, Env.PROD)
        )
        val skyflowClient = init(skyflowConfiguration)
        collectContainer = skyflowClient.container(ContainerType.COLLECT)

        val parent = findViewById<LinearLayout>(R.id.parent)

        // Branded inputStyles reused for the second element (styles the trigger like any collect field).
        val brandInputStyles = Styles(
            base = Style(
                borderColor = Color.parseColor("#3B5BDB"),
                cornerRadius = 12f,
                padding = Padding(28, 22, 28, 22),
                borderWidth = 2,
                textColor = Color.WHITE,
                backgroundColor = Color.parseColor("#3B5BDB")
            )
        )

        // 2. Create file elements. Same input/options classes as text fields; type picks single/multi.

        // Default single-file element, Files only.
        val fieldElement = collectContainer.createFileElement(
            this,
            CollectElementInput(
                table = "files",
                column = "document",
                label = "Default · single · Files",
                placeholder = "Choose a document",
                type = FileElementType.FILE_INPUT
            ),
            CollectElementOptions(
                required = false,
                allowedFileType = arrayOf(".pdf", ".png", ".jpg"),
                sources = arrayOf(FileSource.FILES)
            )
        )

        // Branded single-file element, Files + Photos (styled trigger via inputStyles).
        val brandedElement = collectContainer.createFileElement(
            this,
            CollectElementInput(
                table = "files",
                column = "photo",
                inputStyles = brandInputStyles,
                label = "Branded · single · Files + Photos",
                type = FileElementType.FILE_INPUT
            ),
            CollectElementOptions(
                required = false,
                sources = arrayOf(FileSource.FILES, FileSource.PHOTOS)
            )
        )

        // Multi-file element, all sources, max 4.
        val multiElement = collectContainer.createFileElement(
            this,
            CollectElementInput(
                table = "files",
                column = "attachments",
                label = "Multi · up to 4 · all sources",
                type = FileElementType.MULTI_FILE_INPUT
            ),
            CollectElementOptions(
                required = false,
                maxFileCount = 4,
                sources = arrayOf(FileSource.FILES, FileSource.PHOTOS, FileSource.CAMERA)
            )
        )

        // Multi-file element driven by the app's own trigger (openPicker()).
        val customElement = collectContainer.createFileElement(
            this,
            CollectElementInput(
                table = "files",
                column = "receipts",
                label = "Custom trigger · multiple (app's own button)",
                type = FileElementType.MULTI_FILE_INPUT
            ),
            CollectElementOptions(
                required = false,
                maxFileCount = 3,
                sources = arrayOf(FileSource.FILES, FileSource.PHOTOS)
            )
        )

        // 3. Mount every element, then the host-drawn trigger for the custom element.
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(40, 24, 40, 8) }

        for (element in listOf(fieldElement, brandedElement, multiElement, customElement)) {
            element.layoutParams = lp
            parent.addView(element)
        }

        // The custom element's own trigger: any app button/icon that calls openPicker().
        val customTrigger = Button(this).apply {
            text = "＋ Attach receipts (openPicker)"
            isAllCaps = false
            setBackgroundColor(Color.parseColor("#6B3E40"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(40, 0, 40, 24) }
            setOnClickListener { customElement.openPicker() }
        }
        parent.addView(customTrigger)

        // 3b. Event listeners — file elements support the same on(...) API as other collect elements.
        //     CHANGE fires on pick/replace/remove, READY on mount, FOCUS/BLUR from the built-in trigger.
        //     The state carries counts and flags only (isEmpty / isValid / isFocused / fileCount), never files.
        for (element in listOf(fieldElement, brandedElement, multiElement, customElement)) {
            element.on(EventName.CHANGE) { state -> Log.d(TAG, "CHANGE: $state") }
            element.on(EventName.READY) { state -> Log.d(TAG, "READY: $state") }
            element.on(EventName.FOCUS) { state -> Log.d(TAG, "FOCUS: $state") }
            element.on(EventName.BLUR) { state -> Log.d(TAG, "BLUR: $state") }
        }

        // 4. Upload. One call uploads every selected file across all elements.
        binding.upload.setOnClickListener { upload(metaData = null) }

        // 4b. Upload with per-record metadata: MULTI_FILE_INPUT elements insert one record per file
        //     (metaData as the fields) and attach each file to its new skyflow_id.
        binding.uploadWithMetadata.setOnClickListener {
            val metaData = JSONObject().put("uploaded_by", "android-sample")
            upload(metaData)
        }

        binding.clear.setOnClickListener {
            listOf(fieldElement, brandedElement, multiElement, customElement).forEach { it.unmount() }
            binding.result.text = ""
        }
    }

    private fun upload(metaData: JSONObject?) {
        val dialog = AlertDialog.Builder(this).create()
        dialog.setMessage("Uploading…")
        dialog.show()
        val callback = object : Callback {
            override fun onSuccess(responseBody: Any) {
                runOnUiThread {
                    dialog.dismiss()
                    Log.d(TAG, "upload success: $responseBody")
                    binding.result.text = "Success:\n$responseBody"
                }
            }

            override fun onFailure(exception: Any) {
                runOnUiThread {
                    dialog.dismiss()
                    val message = (exception as? Exception)?.message ?: exception.toString()
                    Log.d(TAG, "upload failure: $message")
                    binding.result.text = "Failure:\n$message"
                }
            }
        }
        if (metaData == null) collectContainer.uploadFiles(callback)
        else collectContainer.uploadFiles(metaData, callback)
    }

    class DemoTokenProvider : TokenProvider {
        override fun getBearerToken(callback: Callback) {
            val url = "TOKEN_URL"
            val request = okhttp3.Request.Builder().url(url).build()
            val okHttpClient = OkHttpClient()
            // The exception handler must live INSIDE the worker thread — the network call runs there,
            // not on this method's stack. Catching around Thread.start() would miss every failure the
            // request throws (bad status, invalid JSON, I/O error) and let it crash the app instead of
            // reporting it through the callback.
            Thread {
                try {
                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        val accessTokenObject = JSONObject(response.body!!.string().toString())
                        callback.onSuccess("${accessTokenObject["accessToken"]}")
                    }
                } catch (exception: Exception) {
                    callback.onFailure(exception)
                }
            }.start()
        }
    }
}
