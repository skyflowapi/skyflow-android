package com.Skyflow

import Skyflow.LogLevel
import Skyflow.SkyflowError
import Skyflow.core.file.FileUploadValidator
import Skyflow.core.file.SelectedFile
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for the shared client-side file validator ([FileUploadValidator]). Covers the count,
 * type, empty, size and name rules, and the JS-parity constants.
 */
@RunWith(RobolectricTestRunner::class)
class FileUploadValidatorTest {

    private val tag = "FileUploadValidatorTest"
    private val log = LogLevel.ERROR

    private fun file(name: String, size: Long = 100L): SelectedFile =
        SelectedFile(Uri.parse("content://test/$name"), name, size, "application/octet-stream")

    /** Assert that [block] throws a [SkyflowError]. */
    private fun assertRejects(block: () -> Unit) {
        try {
            block()
            fail("expected a SkyflowError to be thrown")
        } catch (e: SkyflowError) {
            // expected
        }
    }

    // --- constants (skyflow-js parity) ---

    @Test
    fun singleFileCapIs32MillionBytes() {
        assertEquals(32_000_000L, FileUploadValidator.SINGLE_FILE_MAX_BYTES)
    }

    @Test
    fun defaultMaxFileCountIsFour() {
        assertEquals(4, FileUploadValidator.DEFAULT_MAX_FILE_COUNT)
    }

    // --- validateCount ---

    @Test
    fun requiredWithNoFilesIsRejected() {
        assertRejects {
            FileUploadValidator.validateCount(0, required = true, maxCount = 1, "document", tag, log)
        }
    }

    @Test
    fun notRequiredWithNoFilesIsAllowed() {
        FileUploadValidator.validateCount(0, required = false, maxCount = 1, "document", tag, log)
    }

    @Test
    fun countWithinMaxIsAllowed() {
        FileUploadValidator.validateCount(3, required = true, maxCount = 4, "attachments", tag, log)
    }

    @Test
    fun countOverMaxIsRejected() {
        assertRejects {
            FileUploadValidator.validateCount(5, required = true, maxCount = 4, "attachments", tag, log)
        }
    }

    // --- validateFile: type ---

    @Test
    fun blockedExtensionIsRejectedEvenWhenAllowedListIsEmpty() {
        for (blocked in listOf("app.exe", "a.zip", "a.rar", "a.deb", "a.apk")) {
            assertRejects {
                FileUploadValidator.validateFile(
                    file(blocked), allowedFileType = null, blockEmptyFiles = false,
                    maxFileSizeBytes = 32_000_000L, checkName = true, tag = tag, logLevel = log
                )
            }
        }
    }

    @Test
    fun extensionNotInAllowedListIsRejected() {
        assertRejects {
            FileUploadValidator.validateFile(
                file("photo.gif"), allowedFileType = arrayOf(".pdf", ".png"), blockEmptyFiles = false,
                maxFileSizeBytes = 32_000_000L, checkName = true, tag = tag, logLevel = log
            )
        }
    }

    @Test
    fun extensionInAllowedListIsAccepted() {
        // Allowed list is matched case-insensitively and with or without the dot.
        FileUploadValidator.validateFile(
            file("photo.PNG"), allowedFileType = arrayOf(".pdf", "png"), blockEmptyFiles = false,
            maxFileSizeBytes = 32_000_000L, checkName = true, tag = tag, logLevel = log
        )
    }

    @Test
    fun anyNonBlockedTypeIsAllowedWhenNoAllowedList() {
        FileUploadValidator.validateFile(
            file("notes.txt"), allowedFileType = null, blockEmptyFiles = false,
            maxFileSizeBytes = 32_000_000L, checkName = true, tag = tag, logLevel = log
        )
    }

    // --- validateFile: empty / size ---

    @Test
    fun emptyFileRejectedOnlyWhenBlockEmptyFilesIsOn() {
        assertRejects {
            FileUploadValidator.validateFile(
                file("empty.pdf", size = 0L), allowedFileType = null, blockEmptyFiles = true,
                maxFileSizeBytes = 32_000_000L, checkName = true, tag = tag, logLevel = log
            )
        }
        // same 0-byte file passes when the flag is off
        FileUploadValidator.validateFile(
            file("empty.pdf", size = 0L), allowedFileType = null, blockEmptyFiles = false,
            maxFileSizeBytes = 32_000_000L, checkName = true, tag = tag, logLevel = log
        )
    }

    @Test
    fun fileOverSizeCapIsRejected() {
        assertRejects {
            FileUploadValidator.validateFile(
                file("big.pdf", size = 32_000_001L), allowedFileType = null, blockEmptyFiles = false,
                maxFileSizeBytes = 32_000_000L, checkName = true, tag = tag, logLevel = log
            )
        }
    }

    @Test
    fun fileAtSizeCapIsAccepted() {
        FileUploadValidator.validateFile(
            file("ok.pdf", size = 32_000_000L), allowedFileType = null, blockEmptyFiles = false,
            maxFileSizeBytes = 32_000_000L, checkName = true, tag = tag, logLevel = log
        )
    }

    // --- validateFile: name ---

    @Test
    fun invalidNameRejectedWhenCheckNameOn() {
        assertRejects {
            FileUploadValidator.validateFile(
                file("in voice#.pdf"), allowedFileType = null, blockEmptyFiles = false,
                maxFileSizeBytes = 32_000_000L, checkName = true, tag = tag, logLevel = log
            )
        }
    }

    @Test
    fun invalidNameAllowedWhenCheckNameOff() {
        // preserveFileName = false → the SDK sends a generated name, so the original isn't checked.
        FileUploadValidator.validateFile(
            file("in voice#.pdf"), allowedFileType = null, blockEmptyFiles = false,
            maxFileSizeBytes = 32_000_000L, checkName = false, tag = tag, logLevel = log
        )
    }

    @Test
    fun validNameAccepted() {
        FileUploadValidator.validateFile(
            file("invoice-2024_final(1).pdf"), allowedFileType = null, blockEmptyFiles = false,
            maxFileSizeBytes = 32_000_000L, checkName = true, tag = tag, logLevel = log
        )
    }
}
