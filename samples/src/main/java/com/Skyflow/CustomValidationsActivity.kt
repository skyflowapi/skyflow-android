/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package com.Skyflow

import Skyflow.*
import Skyflow.collect.elements.validations.ElementValueMatchRule
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import com.Skyflow.collect.elements.validations.LengthMatchRule
import com.Skyflow.collect.elements.validations.RegexMatchRule
import com.Skyflow.collect.elements.validations.ValidationSet
import kotlinx.android.synthetic.main.activity_custom_validations.*

class CustomValidationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_validations)
        val skyflowConfiguration = Skyflow.Configuration(
            "VAULT_ID",
            "VAULT_URL",
            CollectActivity.DemoTokenProvider(),
            Options(LogLevel.ERROR, Env.PROD)
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val collectContainer = skyflowClient.container(ContainerType.COLLECT)
        val padding = Skyflow.Padding(8, 8, 8, 8)
        val bstyle = Skyflow.Style(
            Color.parseColor("#403E6B"),
            10f,
            padding,
            null,
            R.font.roboto_light,
            Gravity.START,
            Color.parseColor("#403E6B")
        )
        val cstyle = Skyflow.Style(
            Color.GREEN,
            10f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.END,
            Color.GREEN
        )
        val fstyle = Skyflow.Style(
            Color.parseColor("#403E6B"),
            10f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.START,
            Color.GREEN
        )
        val estyle = Skyflow.Style(
            Color.YELLOW,
            10f,
            padding,
            4,
            R.font.roboto_light,
            Gravity.CENTER,
            Color.YELLOW
        )
        val istyle =
            Skyflow.Style(Color.RED, 15f, padding, 6, R.font.roboto_light, Gravity.START, Color.RED)
        val styles = Skyflow.Styles(bstyle, null, estyle, fstyle, istyle)
        var labelStyles = Styles(bstyle, null, estyle, fstyle, istyle)
        var baseErrorStyles =
            Style(null, null, padding, null, R.font.roboto_light, Gravity.START, Color.RED)
        val errorStyles = Styles(baseErrorStyles)

        val nameValidationSet = ValidationSet()
        nameValidationSet.add(RegexMatchRule("[A-Za-z]+","name should have alphabets"))
        nameValidationSet.add((LengthMatchRule(5,20,"length should be between 5 and 20")))
        val nameInput = Skyflow.CollectElementInput(
            "cards", "name", SkyflowElementType.INPUT_FIELD,
            styles, labelStyles, errorStyles, label = "full name", placeholder = "enter full name",validations = nameValidationSet
        )
        val nameWithCustomValidation = collectContainer.create(this, nameInput)

        //pin and confirm pin
        val pinInput = Skyflow.CollectElementInput(
            "cards",
            "pin",
            Skyflow.SkyflowElementType.PIN,
            styles,
            labelStyles,
            errorStyles,
            "pin" ,
            "enter pin",
        )
        val pin = collectContainer.create(this, pinInput)
        val pinValidationSet = ValidationSet()
        pinValidationSet.add(ElementValueMatchRule(pin,"value not matched"))
        val confirmPinInput = Skyflow.CollectElementInput(
            "cards",
            "confim_pin",
            SkyflowElementType.PIN,
            styles,
            labelStyles,
            errorStyles,
            "confirm pin",
            "enter confirm pin",validations = pinValidationSet
        )
        val confirmPin = collectContainer.create(this, confirmPinInput)
        //end


        val parent = findViewById<LinearLayout>(R.id.parent)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(20, -20, 20, 0)
        nameWithCustomValidation.layoutParams = lp
        pin.layoutParams = lp
        confirmPin.layoutParams = lp

        parent.addView(nameWithCustomValidation)
        parent.addView(pin)
        parent.addView(confirmPin)


    }
}