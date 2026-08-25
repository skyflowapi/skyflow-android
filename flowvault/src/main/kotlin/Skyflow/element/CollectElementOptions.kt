package Skyflow

class CollectElementOptions(
    var required: Boolean = false,
    var enableCardIcon: Boolean = true,
    var format: String = "",
    var translation: HashMap<Char, String>? = null,
    val enableCopy: Boolean = false,
    var cardMetadata: CardMetadata = CardMetadata(arrayOf()),
    // v2-only: when true, a CVV element's token is replaced in the collect response with a fixed
    // mock value (see MockCVV). No-op for non-CVV element types. Default false = return real token.
    var returnMockValue: Boolean = false
) {
    internal var inputFormat: HashMap<Char, Regex> = hashMapOf()
    private val HYPHEN_CARD_NUMBER_FORMAT = "XXXX-XXXX-XXXX-XXXX"
    internal fun parseFormatForSeparator(): Char {
        return if (format == HYPHEN_CARD_NUMBER_FORMAT) '-'
        else ' '
    }

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
