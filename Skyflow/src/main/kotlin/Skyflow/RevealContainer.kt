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
        if(apiClient.vaultURL.isEmpty() || apiClient.vaultURL == "/v1/vaults/")
        {
            val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL, tag, configuration.options.logLevel)
            throw error
        }
        else if(apiClient.vaultId.isEmpty())
        {

            val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
            throw error
        }
        else {
            for (element in this.revealElements) {
                val token = element.revealInput.token
                if(!checkIfElementsMounted(element))
                {
                    val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, configuration.options.logLevel, arrayOf(element.revealInput.label))
                    throw error
                }
                if (element.isTokenNull) {
                    throw SkyflowError(SkyflowErrorCode.MISSING_TOKEN, tag, configuration.options.logLevel)
                }  else if (token!!.isEmpty()) {
                    throw SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID, tag, configuration.options.logLevel)
                }
                else if(element.isError)
                {
                    throw SkyflowError(SkyflowErrorCode.INVALID_INPUT, tag, configuration.options.logLevel,
                        arrayOf("${element.error.text}"))
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
                val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL, tag, configuration.options.logLevel, arrayOf(apiClient.vaultURL))
                throw error
            }
        }
    }
    catch (e: Exception)
    {
        callback.onFailure(Utils.constructError(e))
    }
}