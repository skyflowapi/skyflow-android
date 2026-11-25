package com.Skyflow

import Skyflow.FileElementAppearance
import Skyflow.FileElementInput
import Skyflow.FileElementOptions
import Skyflow.FileElementType
import Skyflow.FileInputElement
import Skyflow.LogLevel
import Skyflow.core.file.SelectedFile
import Skyflow.utils.EventName
import android.app.Activity
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

/**
 * Unit tests for [FileInputElement] selection logic, state, validation and per-appearance rendering.
 * Builds the element directly (bypassing the container) so the tests stay focused on the view.
 */
@RunWith(RobolectricTestRunner::class)
class FileInputElementTest {

    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity

    @Before
    fun setup() {
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()
    }

    private fun file(name: String, size: Long = 100L): SelectedFile =
        SelectedFile(Uri.parse("content://test/$name"), name, size, "application/octet-stream")

    private fun element(
        type: FileElementType = FileElementType.FILE_INPUT,
        appearance: FileElementAppearance = FileElementAppearance.FIELD,
        options: FileElementOptions = FileElementOptions(),
    ): FileInputElement {
        val input = FileElementInput(
            table = "files", column = "document", type = type, label = "Upload", appearance = appearance
        )
        return FileInputElement(activity, LogLevel.ERROR).apply { setupFileField(input, options) }
    }

    // --- selection ---

    @Test
    fun singleInputKeepsOnlyOneFile() {
        val el = element(FileElementType.FILE_INPUT)
        el.attachSelectedFiles(listOf(file("a.pdf"), file("b.pdf")))
        assertEquals(1, el.getSelectedFiles().size)
        assertEquals("a.pdf", el.getSelectedFiles()[0].name)
    }

    @Test
    fun multiInputKeepsAllAttachedFiles() {
        val el = element(FileElementType.MULTI_FILE_INPUT)
        el.attachSelectedFiles(listOf(file("a.pdf"), file("b.pdf"), file("c.pdf")))
        assertEquals(3, el.getSelectedFiles().size)
    }

    // --- effective limits ---

    @Test
    fun singleInputEffectiveCountIsOneAndSizeIs32M() {
        val el = element(FileElementType.FILE_INPUT)
        assertEquals(1, el.effectiveMaxFileCount())
        assertEquals(32_000_000L, el.effectiveMaxFileSizeBytes())
    }

    @Test
    fun multiInputDefaultsToFourFilesAnd32M() {
        val el = element(FileElementType.MULTI_FILE_INPUT)
        assertEquals(4, el.effectiveMaxFileCount())
        assertEquals(32_000_000L, el.effectiveMaxFileSizeBytes())
    }

    @Test
    fun multiInputHonoursConfiguredLimits() {
        val el = element(
            FileElementType.MULTI_FILE_INPUT,
            options = FileElementOptions(maxFileCount = 2, maxFileSize = 10_000L)
        )
        assertEquals(2, el.effectiveMaxFileCount())
        assertEquals(10_000L, el.effectiveMaxFileSizeBytes())
    }

    // --- state ---

    @Test
    fun stateReflectsSelection() {
        val el = element(FileElementType.MULTI_FILE_INPUT)

        var state = el.getState()
        assertEquals("MULTI_FILE_INPUT", state.getString("elementType"))
        assertEquals("document", state.getString("columnName"))
        assertFalse(state.getBoolean("isFilled"))
        assertEquals(0, state.getInt("fileCount"))

        el.attachSelectedFiles(listOf(file("a.pdf"), file("b.pdf")))
        state = el.getState()
        assertTrue(state.getBoolean("isFilled"))
        assertEquals(2, state.getInt("fileCount"))
    }

    // --- validation ---

    @Test
    fun requiredElementIsInvalidUntilAFileIsSelected() {
        val el = element(FileElementType.FILE_INPUT, options = FileElementOptions(required = true))
        assertNotEquals("", el.validate())
        el.attachSelectedFiles(listOf(file("a.pdf")))
        assertEquals("", el.validate())
    }

    @Test
    fun optionalEmptyElementIsValid() {
        val el = element(FileElementType.FILE_INPUT, options = FileElementOptions(required = false))
        assertEquals("", el.validate())
    }

