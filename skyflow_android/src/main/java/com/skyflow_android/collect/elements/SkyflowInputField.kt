package com.skyflowandroid.collect.elements

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.skyflow_android.R
import com.skyflowandroid.collect.elements.utils.VibrationHelper

open class SkyflowInputField : TextInputEditText , TextWatcher {


    private var mContext: Context? = null
    private var attrs: AttributeSet? = null
    private var styleAttr = 0

    private var mErrorAnimator: Animation? = null
    private var mError = false
    private var mRequired = false

    private var error_message:String ?= null
    private var fieldname: String ? =null

    constructor(context: Context?) : super(context!!) {
        this.mContext = context
        initView()
    }


    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        this.mContext=context;
        this.attrs=attrs;
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    ) {
        this.mContext=context;
        this.attrs=attrs;
        this.styleAttr = defStyle
        initView()
    }

    @SuppressLint("ResourceType")
    private fun initView()
    {

        isSingleLine = true
        mErrorAnimator = AnimationUtils.loadAnimation(context, R.anim.error_animation)
        mError = false
        mRequired =false
        val attributes = mContext!!.obtainStyledAttributes(
            attrs, R.styleable.SkyflowInputField,
            styleAttr, 0
        )

        var required_from_xml = attributes.getBoolean(R.styleable.SkyflowInputField_isrequired, false)
        var fieldname_from_xml = attributes.getString(R.styleable.SkyflowInputField_fieldname)
        var error_from_xml = attributes.getString(R.styleable.SkyflowInputField_error_message)
        if(required_from_xml!=null)
            setRequired(required_from_xml)
        if(error_from_xml!=null)
            error_message = error_from_xml
        if(fieldname_from_xml!=null)
            fieldname =fieldname_from_xml
    }


    fun setFieldName(fieldname: String)
    {
        this.fieldname = fieldname
    }
    @JvmName("getFieldname1")
    fun getFieldname(): String
    {
        return fieldname.toString()
    }



    fun isTextEmpty(): Boolean
    {
        return  text!!.trim().toString().isEmpty()
    }
    open fun isRequired(): Boolean {
        return mRequired
    }

    open fun setRequired(required: Boolean)  {
        mRequired = required

    }


    private fun setError(errorMessage: String?) {
        mError = !TextUtils.isEmpty(errorMessage)
        val textInputLayout: TextInputLayout? = getTextInputLayoutParent()
        if (textInputLayout != null) {
            textInputLayout.isErrorEnabled = !TextUtils.isEmpty(errorMessage)
            textInputLayout.error = errorMessage
        }
        if (mErrorAnimator != null && mError) {
            startAnimation(mErrorAnimator)
            VibrationHelper.vibrate(context, 10)
        }
    }


    fun setErrorMessage(error: String)
    {
        this.error_message = error
    }

    open fun getErrorMessage(): String? {
        return error_message
    }

    //override this method logic
    open fun isValid(): Boolean {
        return true
    }

    open fun getTextInputLayoutParent(): TextInputLayout? {
        return if (parent != null && parent.parent is TextInputLayout) {
            parent.parent as TextInputLayout
        } else null
    }


    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if(!isRequired())
        {
            return
        }
        else if(!focused && isTextEmpty())
            setError(context.getString(R.string.field_required))
        else if (!focused && !isValid()) {
            setError(getErrorMessage())
        }
        else
        {
            val textInputLayout: TextInputLayout? = getTextInputLayoutParent()
            if (textInputLayout != null) {
                textInputLayout.isErrorEnabled = false
            }
        }
    }

    @SuppressLint("WrongConstant")
    open fun focusNextView(): View? {
        if (imeActionId == EditorInfo.IME_ACTION_GO) {
            return null
        }
        val next: View?
        next = try {
            focusSearch(FOCUS_FORWARD)
        } catch (e: IllegalArgumentException) {
            focusSearch(FOCUS_DOWN)
        }
        return if (next != null && next.requestFocus()) {
            next
        } else null
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
    }


}