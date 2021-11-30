package Skyflow

import Skyflow.core.Logger
import Skyflow.utils.Utils



class SkyflowError(val skyflowErrorCode: SkyflowErrorCode = SkyflowErrorCode.UNKNOWN_ERROR, val tag : String? = "", logLevel: LogLevel = LogLevel.ERROR, params: Array<String?> = arrayOf()) : Exception(skyflowErrorCode.getMessage()) {

    override var message = ""
    private var code = skyflowErrorCode.getCode()

    init {
        val logMessage =  Utils.constructMessage(skyflowErrorCode.getMessage(), *params)
        Logger.error(tag, logMessage, logLevel)
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

//    fun setErrorMessage(message:String)
//    {
//        this.message = message
//    }

    fun getErrorMessage() :String
    {
        return this.message
    }

//    fun setErrorResponse(vararg params: String?)
//    {
//        if(!params.isEmpty())
//           message =  Utils.constructMessage(this.skyflowErrorCode.message,*params)
//    }
}

