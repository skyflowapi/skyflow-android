package  Skyflow

@Description("This is the description for Styles class")
public class Styles(
    @Description("Description for base param")
    base: Style? = Style(),
    @Description("Description for complete param")
    complete: Style? = Style(),
    @Description("Description for empty param")
    empty: Style? =  Style(),
    @Description("Description for focus param")
    focus: Style? = Style(),
    @Description("Description for invalid param")
    invalid: Style? = Style()
) {
    var base = base ?: Style()
    var complete = complete ?: Style()
    var empty = empty ?: Style()
    var focus = focus ?: Style()
    var invalid = invalid ?: Style()
}