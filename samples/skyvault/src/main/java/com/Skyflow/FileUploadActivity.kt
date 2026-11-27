package com.Skyflow

import Skyflow.*
import Skyflow.LogLevel
import Skyflow.Options
import Skyflow.utils.EventName
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Skyflow.databinding.ActivityFileUploadBinding
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.IOException

/**
 * Demonstrates the skyvault file-upload element across every option:
 *
 * - [FileElementType] — `FILE_INPUT` (single) and `MULTI_FILE_INPUT` (multiple).
 * - [FileElementAppearance] — `FIELD`, `BUTTON`, `ICON`, `CARD` and `CUSTOM` (the app's own trigger
 *   calls [FileInputElement.openPicker]).
 * - [FileSource] — `FILES`, `PHOTOS`, `CAMERA` (the element shows a source chooser when more than one
 *   is enabled, and hides `CAMERA` on devices without a camera).
 * - Styling — a plain default element and branded `buttonStyles` on the `BUTTON` and `CARD` looks.
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

        // Branded styles reused for the BUTTON and CARD looks (only `base` is applied to the trigger).
        val brandButtonStyles = Styles(
            base = Style(
                borderColor = Color.parseColor("#3B5BDB"),
                cornerRadius = 12f,
                padding = Padding(28, 22, 28, 22),
                borderWidth = 0,
                textColor = Color.WHITE,
                backgroundColor = Color.parseColor("#3B5BDB")
            )
        )
        val cardStyles = Styles(
            base = Style(
                borderColor = Color.parseColor("#3B5BDB"),
                cornerRadius = 16f,
                padding = Padding(16, 28, 16, 28),
                borderWidth = 2,
                textColor = Color.parseColor("#3B5BDB"),
                backgroundColor = Color.parseColor("#EEF2FF")
            )
        )

        // 2. Create one file element per appearance / type / source combination.

        // FIELD (default look), single file, Files only. No buttonStyles → default bordered box.
        val fieldElement = collectContainer.create(
            this,
            FileElementInput(
                table = "files",
                column = "document",
                type = FileElementType.FILE_INPUT,
                label = "FIELD · single · Files",
                appearance = FileElementAppearance.FIELD
            ),
            FileElementOptions(
                required = false,
                allowedFileType = arrayOf(".pdf", ".png", ".jpg"),
                sources = arrayOf(FileSource.FILES)
            )
        )

        // BUTTON look, single file, Files + Photos, branded buttonStyles.
        val buttonElement = collectContainer.create(
            this,
            FileElementInput(
                table = "files",
                column = "photo",
                type = FileElementType.FILE_INPUT,
                label = "BUTTON · single · Files + Photos",
                appearance = FileElementAppearance.BUTTON,
                buttonStyles = brandButtonStyles
            ),
            FileElementOptions(
                required = false,
                sources = arrayOf(FileSource.FILES, FileSource.PHOTOS)
            )
        )

        // ICON look, single file, Photos + Camera. Default icon (pass ICON(R.drawable.my_icon) to override).
        val iconElement = collectContainer.create(
            this,
            FileElementInput(
                table = "files",
                column = "avatar",
                type = FileElementType.FILE_INPUT,
                label = "ICON · single · Photos + Camera",
                appearance = FileElementAppearance.ICON()
            ),
            FileElementOptions(
                required = false,
                sources = arrayOf(FileSource.PHOTOS, FileSource.CAMERA)
            )
        )

        // CARD look, MULTIPLE files, all sources, styled card, max 4 files.
        val cardElement = collectContainer.create(
            this,
            FileElementInput(
                table = "files",
                column = "attachments",
                type = FileElementType.MULTI_FILE_INPUT,
                label = "CARD · multiple · all sources (max 4)",
                appearance = FileElementAppearance.CARD,
                buttonStyles = cardStyles
            ),
            FileElementOptions(
                required = false,
                maxFileCount = 4,
                sources = arrayOf(FileSource.FILES, FileSource.PHOTOS, FileSource.CAMERA)
            )
        )

        // CUSTOM look, MULTIPLE files. The SDK draws no trigger — we add our own button below.
        val customElement = collectContainer.create(
            this,
            FileElementInput(
                table = "files",
                column = "receipts",
                type = FileElementType.MULTI_FILE_INPUT,
                label = "CUSTOM · multiple · app's own trigger",
                appearance = FileElementAppearance.CUSTOM
            ),
            FileElementOptions(
                required = false,
                maxFileCount = 3,
                sources = arrayOf(FileSource.FILES, FileSource.PHOTOS)
            )
        )

        // 3. Mount every element, then the host-drawn trigger for the CUSTOM element.
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(40, 24, 40, 8) }

        for (element in listOf(fieldElement, buttonElement, iconElement, cardElement, customElement)) {
            element.layoutParams = lp
            parent.addView(element)
        }

        // The CUSTOM element's own trigger: any app button/icon that calls openPicker().
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
        //     The state payload is privacy-safe (no bytes, Uri or file name).
        for (element in listOf(fieldElement, buttonElement, iconElement, cardElement, customElement)) {
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
            listOf(fieldElement, buttonElement, iconElement, cardElement, customElement)
                .forEach { it.unmount() }
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
