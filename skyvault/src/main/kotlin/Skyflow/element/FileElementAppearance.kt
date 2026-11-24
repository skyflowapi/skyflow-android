package Skyflow

import androidx.annotation.DrawableRes

/**
 * How a [FileInputElement] draws its **trigger** — the control the user taps to open the picker.
 * The behaviour (picking, validation, the per-file remove ✕, errors, upload) is identical across all
 * five looks; only the trigger's appearance changes. Mirrors the iOS `FileElementAppearance` enum
 * (Swift enum-with-associated-value → Kotlin sealed class so [ICON] can carry a drawable).
 *
 * Set via [FileElementInput.appearance]; style the trigger with [FileElementInput.buttonStyles].
 *
 * - [FIELD]  — bordered box with a "Choose File" control; file rows below. The default, and the
 *              closest match to the browser file input skyflow-js renders.
 * - [BUTTON] — a full-width button; the selected files are listed below it.
 * - [ICON]   — a compact icon (optionally [ICON.icon]) with the selected file names beside it. For
 *              tight spaces (toolbars, chat input bars, the end of a form row).
 * - [CARD]   — a large dashed tap area; the whole area is the trigger.
 * - [CUSTOM] — the SDK draws **no** trigger, only the selected files, their ✕ and errors. The host
 *              app supplies its own button/icon and calls [FileInputElement.openPicker].
 */
sealed class FileElementAppearance {
    object FIELD : FileElementAppearance()
    object BUTTON : FileElementAppearance()
    data class ICON(@DrawableRes val icon: Int? = null) : FileElementAppearance()
    object CARD : FileElementAppearance()
    object CUSTOM : FileElementAppearance()
}
