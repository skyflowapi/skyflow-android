/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package  Skyflow

public class Styles( base: Style? = Style(),
                     complete: Style? = Style(),
                     empty: Style? =  Style(),
                     focus: Style? = Style(),
                     invalid: Style? = Style()
) {
    var base = base ?: Style()
    var complete = complete ?: Style()
    var empty = empty ?: Style()
    var focus = focus ?: Style()
    var invalid = invalid ?: Style()
}