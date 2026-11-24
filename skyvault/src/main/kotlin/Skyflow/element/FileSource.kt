package Skyflow

/**
 * Where a [FileInputElement] lets the user pick a file from. Passed via
 * [FileElementOptions.sources]. Android-only (there is no browser file input).
 *
 * - [FILES]  — Storage Access Framework document picker (`ACTION_OPEN_DOCUMENT`). No runtime
 *              permission required.
 * - [PHOTOS] — Android Photo Picker. No runtime permission required.
 * - [CAMERA] — capture a new photo (`ACTION_IMAGE_CAPTURE`). Opt-in: the host app must declare the
 *              `CAMERA` permission and a `FileProvider`. The element hides this source when the host
 *              is not configured for it, so the app never crashes. (Rollout phase 2.)
 */
enum class FileSource {
    FILES,
    PHOTOS,
    CAMERA,
}
