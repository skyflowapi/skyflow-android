package com.skyflowandroid.reveal.elements

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import com.skyflow_android.R

@SuppressLint("AppCompatCustomView")
class SkyflowOutputField: TextView {


    private var mContext: Context? = null
    private var attrs: AttributeSet? = null
    private var styleAttr = 0
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

    private fun initView()
    {
        setTextColor(Color.parseColor("#2a2a2a"))
        val attributes = mContext!!.obtainStyledAttributes(
                attrs, R.styleable.SkyflowOutputField,
                styleAttr, 0
        )
        val fieldname_from_xml = attributes.getString(R.styleable.SkyflowOutputField_fieldname)
         if(fieldname_from_xml != null)
            fieldname =fieldname_from_xml


    }


    fun setFieldName(fieldname: String)
    {
        this.fieldname = fieldname
    }
    fun getFieldname(): String
    {
        return fieldname.toString()
    }


}