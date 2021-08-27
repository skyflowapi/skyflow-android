package Skyflow

import android.content.Context
import com.Skyflow.core.container.ContainerProtocol
import Skyflow.reveal.RevealRequestBody
import Skyflow.reveal.RevealValueCallback
import org.json.JSONObject

class RevealContainer: ContainerProtocol
{
}

fun Container<RevealContainer>.create(context: Context, input : RevealElementInput, options : RevealElementOptions) : Label
{
    val revealElement = Label(context)
    revealElement.setupField(input,options)
    revealElements.add(revealElement)
    return revealElement
}

fun Container<RevealContainer>.reveal(callback: Callback, options: RevealOptions? = RevealOptions())
{
    val revealValueCallback = RevealValueCallback(callback, this.revealElements)
    val records = RevealRequestBody.createRequestBody(this.revealElements)
    this.client.get(JSONObject(records), options,revealValueCallback)
}