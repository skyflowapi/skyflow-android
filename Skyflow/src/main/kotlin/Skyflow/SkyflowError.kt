package Skyflow

import Skyflow.utils.Utils



class SkyflowError(val skyflowErrorCode: SkyflowErrorCode = SkyflowErrorCode.UNKNOWN_ERROR) : Exception(skyflowErrorCode.getMessage()) {

    override var message = skyflowErrorCode.getMessage()
    private var code = skyflowErrorCode.getCode()

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

    fun setErrorResponse(vararg params: String?)
    {
        if(!params.isEmpty())
           message =  Utils.constructMessage(this.skyflowErrorCode.message,*params)
    }
}

