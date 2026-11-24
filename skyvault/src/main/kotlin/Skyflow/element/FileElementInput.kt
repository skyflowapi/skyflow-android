package Skyflow

/**
 * Describes a file-collection field. Separate from [CollectElementInput] (and the neutral
 * [BaseCollectElementInput]) because file elements use their own [FileElementType] rather than the
 * shared [SkyflowElementType] — this is what keeps file upload out of flowvault's public surface.
 *
 * The upload targets `tableName` / `column` in the vault. Pass [skyflowID] to attach the file(s) to
 * an existing record; omit it to create a new record on upload.
 *
 * @param table        vault table the file column belongs to.
 * @param column       file column to upload into.
 * @param type         [FileElementType.FILE_INPUT] (single) or [FileElementType.MULTI_FILE_INPUT].
 * @param label        optional label rendered above the picker control.
 * @param skyflowID    optional existing record id to attach the file(s) to.
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
    skyflowID: String? = null,
    val appearance: FileElementAppearance = FileElementAppearance.FIELD,
    val buttonStyles: Styles? = null,
) {
    // Public param names kept in v1 spelling (`table`, `skyflowID`); internal neutral storage
    // mirrors the Element fields (`tableName`, `skyflowId`).
    internal val tableName: String? = table
    internal val skyflowId: String? = skyflowID
}
