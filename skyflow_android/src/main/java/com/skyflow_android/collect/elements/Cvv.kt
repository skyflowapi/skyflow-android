package com.skyflowandroid.collect.elements

import android.content.Context
import android.text.*
import android.util.AttributeSet
import android.widget.Toast
import com.skyflow_android.R
import com.skyflowandroid.collect.elements.utils.CardType

class Cvv : SkyflowInputField {
    private var mContext: Context? = null
    private var attrs: AttributeSet? = null
    private var styleAttr = 0


    private var cardType : CardType? =null
    private val DEFAULT_MAX_LENGTH: Int = 3
    var regex:String ? =null
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
        setErrorMessage(context.getString(R.string.invalid_cvv))
        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(DEFAULT_MAX_LENGTH))
        setFilters(filters)
        addTextChangedListener(this)

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        if(selectionStart == getSecurityCodeLength())
            focusNextView()
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }


    fun setCardType(cardType : CardType) {
        this.cardType = cardType;
        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(cardType.securityCodeLength))
        setFilters(filters);

        setContentDescription(getContext().getString(cardType.securityCodeName));
        if (getTextInputLayoutParent() != null) {
            getTextInputLayoutParent()!!.setHint(cardType.securityCodeName);
        } else {
            setHint(cardType.securityCodeName);
        }

        invalidate();
    }

    override fun getErrorMessage():String {
        var securityCodeName:String;
        if (cardType == null) {
            securityCodeName = getContext().getString(R.string.bt_cvv);
        } else {
            securityCodeName = getContext().getString(cardType!!.securityCodeName);
        }

        if (TextUtils.isEmpty(getText())) {
            return securityCodeName+" is required"
        } else {
            return securityCodeName + " is invalid"
        }
    }

    private fun getSecurityCodeLength() : Int{
        if (cardType == null) {
            return DEFAULT_MAX_LENGTH;
        } else {
            return cardType!!.securityCodeLength
        }
    }



    override fun isValid(): Boolean {
        return  !isRequired() ||  text.toString().length == getSecurityCodeLength()
    }

}