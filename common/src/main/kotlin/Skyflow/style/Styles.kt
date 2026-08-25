package  Skyflow

import android.graphics.Color

class Styles(
    base: Style? = Style(),
    complete: Style? = Style(),
    empty: Style? = Style(),
    focus: Style? = Style(),
    invalid: Style? = Style(),
    requiredAsterisk: Style? = Style(textColor = Color.RED)
) {
    var base = base ?: Style()
    var complete = complete ?: Style()
    var empty = empty ?: Style()
    var focus = focus ?: Style()
    var invalid = invalid ?: Style()
    var requiredAsterisk = requiredAsterisk ?: Style()
}