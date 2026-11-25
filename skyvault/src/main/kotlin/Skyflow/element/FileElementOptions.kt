package Skyflow

/**
 * Options for a [FileInputElement]. Mirrors the file options in skyflow-js / iOS, plus the
 * Android-only [sources].
 *
 * @param required            fail validation (and `uploadFiles`) if no file is selected.
 * @param allowedFileType     extensions the picker accepts and the SDK validates, e.g.
 *                            `arrayOf(".pdf", ".png")`. `null`/empty = any (minus the blocked list).
 * @param blockEmptyFiles     reject a selected file whose size is 0 bytes.
 * @param preserveFileName    `true` (default) keeps the original name; `false` sends a generated
 *                            UUID name, which keeps any PII in the file name out of the vault.
 * @param returnFileMetadata  ask the server to return `fileName` / `fileSize` / `fileType` on success.
 * @param maxFileSize         per-file byte cap. [FileElementType.MULTI_FILE_INPUT] only; a single
 *                            [FileElementType.FILE_INPUT] is fixed at 32 MB by the server.
 * @param maxFileCount        max files in one [FileElementType.MULTI_FILE_INPUT] upload (default 4).
 * @param stripImageMetadata  remove location/EXIF metadata from JPEG photos before upload (GPS,
 *                            camera make/model, timestamps). Off by default. Stripping copies the
 *                            photo to a private cache file to rewrite it, then deletes it after upload
 *                            (the only case where a file briefly touches disk).
 * @param sources             which pickers the element offers. CAMERA is opt-in (see [FileSource]).
 */
class FileElementOptions(
    var required: Boolean = false,
    var allowedFileType: Array<String>? = null,
    var blockEmptyFiles: Boolean = false,
    var preserveFileName: Boolean = true,
    var returnFileMetadata: Boolean = false,
    var maxFileSize: Long? = null,
    var maxFileCount: Int? = null,
    var stripImageMetadata: Boolean = false,
    var sources: Array<FileSource> = arrayOf(FileSource.FILES, FileSource.PHOTOS),
)
