package Skyflow

class RevealElementOptions(
    var formatRegex: String = "",
    var replaceText: String? = null,
    var format: String = "",
    var translation: HashMap<Char, String>? = null,
    val enableCopy: Boolean = true
) {
    internal var inputFormat: HashMap<Char, Regex> = hashMapOf()

    internal fun createRegexMap() {
        if (translation != null) {
            for (key in translation!!.keys) {
                val value = translation!![key] ?: return
                inputFormat[key] = if (value.isEmpty()) {
                    Regex("[\\s\\S]*")
                } else {
                    Regex(value)
                }
            }
        }
    }
}