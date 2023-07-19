package  Skyflow

@Description("Contains different styles to apply on a skyflow element.")
public class Styles(
    @Description("Styles applied on skyflow elements in its base form.")
    base: Style? = Style(),
    @Description("Styles applied when value is valid.")
    complete: Style? = Style(),
    @Description("Styles applied when skyflow element is empty.")
    empty: Style? =  Style(),
    @Description("Styles applied when skyflow element is focused.")
    focus: Style? = Style(),
    @Description("Styles applied when value is invalid.")
    invalid: Style? = Style()
) {
    var base = base ?: Style()
    var complete = complete ?: Style()
    var empty = empty ?: Style()
    var focus = focus ?: Style()
    var invalid = invalid ?: Style()
}