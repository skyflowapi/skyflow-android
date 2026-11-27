package Skyflow

import androidx.core.content.FileProvider

/**
 * The SDK's own [FileProvider], used only by the CAMERA file source to hand the camera app a
 * `content://` Uri in the SDK's cache to write the capture into.
 *
 * Declared as a dedicated subclass (rather than referencing `androidx.core.content.FileProvider`
 * straight from the manifest) so the SDK's `<provider>` is keyed by a **unique class name** during
 * manifest merge. A host app that declares its own `androidx.core.content.FileProvider` would
 * otherwise collide with the SDK's declaration — the merger matches providers by `android:name`, and
 * two with the same name but different authorities/path resources fail the build. A distinct class
 * keeps the two providers independent. No behaviour change: it inherits everything from FileProvider.
 */
internal class SkyflowFileProvider : FileProvider()
