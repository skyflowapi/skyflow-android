package  com.skyflow_android.collect.elements.validations

import android.util.Log

internal class SkyflowValidator {

    companion object {

        fun validate(input: String?, rules: SkyflowValidationSet) : MutableList<SkyflowValidationError>
        {
            val errors  = mutableListOf<SkyflowValidationError>()
            val iterator : MutableIterator<SkyflowValidationProtocol> = rules.rules.iterator()
            while (iterator.hasNext())
            {
                val value = iterator.next()
                if(!value.validate(input))
                    errors.add(value.error)
            }


            return if(errors.isEmpty())
                mutableListOf()
            else
                errors
        }
    }
}
