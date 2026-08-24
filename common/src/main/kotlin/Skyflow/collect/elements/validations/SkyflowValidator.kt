package  com.Skyflow.collect.elements.validations

internal class SkyflowValidator {

    companion object {

        fun validate(input: String?, rules: ValidationSet) : SkyflowValidationError
        {
            val iterator : MutableIterator<ValidationRule> = rules.rules.iterator()
            while (iterator.hasNext())
            {
                val value = iterator.next()
                if(!value.validate(input))
                    return value.error
            }
            return ""
        }
    }
}
