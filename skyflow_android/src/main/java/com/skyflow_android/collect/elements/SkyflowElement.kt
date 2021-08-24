package com.skyflow_android.collect.elements

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.skyflow_android.collect.elements.validations.SkyflowValidationError
import com.skyflow_android.core.elements.state.State

open class SkyflowElement @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr)  {

    internal var isRequired: Boolean = false
    internal var columnName: String  =""
    internal lateinit var tableName: String
    internal lateinit var collectInput : CollectElementInput
    private lateinit var options : CollectElementOptions
    internal lateinit var fieldType: SkyflowElementType

    /// Describes `SkyflowElement` input   State`
    internal open var state: State = State(columnName,isRequired)


    internal open fun getState() : HashMap<String,Any>
    {
        return state.getState()
    }
    /// Field Configuration
    internal open fun setupField(collectInput:CollectElementInput,options: CollectElementOptions) {
        this.collectInput = collectInput
        this.options = options
        tableName = this.collectInput.table
        columnName = this.collectInput.column
        fieldType = this.collectInput.type
        isRequired = this.options.required
        state = State(columnName,isRequired)

    }

    internal open fun getOutput() : String {
        return ""
    }
    internal open fun validate() : MutableList<SkyflowValidationError> {
        return mutableListOf()
    }

}