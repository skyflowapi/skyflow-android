package Skyflow.core.file

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException

/**
 * An OkHttp [RequestBody] that streams a file straight from its `content://` [uri] via
 * [ContentResolver]. It never reads the whole file into memory (4 × 32 MB would OOM the app) and
 * never copies it to disk — the bytes go from the provider's input stream to the socket.
 *
 * Internal to `common/` and contract-agnostic, so both SDKs' upload paths share it.
 */
internal class FileUploadRequestBody(
    private val contentResolver: ContentResolver,
    private val uri: Uri,
    mimeType: String?,
    private val declaredLength: Long = -1L,
) : RequestBody() {

    private val mediaType: MediaType? =
        (mimeType ?: "application/octet-stream").toMediaTypeOrNull()

    override fun contentType(): MediaType? = mediaType

    override fun contentLength(): Long = declaredLength

    override fun writeTo(sink: BufferedSink) {
        val input = contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open input stream for $uri")
        input.use { stream ->
            sink.writeAll(stream.source())
        }
    }
}
