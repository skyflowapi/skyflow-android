package  com.Skyflow.collect.elements.validations

abstract class SkyflowValidationProtocol {
    abstract var error: SkyflowValidationError
    abstract fun validate(text: String?) : Boolean
}