package Skyflow

import Skyflow.core.*
import Skyflow.core.Logger
import Skyflow.reveal.GetByIdRecord
import Skyflow.soap.SoapConnectionConfig
import Skyflow.utils.Utils
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONArray
import org.json.JSONObject
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.Exception
import kotlin.reflect.KClass

@Description("This class contains an instance of the Skyflow client.")
class Client internal constructor(
    @Description("The skyflow client initialization includes configuration options.")
    val configuration: Configuration,
){
    internal val tag = Client::class.qualifiedName

    internal val apiClient = APIClient(configuration.vaultID, configuration.vaultURL,
        configuration.tokenProvider,configuration.options.logLevel)

    internal val elementMap = HashMap<String,Any>()

    @Description("The function inserts data into the vault.")
    fun insert(
        @Description("Inserts records into the vault.")
        records:  JSONObject, 
        @Description("Additional options for insert method/request.")
        options: InsertOptions? = InsertOptions(),
        @Description("Implementation of Skyflow.Callback.")
        callback: Callback
    ){
        try {
            Utils.checkVaultDetails(configuration)
            Logger.info(tag, Messages.INSERTING_RECORDS.getMessage(configuration.vaultID), configuration.options.logLevel)
            apiClient.post(records, loggingCallback(callback, Messages.INSERTING_RECORDS_SUCCESS.getMessage(configuration.vaultID)), options!!)
        }
        catch (e:Exception)
        {
            callback.onFailure(e)
        }
    }

    @Description("This function retrieves record the data using tokens.")
    fun detokenize(
        @Description("This parameter takes a JSON object that contains tokens for fetching record values.")
        records: JSONObject,
        @Description("Implementation of Skyflow.Callback.")
        callback: Callback
    ) {
        try {
            Utils.checkVaultDetails(configuration)
            this.apiClient.get(records,loggingCallback(callback, Messages.DETOKENIZE_SUCCESS.getMessage()))
        }
        catch (e:Exception)
        {
            callback.onFailure(Utils.constructError(e))
        }
    }

    @Description("This function retrieves using SkyflowID's.")
    fun getById(
        @Description("This parameter takes a JSON object that contains records to fetch.")
        records: JSONObject,
        @Description("Implementation of Skyflow.Callback.")
        callback: Callback
    ){
        Logger.info(tag, Messages.GET_BY_ID_CALLED.getMessage(), configuration.options.logLevel)
        try {
            Utils.checkVaultDetails(configuration)
            Logger.info(tag, Messages.GET_BY_ID_CALLED.getMessage(), configuration.options.logLevel)
            val result = constructBodyForGetById(records)
            this.apiClient.getById(result, callback)
        } catch (e: Exception) {
            callback.onFailure(Utils.constructError(e))
        }
    }

    @Deprecated("Support for this method will be removed soon. Please use any of the Server Side SDKs to invoke a connection", level = DeprecationLevel.WARNING)
    internal fun invokeConnection(connectionConfig: ConnectionConfig, callback: Callback) {
        try {
            Logger.info(tag, Messages.INVOKE_CONNECTION_CALLED.getMessage(), configuration.options.logLevel)
            val checkUrl = Utils.checkUrl(connectionConfig.connectionURL)
            if(connectionConfig.connectionURL.isEmpty())
             throw SkyflowError(SkyflowErrorCode.EMPTY_CONNECTION_URL,tag, configuration.options.logLevel)
            if (!checkUrl)
                throw SkyflowError(SkyflowErrorCode.INVALID_CONNECTION_URL,tag, configuration.options.logLevel, arrayOf(connectionConfig.connectionURL))
            this.apiClient.invokeConnection(connectionConfig, callback,this)
        }
        catch (e:Exception)
        {
            callback.onFailure(Utils.constructError(e))
        }
    }

    @Deprecated("Support for this method will be removed soon. Please contact admin", level = DeprecationLevel.WARNING)
    internal fun invokeSoapConnection(soapConnectionConfig: SoapConnectionConfig,callback: Callback) {
        try {
            validateSoapConnectionDetails(soapConnectionConfig)
            this.apiClient.invokeSoapConnection(soapConnectionConfig, this, callback)
        }
        catch (e: Exception){
            callback.onFailure(e)
        }
    }
    internal fun validateSoapConnectionDetails(soapConnectionConfig: SoapConnectionConfig)
    {
        if (soapConnectionConfig.connectionURL.isEmpty()) {
            throw SkyflowError(SkyflowErrorCode.EMPTY_CONNECTION_URL, Utils.tag, configuration.options.logLevel)
        }
        if (!Utils.checkUrl(soapConnectionConfig.connectionURL)) {
            throw SkyflowError(SkyflowErrorCode.INVALID_CONNECTION_URL,
                Utils.tag, configuration.options.logLevel, params = arrayOf(soapConnectionConfig.connectionURL))
        }
        if (soapConnectionConfig.requestXML.trim().isEmpty()) {
            throw SkyflowError(SkyflowErrorCode.EMPTY_REQUEST_XML, Utils.tag, configuration.options.logLevel)
        }
        if (soapConnectionConfig.requestXML.trim().isNotEmpty()) {
            try {
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(soapConnectionConfig.requestXML)));
            } catch (e: Exception) {
                throw SkyflowError(SkyflowErrorCode.INVALID_REQUEST_XML, Utils.tag, configuration.options.logLevel, params = arrayOf(e.message.toString()))
            }
        }
        if (soapConnectionConfig.responseXML.trim().isNotEmpty()) {
            try {
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(soapConnectionConfig.responseXML)));
            } catch (e: Exception) {
                throw SkyflowError(SkyflowErrorCode.INVALID_RESPONSE_XML, Utils.tag, configuration.options.logLevel, params = arrayOf(e.message.toString()))
            }
        }
    }

    internal fun constructBodyForGetById(records: JSONObject): MutableList<GetByIdRecord> {
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
                val record = GetByIdRecord(skyflowIds, jsonObj.get("table").toString(), jsonObj.get("redaction").toString())
                result.add(record)
            }
            i++
        }
        return result
    }

    @Description("This method creates a skyflow container.")
    fun <T:ContainerProtocol> container(
        @Description("This parameter defines the type of the container.")
        type: KClass<T>
    ) : Container<T>{
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


