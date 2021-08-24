package com.skyflow_android.reveal.client

import android.content.Context
import com.skyflow_android.core.protocol.SkyflowCallback
import com.skyflow_android.core.container.Container
import com.skyflow_android.core.container.ContainerProtocol
import com.skyflow_android.reveal.elements.SkyflowLabel

class RevealContainer: ContainerProtocol
{
}

fun Container<RevealContainer>.create(context: Context, input : RevealElementInput, options : RevealElementOptions) : SkyflowLabel
{
    val revealElement = SkyflowLabel(context)
    revealElement.setupField(input,options)
    revealElements.add(revealElement)
    return revealElement
}

fun Container<RevealContainer>.reveal(callback: SkyflowCallback, options: RevealOptions? = RevealOptions())
{
    val revealValueCallback = RevealValueCallback(callback, this.revealElements)
    val records = RevealRequestBody.createRequestBody(this.revealElements)
    this.skyflow.get(records, options,revealValueCallback)
}