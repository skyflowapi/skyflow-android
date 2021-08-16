package com.skyflowandroid.collect.elements

import android.annotation.SuppressLint
import android.content.Context
import android.text.*
import android.util.AttributeSet
import com.skyflow_android.R
import com.skyflowandroid.collect.elements.utils.DateValidator
import com.skyflowandroid.collect.elements.utils.SlashSpan


class ExpireDate  : SkyflowInputField {
    private var mContext: Context? = null
    private var attrs: AttributeSet? = null
    private var styleAttr = 0

    private var zeroAtBegining:Boolean =false
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
        setInputType(InputType.TYPE_CLASS_DATETIME)
        addTextChangedListener(this)
        setErrorMessage(context.getString(R.string.invalid_expiredate))

    }


    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    @SuppressLint("SetTextI18n")
    override fun afterTextChanged(editable: Editable?) {
        if (zeroAtBegining) {
            if (editable!!.length == 1 && Character.getNumericValue(editable.get(0)) >= 2) {
                prependLeadingZero(editable)
            }

        }
        addSlash(editable!!)

        if(selectionStart == 7 && isValid())
            focusNextView()




    }

    private fun addSlash(editable: Editable) {
        val index = 2
        val length = editable.length
        if (index <= length) {
            editable.setSpan(
                SlashSpan(), index - 1, index,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun prependLeadingZero(editable: Editable) {
        val firstChar = editable[0]
        editable.replace(0, 1, "0").append(firstChar)
    }


    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        super.onTextChanged(text, start, before, count)
        zeroAtBegining = count > before

    }

    private fun getMonth() :String
    {
        val string: String = text.toString()
        return if (string.length < 2) {
            ""
        } else string.substring(0, 2)
    }

    private fun getYear() : String{
        val string: String = text.toString().replace("/", "")
        return if (string.length == 4 || string.length == 6) {
            string.substring(2)
        } else ""
    }

    override fun isValid(): Boolean {
        return !isRequired() ||  DateValidator().isValid(getMonth(), getYear())
    }

    fun setValue(month: String, year: String)
    {
        if(month.length==1)
            setText("0" + month + "/" + year)
        else
            setText(month + "/" + year)
    }
}

