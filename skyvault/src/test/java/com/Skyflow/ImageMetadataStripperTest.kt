package com.Skyflow

import Skyflow.collect.client.ImageMetadataStripper
import Skyflow.core.file.SelectedFile
import android.net.Uri
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Covers [ImageMetadataStripper.shouldStrip] — which files `stripImageMetadata` targets. The gate
 * must catch JPEGs by MIME type too, not just extension, so a provider that reports `image/jpeg`
 * with an extensionless or `.jfif` name is still stripped rather than silently uploaded with GPS.
 */
@RunWith(RobolectricTestRunner::class)
class ImageMetadataStripperTest {

    private fun file(name: String, mime: String?): SelectedFile =
        SelectedFile(Uri.parse("content://test/$name"), name, 100L, mime)

    @Test
    fun stripsJpegByExtension() {
        assertTrue(ImageMetadataStripper.shouldStrip(file("photo.jpg", "image/jpeg")))
        assertTrue(ImageMetadataStripper.shouldStrip(file("photo.JPEG", "application/octet-stream")))
    }

    @Test
    fun stripsJpegByMimeWhenExtensionMissingOrUnknown() {
        assertTrue(ImageMetadataStripper.shouldStrip(file("IMG_0001", "image/jpeg")))
        assertTrue(ImageMetadataStripper.shouldStrip(file("scan.jfif", "image/jpeg")))
        // MIME with a parameter still matches.
        assertTrue(ImageMetadataStripper.shouldStrip(file("p", "image/jpeg; charset=binary")))
    }

    @Test
    fun doesNotStripNonJpeg() {
        assertFalse(ImageMetadataStripper.shouldStrip(file("doc.pdf", "application/pdf")))
        assertFalse(ImageMetadataStripper.shouldStrip(file("photo.png", "image/png")))
        assertFalse(ImageMetadataStripper.shouldStrip(file("noext", null)))
    }
}
