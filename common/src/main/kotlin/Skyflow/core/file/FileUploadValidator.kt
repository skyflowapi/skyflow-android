package Skyflow.core.file

import Skyflow.LogLevel
import Skyflow.SkyflowErrorCode
import Skyflow.SkyflowInternalError

/**
 * Client-side file validation shared by both SDKs. Pure and contract-agnostic: it works on
 * primitives + [SelectedFile] (no skyvault/flowvault types), so it lives in `common/` and can be
 * reused when flowvault adopts file upload.
 *
 * Every check throws [SkyflowInternalError] (which is the product's `SkyflowError`) so the failure
 * flows into the caller's `onFailure` with no network request — the "fail fast" contract from the
 * design doc. The server enforces these too; this is the first, friendlier line of defence.
 */
internal object FileUploadValidator {

    /**
     * Server-fixed cap for a single [Skyflow.FileElementType.FILE_INPUT], and the default per-file
     * cap for a multi-file element. 32 MB decimal, matching skyflow-js (`maxFileSize` default
     * `32_000_000`).
     */
    const val SINGLE_FILE_MAX_BYTES: Long = 32_000_000L

    /** Default max files for a multi-file element when [maxFileCount] is not set. */
    const val DEFAULT_MAX_FILE_COUNT: Int = 4

    // Dangerous/unsupported types, blocked regardless of allowedFileType. Keeps parity with JS and
    // adds .apk for Android.
    private val BLOCKED_EXTENSIONS = setOf("zip", "exe", "rar", "deb", "apk")

    // JS file-name rule: alphanumerics and ! - _ . * ( ) only.
    private val VALID_NAME = Regex("^[A-Za-z0-9!\\-_.*()]+$")

    /**
     * Validate the number of files picked for one element.
     *
     * @param count       how many files are currently selected.
     * @param required    whether at least one file must be selected.
     * @param maxCount    the effective max (already defaulted by the caller for multi-file).
     */
    fun validateCount(
        count: Int,
        required: Boolean,
        maxCount: Int,
        column: String?,
        tag: String?,
        logLevel: LogLevel,
    ) {
        if (count == 0) {
            if (required) {
                throw SkyflowInternalError(
                    SkyflowErrorCode.FILE_NOT_SELECTED, tag, logLevel, arrayOf(column ?: "")
                )
            }
            return
        }
        if (count > maxCount) {
            throw SkyflowInternalError(
                SkyflowErrorCode.FILE_COUNT_EXCEEDED, tag, logLevel, arrayOf(column ?: "")
            )
        }
    }

    /**
     * Validate a single picked file (type, emptiness, size, and name).
     *
     * @param maxFileSizeBytes  effective per-file cap in bytes.
     * @param checkName         only enforce the name rule when the original name is preserved
     *                          (when `preserveFileName = false` the SDK sends a generated UUID name).
     */
    fun validateFile(
        file: SelectedFile,
        allowedFileType: Array<String>?,
        blockEmptyFiles: Boolean,
        maxFileSizeBytes: Long,
        checkName: Boolean,
        tag: String?,
        logLevel: LogLevel,
    ) {
        val ext = file.extension

        if (BLOCKED_EXTENSIONS.contains(ext)) {
            throw SkyflowInternalError(
                SkyflowErrorCode.FILE_TYPE_NOT_ALLOWED, tag, logLevel, arrayOf(file.name)
            )
        }

        if (!allowedFileType.isNullOrEmpty()) {
            val allowed = allowedFileType.map { it.trimStart('.').lowercase() }
            if (!allowed.contains(ext)) {
                throw SkyflowInternalError(
                    SkyflowErrorCode.FILE_TYPE_NOT_ALLOWED, tag, logLevel, arrayOf(file.name)
                )
            }
        }

        if (blockEmptyFiles && file.sizeBytes == 0L) {
            throw SkyflowInternalError(
                SkyflowErrorCode.EMPTY_FILE, tag, logLevel, arrayOf(file.name)
            )
        }

        if (file.sizeBytes > maxFileSizeBytes) {
            throw SkyflowInternalError(
                SkyflowErrorCode.FILE_SIZE_EXCEEDED, tag, logLevel, arrayOf(file.name)
            )
        }

        if (checkName && !VALID_NAME.matches(file.name)) {
            throw SkyflowInternalError(
                SkyflowErrorCode.INVALID_FILE_NAME, tag, logLevel, arrayOf(file.name)
            )
        }
    }
}
