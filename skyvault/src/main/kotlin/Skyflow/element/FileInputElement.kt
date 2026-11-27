package Skyflow

import Skyflow.core.Logger
import Skyflow.core.file.FileUploadValidator
import Skyflow.core.file.SelectedFile
import Skyflow.utils.EventName
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.Skyflow.collect.elements.validations.SkyflowValidationError
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONObject
import java.io.File
import java.util.Locale

/**
 * The native file-collection View returned by `container.create(context, FileElementInput, …)`.
 * Subclasses the shared [Element] so it mounts like any other element, but it is a `skyvault`-only
 * type (keeps file upload out of flowvault).
 *
 * Security: the boundary is the API, not an iframe. This View holds the picked `content://` Uri(s)
 * internally and exposes **no** public getter for the bytes, Uri or file name — the same model as
 * [TextField]. State/events never carry the file contents. (See the design doc: native Android has
 * no in-app isolation equivalent to the browser iframe; this is encapsulation, not a sandbox.)
 *
 * UI: the trigger (the control that opens the picker) is drawn per [FileElementInput.appearance] —
 * [FileElementAppearance.FIELD], [FileElementAppearance.BUTTON], [FileElementAppearance.ICON],
 * [FileElementAppearance.CARD], or [FileElementAppearance.CUSTOM] (no trigger; the host app calls
 * [openPicker]). The behaviour — picking, per-file remove (✕), validation, errors — is identical
 * across all five looks. A Material source chooser appears when several [FileSource]s are enabled.
 */
