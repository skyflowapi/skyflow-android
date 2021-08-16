package com.skyflowandroid.collect.elements


import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.util.AttributeSet
import com.skyflow_android.R
import java.util.regex.Pattern

class CardHolderName : SkyflowInputField {

    private var mContext: Context? = null
    private var attrs: AttributeSet? = null
    private var styleAttr = 0

    private val regex_for_name = Pattern.compile("^[\\d\\s-]+$")

    constructor(context: Context?) : super(context) {
        this.mContext = context
        initView()
    }


    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        this.mContext=context;
        this.attrs=attrs;
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        this.mContext=context;
        this.attrs=attrs;
        this.styleAttr = defStyle
        initView()
    }

    private fun initView() {
        setInputType(InputType.TYPE_CLASS_TEXT)
        val filters = arrayOf<InputFilter>(LengthFilter(255))
        setFilters(filters)
        setErrorMessage(context.getString(R.string.invalid_card_holder_name))

    }

    override fun isValid(): Boolean {
        return !isRequired() || !regex_for_name.matcher(text.toString()).matches()
    }



    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
    }

}