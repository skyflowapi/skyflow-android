package Skyflow.core.file

import android.net.Uri

/**
 * A file the user picked, resolved to just the metadata the SDK needs — never the bytes. The bytes
 * are streamed straight from [uri] at upload time (see [FileUploadRequestBody]); nothing is copied
 * to disk and no public API exposes this holder.
 *
 * Internal to `common/` so both the validator and the upload path can share it. Contract-agnostic:
 * it references no product-specific (skyvault/flowvault) types, so compiling it into flowvault is
 * harmless.
 *
 * @param uri        the `content://` Uri returned by the picker.
 * @param name       the file's display name (used for validation and as the multipart filename).
 * @param sizeBytes  file size in bytes, or -1 if the provider didn't report it.
 * @param mimeType   `ContentResolver.getType(uri)`, or null if unknown.
 */
internal data class SelectedFile(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String?,
) {
    /** Lower-case extension without the dot (e.g. "pdf"), or "" if the name has none. */
    val extension: String
        get() = name.substringAfterLast('.', "").lowercase()
}
