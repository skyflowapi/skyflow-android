package Skyflow.collect.client

import Skyflow.core.file.SelectedFile
import android.content.Context
import androidx.exifinterface.media.ExifInterface
import java.io.File

/**
 * Removes location / identifying EXIF metadata from JPEG photos before upload, for
 * `FileElementOptions.stripImageMetadata`. skyvault-only (keeps the ExifInterface dependency out of
 * flowvault).
 *
 * The SDK normally streams bytes straight from the picked `content://` Uri with nothing on disk.
 * Stripping is the one exception: ExifInterface rewrites a seekable file, so the photo is copied to a
 * private app-cache file, cleaned, uploaded from there, and the temp file is deleted once the request
 * finishes (success or failure). Only JPEG carries the GPS/EXIF this targets; other types are left to
 * stream straight from the Uri.
 */
internal object ImageMetadataStripper {

    private val STRIPPABLE_EXTENSIONS = setOf("jpg", "jpeg")

    // GPS, camera make/model and capture timestamps — the fields that identify who/where/when.
    private val SENSITIVE_TAGS = listOf(
        ExifInterface.TAG_GPS_LATITUDE, ExifInterface.TAG_GPS_LATITUDE_REF,
        ExifInterface.TAG_GPS_LONGITUDE, ExifInterface.TAG_GPS_LONGITUDE_REF,
        ExifInterface.TAG_GPS_ALTITUDE, ExifInterface.TAG_GPS_ALTITUDE_REF,
        ExifInterface.TAG_GPS_TIMESTAMP, ExifInterface.TAG_GPS_DATESTAMP,
        ExifInterface.TAG_GPS_PROCESSING_METHOD, ExifInterface.TAG_GPS_AREA_INFORMATION,
        ExifInterface.TAG_GPS_SPEED, ExifInterface.TAG_GPS_SPEED_REF,
        ExifInterface.TAG_GPS_IMG_DIRECTION, ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
        ExifInterface.TAG_DATETIME, ExifInterface.TAG_DATETIME_ORIGINAL,
        ExifInterface.TAG_DATETIME_DIGITIZED,
        ExifInterface.TAG_MAKE, ExifInterface.TAG_MODEL, ExifInterface.TAG_SOFTWARE,
    )

    /** Whether [stripToTempFile] can strip this file (a JPEG we can rewrite with ExifInterface). */
    fun shouldStrip(file: SelectedFile): Boolean = file.extension in STRIPPABLE_EXTENSIONS

    /**
     * Copy [file] to a private cache file with [SENSITIVE_TAGS] removed and return it (the caller
     * uploads from it and then deletes it), or `null` if stripping isn't applicable or fails — in
     * which case the caller streams straight from the Uri, unstripped, rather than blocking the upload.
     */
    fun stripToTempFile(context: Context, file: SelectedFile): File? {
        if (!shouldStrip(file)) return null
        var temp: File? = null
        return try {
            val dir = File(context.cacheDir, "skyflow_strip").apply { mkdirs() }
            temp = File.createTempFile("upload", ".${file.extension}", dir)
            val copied = context.contentResolver.openInputStream(file.uri)?.use { input ->
                temp!!.outputStream().use { output -> input.copyTo(output) }
                true
            } ?: false
            if (!copied) {
                temp?.delete()
                return null
            }
            val exif = ExifInterface(temp!!.absolutePath)
            for (tag in SENSITIVE_TAGS) exif.setAttribute(tag, null)
            exif.saveAttributes()
            temp
        } catch (e: Exception) {
            temp?.delete()
            null
        }
    }
}
