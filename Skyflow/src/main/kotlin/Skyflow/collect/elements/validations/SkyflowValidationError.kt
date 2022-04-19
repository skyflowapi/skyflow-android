package  com.Skyflow.collect.elements.validations

/// skyflow.Skyflow Validation Error object type
 typealias SkyflowValidationError = String

/// Default validation error types
internal enum class SkyflowValidationErrorType(var rawValue:String) //: String {
{
    /// Default Validation error for `SkyflowValidateCardNumber`
    cardNumber("INVALID_CARD_NUMBER"),

    /// Default Validation error for `SkyflowValidateLength`
     length ("INVALID_LENGTH"),

    /// Default Validation error for `SkyflowValidatePattern`
     pattern("INVALID_PATTERN"),

    /// Default Validation error for `SkyflowValidateLengthMatch`
    lengthMathes("INVALID_LENGTH_MATCH"),

    /// Default Validation error for `SkyflowValidateExpireDate`
     expireDate("INVALID_EXPIRE_DATE"),

    invalidPin("INVALID_PIN"),

    invalidmonth("INVALID_MONTH"),

    invalidyear("INVALID_YEAR"),

    allowNumbers("ONLY_NUMBERS_ARE_ALLOWED");

}