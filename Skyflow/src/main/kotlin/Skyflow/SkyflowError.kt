package Skyflow

import Skyflow.core.LogLevel
import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.utils.Utils



class SkyflowError(val skyflowErrorCode: SkyflowErrorCode = SkyflowErrorCode.UNKNOWN_ERROR, val tag : String? = "", logLevel: LogLevel = LogLevel.PROD, params: Array<String?> = arrayOf()) : Exception(skyflowErrorCode.getMessage()) {

    override var message = ""
    private var code = skyflowErrorCode.getCode()

    init {
        this.message =  Utils.constructMessage(skyflowErrorCode.getMessage(), *params)
        Logger.error(tag, message, logLevel)
    }


    fun setErrorCode(code:Int)
    {
        this.code = code
    }
    fun getErrorcode(): Int
    {
        return this.code
    }

    fun setErrorMessage(message:String)
    {
        this.message = message
    }

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

