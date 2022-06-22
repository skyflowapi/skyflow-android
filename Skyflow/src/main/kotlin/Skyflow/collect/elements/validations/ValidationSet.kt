/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package  com.Skyflow.collect.elements.validations


 class ValidationSet {

    internal var rules = mutableListOf<ValidationRule>()


     constructor(rules: MutableList<ValidationRule>) {

        this.rules = rules
    }
    constructor(){}

    /// Add validation rule
    fun add(rule: ValidationRule) {
        rules.add(rule)
    }

    /// Add validation rules
    fun add(rule: MutableList<ValidationRule>) {
        rules.addAll(rule)
    }
}

