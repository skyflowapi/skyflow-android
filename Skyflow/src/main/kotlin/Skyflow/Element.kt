package Skyflow

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.Skyflow.collect.elements.validations.SkyflowValidationError

open class Element @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr)  {

    internal var isRequired: Boolean = false
    internal var columnName: String  = ""
    internal var tableName: String = ""
    internal lateinit var collectInput : CollectElementInput
    private lateinit var options : Skyflow.CollectElementOptions
    internal lateinit var fieldType: SkyflowElementType

    /// Describes `SkyflowElement` input   State`
    internal open var state: State = State(columnName,isRequired)


    internal open fun getState() : HashMap<String,Any>
    {
        return state.getState()
    }
    /// Field Configuration
    internal open fun setupField(collectInput: CollectElementInput, options: Skyflow.CollectElementOptions) {
        this.collectInput = collectInput
        this.options = options
        if(!this.collectInput.table.equals(null))
            tableName = this.collectInput.table!!
        if(!this.collectInput.column.equals(null))
            columnName = this.collectInput.column!!
        fieldType = this.collectInput.type
        isRequired = this.options.required
        state = State(columnName,isRequired)

    }

    internal open fun validate() : MutableList<SkyflowValidationError> {
        return mutableListOf()
    }

     internal open fun getValue(): String {
        return ""
    }


}