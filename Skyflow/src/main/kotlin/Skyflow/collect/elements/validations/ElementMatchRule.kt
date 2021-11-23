package Skyflow.collect.elements.validations

import Skyflow.BaseElement
import Skyflow.Element
import com.Skyflow.collect.elements.validations.SkyflowValidationError
import com.Skyflow.collect.elements.validations.ValidationRule

class ElementMatchRule(var element:BaseElement, override var error: SkyflowValidationError = "ELEMENT NOT MATCHED"): ValidationRule,SkyflowInternalValidationProtocol {

    override fun validate(text: String?): Boolean {
        return element.getValue().equals(text)
    }
}