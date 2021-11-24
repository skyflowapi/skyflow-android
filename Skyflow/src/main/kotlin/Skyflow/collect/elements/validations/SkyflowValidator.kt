package  com.Skyflow.collect.elements.validations

import Skyflow.collect.elements.validations.SkyflowInternalValidationProtocol

internal class SkyflowValidator {

    companion object {

        fun validate(input: String?, rules: ValidationSet) : SkyflowValidationError
        {
            val iterator : MutableIterator<ValidationRule> = rules.rules.iterator()
            while (iterator.hasNext())
            {
                val value = iterator.next()
                value as SkyflowInternalValidationProtocol
                if(!value.validate(input))
                    return value.error
            }
            return ""
        }
    }
}
