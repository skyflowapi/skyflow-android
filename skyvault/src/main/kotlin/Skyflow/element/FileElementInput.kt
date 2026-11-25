package Skyflow

/**
 * Describes a file-collection field. Separate from [CollectElementInput] (and the neutral
 * [BaseCollectElementInput]) because file elements use their own [FileElementType] rather than the
 * shared [SkyflowElementType] — this is what keeps file upload out of flowvault's public surface.
 *
 * The upload targets `tableName` / `column` in the vault. For a single-file element
 * ([FileElementType.FILE_INPUT]) pass [skyflowId] to attach the file to an existing record; omit it
 * to create a new record. [skyflowId] is ignored for [FileElementType.MULTI_FILE_INPUT] (matching
 * skyflow-js): each file creates a new record, or — with upload metadata — uses the record inserted
 * for it.
 *
 * @param table        vault table the file column belongs to.
 * @param column       file column to upload into.
 * @param type         [FileElementType.FILE_INPUT] (single) or [FileElementType.MULTI_FILE_INPUT].
 * @param label        optional label rendered above the picker control.
 * @param skyflowId    optional existing record id to attach a single file to; ignored for
 *                     [FileElementType.MULTI_FILE_INPUT].
 * @param appearance   how the trigger looks ([FileElementAppearance]); default [FileElementAppearance.FIELD].
 *                     Use [FileElementAppearance.CUSTOM] to draw your own button and call
 *                     [FileInputElement.openPicker].
 * @param buttonStyles optional [Styles] for the trigger control (ignored for
 *                     [FileElementAppearance.CUSTOM], which draws no trigger). Only `base` is used.
 */
class FileElementInput(
    table: String? = null,
    val column: String? = null,
    val type: FileElementType = FileElementType.FILE_INPUT,
    val label: String = "",
    val skyflowId: String? = null,
    val appearance: FileElementAppearance = FileElementAppearance.FIELD,
    val buttonStyles: Styles? = null,
) {
    // `table` is exposed under the neutral internal name the Element base uses.
    internal val tableName: String? = table
}
