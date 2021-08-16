package com.skyflowandroid.collect.elements

import android.content.Context
import android.text.*
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import com.skyflow_android.R
import com.skyflowandroid.collect.elements.utils.CardType
import com.skyflowandroid.collect.elements.utils.SpaceSpan


class CardNumber  : SkyflowInputField {
    private var mContext: Context? = null
    private var attrs: AttributeSet? = null
    private var styleAttr = 0

    var cardType : CardType? =null
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
        setInputType(InputType.TYPE_CLASS_NUMBER)
        addTextChangedListener(this)
        setErrorMessage(context.getString(R.string.invalid_card_number))

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        updateCardType()
        addSpans(s!!, cardType!!.getSpaceIndices())

        if(selectionStart == cardType!!.maxCardLength && isValid())
            focusNextView()

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    private fun addSpans(editable: Editable, spaceIndices: IntArray) {
        val length = editable.length
        for (index in spaceIndices) {
            if (index <= length) {
                editable.setSpan(
                    SpaceSpan(), index - 1, index,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    @JvmName("getCardType1")
    fun getCardType(): CardType {
        return cardType!!
    }
    fun updateCardType()
    {
        var type = CardType.forCardNumber(text.toString())
        if(cardType!=type)
        {
            cardType =type

            val filters = arrayOf<InputFilter>(LengthFilter(cardType!!.maxCardLength))
            setFilters(filters)

        }

    }

    override fun isValid(): Boolean {
        return !isRequired() ||  cardType!!.validate(text.toString())
    }



}