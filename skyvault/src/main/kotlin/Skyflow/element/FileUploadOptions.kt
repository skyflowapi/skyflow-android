package Skyflow

/**
 * Per-call options for `container.uploadFiles(...)` — timeout and progress. Pass with the
 * `uploadFiles(callback, uploadOptions)` / `uploadFiles(metaData, callback, uploadOptions)` overloads.
 * An in-flight upload can be stopped with `container.cancelFileUploads()`.
 *
 * @param timeoutMs         per-upload call timeout in milliseconds (OkHttp `callTimeout`), applied to
 *                          every file request. `0` (default) leaves no timeout. With a timeout a
 *                          stalled upload fails fast instead of hanging forever.
 * @param progressListener  optional [FileUploadProgressListener] notified of byte progress across all
 *                          files in the upload.
 */
class FileUploadOptions(
    var timeoutMs: Long = 0L,
    var progressListener: FileUploadProgressListener? = null,
)
