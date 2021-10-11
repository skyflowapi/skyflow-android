package Skyflow

import Skyflow.core.*
import Skyflow.core.Logger
import Skyflow.reveal.GetByIdRecord
import Skyflow.utils.Utils
import com.Skyflow.core.container.ContainerProtocol
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
        Logger.info(tag, Messages.INSERT_CALLED.getMessage(), configuration.options.logLevel)
        val isUrlValid = Utils.checkUrl(configuration.vaultURL, configuration.options.logLevel, tag)
        if(isUrlValid) {
            Logger.info(tag, Messages.INSERTING_RECORDS.getMessage(configuration.vaultID), configuration.options.logLevel)
            apiClient.post(records,
                loggingCallback(callback,
                    Messages.INSERTING_RECORDS_SUCCESS.getMessage(configuration.vaultID),
                    Messages.INSERTING_RECORDS_FAILED.getMessage(configuration.vaultID)), options!!)
        }
        else
            callback.onFailure(Exception("Url is not valid/not secure"))
    }

    fun <T:ContainerProtocol> container(type: KClass<T>) : Container<T> {
        return Container<T>(apiClient, configuration)
    }

    fun detokenize(records: JSONObject, callback: Callback) {
        Logger.info(tag, Messages.DETOKENIZE_CALLED.getMessage(), configuration.options.logLevel)
        val isUrlValid =Utils.checkUrl(configuration.vaultURL, configuration.options.logLevel, tag)
        if(isUrlValid) {
            Logger.info(tag, Messages.DETOKENIZING_RECORDS.getMessage(configuration.vaultID), configuration.options.logLevel)
            this.apiClient.get(records,
                loggingCallback(callback, Messages.DETOKENIZE_SUCCESS.getMessage(),
                Messages.DETOKENIZING_FAILED.getMessage(configuration.vaultID)))
        }
        else
            callback.onFailure(Exception("Url is not valid/not secure"))
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
                        skyflow_ids = skyflow_ids as ArrayList<String>
                    } catch (e: Exception) {
                        callback.onFailure(Exception("skyflow_ids is not an ArrayList"))
                        return
                    }
                    val record = GetByIdRecord(skyflow_ids,
                        jsonObj.get("table").toString(),
                        jsonObj.get("redaction").toString())
                    result.add(record)
                    i++
                }
                this.apiClient.get(result, callback)
            } catch (e: Exception) {
                callback.onFailure(e)
            }
        }
        else
            callback.onFailure(Exception("Url is not valid/not secure"))

    }
    fun invokeGateway(gatewayConfig:GatewayConfiguration,callback: Callback)
    {
        Logger.info(tag, Messages.INVOKE_GATEWAY_CALLED.getMessage(), configuration.options.logLevel)
        val checkUrl = Utils.checkUrl(gatewayConfig.gatewayURL, configuration.options.logLevel, tag)
        if(checkUrl)
            this.apiClient.invokeGateway(gatewayConfig,callback)
        else
            callback.onFailure(Exception("Url is not valid/not secure"))
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