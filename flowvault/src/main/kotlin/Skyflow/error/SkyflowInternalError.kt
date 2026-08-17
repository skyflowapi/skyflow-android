package Skyflow

import Skyflow.core.Logger
import Skyflow.utils.Utils

/**
 * FlowVault (v2) internal exception. The shared `common/` code throws this; flowvault's typed
 * callbacks then convert it into the public [SkyflowError] **data class** before anything reaches
 * the app. It is `internal` because the v2 public surface exposes only that `SkyflowError` data
 * class — `SkyflowInternalError` is pure plumbing and never leaves the module.
 *
 * (In the legacy SDK this same name is instead a typealias onto the public v1 `SkyflowError`
 * exception — see that module's error/. Keeping the class here, per-product, is what lets each SDK
 * own its error types while `common` throws under one neutral name.)
 */
internal class SkyflowInternalError(val skyflowErrorCode: SkyflowErrorCode = SkyflowErrorCode.UNKNOWN_ERROR, val tag : String? = "", logLevel: LogLevel? = null, params: Array<String?> = arrayOf()) : Exception(skyflowErrorCode.getMessage()) {

    override var message = ""
    internal var internalMessage = ""
    private var code = skyflowErrorCode.getCode()
    var xmlBody:String = ""

    init {
        val logMessage =  Utils.constructMessage(skyflowErrorCode.getMessage(), *params)
        if(logLevel != null)
            Logger.error(tag, logMessage, logLevel)
        this.internalMessage = logMessage
        this.message = logMessage
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

    fun setXml(xml:String)
    {
        this.xmlBody = xml
    }

    fun getXml(): String {
        return xmlBody
    }

    override fun toString(): String {
        return this.message
    }
}