    // --- unmount ---

    @Test
    fun unmountClearsSelection() {
        val el = element(FileElementType.MULTI_FILE_INPUT)
        el.attachSelectedFiles(listOf(file("a.pdf"), file("b.pdf")))
        el.unmount()
        assertTrue(el.getSelectedFiles().isEmpty())
        assertEquals(0, el.getState().getInt("fileCount"))
    }

    // --- event listeners ---

    private fun firstButton(view: View): Button? = when (view) {
        is Button -> view
        is ViewGroup -> (0 until view.childCount)
            .asSequence().mapNotNull { firstButton(view.getChildAt(it)) }.firstOrNull()
        else -> null
    }

    @Test
    fun changeFiresOnSelectWithState() {
        val el = element(FileElementType.MULTI_FILE_INPUT)
        var last: JSONObject? = null
        var calls = 0
        el.on(EventName.CHANGE) { calls++; last = it }

        el.attachSelectedFiles(listOf(file("a.pdf"), file("b.pdf")))
        assertEquals(1, calls)
        assertNotNull(last)
        assertTrue(last!!.getBoolean("isFilled"))
        assertEquals(2, last!!.getInt("fileCount"))
        assertEquals("document", last!!.getString("columnName"))
    }

    @Test
    fun changeFiresOnUnmountClear() {
        val el = element(FileElementType.MULTI_FILE_INPUT)
        el.attachSelectedFiles(listOf(file("a.pdf")))
        var last: JSONObject? = null
        el.on(EventName.CHANGE) { last = it }
        el.unmount()
        assertNotNull(last)
        assertFalse(last!!.getBoolean("isFilled"))
        assertEquals(0, last!!.getInt("fileCount"))
    }

    @Test
    fun changeDoesNotFireWithoutAHandler() {
        // Registering no CHANGE handler must not crash when the selection changes.
        val el = element(FileElementType.FILE_INPUT)
        el.attachSelectedFiles(listOf(file("a.pdf")))
        assertEquals(1, el.getSelectedFiles().size)
    }

    @Test
    fun readyFiresOnceOnMount() {
        val el = element()
        var calls = 0
        el.on(EventName.READY) { calls++ }
        activity.setContentView(el) // attaches to the window -> READY
        assertEquals(1, calls)
    }

    @Test
    fun readyFiresImmediatelyIfRegisteredAfterMount() {
        val el = element()
        activity.setContentView(el)
        var state: JSONObject? = null
        el.on(EventName.READY) { state = it } // already mounted -> immediate
        assertNotNull(state)
    }

    @Test
    fun focusAndBlurFireFromTheTrigger() {
        val el = element(appearance = FileElementAppearance.FIELD)
        activity.setContentView(el)
        var focus = 0
        var blur = 0
        el.on(EventName.FOCUS) { focus++ }
        el.on(EventName.BLUR) { blur++ }

        val trigger = firstButton(el)
        assertNotNull("FIELD appearance should have a trigger button", trigger)
        trigger!!.requestFocus()
        assertEquals(1, focus)
        trigger.clearFocus()
        assertEquals(1, blur)
    }

    @Test
    fun submitEventIsRejectedWithoutCrashing() {
        val el = element()
        var called = false
        el.on(EventName.SUBMIT) { called = true } // unsupported -> logs, never invoked
        el.attachSelectedFiles(listOf(file("a.pdf")))
        assertFalse(called)
    }

    // --- rendering: every appearance builds without error ---

    @Test
    fun everyAppearanceBuildsAndHoldsSelection() {
        val appearances = listOf(
            FileElementAppearance.FIELD,
            FileElementAppearance.BUTTON,
            FileElementAppearance.ICON(),
            FileElementAppearance.CARD,
            FileElementAppearance.CUSTOM,
        )
        for (appearance in appearances) {
            val el = element(FileElementType.MULTI_FILE_INPUT, appearance = appearance)
            assertTrue("appearance $appearance should build child views", el.childCount > 0)
            el.attachSelectedFiles(listOf(file("a.pdf")))
            assertEquals(
                "appearance $appearance should hold the selection",
                1, el.getSelectedFiles().size
            )
        }
    }
}
