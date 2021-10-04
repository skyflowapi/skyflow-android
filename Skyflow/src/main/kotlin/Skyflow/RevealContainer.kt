package Skyflow

import android.content.Context
import com.Skyflow.core.container.ContainerProtocol
import Skyflow.reveal.RevealRequestBody
import Skyflow.reveal.RevealRequestRecord
import Skyflow.reveal.RevealValueCallback
import Skyflow.utils.Utils
import Skyflow.utils.Utils.Companion.checkIfElementsMounted
import org.json.JSONObject

class RevealContainer: ContainerProtocol
{
}

fun Container<RevealContainer>.create(context: Context, input : RevealElementInput, options : RevealElementOptions = RevealElementOptions()) : Label
{
    val revealElement = Label(context)
    revealElement.setupField(input,options)
    revealElements.add(revealElement)
    return revealElement
}

fun Container<RevealContainer>.reveal(callback: Callback, options: RevealOptions? = RevealOptions())
{
    for(element in this.revealElements)
    {
        if(element.isTokenNull)
        {
            callback.onFailure(Exception("invalid token"))
            return
        }
        else if(element.isRedactionNull)
        {
            callback.onFailure(Exception("invalid redaction type"))
            return
        }
    }
    val isUrlValid = Utils.checkUrl(apiClient.vaultURL)
    if(isUrlValid) {
        val unMountedElements = checkIfElementsMounted(this.revealElements)
        if(unMountedElements == null) {
            val revealValueCallback = RevealValueCallback(callback, this.revealElements)
            val records = JSONObject(RevealRequestBody.createRequestBody(this.revealElements))
            this.apiClient.get(records, revealValueCallback)
        }
        else{
            callback.onFailure(Exception("Reveal Element with label ${unMountedElements.revealInput.label} is not attached to window"))
            return
        }
    }
    else
        callback.onFailure(Exception("Url is not valid/not secure"))
}