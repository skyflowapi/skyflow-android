package Skyflow

/**
 * Options for a collect element. The text/card options are unchanged; the file options below are used
 * only by `container.createFileElement(...)` (a [CollectElementInput] built with a [FileElementType])
 * and ignored by text elements — the same single options class text and file fields share in
 * skyflow-js / iOS. skyvault's own copy, so none of this reaches flowvault.
 *
 * @param allowedFileType     extensions the picker accepts and the SDK validates, e.g.
 *                            `arrayOf(".pdf", ".png")`. `null`/empty = any (minus the blocked list).
 * @param blockEmptyFiles     reject a selected file whose size is 0 bytes.
 * @param preserveFileName    `true` (default) keeps the original name; `false` sends a generated UUID
 *                            name, keeping any PII in the file name out of the vault.
 * @param returnFileMetadata  ask the server to return `fileName` / `fileSize` / `fileType` on success.
 * @param maxFileSize         per-file byte cap. [FileElementType.MULTI_FILE_INPUT] only; a single
 *                            [FileElementType.FILE_INPUT] is fixed at 32 MB by the server.
 * @param maxFileCount        max files in one [FileElementType.MULTI_FILE_INPUT] upload (default 4).
 * @param stripImageMetadata  remove location/EXIF metadata from JPEG photos before upload (GPS,
 *                            camera make/model, timestamps). Off by default.
 * @param sources             which pickers the element offers. CAMERA is opt-in (see [FileSource]).
 */
class CollectElementOptions(
    var required: Boolean = false,
    var enableCardIcon: Boolean = true,
    var format: String = "",
    var translation: HashMap<Char, String>? = null,
    val enableCopy: Boolean = false,
    var cardMetadata: CardMetadata = CardMetadata(arrayOf()),
    // --- File-collection options (used by createFileElement; ignored by text/card elements) ---
    var allowedFileType: Array<String>? = null,
    var blockEmptyFiles: Boolean = false,
    var preserveFileName: Boolean = true,
    var returnFileMetadata: Boolean = false,
    var maxFileSize: Long? = null,
    var maxFileCount: Int? = null,
    var stripImageMetadata: Boolean = false,
    var sources: Array<FileSource> = arrayOf(FileSource.FILES, FileSource.PHOTOS)
) {
    internal var inputFormat: HashMap<Char, Regex> = hashMapOf()
    private val HYPHEN_CARD_NUMBER_FORMAT = "XXXX-XXXX-XXXX-XXXX"
    internal fun parseFormatForSeparator(): Char {
        return if (format == HYPHEN_CARD_NUMBER_FORMAT) '-'
        else ' '
    }

    internal fun createRegexMap() {
        if (translation != null) {
            for (key in translation!!.keys) {
                val value = translation!![key] ?: return
                inputFormat[key] = if (value.isEmpty()) {
                    Regex("[\\s\\S]*")
                } else {
                    Regex(value)
                }
            }
        }
    }
}
