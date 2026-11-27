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
 * type, empty and size rules, the name-sanitizing helper, and the JS-parity constants.
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
                    maxFileSizeBytes = 32_000_000L, tag = tag, logLevel = log
                )
            }
        }
    }

    @Test
    fun extensionNotInAllowedListIsRejected() {
        assertRejects {
            FileUploadValidator.validateFile(
                file("photo.gif"), allowedFileType = arrayOf(".pdf", ".png"), blockEmptyFiles = false,
                maxFileSizeBytes = 32_000_000L, tag = tag, logLevel = log
            )
        }
    }

    @Test
    fun extensionInAllowedListIsAccepted() {
        // Allowed list is matched case-insensitively and with or without the dot.
        FileUploadValidator.validateFile(
            file("photo.PNG"), allowedFileType = arrayOf(".pdf", "png"), blockEmptyFiles = false,
            maxFileSizeBytes = 32_000_000L, tag = tag, logLevel = log
        )
    }

    @Test
    fun blockedExtensionWithTrailingWhitespaceIsStillRejected() {
        // Trailing whitespace must not sneak a blocked type past validation: the name is trimmed
        // before the extension is read, matching the trimmed name that sanitizeFileName uploads.
        for (blocked in listOf("app.apk ", "archive.zip ", "run.EXE ")) {
            assertRejects {
                FileUploadValidator.validateFile(
                    file(blocked), allowedFileType = null, blockEmptyFiles = false,
                    maxFileSizeBytes = 32_000_000L, tag = tag, logLevel = log
                )
            }
        }
    }

    @Test
    fun allowedExtensionWithTrailingWhitespaceIsAccepted() {
        // The same trimming lets a valid type through despite trailing whitespace.
        FileUploadValidator.validateFile(
            file("statement.pdf "), allowedFileType = arrayOf(".pdf"), blockEmptyFiles = false,
            maxFileSizeBytes = 32_000_000L, tag = tag, logLevel = log
        )
    }

    @Test
    fun anyNonBlockedTypeIsAllowedWhenNoAllowedList() {
        FileUploadValidator.validateFile(
            file("notes.txt"), allowedFileType = null, blockEmptyFiles = false,
            maxFileSizeBytes = 32_000_000L, tag = tag, logLevel = log
        )
    }

    // --- validateFile: empty / size ---

    @Test
    fun emptyFileRejectedOnlyWhenBlockEmptyFilesIsOn() {
        assertRejects {
            FileUploadValidator.validateFile(
                file("empty.pdf", size = 0L), allowedFileType = null, blockEmptyFiles = true,
                maxFileSizeBytes = 32_000_000L, tag = tag, logLevel = log
            )
        }
        // same 0-byte file passes when the flag is off
        FileUploadValidator.validateFile(
            file("empty.pdf", size = 0L), allowedFileType = null, blockEmptyFiles = false,
            maxFileSizeBytes = 32_000_000L, tag = tag, logLevel = log
        )
    }

    @Test
    fun fileOverSizeCapIsRejected() {
        assertRejects {
            FileUploadValidator.validateFile(
                file("big.pdf", size = 32_000_001L), allowedFileType = null, blockEmptyFiles = false,
                maxFileSizeBytes = 32_000_000L, tag = tag, logLevel = log
            )
        }
    }

    @Test
    fun fileAtSizeCapIsAccepted() {
        FileUploadValidator.validateFile(
            file("ok.pdf", size = 32_000_000L), allowedFileType = null, blockEmptyFiles = false,
            maxFileSizeBytes = 32_000_000L, tag = tag, logLevel = log
        )
    }

    @Test
    fun ordinaryNameWithSpacesIsNotRejected() {
        // Names are cleaned, never rejected (the cross-SDK fix) — this must not throw.
        FileUploadValidator.validateFile(
            file("my vacation photo.jpg"), allowedFileType = null, blockEmptyFiles = false,
            maxFileSizeBytes = 32_000_000L, tag = tag, logLevel = log
        )
    }

    // --- sanitizeFileName ---

    @Test
    fun sanitizeReplacesSpacesAndUnsafeCharsKeepingExtension() {
        assertEquals("my_photo.jpg", FileUploadValidator.sanitizeFileName("my photo.jpg"))
        assertEquals("in_voice_.pdf", FileUploadValidator.sanitizeFileName("in voice#.pdf"))
        // non-ASCII and spaces both become '_'; the dot/extension is preserved.
        assertEquals("r__sum__.pdf", FileUploadValidator.sanitizeFileName("ré sumé .pdf"))
    }

    @Test
    fun sanitizeLeavesAlreadyValidNamesUnchanged() {
        assertEquals(
            "invoice-2024_final(1).pdf",
            FileUploadValidator.sanitizeFileName("invoice-2024_final(1).pdf")
        )
    }

    @Test
    fun sanitizeFallsBackToFileForBlankNames() {
        assertEquals("file", FileUploadValidator.sanitizeFileName(""))
        assertEquals("file", FileUploadValidator.sanitizeFileName("   "))
    }
}
