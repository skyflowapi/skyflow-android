package  com.Skyflow.collect.elements.validations


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

    /// Add validation rules
    fun add(rule: MutableList<SkyflowValidationProtocol>) {
        rules.addAll(rule)
    }
}

