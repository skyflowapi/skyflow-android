package Skyflow

import Skyflow.core.Logger
import Skyflow.utils.Utils

/**
 * v1 public error type — the exception thrown by the legacy SDK and caught by consumers.
 *
 * This is byte-for-byte the `SkyflowError` class shipped in 1.27.0, restored here so the legacy
 * artifact keeps the exact JVM class `Skyflow.SkyflowError` — preserving backward compatibility for
 * Kotlin, Java, and already-compiled (binary) consumers. `common` throws this via the neutral name
 * `SkyflowInternalError` (see SkyflowInternalError.kt in this module, a typealias onto this class);
 * in the FlowVault SDK that name is instead a separate internal exception class.
 */
class SkyflowError(val skyflowErrorCode: SkyflowErrorCode = SkyflowErrorCode.UNKNOWN_ERROR, val tag : String? = "", logLevel: LogLevel? = null, params: Array<String?> = arrayOf()) : Exception(skyflowErrorCode.getMessage()) {

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
