package  com.Skyflow.collect.elements.validations

/**
 * Contract for a custom validation rule attached to a collect element via [ValidationSet].
 * Implement this interface to plug in your own validation logic.
 */
interface ValidationRule {
     var error: SkyflowValidationError

     /**
      * Return `true` when [text] (the element's current value) is valid, `false` otherwise.
      * When it returns `false`, [error] is surfaced on the element. Follow the built-in rules'
      * convention of returning `true` for empty input, so emptiness is governed by the element's
      * `required` flag rather than by custom rules.
      */
     fun validate(text: String?): Boolean
}