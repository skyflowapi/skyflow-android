package com.skyflow_android.collect.elements

import com.skyflow_android.collect.elements.core.styles.SkyflowStyles

class CollectElementInput(var table: String, var column: String,
                          var styles: SkyflowStyles? = SkyflowStyles(), var label: String? = "",
                          var placeholder: String? = "", var type: SkyflowElementType) {
}