package Skyflow

/**
 * Element types for file collection. Kept **separate** from the shared [SkyflowElementType]
 * (which lives in `common/` and is visible to both SDKs) so that file upload stays a
 * `skyvault` (PDB / v1) feature and never leaks into `flowvault`. This mirrors the JS
 * (`BaseElementType` + `FileElementType`) and iOS split. When flowvault wires up FlowDB's
 * file upload API it can opt in to the same types from its own module.
 *
 * See the design doc: "File upload support in Android SDK".
 */
enum class FileElementType {
    /** A single-file picker. Upload via `container.uploadFiles(...)`. */
    FILE_INPUT,

    /** A multi-file picker (several files in one element). Upload via `container.uploadFiles(...)`. */
    MULTI_FILE_INPUT,
}
