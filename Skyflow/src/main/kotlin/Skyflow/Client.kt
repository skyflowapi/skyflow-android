package Skyflow

import Skyflow.core.*
import Skyflow.core.Logger
import Skyflow.reveal.GetByIdRecord
import Skyflow.soap.SoapConnectionConfig
import Skyflow.utils.Utils
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONArray
import org.json.JSONObject
import kotlin.Exception
import kotlin.reflect.KClass

class Client internal constructor(
    val configuration: Configuration,
){
    internal val tag = Client::class.qualifiedName

    internal val apiClient = APIClient(configuration.vaultID, configuration.vaultURL,
        configuration.tokenProvider,configuration.options.logLevel)

    internal val elementMap = HashMap<String,Any>()
    fun insert(records:  JSONObject, options: InsertOptions? = InsertOptions(), callback: Callback){

        if(configuration.vaultURL.isEmpty() || configuration.vaultURL == "/v1/vaults/")
        {
            val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL, tag, configuration.options.logLevel)
            callback.onFailure(error)
        }
        else if(configuration.vaultID.isEmpty())
        {
            val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID, tag, configuration.options.logLevel)
            callback.onFailure(error)
        }
        else {
            val isUrlValid = Utils.checkUrl(configuration.vaultURL)
            if (isUrlValid)
            {
                Logger.info(tag, Messages.INSERTING_RECORDS.getMessage(configuration.vaultID), configuration.options.logLevel)
                apiClient.post(records,
                    loggingCallback(
                        callback,
                        Messages.INSERTING_RECORDS_SUCCESS.getMessage(configuration.vaultID)
                    ), options!!)
            }
            else {
                val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL, tag, configuration.options.logLevel, arrayOf(apiClient.vaultURL))
                callback.onFailure(error)
            }
        }
    }

    fun detokenize(records: JSONObject, callback: Callback) {
        if(configuration.vaultURL.isEmpty() || configuration.vaultURL == "/v1/vaults/")
        {
            val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL, tag, configuration.options.logLevel)
            callback.onFailure(Utils.constructError(error))
        }
        else if(configuration.vaultID.isEmpty())
        {
            val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID, tag, configuration.options.logLevel)
            callback.onFailure(Utils.constructError(error))
        }
        else {
            val isUrlValid = Utils.checkUrl(configuration.vaultURL)
            if (isUrlValid)
            {
                this.apiClient.get(records,
                    loggingCallback(callback, Messages.DETOKENIZE_SUCCESS.getMessage()))
            }
            else {
                val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL, tag, configuration.options.logLevel, arrayOf(apiClient.vaultURL))
                callback.onFailure(Utils.constructError(error))
            }
        }

    }

    fun getById(records: JSONObject, callback: Callback)
    {
        Logger.info(tag, Messages.GET_BY_ID_CALLED.getMessage(), configuration.options.logLevel)
        try {
            if (configuration.vaultURL.isEmpty() || configuration.vaultURL == "/v1/vaults/") {
                val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL,tag, configuration.options.logLevel)
                callback.onFailure(Utils.constructError(error))
            } else if (configuration.vaultID.isEmpty()) {
                val error = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID,tag, configuration.options.logLevel)
                callback.onFailure(Utils.constructError(error))
            } else {
                Logger.info(tag,
                    Messages.GET_BY_ID_CALLED.getMessage(),
                    configuration.options.logLevel)
                val isUrlValid = Utils.checkUrl(configuration.vaultURL)
                if (isUrlValid) {
                    Logger.info(tag,
                        Messages.GETTING_RECORDS_BY_ID_CALLED.getMessage(),
                        configuration.options.logLevel)
                    try {
                        if(!records.has("records"))
                            throw SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND,tag, configuration.options.logLevel)
                        else if(records.get("records") !is JSONArray)
                            throw SkyflowError(SkyflowErrorCode.INVALID_RECORDS,tag, configuration.options.logLevel)
                        val jsonArray = records.getJSONArray("records")
                        if(jsonArray.length() == 0)
                            throw SkyflowError(SkyflowErrorCode.EMPTY_RECORDS,tag, configuration.options.logLevel)
                        var i = 0
                        val result = mutableListOf<GetByIdRecord>()
                        while (i < jsonArray.length()) {
                            val jsonObj = jsonArray.getJSONObject(i)
                            if(jsonObj == {})
                                throw SkyflowError(SkyflowErrorCode.EMPTY_RECORDS, tag, configuration.options.logLevel)
                            else if (!jsonObj.has("table")) {
                                throw SkyflowError(SkyflowErrorCode.MISSING_TABLE_KEY,tag, configuration.options.logLevel)
                            } else if (jsonObj.get("table") !is String)
                                throw SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME,tag, configuration.options.logLevel)
                            else if (!jsonObj.has("redaction")) {
                                throw SkyflowError(SkyflowErrorCode.REDACTION_KEY_ERROR,tag, configuration.options.logLevel)
                            } else if (!jsonObj.has("ids")) {
                                throw SkyflowError(SkyflowErrorCode.MISSING_IDS,tag, configuration.options.logLevel)
                            } else if (jsonObj.getString("table").isEmpty()) {
                                throw SkyflowError(SkyflowErrorCode.EMPTY_TABLE_KEY,tag, configuration.options.logLevel)
                            } else if (jsonObj.getString("redaction").isEmpty()) {
                                throw SkyflowError(SkyflowErrorCode.MISSING_REDACTION_VALUE,tag, configuration.options.logLevel)
                            } else if (!(jsonObj.get("redaction").toString()
                                    .equals("PLAIN_TEXT") || jsonObj.get("redaction")
                                    .toString()
                                    .equals("DEFAULT") ||
                                        jsonObj.get("redaction").toString()
                                            .equals("MASKED") || jsonObj.get("redaction")
                                    .toString()
                                    .equals("REDACTED"))
                            ) {
                                throw SkyflowError(SkyflowErrorCode.INVALID_REDACTION_TYPE,tag, configuration.options.logLevel)
                            } else {
                                var skyflowIds = jsonObj.get("ids")
                                try {
                                    skyflowIds = skyflowIds as ArrayList<String>
                                }
                                catch (e:Exception)
                                {
                                    throw SkyflowError(SkyflowErrorCode.INVALID_RECORD_IDS,tag, configuration.options.logLevel)
                                }
                                if (skyflowIds.isEmpty()) {
                                    throw SkyflowError(SkyflowErrorCode.EMPTY_RECORD_IDS,tag, configuration.options.logLevel)
                                }
                                for (j in 0 until skyflowIds.size) {
                                    if (skyflowIds.get(j).isEmpty())
                                        throw SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID,tag, configuration.options.logLevel)
                                }
                                val record = GetByIdRecord(skyflowIds,
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
                    val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL, tag, configuration.options.logLevel, arrayOf(apiClient.vaultURL))
                    throw error
                }
            }
        } catch (e: Exception) {
            callback.onFailure(Utils.constructError(e))
        }

    }

    fun invokeConnection(connectionConfig: ConnectionConfig, callback: Callback) {

        Logger.info(tag,
            Messages.INVOKE_CONNECTION_CALLED.getMessage(),
            configuration.options.logLevel)
        val checkUrl = Utils.checkUrl(connectionConfig.connectionURL)
        if(connectionConfig.connectionURL.isEmpty())
        {
            val error = SkyflowError(SkyflowErrorCode.EMPTY_CONNECTION_URL,tag, configuration.options.logLevel)
            callback.onFailure(Utils.constructError(error))
        }
        else if (checkUrl)
            this.apiClient.invokeConnection(connectionConfig, callback)
        else {
            val error = SkyflowError(SkyflowErrorCode.INVALID_CONNECTION_URL,tag, configuration.options.logLevel,
                arrayOf(connectionConfig.connectionURL))
            callback.onFailure(Utils.constructError(error))
        }
    }

    fun invokeSoapConnection(soapConnectionConfig: SoapConnectionConfig,callback: Callback)
    {
        if(soapConnectionConfig.connectionURL.isEmpty())
        {
            callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_CONNECTION_URL,tag,configuration.options.logLevel))
        }
        else if(!Utils.checkUrl(soapConnectionConfig.connectionURL))
        {
            callback.onFailure(SkyflowError(SkyflowErrorCode.INVALID_CONNECTION_URL,tag,configuration.options.logLevel, params = arrayOf(soapConnectionConfig.connectionURL)))
        }
        else if(soapConnectionConfig.requestXML.trim().isEmpty()){
            callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_REQUEST_XML,tag,configuration.options.logLevel))
        }
        else if(soapConnectionConfig.requestXML.trim().isNotEmpty() && !Utils.isValidXML(soapConnectionConfig.requestXML))
        {
            callback.onFailure(SkyflowError(SkyflowErrorCode.INVALID_REQUEST_XML,tag,configuration.options.logLevel))
        }
        else if(soapConnectionConfig.responseXML.trim().isNotEmpty() && !Utils.isValidXML(soapConnectionConfig.responseXML))
        {
            callback.onFailure(SkyflowError(SkyflowErrorCode.INVALID_RESPONSE_XML,tag,configuration.options.logLevel))
        }
        else {
            this.apiClient.invokeSoapConnection(soapConnectionConfig, this, callback)
        }
    }

    fun <T:ContainerProtocol> container(type: KClass<T>) : Container<T>{
        if(type == ContainerType.COLLECT){
            Logger.info(tag, Messages.COLLECT_CONTAINER_CREATED.getMessage(), configuration.options.logLevel)
        }
        else if(type == ContainerType.REVEAL){
            Logger.info(tag, Messages.REVEAL_CONTAINER_CREATED.getMessage(), configuration.options.logLevel)
        }
        return Container<T>(configuration,this)
    }

    inner class loggingCallback(
        private val clientCallback: Callback,
        private val successMessage: String,
    ) : Callback {
        override fun onSuccess(responseBody: Any) {
            Logger.info(tag, successMessage, configuration.options.logLevel)
            clientCallback.onSuccess(responseBody)
        }

        override fun onFailure(exception: Any) {
            clientCallback.onFailure(exception)
        }


    }
}



