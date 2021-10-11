package Skyflow

import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import android.content.Context
import com.Skyflow.core.container.ContainerProtocol
import Skyflow.reveal.RevealRequestBody
import Skyflow.reveal.RevealRequestRecord
import Skyflow.reveal.RevealValueCallback
import Skyflow.utils.Utils
import Skyflow.utils.Utils.Companion.checkIfElementsMounted
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class RevealContainer: ContainerProtocol
{
}

private val tag = RevealContainer::class.qualifiedName

fun Container<RevealContainer>.create(context: Context, input : RevealElementInput, options : RevealElementOptions = RevealElementOptions()) : Label
{
    Logger.info(tag, Messages.CREATED_REVEAL_ELEMENT.getMessage(input.label), configuration.options.logLevel)
    val revealElement = Label(context)
    revealElement.setupField(input,options)
    revealElements.add(revealElement)
    return revealElement
}

fun Container<RevealContainer>.reveal(callback: Callback, options: RevealOptions? = RevealOptions())
{
    try {
        if(apiClient.vaultURL.isEmpty() || apiClient.vaultURL.equals("/v1/vaults/"))
        {
            val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
            throw error
        }
        else if(apiClient.vaultId.isEmpty())
        {

            val finalError = JSONObject()
            val errors = JSONArray()
            val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
            errors.put(error)
            finalError.put("errors",errors)
            callback.onFailure(finalError)
        }
        else {
            for (element in this.revealElements) {
                val token = element.revealInput.token
                if (element.isTokenNull) {
                    throw SkyflowError(SkyflowErrorCode.MISSING_TOKEN)
                } else if (element.isRedactionNull) {
                    throw  SkyflowError(SkyflowErrorCode.MISSING_REDACTION)
                } else if (token!!.isEmpty()) {
                    throw SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID)
                }
                else if(!checkIfElementsMounted(element))
                {
                    val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED)
                    error.setErrorResponse(element.revealInput.label)
                    throw error
                }
            }
            val isUrlValid = Utils.checkUrl(apiClient.vaultURL)
            if (isUrlValid) {
                    Logger.info(tag, Messages.VALIDATE_REVEAL_RECORDS.getMessage(), configuration.options.logLevel)
                    val revealValueCallback = RevealValueCallback(callback, this.revealElements)
                    val records =
                        JSONObject(RevealRequestBody.createRequestBody(this.revealElements))
                    this.apiClient.get(records, revealValueCallback)

            } else {
                val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL)
                error.setErrorResponse(apiClient.vaultURL)
                throw error
            }
        }
    }
    catch (e: Exception)
    {
        callback.onFailure(Utils.constructError(e))
    }
}