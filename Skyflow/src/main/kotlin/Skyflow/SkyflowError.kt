package Skyflow

import Skyflow.core.Logger
import Skyflow.utils.Utils



class SkyflowError(val skyflowErrorCode: SkyflowErrorCode = SkyflowErrorCode.UNKNOWN_ERROR, val tag : String? = "", logLevel: LogLevel? = null, params: Array<String?> = arrayOf()) : Exception(skyflowErrorCode.getMessage()) {

    override var message = ""
    internal var internalMessage = ""
    private var code = skyflowErrorCode.getCode()

    init {
        val logMessage =  Utils.constructMessage(skyflowErrorCode.getMessage(), *params)
        if(logLevel != null)
            Logger.error(tag, logMessage, logLevel)
        this.internalMessage = logMessage
        this.message = "Interface : $tag - $logMessage"
    }
    fun setErrorCode(code:Int)
    {
        this.code = code
    }
    fun getErrorcode(): Int
    {
        return this.code
    }
    fun getErrorMessage() :String
    {
        return this.message
    }
    internal fun getInternalErrorMessage():String{
        return this.internalMessage
    }
}

