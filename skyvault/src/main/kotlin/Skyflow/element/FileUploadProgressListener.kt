package Skyflow

/**
 * Observes overall file-upload progress. Pass one via [FileUploadOptions.progressListener] to
 * `container.uploadFiles(...)`.
 *
 * [onProgress] is called repeatedly as bytes are written for every file in the upload, and fires on
 * a background (network) thread — marshal to the main thread before touching UI.
 */
interface FileUploadProgressListener {
    /**
     * @param bytesUploaded cumulative bytes written so far across all files in this upload.
     * @param totalBytes    total bytes to upload across all files (sum of the picked file sizes); may
     *                      be imprecise if a picker did not report a file's size.
     */
    fun onProgress(bytesUploaded: Long, totalBytes: Long)
}
