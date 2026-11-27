package Skyflow

import Skyflow.core.Logger
import Skyflow.core.file.SelectedFile
import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File

/**
 * Bridges a [FileInputElement] to the Android system pickers (SAF documents, the Photo Picker, and
 * the camera). It registers an [ActivityResultLauncher] on the host [ComponentActivity]'s
 * `activityResultRegistry` at tap time — so it works after `onCreate` — launches the chosen source,
 * resolves each returned `content://` Uri to a metadata-only [SelectedFile], and hands the list back
 * through [onPicked].
 *
 * Isolation note (see the design doc): the picker runs out-of-process, so the app only ever receives
 * the Uri(s) the user chose. FILES and PHOTOS need **no** runtime permission; CAMERA writes to the
 * SDK's own FileProvider cache and needs none either, unless the host app itself declares `CAMERA`.
 *
 * Limitation: the launcher is registered without a LifecycleOwner, so a configuration change while
 * the picker is open drops the in-flight result. The read grant to the picked Uri is unaffected.
 */
internal class FilePicker(
    private val element: FileInputElement,
    private val logLevel: LogLevel,
    private val onPicked: (List<SelectedFile>) -> Unit,
) {
    private val tag = FilePicker::class.qualifiedName
    private val context: Context get() = element.context

    private var cameraUri: Uri? = null
    private var cameraFile: File? = null

    fun launch(source: FileSource, multiple: Boolean, allowedFileType: Array<String>?) {
        val activity = context.findComponentActivity()
        if (activity == null) {
            Logger.error(
                tag,
                "File picker needs an Activity context. Pass the Activity (not application context) " +
                    "to container.create(context, fileInput, options).",
                logLevel
            )
            return
        }
        try {
            when (source) {
                FileSource.FILES -> launchDocuments(activity, multiple, allowedFileType)
                FileSource.PHOTOS -> launchPhotos(activity, multiple)
                FileSource.CAMERA -> launchCamera(activity)
            }
        } catch (e: Exception) {
            Logger.error(tag, "Could not launch ${source.name} picker: ${e.message}", logLevel)
        }
    }

    // --- Sources ---

    private fun launchDocuments(activity: ComponentActivity, multiple: Boolean, allowed: Array<String>?) {
        val mimeTypes = mimeTypesFor(allowed)
        if (multiple) {
            register(activity, ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                if (uris.isNotEmpty()) deliver(uris)
            }.launch(mimeTypes)
        } else {
            register(activity, ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let { deliver(listOf(it)) }
            }.launch(mimeTypes)
        }
    }

    private fun launchPhotos(activity: ComponentActivity, multiple: Boolean) {
        val request = PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            .build()
        if (multiple) {
            val max = element.effectiveMaxFileCount().coerceAtLeast(2)
            register(activity, ActivityResultContracts.PickMultipleVisualMedia(max)) { uris ->
                if (uris.isNotEmpty()) deliver(uris)
            }.launch(request)
        } else {
            register(activity, ActivityResultContracts.PickVisualMedia()) { uri ->
                uri?.let { deliver(listOf(it)) }
            }.launch(request)
        }
    }

    private fun launchCamera(activity: ComponentActivity) {
        val file = newCameraFile() ?: return
        val uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.skyflow.fileprovider", file)
        } catch (e: Exception) {
            Logger.error(
                tag,
                "Camera source needs the SDK FileProvider. Could not create a capture file: ${e.message}",
                logLevel
            )
            return
        }
        cameraUri = uri
        cameraFile = file
        register(activity, ActivityResultContracts.TakePicture()) { success ->
            val capturedUri = cameraUri
            val capturedFile = cameraFile
            cameraUri = null
            cameraFile = null
            if (success && capturedUri != null) {
                // Track the SDK-owned capture so the element can delete it once it is no longer part
                // of the selection. (User-picked documents are not SDK-owned and are never deleted.)
                element.registerCaptureFile(capturedUri, capturedFile)
                deliver(listOf(capturedUri))
            } else {
                // Capture cancelled — delete the placeholder file we created so captures don't pile
                // up in the cache.
                capturedFile?.delete()
            }
        }.launch(uri)
    }

    // --- Registration / result plumbing ---

    /**
     * Register a one-shot launcher on the activity's result registry under a unique key, and
     * unregister it as soon as the result comes back.
     */
    private fun <I, O> register(
        activity: ComponentActivity,
        contract: ActivityResultContract<I, O>,
        onResult: (O) -> Unit,
    ): ActivityResultLauncher<I> {
        val key = "skyflow_file_${element.getID()}_${System.nanoTime()}"
        var launcher: ActivityResultLauncher<I>? = null
        launcher = activity.activityResultRegistry.register(key, contract) { result ->
            launcher?.unregister()
            onResult(result)
        }
        return launcher
    }

    private fun deliver(uris: List<Uri>) {
        onPicked(uris.map { resolve(it) })
    }

    /** Resolve a `content://` Uri to display name + size + MIME — the only data the element keeps. */
    private fun resolve(uri: Uri): SelectedFile {
        var name = uri.lastPathSegment ?: "file"
        var size = -1L
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx >= 0) cursor.getString(nameIdx)?.let { name = it }
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
                }
            }
        } catch (e: Exception) {
            Logger.error(tag, "Could not read file metadata: ${e.message}", logLevel)
        }
        // Some providers omit OpenableColumns.SIZE. Fall back to the file-descriptor length so the
        // size/empty-file validation actually runs — otherwise an unknown size (-1) slips past both
        // the per-file cap and the empty-file check.
        if (size < 0L) size = sizeFromDescriptor(uri)
        return SelectedFile(uri, name, size, context.contentResolver.getType(uri))
    }

    /** Resolve a file's byte length from its descriptor, or -1 if the provider can't report it. */
    private fun sizeFromDescriptor(uri: Uri): Long = try {
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
            if (afd.length == AssetFileDescriptor.UNKNOWN_LENGTH) -1L else afd.length
        } ?: -1L
    } catch (e: Exception) {
        -1L
    }

    private fun newCameraFile(): File? = try {
        val dir = File(context.cacheDir, "skyflow_uploads").apply { mkdirs() }
        File(dir, "capture_${System.currentTimeMillis()}.jpg")
    } catch (e: Exception) {
        Logger.error(
            tag,
            "Camera source needs the SDK FileProvider. Could not create a capture file: ${e.message}",
            logLevel
        )
        null
    }

    private fun mimeTypesFor(allowed: Array<String>?): Array<String> {
        if (allowed.isNullOrEmpty()) return arrayOf("*/*")
        val map = MimeTypeMap.getSingleton()
        val mimes = allowed
            .map { it.trimStart('.').lowercase() }
            .mapNotNull { map.getMimeTypeFromExtension(it) }
            .distinct()
        return if (mimes.isEmpty()) arrayOf("*/*") else mimes.toTypedArray()
    }
}

/** Unwrap a [ContextWrapper] chain to the hosting [ComponentActivity], or null if there isn't one. */
internal fun Context.findComponentActivity(): ComponentActivity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is ComponentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
