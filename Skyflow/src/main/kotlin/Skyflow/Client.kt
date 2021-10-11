package Skyflow

import Skyflow.core.*
import Skyflow.core.Logger
import Skyflow.reveal.GetByIdRecord
import Skyflow.utils.Utils
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONArray
import org.json.JSONObject
import kotlin.Exception
import kotlin.reflect.KClass

class Client internal constructor(
    val configuration: Configuration
){
    internal val tag = Client::class.qualifiedName

    private val apiClient = APIClient(configuration.vaultID, configuration.vaultURL,
        configuration.tokenProvider,configuration.options.logLevel)

    fun insert(records:  JSONObject, options: InsertOptions? = InsertOptions(), callback: Callback){

            if(configuration.vaultURL.isEmpty() || configuration.vaultURL.equals("/v1/vaults/"))
            {
                val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                callback.onFailure(error)
            }
            else if(configuration.vaultID.isEmpty())
            {
                val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                callback.onFailure(error)
            }
            else {
                val isUrlValid = Utils.checkUrl(configuration.vaultURL)
                if (isUrlValid)
                {
                    Logger.info(tag, Messages.INSERTING_RECORDS.getMessage(configuration.vaultID), configuration.options.logLevel)
            apiClient.post(records,
                loggingCallback(callback,
                    Messages.INSERTING_RECORDS_SUCCESS.getMessage(configuration.vaultID),
                    Messages.INSERTING_RECORDS_FAILED.getMessage(configuration.vaultID)), options!!)
                }
                else {
                    val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL)
                    error.setErrorResponse(apiClient.vaultURL)
                    callback.onFailure(error)
                }
            }
    }

    fun <T:ContainerProtocol> container(type: KClass<T>) : Container<T> {
        return Container<T>(apiClient, configuration)
    }

    fun detokenize(records: JSONObject, callback: Callback) {
        if(configuration.vaultURL.isEmpty() || configuration.vaultURL.equals("/v1/vaults/"))
        {
            val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
            callback.onFailure(Utils.constructError(error))
        }
        else if(configuration.vaultID.isEmpty())
        {
            val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
            callback.onFailure(Utils.constructError(error))
        }
        else {
            val isUrlValid = Utils.checkUrl(configuration.vaultURL)
            if (isUrlValid)
            {
                this.apiClient.get(records,
                loggingCallback(callback, Messages.DETOKENIZE_SUCCESS.getMessage(),
                Messages.DETOKENIZING_FAILED.getMessage(configuration.vaultID)))
            }
            else {
                val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL)
                error.setErrorResponse(apiClient.vaultURL)
                callback.onFailure(Utils.constructError(error))
            }
        }

    }

    fun getById(records: JSONObject, callback: Callback)
    {
        Logger.info(tag, Messages.GET_BY_ID_CALLED.getMessage(), configuration.options.logLevel)
        val isUrlValid =Utils.checkUrl(configuration.vaultURL, configuration.options.logLevel, tag)
        if(isUrlValid) {
            Logger.info(tag, Messages.GETTING_RECORDS_BY_ID_CALLED.getMessage(), configuration.options.logLevel)
            try {
                val jsonArray = records.getJSONArray("records")
                var i = 0
                val result = mutableListOf<GetByIdRecord>()
                while (i < jsonArray.length()) {
                    val jsonObj = jsonArray.getJSONObject(i)
                    var skyflow_ids = jsonObj.get("ids")
        try {
            if(configuration.vaultURL.isEmpty() || configuration.vaultURL.equals("/v1/vaults/"))
            {
                val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                callback.onFailure(Utils.constructError(error))
            }
            else if(configuration.vaultID.isEmpty())
            {
                val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                callback.onFailure(Utils.constructError(error))
            }
            else {
                Logger.info(tag, Messages.GET_BY_ID_CALLED.getMessage(), configuration.options.logLevel)
                val isUrlValid = Utils.checkUrl(configuration.vaultURL)
                if (!records.has("records")) {
                    throw SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND)
                } else if (records.get("records").toString().isEmpty()) {
                    throw SkyflowError(SkyflowErrorCode.EMPTY_RECORDS)
                } else if (!(records.get("records") is JSONArray)) {
                    throw SkyflowError(SkyflowErrorCode.INVALID_RECORDS)
                } else if (isUrlValid) {
                         Logger.info(tag, Messages.GETTING_RECORDS_BY_ID_CALLED.getMessage(), configuration.options.logLevel)
                    try {
                        val jsonArray = records.getJSONArray("records")
                        var i = 0
                        val result = mutableListOf<GetByIdRecord>()
                        while (i < jsonArray.length()) {
                            val jsonObj = jsonArray.getJSONObject(i)
                            var skyflow_ids = jsonObj.get("ids")
                            if (!jsonObj.has("table")) {
                                throw SkyflowError(SkyflowErrorCode.TABLE_KEY_ERROR)
                            } else if (jsonObj.get("table") !is String)
                                throw SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME)
                            else if (!jsonObj.has("redaction")) {
                                throw SkyflowError(SkyflowErrorCode.REDACTION_KEY_ERROR)
                            } else if (!jsonObj.has("ids")) {
                                throw SkyflowError(SkyflowErrorCode.MISSING_IDS)
                            } else if (jsonObj.getString("table").isEmpty()) {
                                throw SkyflowError(SkyflowErrorCode.EMPTY_TABLE_NAME)
                            } else if (jsonObj.getString("redaction").isEmpty()) {
                                throw SkyflowError(SkyflowErrorCode.MISSING_REDACTION_VALUE)
                            } else if (!(jsonObj.get("redaction").toString()
                                    .equals("PLAIN_TEXT") || jsonObj.get("redaction").toString()
                                    .equals("DEFAULT") ||
                                        jsonObj.get("redaction").toString()
                                            .equals("MASKED") || jsonObj.get("redaction").toString()
                                    .equals("REDACTED"))
                            ) {
                                throw SkyflowError(SkyflowErrorCode.INVALID_REDACTION_TYPE)
                            } else {
                                skyflow_ids = skyflow_ids as ArrayList<String>
                                if (skyflow_ids.isEmpty()) {
                                    throw SkyflowError(SkyflowErrorCode.EMPTY_RECORD_IDS)
                                }
                                for (j in 0 until skyflow_ids.size) {
                                    if (skyflow_ids.get(j).isEmpty())
                                        throw SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID)
                                }
                                val record = GetByIdRecord(skyflow_ids,
                                    jsonObj.get("table").toString(),
                                    jsonObj.get("redaction").toString())
                                result.add(record)
                            }
                            i++
                        }
                        this.apiClient.get(result, callback)
                    } catch (e: Exception) {
                        throw e
                    }
                } else {
                    val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL)
                    error.setErrorResponse(apiClient.vaultURL)
                    throw error
                }
            }
        }
        catch (e:Exception)
        {
            callback.onFailure(Utils.constructError(e))
        }

    }
    fun invokeGateway(gatewayConfig:GatewayConfiguration,callback: Callback)
    {
<<<<<<< HEAD
        Logger.info(tag, Messages.INVOKE_GATEWAY_CALLED.getMessage(), configuration.options.logLevel)
        val checkUrl = Utils.checkUrl(gatewayConfig.gatewayURL, configuration.options.logLevel, tag)
        if(checkUrl)
            this.apiClient.invokeGateway(gatewayConfig,callback)
        else
            callback.onFailure(Exception("Url is not valid/not secure"))
=======
            if(configuration.vaultURL.isEmpty() || configuration.vaultURL.equals("/v1/vaults/"))
            {
                val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                callback.onFailure(Utils.constructError(error))
            }
            else if(configuration.vaultID.isEmpty())
            {
                val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                callback.onFailure(Utils.constructError(error))
            }
            else {
                val checkUrl = Utils.checkUrl(gatewayConfig.gatewayURL)
                if (checkUrl)
                    this.apiClient.invokeGateway(gatewayConfig, callback)
                else {
                    val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL)
                    error.setErrorResponse(apiClient.vaultURL)
                    callback.onFailure(Utils.constructError(error))

                }
            }
>>>>>>> standardized errors
    }

    inner class loggingCallback(
        private val clientCallback: Callback,
        private val successMessage: String,
        val failureMessage: String) : Callback {
        override fun onSuccess(responseBody: Any) {
            Logger.info(tag, successMessage, configuration.options.logLevel)
            clientCallback.onSuccess(responseBody)
        }

        override fun onFailure(exception: Exception) {
            Logger.error(tag, failureMessage, configuration.options.logLevel)
            clientCallback.onFailure(exception)
        }

    }
}