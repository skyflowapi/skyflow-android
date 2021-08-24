package  com.skyflow_android.collect.elements.validations


class SkyflowValidationSet {

    internal var rules = mutableListOf<SkyflowValidationProtocol>()


     constructor(rules: MutableList<SkyflowValidationProtocol>) {

        this.rules = rules
    }
    constructor(){}

    /// Add validation rule
    fun add(rule: SkyflowValidationProtocol) {
        rules.add(rule)
    }
}

