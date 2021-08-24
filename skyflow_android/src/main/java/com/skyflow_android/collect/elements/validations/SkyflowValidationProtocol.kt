package  com.skyflow_android.collect.elements.validations

abstract class SkyflowValidationProtocol {
    abstract var error: SkyflowValidationError
    abstract fun validate(text: String?) : Boolean
}