class FileInputElement internal constructor(
    context: Context,
    internal val logLevel: LogLevel,
) : Element(context) {

    internal lateinit var fileInput: FileElementInput
    internal lateinit var fileOptions: FileElementOptions

    // The current selection. Never exposed publicly.
    private val selectedFiles: MutableList<SelectedFile> = mutableListOf()

    // SDK-owned camera capture files, keyed by the content Uri they were delivered under. The SDK
    // writes these into its own cache; they're deleted once no longer referenced by the selection
    // (see [deleteUnreferencedCaptures]) so repeated captures don't accumulate. User-picked documents
    // are never SDK-owned and never appear here.
    private val captureFiles: MutableMap<Uri, File> = mutableMapOf()

    private val tag = FileInputElement::class.qualifiedName

    // UI (shared across looks)
    private var labelView: TextView? = null
    private var filesContainer: LinearLayout? = null
    private var emptyView: TextView? = null
    private var errorView: TextView? = null

    // Trigger views (only one is non-null, by appearance; all null for CUSTOM)
    private var triggerButton: Button? = null       // FIELD, BUTTON
    private var cardView: TextView? = null           // CARD
    private var iconButton: ImageButton? = null      // ICON

    private var picker: FilePicker? = null

    // Event listeners (parity with TextField.on and the skyflow-js file element).
    private var userOnChangeListener: ((JSONObject) -> Unit)? = null
    private var userOnReadyListener: ((JSONObject) -> Unit)? = null
    private var userOnFocusListener: ((JSONObject) -> Unit)? = null
    private var userOnBlurListener: ((JSONObject) -> Unit)? = null
    private var attachedToWindow = false
    private var readyEmitted = false

    internal val isMultiple: Boolean
        get() = ::fileInput.isInitialized && fileInput.type == FileElementType.MULTI_FILE_INPUT

    private val appearance: FileElementAppearance
        get() = fileInput.appearance

    internal fun setupFileField(input: FileElementInput, options: FileElementOptions) {
        this.fileInput = input
        this.fileOptions = options
        this.columnName = input.column ?: ""
        this.tableName = input.tableName ?: ""
        this.skyflowID = input.skyflowId
        this.isRequired = options.required
        buildView()
    }

    private fun buildView() {
        orientation = LinearLayout.VERTICAL

        if (fileInput.label.isNotEmpty()) {
            labelView = TextView(context).apply {
                text = fileInput.label
                setPadding(0, 0, 0, dp(6))
            }
            addView(labelView)
        }

        filesContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(4), 0, 0)
        }

        val trigger = buildTrigger()

        // ICON sits beside the file names; every other look stacks the names under the trigger.
        if (appearance is FileElementAppearance.ICON && trigger != null) {
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            row.addView(trigger)
            filesContainer!!.layoutParams =
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            row.addView(filesContainer)
            addView(row)
        } else {
            if (trigger != null) addView(trigger)
            addView(filesContainer)
        }

        emptyView = TextView(context).apply {
            text = "No file selected"
            setPadding(0, dp(8), 0, 0)
        }
        addView(emptyView)

        errorView = TextView(context).apply {
            text = ""
            gravity = Gravity.START
            setPadding(0, dp(4), 0, 0)
            visibility = View.GONE
        }
        addView(errorView)

        renderFiles()
    }

    /** Build the trigger view for the configured [appearance], or `null` for CUSTOM (no trigger). */
    private fun buildTrigger(): View? = when (val a = appearance) {
        is FileElementAppearance.FIELD -> Button(context).apply {
            isAllCaps = false
            setOnClickListener { openPicker() }
            if (fileInput.buttonStyles == null) background = defaultBorder(dashed = false)
            triggerButton = this
        }
        is FileElementAppearance.BUTTON -> Button(context).apply {
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { openPicker() }
            triggerButton = this
        }
        is FileElementAppearance.ICON -> ImageButton(context).apply {
            setImageResource(a.icon ?: android.R.drawable.ic_menu_upload)
            contentDescription = "Choose file"
            background = null
            setOnClickListener { openPicker() }
            iconButton = this
        }
        is FileElementAppearance.CARD -> TextView(context).apply {
            gravity = Gravity.CENTER
            minHeight = dp(120)
            setPadding(dp(16), dp(24), dp(16), dp(24))
            isClickable = true
            if (fileInput.buttonStyles == null) background = defaultBorder(dashed = true)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { openPicker() }
            cardView = this
        }
        is FileElementAppearance.CUSTOM -> null
    }.also { view ->
        if (view != null) {
            fileInput.buttonStyles?.base?.let { applyButtonStyle(view, it) }
            wireFocusEvents(view)
        }
    }

    // --- Picking ---

    /**
     * Open the file picker. The public entry point for [FileElementAppearance.CUSTOM] (the host app's
     * own button calls this), and the same action the built-in triggers fire on tap. Shows the source
     * chooser when more than one [FileSource] is available, otherwise opens that source directly.
     */
    fun openPicker() {
        clearError()
        val sources = availableSources()
        if (sources.isEmpty()) {
            Logger.error(tag, "No file sources are available to pick from.", logLevel)
            return
        }
        if (sources.size == 1) launchSource(sources.first()) else showSourceChooser(sources)
    }

    /** Configured sources, minus CAMERA on devices with no camera (so the control never dead-ends). */
    private fun availableSources(): List<FileSource> {
        val configured = fileOptions.sources.toList().ifEmpty { listOf(FileSource.FILES) }
        return configured.filter { it != FileSource.CAMERA || deviceHasCamera() }
    }

    private fun deviceHasCamera(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    private fun launchSource(source: FileSource) {
        val p = picker ?: FilePicker(this, logLevel) { files -> addPickedFiles(files) }.also { picker = it }
        p.launch(source, isMultiple, fileOptions.allowedFileType)
    }

    private fun showSourceChooser(sources: List<FileSource>) {
        val dialog = BottomSheetDialog(context)
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(16))
        }
        container.addView(TextView(context).apply {
            text = "Add file"
            setPadding(dp(20), dp(12), dp(20), dp(8))
        })
        for (source in sources) {
            container.addView(sourceRow(source) {
                dialog.dismiss()
                launchSource(source)
            })
        }
        dialog.setContentView(container)
        dialog.show()
    }

    private fun sourceRow(source: FileSource, onClick: () -> Unit): TextView {
        val (label, icon) = when (source) {
            FileSource.FILES -> "Files" to android.R.drawable.ic_menu_agenda
            FileSource.PHOTOS -> "Photos" to android.R.drawable.ic_menu_gallery
            FileSource.CAMERA -> "Take photo" to android.R.drawable.ic_menu_camera
        }
        return TextView(context).apply {
            text = label
            textSize = 16f
            setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0)
            compoundDrawablePadding = dp(20)
            setPadding(dp(20), dp(16), dp(20), dp(16))
            isClickable = true
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { onClick() }
        }
    }

    /**
     * Apply a picker result: single replaces the selection; multi appends up to [effectiveMaxFileCount].
     */
    private fun addPickedFiles(picked: List<SelectedFile>) {
        if (!isMultiple) {
            selectedFiles.clear()
            picked.firstOrNull()?.let { selectedFiles.add(it) }
        } else {
            val max = effectiveMaxFileCount()
            var truncated = false
            for (file in picked) {
                if (selectedFiles.size >= max) { truncated = true; break }
                selectedFiles.add(file)
            }
            if (truncated) setError("You can add up to $max files.")
        }
        deleteUnreferencedCaptures()
        renderFiles()
        emitChange()
    }

    private fun removeFileAt(index: Int) {
        if (index in selectedFiles.indices) {
            selectedFiles.removeAt(index)
            deleteUnreferencedCaptures()
            clearError()
            renderFiles()
            emitChange()
        }
    }

    /** Record a SDK-owned camera capture file so it can be deleted when no longer selected. */
    internal fun registerCaptureFile(uri: Uri, file: File?) {
        if (file != null) captureFiles[uri] = file
    }

    /** Delete any tracked capture file that is no longer part of the current selection. */
    private fun deleteUnreferencedCaptures() {
        if (captureFiles.isEmpty()) return
        val selectedUris = selectedFiles.mapTo(HashSet()) { it.uri }
        val iterator = captureFiles.entries.iterator()
        while (iterator.hasNext()) {
            val (uri, file) = iterator.next()
            if (uri !in selectedUris) {
                try { file.delete() } catch (_: Exception) {}
                iterator.remove()
            }
        }
    }

    // --- Rendering ---

    private fun renderFiles() {
        val container = filesContainer ?: return
        container.removeAllViews()

        val atMax = isMultiple && selectedFiles.size >= effectiveMaxFileCount()

        if (selectedFiles.isEmpty()) {
            // CARD communicates the empty state in its own prompt; others show the hint line.
            emptyView?.visibility =
                if (appearance is FileElementAppearance.CARD) View.GONE else View.VISIBLE
        } else {
            emptyView?.visibility = View.GONE
            selectedFiles.forEachIndexed { index, file -> container.addView(buildFileRow(index, file)) }
        }

        // Trigger label + enabled state (ICON has no text; it only toggles enabled).
        val enabled = !atMax
        setTriggerLabel(triggerText())
        setTriggerEnabled(enabled)
    }

    private fun triggerText(): String {
        val hasFiles = selectedFiles.isNotEmpty()
        return when (appearance) {
            is FileElementAppearance.CARD ->
                if (!hasFiles) (if (isMultiple) "Tap to choose files" else "Tap to choose file")
                else if (isMultiple) "Tap to add files" else "Tap to replace file"
            else ->
                if (!hasFiles) (if (isMultiple) "Choose files" else "Choose file")
                else if (isMultiple) "Add files" else "Replace file"
        }
    }

    private fun setTriggerLabel(text: String) {
        triggerButton?.text = text
        cardView?.text = text
        // iconButton: no text label, by design.
    }

    private fun setTriggerEnabled(enabled: Boolean) {
        triggerButton?.isEnabled = enabled
        iconButton?.isEnabled = enabled
        cardView?.apply {
            isEnabled = enabled
            isClickable = enabled
            alpha = if (enabled) 1f else 0.5f
        }
    }

    private fun buildFileRow(index: Int, file: SelectedFile): View {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(4), 0, dp(4))
        }
        val info = TextView(context).apply {
            text = "${file.name}  ·  ${formatSize(file.sizeBytes)}"
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.MIDDLE
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val remove = Button(context).apply {
            text = "✕"
            isAllCaps = false
            contentDescription = "Remove ${file.name}"
            minWidth = dp(44)
            minimumWidth = dp(44)
            setOnClickListener { removeFileAt(index) }
        }
        row.addView(info)
        row.addView(remove)
        return row
    }

    // --- Styling ---

    /** Default trigger background when no [FileElementInput.buttonStyles] is supplied. */
    private fun defaultBorder(dashed: Boolean): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(Color.TRANSPARENT)
        cornerRadius = dp(if (dashed) 10 else 6).toFloat()
        if (dashed) setStroke(dp(2), Color.parseColor("#9E9E9E"), dp(6).toFloat(), dp(4).toFloat())
        else setStroke(dp(1), Color.parseColor("#9E9E9E"))
    }

    /** Apply the host-supplied [Style] (buttonStyles.base) to a trigger view, matching [TextField]. */
    private fun applyButtonStyle(view: View, style: Style) {
        val dashed = appearance is FileElementAppearance.CARD
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(style.backgroundColor)
            cornerRadius = style.cornerRadius
            if (dashed) setStroke(style.borderWidth, style.borderColor, dp(6).toFloat(), dp(4).toFloat())
            else setStroke(style.borderWidth, style.borderColor)
        }
        view.background = bg
        val pad = style.padding
        view.setPadding(pad.left, pad.top, pad.right, pad.bottom)

        val lp = LinearLayout.LayoutParams(style.width, style.height)
        val m = style.margin
        lp.setMargins(m.left, m.top, m.right, m.bottom)
        view.layoutParams = lp

        if (view is TextView) {
            view.setTextColor(style.textColor)
            view.gravity = style.textAlignment
            if (style.font != Typeface.NORMAL) {
                try { view.typeface = ResourcesCompat.getFont(context, style.font) } catch (_: Exception) {}
            }
        }
    }

    private fun formatSize(bytes: Long): String {
        if (bytes < 0) return "unknown"
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
        return String.format(Locale.US, "%.2f MB", kb / 1024.0)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    // --- Selection API (internal) ---

    /**
     * Replace the current selection (picker entry point for tests, and a direct "set" for hosts that
     * resolve files themselves). The live picker uses the append-aware path.
     */
    internal fun attachSelectedFiles(files: List<SelectedFile>) {
        selectedFiles.clear()
        selectedFiles.addAll(if (isMultiple) files else files.take(1))
        deleteUnreferencedCaptures()
        clearError()
        renderFiles()
        emitChange()
    }

    /** The current selection, for the upload path. Internal only. */
    internal fun getSelectedFiles(): List<SelectedFile> = selectedFiles.toList()

    /** Effective per-file byte cap: single is server-fixed at 32 MB; multi honours [maxFileSize]. */
    internal fun effectiveMaxFileSizeBytes(): Long =
        if (isMultiple) (fileOptions.maxFileSize ?: FileUploadValidator.SINGLE_FILE_MAX_BYTES)
        else FileUploadValidator.SINGLE_FILE_MAX_BYTES

    /** Effective max file count: single is always 1; multi honours [maxFileCount] (default 4). */
    internal fun effectiveMaxFileCount(): Int =
        if (isMultiple) (fileOptions.maxFileCount ?: FileUploadValidator.DEFAULT_MAX_FILE_COUNT)
        else 1

    internal fun setError(message: String) {
        errorView?.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun clearError() {
        errorView?.apply {
            text = ""
            visibility = View.GONE
        }
    }

    override fun getState(): JSONObject {
        // No bytes / Uri / file name — just enough for the host to drive UI.
        return JSONObject()
            .put("elementType", if (::fileInput.isInitialized) fileInput.type.name else "")
            .put("columnName", columnName)
            .put("isRequired", isRequired)
            .put("isFilled", selectedFiles.isNotEmpty())
            .put("fileCount", selectedFiles.size)
    }

    override fun validate(): SkyflowValidationError {
        // Lightweight, UI-facing check; the authoritative, throwing validation runs in uploadFiles.
        if (isRequired && selectedFiles.isEmpty()) {
            return "No file selected for '$columnName'"
        }
        return ""
    }

    /** Clear the selection and reset the element to its initial state (mirrors [TextField.unmount]). */
    fun unmount() {
        selectedFiles.clear()
        deleteUnreferencedCaptures()
        filesContainer?.removeAllViews()
        clearError()
        renderFiles()
        emitChange()
    }

    // --- Event listeners (parity with TextField.on and the skyflow-js file element) ---

    /**
     * Register a handler for an element event, mirroring [TextField.on] and the skyflow-js file
     * element's `on(...)`:
     *  - [EventName.CHANGE] — fired whenever the selection changes (pick / replace / remove / clear).
     *  - [EventName.READY]  — fired once, when the element is mounted (attached to the window). If the
     *    element is already mounted when the handler is registered, it fires immediately.
     *  - [EventName.FOCUS]  — fired when the built-in trigger gains focus.
     *  - [EventName.BLUR]   — fired when the built-in trigger loses focus.
     *
     * The handler receives the privacy-safe [getState] payload (never bytes, Uri or file name). As
     * with [FileElementAppearance.CUSTOM] there is no built-in trigger, FOCUS/BLUR do not fire for it.
     * [EventName.SUBMIT] is not supported for file elements.
     */
    fun on(eventName: EventName, handler: (state: JSONObject) -> Unit) {
        when (eventName) {
            EventName.CHANGE -> userOnChangeListener = handler
            EventName.READY -> {
                userOnReadyListener = handler
                maybeEmitReady()
            }
            EventName.FOCUS -> userOnFocusListener = handler
            EventName.BLUR -> userOnBlurListener = handler
            EventName.SUBMIT ->
                Logger.error(tag, "SUBMIT event is not supported for file elements.", logLevel)
        }
    }

    private fun emitChange() {
        userOnChangeListener?.invoke(getState())
    }

    /** Wire FOCUS/BLUR on a built-in trigger view so the host can observe focus changes. */
    private fun wireFocusEvents(view: View) {
        view.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) userOnFocusListener?.invoke(getState())
            else userOnBlurListener?.invoke(getState())
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachedToWindow = true
        maybeEmitReady()
    }

    /** Emit READY once, as soon as the element is both mounted and has a READY handler registered. */
    private fun maybeEmitReady() {
        if (attachedToWindow && !readyEmitted && userOnReadyListener != null) {
            readyEmitted = true
            userOnReadyListener?.invoke(getState())
        }
    }
}
