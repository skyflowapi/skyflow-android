# skyflow-android
---
[![CI](https://img.shields.io/static/v1?label=CI&message=passing&color=green?style=plastic&logo=github)](https://github.com/skyflowapi/skyflow-android/actions)
[![GitHub release](https://img.shields.io/github/v/release/skyflowapi/skyflow-android.svg)](https://github.com/skyflowapi/skyflow-android/releases)
[![License](https://img.shields.io/github/license/skyflowapi/skyflow-android)](https://github.com/skyflowapi/skyflow-android/blob/main/LICENSE)

Skyflow’s android SDK can be used to securely collect, tokenize, and display sensitive data in the mobile without exposing your front-end infrastructure to sensitive data.

# Table of Contents
* [Installation](#installation)
  * [Requirements](#requirements)
  * [Configuration](#configuration)
* [Initializing Skyflow-android](#initializing-skyflow-android)
* [Securely collecting data client-side](#securely-collecting-data-client-side)
* [Securely collecting data client-side using composable elements](#securely-collecting-data-client-side-using-composable-elements)
* [Securely revealing data client-side](#securely-revealing-data-client-side)
* [Typed callbacks and response handling](#typed-callbacks-and-response-handling)
  * [Collect with typed callbacks](#collect-with-typed-callbacks)
  * [Reveal with typed callbacks](#reveal-with-typed-callbacks)

# Installation

## Requirements
- Android 5.0 (API level 21) and above
- compileSdk 35 and above
- Android Gradle Plugin 8.6.0 and above

## Configuration
### Step 1: Generate a Personal Access Token for GitHub
- Inside you GitHub account:
- Settings -> Developer Settings -> Personal Access Tokens -> Generate new token
- Make sure you select the following scopes (“read:packages”) and Generate a token
- After Generating make sure to copy your new personal access token. You cannot see it again! The only option is to generate a new key.

### Step 2: Store your GitHub — Personal Access Token details
- Create a github.properties file within your root Android project
- In case of a public repository make sure you add this file to .gitignore for keep the token private
- Add properties gpr.usr=GITHUB_USER_NAME and gpr.key=PERSONAL_ACCESS_TOKEN
- Replace GITHUB_USER_NAME with personal / organisation Github user NAME and PERSONAL_ACCESS_TOKEN with the token generated in [Step 1](#step-1-generate-a-personal-access-token-for-github)

Alternatively you can also add the GPR_USER_NAME and GPR_PAT values to your environment variables on you local machine or build server to avoid creating a github properties file

### Step 3: Adding the dependency to the project

#### Using gradle

- Add the Github package registry to your root project build.gradle file

  ```java
  def githubProperties = new Properties() 
  githubProperties.load(new FileInputStream(file(“github.properties”)))
  allprojects {
    repositories {
    ...
      maven {
          url "https://maven.pkg.github.com/skyflowapi/skyflow-android-sdk"
          credentials {
            username = githubProperties['gpr.usr'] ?: System.getenv("GPR_USER_NAME")
            password = githubProperties['gpr.key'] ?: System.getenv("GPR_PAT")
          }
      }
    }
    ...
  }
  ```

- Add the dependency to your application's build.gradle file

  ```java
  implementation 'com.skyflowapi.android:skyflow-android-sdk:1.27.0'
  ```

#### Using maven
- Add the Github package registry in the repositories tag and the GITHUB_USER_NAME, PERSONAL_ACCESS_TOKEN collected from  [Step1](#step-1-generate-a-personal-access-token-for-github) in the server tag to your project's settings.xml file. Make sure that the id's for both these tags are the same.

```xml
<repositories>
    <repository>
      <id>github</id>
      <url>https://maven.pkg.github.com/skyflowapi/skyflow-android-sdk</url>
    </repository>
</repositories>

<servers>
    <server>
      <id>github</id>
      <username>GITHUB_USER_NAME</username>
      <password>PERSONAL_ACCESS_TOKEN</password>
    </server>
</servers>
  ```

- Add the package dependencies to the dependencies element of your project pom.xml file
```xml
<dependency>
   <groupId>com.skyflowapi.android</groupId>
   <artifactId>skyflow-android-sdk</artifactId>
   <version>1.27.0</version>
</dependency>
```


# Initializing skyflow-android
----
Use the ```init()``` method to initialize a Skyflow client as shown below.
```kt
val demoTokenProvider = DemoTokenProvider() /*DemoTokenProvider is an implementation of
the Skyflow.TokenProvider interface*/

val config = Skyflow.Configuration(
    vaultID = <VAULT_ID>,
    vaultURL = <VAULT_URL>,
    tokenProvider = demoTokenProvider,
    options: Skyflow.Options(
      logLevel : Skyflow.LogLevel, // optional, if not specified loglevel is ERROR.
        env: SKyflow.Env //optiuona, if not specified env is PROD.
       ) 
)

val skyflowClient = Skyflow.init(config)
```
For the tokenProvider parameter, pass in an implementation of the Skyflow.TokenProvider interface that declares a getAccessToken method which retrieves a Skyflow bearer token from your backend. This function will be invoked when the SDK needs to insert or retrieve data from the vault.

For example, if the response of the consumer tokenAPI is in the below format

```
{
   "accessToken": string,
   "tokenType": string
}
```

then, your Skyflow.TokenProvider Implementation should be as below


```kt
class DemoTokenProvider: Skyflow.TokenProvider {
    override fun getBearerToken(callback: Callback) {
        val url = "http://10.0.2.2:8000/js/analystToken"
        val request = okhttp3.Request.Builder().url(url).build()
        val okHttpClient = OkHttpClient()
        try {
            val thread = Thread {
                run {
                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful)
                            throw IOException("Unexpected code $response")
                        val accessTokenObject = JSONObject(
                            response.body()!!.string().toString()
                            )
                        val accessToken = accessTokenObject["accessToken"]
                        callback.onSuccess("$accessToken")
                    }
                }
            }
            thread.start()
        }catch (exception:Exception){
            callback.onFailure(exception)
        }
    }
}
```

NOTE: You should pass access token as `String` value in the success callback of getBearerToken.

For `logLevel` parameter, there are 4 accepted values in Skyflow.LogLevel

- `DEBUG`
    
  When `Skyflow.LogLevel.DEBUG` is passed, all level of logs will be printed(DEBUG, INFO, WARN, ERROR).

- `INFO`

  When `Skyflow.LogLevel.INFO` is passed, INFO logs for every event that has occurred during the SDK flow execution will be printed along with WARN and ERROR logs.


- `WARN`

  When `Skyflow.LogLevel.WARN` is passed, WARN and ERROR logs will be printed.

- `ERROR`

  When `Skyflow.LogLevel.ERROR` is passed, only ERROR logs will be printed.

`Note`:
  - The ranking of logging levels is as follows :  DEBUG < INFO < WARN < ERROR
  - since `logLevel` is optional, by default the logLevel will be  `ERROR`.



For `env` parameter, there are 2 accepted values in Skyflow.Env

- `PROD`
- `DEV`

  In [Event Listeners](#event-listener-on-collect-elements), actual value of element can only be accessed inside the handler when the `env` is set to `DEV`.

`Note`:
  - since `env` is optional, by default the env will be  `PROD`.
  - Use `env` option with caution, make sure the env is set to `PROD` when using `skyflow-android` in production. 



---
# Securely collecting data client-side
-  [**Using Skyflow Elements to collect data**](#using-skyflow-elements-to-collect-data)
-  [**Using Skyflow Elements to update data**](#using-skyflow-elements-to-update-data)
-  [**Event Listener on Collect Elements**](#event-listener-on-collect-elements)
- [**UI Error for Collect Elements**](#ui-error-for-collect-elements)
-  [**Set and Clear value for Collect Elements (DEV ENV ONLY)**](#set-and-clear-value-for-collect-elements-dev-env-only)


## Using Skyflow Elements to collect data

**Skyflow Elements** provide developers with pre-built form elements to securely collect sensitive data client-side.  This reduces your PCI compliance scope by not exposing your front-end application to sensitive data. Follow the steps below to securely collect data with Skyflow Elements in your application.

### Step 1: Create a container

First create a **container** for the form elements using the ```skyflowClient.container(type: Skyflow.ContainerType)``` method as show below

```kt
val container = skyflowClient.container(Skyflow.ContainerType.COLLECT)
```

### Step 2: Create a collect Element

To create a collect element, we must first construct `Skyflow.CollectElementInput` object defined as shown below:

```kt
Skyflow.CollectElementInput(
   table : String,            //the table this data belongs to
   column : String,           //the column into which this data should be inserted
   type: Skyflow.ElementType   //Skyflow.ElementType enum
   inputStyles: Skyflow.Styles,     //optional styles that should be applied to the form element
   labelStyles: Skyflow.Styles, //optional styles that will be applied to the label of the collect element
   errorTextStyles: Skyflow.Styles,  //optional styles that will be applied to the errorText of the collect element
   label: String,            //optional label for the form element
   placeholder: String,      //optional placeholder for the form element
   altText: String,                 //(DEPRECATED) optional string that acts as an initial value for the collect element
   validations: ValidationSet // optional set of validations for collect element
)
```
The `table` and `column` parameters indicate which table and column in the vault the Element corresponds to.
Note: Use dot delimited strings to specify columns nested inside JSON fields (e.g. address.street.line1).

The `inputStyles` field accepts a Skyflow.Styles object which consists of multiple `Skyflow.Style` objects which should be applied to the form element in the following states:

- `base`: all other variants inherit from these styles
- `complete`: applied when the Element has valid input
- `empty`: applied when the Element has no input
- `focus`: applied when the Element has focus
- `invalid`: applied when the Element has invalid input

Each Style object accepts the following properties, please note that each property is optional:

```kotlin
Skyflow.Style(
  borderColor: Int            // optional
  cornerRadius: Float         // optional
  padding: Skyflow.Padding    // optional
  borderWidth: Int            // optional
  font:  Int                  // optional
  textAlignment: Int          // optional
  textColor: Int              // optional
  placeholderColor: Int       // optional
  width: Int                  // optional
  height: Int                 // optional
  margin: Skyflow.Margin      // optional
  backgroundColor: Int        // optional
  minWidth: Int               // optional
  maxWidth: Int               // optional
  minHeight: Int              // optional
  maxHeight: Int              // optional
)
```
Here `Skyflow.Padding` and `Skyflow.Margin` are classes which can be used to set the padding and margin respectively for the composable element which takes all the left, top, right, bottom values.

```kt
Skyflow.Padding(left: Int, top: Int, right: Int, bottom: Int)

Skyflow.Margin(left: Int, top: Int, right: Int, bottom: Int)
```

An example Skyflow.Styles object
```kotlin
val inputStyles = Skyflow.Styles(
  base = Skyflow.Style(),              // optional
  complete  = Skyflow.Style(),         // optional
  empty = Skyflow.Style(),             // optional
  focus = Skyflow.Style(),             // optional
  invalid = Skyflow.Style(),           // optional
  requiredAsterisk = Skyflow.Style()   // optional 
)
```

The `labelStyles` and `errorTextStyles` fields accept the above mentioned `Skyflow.Styles` object which are applied to the `label` and `errorText` text views respectively.

The states that are available for `labelStyles` are `base`, `focus` and `requiredAsterisk`.

`requiredAsterisk`: Styles applied for the Asterisk symbol in the label. Defaults to `red`.

The state that is available for `errorTextStyles` is only the `base` state, it shows up when there is some error in the collect element.

The parameters in `Skyflow.Style` object that are respected for `label` and `errorText` text views are
- padding
- font
- textColor
- textAlignment
- width
- height
- margin
- minWidth
- maxWidth
- minHeight
- maxHeight

Other parameters in the `Skyflow.Style` object are ignored for `label` and `errorText` text views.

Finally, the `type` field takes a Skyflow ElementType. Each type applies the appropriate regex and validations to the form element. There are currently 5 types:
- `INPUT_FIELD`
- `CARDHOLDER_NAME`
- `CARD_NUMBER`
- `EXPIRATION_DATE`
- `EXPIRATION_MONTH`
- `EXPIRATION_YEAR`
- `CVV`
- `PIN`

The `INPUT_FIELD` type is a custom UI element without any built-in validations. See the section on [`validations`](#validations) for more information on validations.

Along with `CollectElementInput` you can define other options in the `CollectElementOptions` object which is described below.

```kotlin
Skyflow.CollectElementOptions(
  required: Boolean, // Indicates whether the field is marked as required. Defaults to 'false'
  enableCardIcon: Boolean, // Indicates whether card icon should be enabled (only for CARD_NUMBER inputs)
  format: String, // Format for the element (currently applicable for "EXPIRATION_DATE", "CARD_NUMBER", "EXPIRATION_YEAR" and "INPUT_FIELD")
  translation: HashMap<Char, String> // Indicates the allowed data type value for format.
  enableCopy: Boolean, // Indicates whether to enable the copy icon in collect elements to copy text to clipboard. Defaults to 'false'
  cardMetadata: Skyflow.CardMetadata, // Optional, metadata to control card number element behavior. (only applicable for CARD_NUMBER ElementType).
)
```

- `required`: Indicates whether the field is marked as required or not. Default is `false`.

- `enableCardIcon`: Indicates whether the icon is visible for the CARD_NUMBER element. Default is `true`.

- `format`:  A string value that indicates the format pattern applicable to the element type. Only applicable to `EXPIRATION_DATE`, `CARD_NUMBER`, `EXPIRATION_YEAR` and `INPUT_FIELD` elements.
  - For `INPUT_FIELD` elements,
    -  the length of `format` determines the expected length of the user input.
    - if `translation` isn't specified, the `format` value is considered a string literal.

- `translation`: A hashmap of key/value pairs, where the key is a character that appears in `format` and the value is a regex pattern of acceptable inputs for that character. Each key can only appear once. Only applicable for `INPUT_FIELD` elements.

- `enableCopy`: Indicates whether to enable the copy icon in collect elements to copy text to clipboard.

- `cardMetadata`: An object of metadata keys to control card number element behavior. It supports an optional key called `scheme`, which accepts an array of Skyflow-supported card types and determines which brands display in the card number element's card brand choice dropdown. `Skyflow.CardType` is an enum with all Skyflow-supported card schemes.

```kotlin
class CardMetadata(var scheme: Array<CardType>) {}
```

#### Supported card types by Skyflow.CardType :
- `VISA`
- `MASTERCARD`
- `AMEX`
- `DINERS_CLUB`
- `DISCOVER`
- `JCB`
- `MAESTRO`
- `UNIONPAY`
- `HIPERCARD`
- `CARTES_BANCAIRES`

Accepted values by element type:

| Element type    | `format`                                                                                  | `translation`                                                         | Examples                                                                                                                                 |
| --------------- | ----------------------------------------------------------------------------------------- | --------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| EXPIRATION_DATE | <ul><li>`mm/yy`(default)</li><li>`mm/yyyy`</li> <li>`yy/mm`</li> <li> `yyyy/mm`</li></ul> | N/A                                                                   | <ul><li>12/27</li><li>12/2027</li> <li>27/12</li> <li> 2027/12</li></ul></ul>                                                            |
| EXPIRATION_YEAR | <ul><li>`yy`(default)</li><li>`yyyy`</li>                                                 | N/A                                                                   | <ul><li>27</li><li>2027</li> </ul>                                                                                                       |
| CARD_NUMBER     | <ul><li> `XXXX XXXX XXXX XXXX` (default)</li><li>`XXXX-XXXX-XXXX-XXXX`</li> </ul>         | N/A                                                                   | <ul><li>1234 5678 9012 3456</li><li>1234-5678-9012-3456</li> </ul>                                                                       |
| INPUT_FIELD     | A string that matches the desired output, with placeholder characters of your choice.     | A hashmap of key/value pairs. Defaults to `hashmapOf('X' to "[0-9]")` | With `format: +91 XXXX-XX-XXXX` and `translation: hashmapOf('X' to "[0-9]")`, user input of "1234121234" displays as "+91 1234-12-1234". |

Collect Element Options examples for INPUT_FIELD

Example 1
```kotlin
Skyflow.CollectElementOptions(
  required: true, 
  enableCardIcon: true,
  format: "+91 XXXX-XX-XXXX",
  translation: hashmapOf('X' to "[0-9]")
)
```
User input: "1234121234"

Value displayed in INPUT_FIELD: "+91 1234-12-1234"    

Example 2
```kotlin
Skyflow.CollectElementOptions(
  required: true, 
  enableCardIcon: true,
  format: "AY XX-XXX-XXXX",
  translation: hashmapOf('X' to "[0-9]", 'Y' to "[A-Z]")
)
```
User input: "B1234121234"

Value displayed in INPUT_FIELD: "AB 12-341-2123"

Once the `Skyflow.CollectElementInput` and `Skyflow.CollectElementOptions` objects are defined, add to the container using the ```create(context:Context,input: CollectElementInput, options: CollectElementOptions)``` method as shown below. The `input` param takes a `Skyflow.CollectElementInput` object as defined above and the `options` parameter takes a `Skyflow.CollectElementOptions`, 
the `context` param takes android `Context` object as described below:

```kotlin
val collectElementInput =  Skyflow.CollectElementInput(
        table = "string",            //the table this data belongs to
        column = "string",           //the column into which this data should be inserted
        type = Skyflow.ElementType.CARD_NUMBER,   //Skyflow.ElementType enum
        inputStyles = Skyflow.Styles(),     /*optional styles that should be applied to the form element*/
        labelStyles = Skyflow.Styles(), //optional styles that will be applied to the label of the collect element
        errorTextStyles = Skyflow.Styles(),  //optional styles that will be applied to the errorText of the collect element
        label = "string",            //optional label for the form element
        placeholder = "string",      //optional placeholder for the form element
        altText: String,                 //(DEPRECATED) optional string that acts as an initial value for the collect element
        validations = ValidationSet()       // optional set of validations for the input element
)

val collectElementOptions = Skyflow.CollectElementOptions(
        required = false,  //indicates whether the field is marked as required. Defaults to 'false'
        enableCardIcon = true //indicates whether card icon should be enabled (only for CARD_NUMBER inputs)  
        format = "mm/yy" //Format for the element (only applies currently for EXPIRATION_DATE element type)
)  

const element = container.create(context = Context, collectElementInput, collectElementOptions)
```



### Step 3: Add Elements to the layout

To specify where the Elements will be rendered on the screen, set layout params to the view and add it to a layout in your app programmatically.

```kt
val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
)
element.layoutParams = layoutParams
existingLayout.addView(element)
```

The Skyflow Element is an implementation of native android View so it can be used/mounted similarly.Alternatively, you can use the `unmount` method to reset any element to it's initial state.

```kt
fun clearFields(elements: List<TextField>) {

    //resets all elements to initial value
    for element in elements {
        element.unmount()
    }
}
```


### Step 4: Collect data from Elements

Call `collect(callback, options)` on the container. `CollectOptions` accepts optional `additionalFields` (non-PCI data) and `upsert` parameters.

```kotlin
val options = CollectOptions(
    additionalFields = AdditionalFields(records = listOf(
        AdditionalFieldsRecord(tableName = "persons", data = mapOf("gender" to "MALE"))
    ))
)
container.collect(object : CollectCallback {
    override fun onSuccess(response: CollectResponse) {
        response.records.forEach { record ->
            if (record.httpCode == 200) Log.d(TAG, "success: ${record.tokens}")
            else Log.d(TAG, "error [${record.httpCode}]: ${record.error}")
        }
    }
    override fun onFailure(error: SkyflowError) {
        Log.d(TAG, "failure: ${error.message}")
    }
}, options)
```


#### [Sample Code](https://github.com/skyflowapi/skyflow-android/blob/main/samples/src/main/java/com/Skyflow/CollectActivity.kt)


## Using Skyflow Elements to update data

You can update data in a vault using Skyflow Elements. Use the following steps to securely update data.

### Step 1: Create a container

First create a **container** for the form elements using the ```skyflowClient.container(type: Skyflow.ContainerType)``` method as shown below:

```kt
val container = skyflowClient.container(Skyflow.ContainerType.COLLECT)
```

### Step 2: Create a collect Element

To create a collect Element, construct a `Skyflow.CollectElementInput` object as shown below:

```kt
val collectElementInput = Skyflow.CollectElementInput(
    table: String,                  // optional, the table this data belongs to
    column: String,                 // optional, the column into which this data should be inserted
    type: Skyflow.ElementType,      // Skyflow.ElementType enum
    inputStyles: Skyflow.Styles,     // optional styles that should be applied to the form element
    labelStyles: Skyflow.Styles,     // optional styles that will be applied to the label of the collect element
    errorTextStyles: Skyflow.Styles, // optional styles that will be applied to the errorText of the collect element
    label: String,                   // optional label for the form element
    placeholder: String,             // optional placeholder for the form element
    altText: String,                 // (DEPRECATED) optional that acts as an initial value for the collect element
    validations: ValidationSet,      // optional set of validations for the input element
    skyflowId: String                // The skyflow_id of the record to be updated
)
```

The `table` and `column` fields indicate which table and column in the vault the Element corresponds to.

**Note:**
- Use dot delimited strings to specify columns nested inside JSON fields (e.g. `address.street.line1`)

Along with `CollectElementInput`, you can define other options in the `CollectElementOptions` object as described in the [collect section](#step-2-create-a-collect-element).

### Step 3: Mount Elements to the Screen

To specify where the Elements will be rendered on the screen, create a parent UIView (like LinearLayout, etc.) and add it programmatically.

```kt
val parent = findViewById<LinearLayout>(R.id.parent)
val lp = LinearLayout.LayoutParams(
    LinearLayout.LayoutParams.MATCH_PARENT,
    LinearLayout.LayoutParams.WRAP_CONTENT
)
parent.addView(element)
```

The Skyflow Element is an implementation of the View so it can be used/mounted similarly. Alternatively, you can use the `unmount` method to reset any collect element to its initial state.

```kt
fun clearFieldsOnSubmit(elements: List<TextField>) {
    // resets all elements in the array
    for (element in elements) {
        element.unmount()
    }
}
```

### Step 4: Update data from Elements

When the form is ready to submit, call the `collect(options?)` method on the container object. The `options` parameter takes an object of optional parameters as shown below:

- `additionalFields`: Non-PCI data to update or insert alongside element values, as an `AdditionalFields` object.
- `upsert`: To support upsert operations, pass a list of `UpsertOptions` specifying the table, update type, and unique columns.

```kotlin
val options = CollectOptions(
    additionalFields = AdditionalFields(records = listOf(
        AdditionalFieldsRecord(
            tableName = "persons",
            data = mapOf("gender" to "MALE"),
            skyflowId = "<SKYFLOW_ID>"
        )
    ))
)
container.collect(object : CollectCallback {
    override fun onSuccess(response: CollectResponse) {
        response.records.forEach { record ->
            if (record.httpCode == 200) Log.d(TAG, "update success: ${record.tokens}")
            else Log.d(TAG, "update error [${record.httpCode}]: ${record.error}")
        }
    }
    override fun onFailure(error: SkyflowError) {
        Log.d(TAG, "update failure: ${error.message}")
    }
}, options)
```


**Note:** `skyflowId` is required to update an existing record. Without it, a new record is inserted.

### Validations

skyflow-android provides two types of validations on Collect Elements

#### 1. Default Validations:
Every Collect Element except of type `INPUT_FIELD` has a set of default validations listed below:
- `CARD_NUMBER`: Card number validation with checkSum algorithm(Luhn algorithm), available card lengths for defined card types
- `CARD_HOLDER_NAME`: Name, should be 2 or more symbols, valid characters shold match pattern `^([a-zA-Z\\ \\,\\.\\-\\']{2,})$`
- `CVV`: Card CVV can have 3-4 digits
- `EXPIRATION_DATE`: Any date starting from current month. By default valid expiration date should be in short year format - `MM/YY`
- `PIN`: Can have 4-12 digits

#### 2. Custom Validations:
Custom validations can be added to any element which will be checked after the default validations have passed. The following Custom validation rules are currently supported:
- `RegexMatchRule`: You can use this rule to specify any Regular Expression to be matched with the text field value
- `LengthMatchRule`: You can use this rule to set the minimum and maximum permissible length of the textfield value
- `ElementValueMatchRule`: You can use this rule to match the value of one element with another

The Sample code below illustrates the usage of custom validations:

```kt
/*
  Reset Password - A simple example that illustrates custom validations. The below code shows two input fields with custom validations, one to enter a Password and the second to confirm the same Password.
*/

var myRuleset = ValidationSet()
val strongPasswordRule = RegexMatchRule(regex= "^^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]*$", error = "At least one letter and one number") // This rule enforces a strong password
val lengthRule = LengthMatchRule(minLength = 8, maxLength = 16, error = "Must be between 8 and 16 digits") // this rule allows input length between 8 and 16 characters

// for the Password element
myRuleset.add(rule = strongPasswordRule)
myRuleset.add(rule = lengthRule)

val passwordInput = CollectElementInput(inputStyles = styles, label = "Password", placeholder = "****", type = ElementType.INPUT_FIELD, validations = myRuleset)

val Password = container.create(passwordInput)

// For confirm Password element - shows error when the PINs don't match
val elementMatchRule = ElementMatchRule(element = Password, error = "PINs don't match")

val confirmPasswordinput = CollectElementInput(inputStyles = styles, label = "Confirm Password", placeholder = "****", type = ElementType.INPUT_FIELD, validations = ValidationSet(rules = mutableListOf(strongPasswordRule, lengthRule, elementMatchRule)))
val confirmPassword = container.create(input = confirmPasswordinput)

//mount elements to the screen
addView(Password)
addView(confirmPassword)

```

### Event Listener on Collect Elements


Helps to communicate with skyflow elements by listening to an event

```kt
element.on(eventName: Skyflow.EventName) { state ->
  //handle function
}
```

There are 4 events in `Skyflow.EventName`
- `CHANGE`  
  Change event is triggered when the Element's value changes.
- `READY`   
   Ready event is triggered when the Element is fully rendered
- `FOCUS`   
 Focus event is triggered when the Element gains focus
- `BLUR`    
  Blur event is triggered when the Element loses focus.
The handler ```(state: JSONObject) -> Unit``` is a callback function you provide, that will be called when the event is fired with the state object as shown below. 

```kt
val state = {
  "elementType": Skyflow.ElementType,
  "isEmpty": Boolean,
  "isRequired": Boolean,
  "isFocused": Boolean,
  "isValid": Boolean,
  "value": String,
  "selectedCardScheme": Skyflow.CardType,
}
```
`Notes:`
- values of SkyflowElements will be returned in element state object only when `env` is `DEV`, else it is empty string i.e, '', but in case of CARD_NUMBER type element when the `env` is `PROD` for all the card types except AMEX, it will return first eight digits, for AMEX it will return first six digits and rest all digits in masked format.
- `selectedCardScheme` is only populated for the `CARD_NUMBER` element states when a user chooses a card brand. By default, `selectedCardScheme` is an empty string.

##### Sample code snippet for using listeners
```kt
//create skyflow client with loglevel:"DEBUG"
val config = Skyflow.Configuration(vaultID = VAULT_ID, vaultURL = VAULT_URL, tokenProvider = demoTokenProvider, options = Skyflow.Options(logLevel = Skyflow.LogLevel.DEBUG))

val skyflowClient = Skyflow.initialize(config)

val container = skyflowClient.container(type = Skyflow.ContainerType.COLLECT)
 
// Create a CollectElementInput
val cardNumberInput = Skyflow.CollectElementInput(
    table = "cards",
    column = "cardNumber",
    type = Skyflow.ElementType.CARD_NUMBER,
)
val cardHolderNameInput = Skyflow.CollectElementInput(
    table = "cards",
    column = "cardHolderName",
    type = Skyflow.ElementType.CARDHOLDER_NAME,
)

val cardNumber = container.create(context = Context, input = cardNumberInput)
val cardHolderName = container.create(context = Context, input = cardHolderNameInput)

//subscribing to CHANGE event, which gets triggered when element changes
cardNumber.on(eventName = Skyflow.EventName.CHANGE) { state ->
  // Your implementation when Change event occurs
  log.info("on change", state)
}
cardHolderName.on(eventName = Skyflow.EventName.CHANGE) { state ->
  // Your implementation when Change event occurs
  log.info("on change", state)
}
```
##### Sample Element state object when `Env` is `DEV`
```kt
{
   "elementType": Skyflow.ElementType.CARD_NUMBER,
   "isEmpty": false,
   "isRequired": false,
   "isFocused": true,
   "isValid": true,
   "value": "4111111111111111"
}
{
   "elementType": Skyflow.ElementType.CARDHOLDER_NAME,
   "isEmpty": false,
   "isRequired": false,
   "isFocused": true,
   "isValid": true,
   "value": "John"
}
```
##### Sample Element state object when `Env` is `PROD`
```kt
{
   "elementType": Skyflow.ElementType.CARD_NUMBER,
   "isEmpty": false,
   "isFocused": true,
   "isValid": true,
   "value": "41111111XXXXXXXX"
}
{
   "elementType": Skyflow.ElementType.CARDHOLDER_NAME,
   "isEmpty": false,
   "isFocused": true,
   "isValid": true,
   "value": ""
}
```

### UI Error for Collect Elements

Helps to display custom error messages on the Skyflow Elements through the methods `setError` and `resetError` on the elements.

`setError(error : String)` method is used to set the error text for the element, when this method is trigerred, all the current errors present on the element will be overridden with the custom error message passed. This error will be displayed on the element until `resetError()` is trigerred on the same element.

`resetError()` method is used to clear the custom error message that is set using `setError`.

##### Sample code snippet for setError and resetError

```kt
//create skyflow client with loglevel:"DEBUG"
val config = Skyflow.Configuration(vaultID = VAULT_ID, vaultURL = VAULT_URL, tokenProvider = demoTokenProvider, options = Skyflow.Options(logLevel = Skyflow.LogLevel.DEBUG))

val skyflowClient = Skyflow.initialize(config)

val container = skyflowClient.container(type = Skyflow.ContainerType.COLLECT)
 
// Create a CollectElementInput
val cardNumberInput = Skyflow.CollectElementInput(
    table = "cards",
    column = "cardNumber",
    type = Skyflow.ElementType.CARD_NUMBER,
)

val cardNumber = container.create(input = cardNumberInput)

//Set custom error
cardNumber.setError("custom error")

//reset custom error
cardNumber.resetError()
```


### Set and Clear value for Collect Elements (DEV ENV ONLY)

`setValue(value: String)` method is used to set the value of the element. This method will override any previous value present in the element.

`clearValue()` method is used to reset the value of the element.

`Note:` This methods are only available in DEV env for testing/developmental purposes and MUST NOT be used in PROD env.

##### Sample code snippet for setValue and clearValue

```kotlin
//create skyflow client with env DEV 
val config = Skyflow.Configuration(
  vaultID = VAULT_ID,
  vaultURL = VAULT_URL,
  tokenProvider = demoTokenProvider,
  options = Skyflow.Options(env = Skyflow.Env.DEV)
)
val skyflowClient = Skyflow.initialize(config)
val container = skyflowClient.container(type = Skyflow.ContainerType.COLLECT)
 
// Create a CollectElementInput
val cardNumberInput = Skyflow.CollectElementInput(
  table = "cards",
  column = "cardNumber",
  type = Skyflow.ElementType.CARD_NUMBER,
)
val cardNumber = container.create(input = cardNumberInput)
//Set a value programatically
cardNumber.setValue("4111111111111111")
//Clear the value
cardNumber.clearValue()
```

---

# Securely collecting data client-side using composable elements
Composable Elements combine multiple Skyflow Elements in a single row. The following steps create a composable element and securely collect data through it.

- [**Using Skyflow Composable Elements to collect data**](#using-skyflow-composable-elements-to-collect-data)
- [**Using Skyflow Composable Elements to update data**](#using-skyflow-composable-elements-to-update-data)
- [**Event Listeners on Composable Elements**](#event-listeners-on-composable-elements)
- [**Update Composable Elements**](#update-composable-elements)
- [**Event Listeners on Composable Container**](#event-listeners-on-composable-container)

## Using Skyflow Composable Elements to collect data
### Step 1: Create a composable container

First create a **container** for the form elements using the `skyflowClient.container(type: Skyflow.ContainerType, options: Skyflow.ContainerOptions)` method as show below

```kotlin
val container  = skyflowClient.container(type = ContainerType.COMPOSABLE, options = ContainerOptions(layout = arrayOf(2, 1)))
```

The container requires an options object that contains the following keys:

- `layout`: An array that indicates the number of rows in the container and the number of elements in each row. The index value of the array defines the number of rows, and each value in the array represents the number of elements in that row, in order.  
  
  For example: `arrayOf(2, 1)` means the container has two rows, with two elements in the first row and one element in the second row.
  
  `Note`: The sum of values in the layout array should be equal to the number of elements created

- `styles`: styles to apply to each composable row.

- `errorTextStyles`: styles to apply if an error is encountered.

```kotlin
val containerOptions = ContainerOptions(
  layout: [1, 1, 2],               // required
  styles: Skyflow.Styles,          // optional
  errorTextStyles: Skyflow.Styles  // optional
)
```
### Step 2: Create Composable Elements
Composable Elements use the following schema:

```kotlin
val composableElementInput = Skyflow.CollectElementInput(
  table: String,                   // optional, the table this data belongs to
  column: String,                  // optional, the column into which this data should be inserted
  inputStyles: Skyflow.Styles,     // optional styles that should be applied to the form element
  labelStyles: Skyflow.Styles,     // optional styles that will be applied to the label of the collect element
  errorTextStyles: Skyflow.Styles, // optional styles that will be applied to the errorText of the collect element
  label: String,                   // optional label for the form element
  placeholder: String,             // optional placeholder for the form element
  altText: String,                 // (DEPRECATED) optional that acts as an initial value for the collect element
  validations: ValidationSet,      // optional set of validations for the input element
  type: Skyflow.ElementType,       // Skyflow.ElementType enum
)
```
The `table` and `column` fields indicate which table and column in the vault the Element correspond to.

**Note**: 
- Use dot delimited strings to specify columns nested inside JSON fields (e.g. `address.street.line1`)
 
The `inputStyles` parameter accepts a `Skyflow.Styles` object which consists of multiple `Skyflow.Style` objects which should be applied to the form element in the following states:
 
- `base`: all other variants inherit from these styles
- `complete`: applied when the Element has valid input
- `empty`: applied when the Element has no input
- `focus`: applied when the Element has focus
- `invalid`: applied when the Element has invalid input
 
Each Style object accepts the following properties, please note that each property is optional:
 
```kotlin
Skyflow.Style(
  borderColor: Int            // optional
  cornerRadius: Float         // optional
  padding: Skyflow.Padding    // optional
  borderWidth: Int            // optional
  font:  Int                  // optional
  textAlignment: Int          // optional
  textColor: Int              // optional
  placeholderColor: Int       // optional
  width: Int                  // optional
  height: Int                 // optional
  margin: Skyflow.Margin      // optional
  backgroundColor: Int        // optional
  minWidth: Int               // optional
  maxWidth: Int               // optional
  minHeight: Int              // optional
  maxHeight: Int              // optional
)
```

Here `Skyflow.Padding` and `Skyflow.Margin` are classes which can be used to set the padding and margin respectively for the composable element which takes all the left, top, right, bottom values.

```kt
Skyflow.Padding(left: Int, top: Int, right: Int, bottom: Int)

Skyflow.Margin(left: Int, top: Int, right: Int, bottom: Int)
```

An example Skyflow.Styles object
```kotlin
val styles = Skyflow.Styles(
  base: Style,                    // optional
  complete: Style,                // optional
  empty: Style,                   // optional
  focus: Style,                   // optional
  invalid: Style                  // optional
)
```

**Notes**:
- The `labelStyles` and `errorTextStyles` fields accept the above mentioned `Skyflow.Styles` object which are applied to the `label` and `errorText` text views respectively.
 
- The states that are available for `labelStyles` are `base` and `focus`.
 
- The `errorTextStyles` will be ignored for composable element passed in `CollectElementInput` and `errorTextStyles` passed in `ContainerOptions` will be used instead.

- The state that is available for `errorTextStyles` is only the base state, it shows up when there is some error in the composable element.
 
- The parameters in `Skyflow.Style` object that are respected for `label` and `errorText` text views are
  - padding
  - font
  - textColor
  - textAlignment
  - width
  - height
  - margin
  - minWidth
  - maxWidth
  - minHeight
  - maxHeight
 
Other parameters in the `Skyflow.Style` object are ignored for `label` and `errorText` text views.
 
Finally, the `type` parameter takes a Skyflow.ElementType. Each type applies the appropriate regex and validations to the form element. 

The Android SDK supports the following composable elements:

- `INPUT_FIELD`
- `CARDHOLDER_NAME`
- `CARD_NUMBER`
- `EXPIRATION_DATE`
- `CVV`
- `PIN`
- `EXPIRATION_YEAR`
- `EXPIRATION_MONTH`

**Note**: Only when the entered value in the below composable elements is valid, the focus shifts automatically. The element types are:

- `CARD_NUMBER`
- `EXPIRATION_DATE`
- `EXPIRATION_MONTH`
- `EXPIRATION_YEAR`

The `INPUT_FIELD` type is a custom UI element without any built-in validations. See the section on [validations](#validations) for more information on validations.
 
Along with `CollectElementInput`, you can define other options in the `CollectElementOptions` object which is described below.
 
```kotlin
Skyflow.CollectElementOptions(
  required: Boolean,                        // Indicates whether the field is marked as required. Defaults to 'false'
  enableCardIcon: Boolean,                  // Indicates whether card icon should be enabled (only for CARD_NUMBER inputs)
  format: String,                           // Format for the element 
  translation: HashMap<Character, String>   // Indicates the allowed data type value for format.
)
```
- `required`: Indicates whether the field is marked as required or not. Default is `false`.
- `enableCardIcon`: Indicates whether the icon is visible for the CARD_NUMBER element. Default is `true`.
- `format`:  A string value that indicates the format pattern applicable to the element type. Only applicable to `EXPIRATION_DATE`, `CARD_NUMBER`, `EXPIRATION_YEAR`, and `INPUT_FIELD` elements.
  - For INPUT_FIELD elements,
    - the length of `format` determines the expected length of the user input.
    - if `translation` isn't specified, the `format` value is considered a string literal.
- `translation`: A dictionary of key/value pairs, where the key is a character that appears in `format` and the value is a regex pattern of acceptable inputs for that character. Each key can only appear once. Only applicable for INPUT_FIELD elements.

Accepted values by element type:

| Element type    | `format`                                                                                  | `translation`                                                         | Examples                                                                                                                                 |
| --------------- | ----------------------------------------------------------------------------------------- | --------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| EXPIRATION_DATE | <ul><li>`mm/yy`(default)</li><li>`mm/yyyy`</li> <li>`yy/mm`</li> <li> `yyyy/mm`</li></ul> | N/A                                                                   | <ul><li>12/27</li><li>12/2027</li> <li>27/12</li> <li> 2027/12</li></ul></ul>                                                            |
| EXPIRATION_YEAR | <ul><li>`yy`(default)</li><li>`yyyy`</li>                                                 | N/A                                                                   | <ul><li>27</li><li>2027</li> </ul>                                                                                                       |
| CARD_NUMBER     | <ul><li> `XXXX XXXX XXXX XXXX` (default)</li><li>`XXXX-XXXX-XXXX-XXXX`</li> </ul>         | N/A                                                                   | <ul><li>1234 5678 9012 3456</li><li>1234-5678-9012-3456</li> </ul>                                                                       |
| INPUT_FIELD     | A string that matches the desired output, with placeholder characters of your choice.     | A hashmap of key/value pairs. Defaults to `hashmapOf('X' to "[0-9]")` | With `format: +91 XXXX-XX-XXXX` and `translation: hashmapOf('X' to "[0-9]")`, user input of "1234121234" displays as "+91 1234-12-1234". |

Collect Element Options examples for INPUT_FIELD

Example 1
```kotlin
Skyflow.CollectElementOptions(
  required: true, 
  enableCardIcon: true,
  format: "+91 XXXX-XX-XXXX",
  translation: hashmapOf('X' to "[0-9]")
)
```
User input: "1234121234"

Value displayed in INPUT_FIELD: "+91 1234-12-1234"    

Example 2
```kotlin
Skyflow.CollectElementOptions(
  required: true, 
  enableCardIcon: true,
  format: "AY XX-XXX-XXXX",
  translation: hashmapOf('X' to "[0-9]", 'Y' to "[A-Z]")
)
```
User input: "B1234121234"

Value displayed in INPUT_FIELD: "AB 12-341-2123"

Once the `Skyflow.CollectElementInput` and `Skyflow.CollectElementOptions` objects are defined, add to the container using the `create(context: Context, input: CollectElementInput, options: CollectElementOptions)` method as shown below. The `input` param takes a `Skyflow.CollectElementInput` object as defined above and the `options` parameter takes an `Skyflow.CollectElementOptions` object as described below:
 
```kotlin
val composableElementInput = Skyflow.CollectElementInput(
  table: String,                  // the table this data belongs to
  column: String,                 // the column into which this data should be inserted
  inputStyles: Skyflow.Styles,     // optional styles that should be applied to the form element
  labelStyles: Skyflow.Styles,     // optional styles that will be applied to the label of the collect element
  errorTextStyles: Skyflow.Styles, // optional styles that will be applied to the errorText of the collect element
  label: String,                   // optional label for the form element
  placeholder: String,             // optional placeholder for the form element
  altText: String,                 // (DEPRECATED) optional that acts as an initial value for the collect element
  validations: ValidationSet,      // optional set of validations for the input element
  type: Skyflow.ElementType,       // Skyflow.ElementType enum
)

val collectElementOptions = Skyflow.CollectElementOptions(
  required: false,  // indicates whether the field is marked as required. Defaults to 'false',
  enableCardIcon: true, // indicates whether card icon should be enabled (only for CARD_NUMBER inputs)
  format: "mm/yy" // Format for the element
)

val element = container.create(context = Context, input: composableElementInput, options: collectElementOptions)
```
### Step 3: Mount Elements to the Screen

To specify where the Elements will be rendered on the screen, fetch composable layout using `container.getComposableLayout()` and add it to a layout in your app programmatically.

```kotlin
try {
  val composableLayout = container.getComposableLayout()
  existingLayout.addView(composableLayout)
} catch(error: Exception) {
  println(error)
}
```

The Skyflow Element is an implementation of native android View so it can be used/mounted similarly.Alternatively, you can use the unmount method to reset any element to it's initial state.

```kotlin
fun clearFieldsOnSubmit(elements: List<TextField>) {
  // resets all elements in the array
  for element in elements {
    element.unmount()
  }
}
```
### Step 4: Collect data from elements

When the form is ready to be submitted, call the `collect(callback: CollectCallback, options: CollectOptions? = null)` method on the container object. The options parameter takes a `CollectOptions` object.

`Skyflow.CollectOptions` takes two optional fields
- `additionalFields`: Non-PCI data to be inserted alongside element values. See [Additional fields](#additional-fields-non-pci-data).
- `upsert`: To support upsert operations, the table and a unique column. See [Upsert support](#upsert-support).

```kotlin
val options = CollectOptions(
    additionalFields = AdditionalFields(records = listOf(
        AdditionalFieldsRecord(tableName = "persons", data = mapOf("gender" to "MALE"))
    ))
)
composableContainer.collect(object : CollectCallback {
    override fun onSuccess(response: CollectResponse) {
        response.records.forEach { record ->
            if (record.httpCode == 200) Log.d(TAG, "success: ${record.tokens}")
            else Log.d(TAG, "error [${record.httpCode}]: ${record.error}")
        }
    }
    override fun onFailure(error: SkyflowError) { Log.d(TAG, "failure: ${error.message}") }
}, options)
```


## Using Skyflow Composable Elements to update data

Composable Elements combine multiple Skyflow Elements in a single row. The following steps create a composable element and securely update data through it.

### Step 1: Create a composable container

First create a **container** for the form elements using the `skyflowClient.container(type: Skyflow.ContainerType, options: Skyflow.ContainerOptions)` method as shown below:

```kt
val containerOptions = ContainerOptions(
    layout = arrayOf(1, 1, 2),  // required
    styles = Skyflow.Styles,     // optional
    errorTextStyles = Skyflow.Styles  // optional
)
val container = skyflowClient.container(
    type = ContainerType.COMPOSABLE, 
    options = containerOptions
)
```

### Step 2: Create Composable Elements

Composable Elements use the following schema:

```kt
val composableElementInput = Skyflow.CollectElementInput(
    table: String,                  // optional, the table this data belongs to
    column: String,                 // optional, the column into which this data should be updated
    type: Skyflow.ElementType,      // Skyflow.ElementType enum
    inputStyles: Skyflow.Styles,     // optional styles that should be applied to the form element
    labelStyles: Skyflow.Styles,     // optional styles that will be applied to the label of the collect element
    errorTextStyles: Skyflow.Styles, // optional styles that will be applied to the errorText of the collect element
    label: String,                   // optional label for the form element
    placeholder: String,             // optional placeholder for the form element
    altText: String,                 // (DEPRECATED) optional that acts as an initial value for the collect element
    validations: ValidationSet,      // optional set of validations for the input element
    skyflowId: String                // The skyflow_id of the record to be updated
)
```

The `table` and `column` fields indicate which table and column in the vault the Element corresponds to.

**Note:**
- Use dot delimited strings to specify columns nested inside JSON fields (e.g. `address.street.line1`)

Along with `CollectElementInput`, you can define other options in the `CollectElementOptions` object as described below:

```kt
Skyflow.CollectElementOptions(
    required: Boolean,               // Indicates whether the field is marked as required. Defaults to 'false'
    enableCardIcon: Boolean,         // Indicates whether card icon should be enabled (only for CARD_NUMBER inputs)
    format: String,                  // Format for the element  
    translation: HashMap<Char, String> // Indicates the allowed data type value for format.
)
```

Once the `Skyflow.CollectElementInput` and `Skyflow.CollectElementOptions` objects are defined, add to the container using the `create(context: Context, input: CollectElementInput, options: CollectElementOptions)` method as shown below:

```kt
val element = container.create(context = this, input = composableElementInput, options = collectElementOptions)
```

### Step 3: Mount Elements to the Screen

To specify where the Elements will be rendered on the screen, fetch composable layout using `container.getComposableLayout()` and add it to a layout in your app programmatically.

```kt
try {
    val composableLayout = container.getComposableLayout()
    existingLayout.addView(composableLayout)
} catch (error: Exception) {
    println(error)
}
```

The Skyflow Element is an implementation of the View so it can be used/mounted similarly. Alternatively, you can use the `unmount` method to reset any collect element to its initial state.

```kt
fun clearFieldsOnSubmit(elements: List<TextField>) {
    // resets all elements in the array
    for (element in elements) {
        element.unmount()
    }
}
```

### Step 4: Update data from Elements

When you submit the form, call the `collect(callback: CollectCallback, options: CollectOptions? = null)` method on the container object.

The options parameter takes a `CollectOptions` object with the following optional fields:

- `additionalFields`: Non-PCI data to insert alongside element values, as an `AdditionalFields` object.
- `upsert`: To support upsert operations, pass a list of `UpsertOptions` specifying the table, update type, and unique columns.

```kotlin
val options = CollectOptions(
    additionalFields = AdditionalFields(records = listOf(
        AdditionalFieldsRecord(
            tableName = "persons",
            data = mapOf("gender" to "MALE"),
            skyflowId = "<SKYFLOW_ID>"
        )
    ))
)
composableContainer.collect(object : CollectCallback {
    override fun onSuccess(response: CollectResponse) {
        response.records.forEach { record ->
            if (record.httpCode == 200) Log.d(TAG, "update success: ${record.tokens}")
            else Log.d(TAG, "update error [${record.httpCode}]: ${record.error}")
        }
    }
    override fun onFailure(error: SkyflowError) { Log.d(TAG, "update failure: ${error.message}") }
}, options)
```


**Note:** `skyflowId` is required to update an existing record. Without it, a new record is inserted.

## Event Listeners on Composable Elements
You can communicate with Skyflow Elements by listening to element events:

```kotlin
element.on(eventName: Skyflow.EventName) { state ->
  // handle function
}
```

The SDK supports four events:

- `CHANGE`: Triggered when the Element's value changes.
- `READY`: Triggered when the Element is fully rendered.
- `FOCUS`: Triggered when the Element gains focus.
- `BLUR`: Triggered when the Element loses focus.

The handler `(state: JSONObject) -> Unit` is a callback function you provide, that will be called when the event is fired with the state object as shown below. 

```kotlin
val state = {
  "elementType": Skyflow.ElementType,
  "isEmpty": Bool ,
  "isRequired": Bool,
  "isFocused": Bool,
  "isValid": Bool,
  "value": String
}
```

`Note`: 
values of SkyflowElements will be returned in element state object only when `env` is `DEV`, else it is empty string i.e, '', but in case of CARD_NUMBER type element when the `env` is `PROD` for all the card types except AMEX, it will return first eight digits, for AMEX it will return first six digits and rest all digits in masked format.

#### Example Usage of Event Listener on Composable Elements

```kotlin
// create skyflow client with loglevel:"DEBUG"
val config = Skyflow.Configuration(
  vaultID = VAULT_ID,
  vaultURL = VAULT_URL,
  tokenProvider = demoTokenProvider,
  options = Skyflow.Options(logLevel: Skyflow.LogLevel.DEBUG)
)

val skyflowClient = Skyflow.init(config)

val containerOptions = ContainerOptions(
  layout = arrayOf(1, 1),
  styles = Styles(base: Style(borderColor: UIColor.gray)),
  errorTextStyles = Styles(base: Style(textColor: UIColor.red))
)

//Create a Composable Container.
val container = skyflowClient.container(type: Skyflow.ContainerType.COMPOSABLE, options: containerOptions)

// Create a CollectElementInput
val cardNumberInput = Skyflow.CollectElementInput(
  table = "cards",
  column = "cardNumber",
  type = Skyflow.ElementType.CARD_NUMBER,
)

val cardHolderNameInput = Skyflow.CollectElementInput(
  table = "cards",
  column = "cardHolderName",
  type = Skyflow.ElementType.CARDHOLDER_NAME,
)    

val cardNumber = container.create(context = Context, input = cardNumberInput)
val cardHolderName = container.create(context = Context, input = cardHolderNameInput)

try {
  val composableLayout = container.getComposableLayout()
  parent.addView(composableLayout)
} catch (error: Exception) {
  println(error)
}

// subscribing to CHANGE event, which gets triggered when element changes
cardNumber.on(eventName: Skyflow.EventName.CHANGE) { state ->
  // Your implementation when Change event occurs
  log.info("on change", state)
}

cardHolderName.on(eventName: Skyflow.EventName.CHANGE) { state ->
  // Your implementation when Change event occurs
  log.info("on change", state)
}
```

#### Sample Element state object when `env` is `DEV`
```kotlin
{
  "elementType": Skyflow.ElementType.CARD_NUMBER,
  "isEmpty": false,
  "isRequired": false,
  "isFocused": true,
  "isValid": true,
  "value": "4111111111111111"
}
{
  "elementType": Skyflow.ElementType.CARDHOLDER_NAME,
  "isEmpty": false,
  "isRequired": false,
  "isFocused": true,
  "isValid": true,
  "value": "John"
}
```
#### Sample Element state object when `env` is `PROD`
```kotlin
{
  "elementType": Skyflow.ElementType.CARD_NUMBER,
  "isEmpty": false,
  "isRequired": false,
  "isFocused": true,
  "isValid": true,
  "value": "41111111XXXXXXXX"
}
{
  "elementType": Skyflow.ElementType.CARDHOLDER_NAME,
  "isEmpty": false,
  "isRequired": false,
  "isFocused": true,
  "isValid": true,
  "value": ""
}
```
## Update Composable Elements
You can update composable element properties with the `update` interface.

The `update` interface takes the below object:
```kotlin
val updateElement =  Skyflow.CollectElementInput(
  table: String,                   // optional the table this data belongs to
  column: String,                  // optional the column into which this data should be inserted
  inputStyles: Skyflow.Styles,     // optional styles that should be applied to the form element
  labelStyles: Skyflow.Styles,     // optional styles that will be applied to the label of the collect element
  errorTextStyles: Skyflow.Styles, // optional styles that will be applied to the errorText of the collect element
  label: String,                   // optional label for the form element
  placeholder: String,             // optional placeholder for the form element
  altText: String,                 // (DEPRECATED) optional that acts as an initial value for the collect element
  validations: ValidationSet,      // optional set of validations for the input element
)
```
Only include the properties that you want to update for the specified composable element.

Properties your provided when you created the element remain the same until you explicitly update them.

`Notes`: 
- You can't update the type property of an element.
- Upon calling the update method, if not passed, all Styles i.e. `inputStyles`, `labelStyles` and `errorTextStyles` will be overridden by default Styles.

#### End to end example
```kotlin
// create skyflow client with loglevel:"DEBUG"
val config = Skyflow.Configuration(
  vaultID = VAULT_ID,
  vaultURL = VAULT_URL,
  tokenProvider = demoTokenProvider,
  options = Skyflow.Options(logLevel: Skyflow.LogLevel.DEBUG)
)

val skyflowClient = Skyflow.init(config)

val containerOptions = ContainerOptions(
  layout = arrayOf(1, 1),
  styles = Styles(base: Style(borderColor: UIColor.gray)),
  errorTextStyles = Styles(base: Style(textColor: UIColor.red))
)

//Create a Composable Container.
val container = skyflowClient.container(type: Skyflow.ContainerType.COMPOSABLE, options: containerOptions)

// Create a CollectElementInput
val cardNumberInput = Skyflow.CollectElementInput(
  table = "cards",
  column = "cardNumber",
  type = Skyflow.ElementType.CARD_NUMBER,
)

val cardHolderNameInput = Skyflow.CollectElementInput(
  table = "cards",
  column = "cardHolderName",
  type = Skyflow.ElementType.CARDHOLDER_NAME,
)    

val cardNumber = container.create(context = Context, input = cardNumberInput)
val cardHolderName = container.create(context = Context, input = cardHolderNameInput)

try {
  val composableLayout = container.getComposableLayout()
  parent.addView(composableLayout)
} catch (error: Exception) {
  println(error)
}

// Update table, column, inputStyles properties on cardNumber.
cardNumber.update(update = CollectElementInput(
  table = "cards",
  column = "cardHolderName",
  inputStyles = Skyflow.Styles(base: Style(borderColor: UIColor.red))
))

val lengthRule = LengthMatchRule(minLength = 5, maxLength = 16, error = "Must be between 5 and 16 digits")

// Update validations and placeholder property on cardHolderName.
cardHolderName.update(update = CollectElementInput(
  placeholder = "cardHolderName",
  validations = ValidationSet(rules = mutableListOf(lengthRule)))
)
```

## Event Listeners on Composable Container

Currently, the SDK supports one event:
- `SUBMIT`: Triggered when the Enter key is pressed in any container element.
  
The handler function `() -> Unit` is a callback function you provide that's called when the `SUBMIT` event fires.

#### Example
```kotlin
// create skyflow client with loglevel:"DEBUG"
val config = Skyflow.Configuration(
  vaultID = VAULT_ID,
  vaultURL = VAULT_URL,
  tokenProvider = demoTokenProvider,
  options = Skyflow.Options(logLevel: Skyflow.LogLevel.DEBUG)
)

val skyflowClient = Skyflow.init(config)

val containerOptions = ContainerOptions(
  layout = arrayOf(1),
  styles = Styles(base: Style(borderColor: UIColor.gray)),
  errorTextStyles = Styles(base: Style(textColor: UIColor.red))
)

//Create a Composable Container.
val container = skyflowClient.container(type: Skyflow.ContainerType.COMPOSABLE, options: containerOptions)

// Create a CollectElementInput
val cardNumberInput = Skyflow.CollectElementInput(
  table = "cards",
  column = "cardNumber",
  type = Skyflow.ElementType.CARD_NUMBER,
)

val cardNumber = container.create(context = Context, input = cardNumberInput)

try {
  val composableLayout = container.getComposableLayout()
  parent.addView(composableLayout)
} catch (error: Exception) {
  println(error)
}

//Call Submit event listener on container
container.on(EventName.SUBMIT) {
  // Your implementation when Submit (enter) event occurs
  log.info("on submit", "submit event triggerred")
}
```

---
# Securely revealing data client-side
-  [**Using Skyflow Elements to reveal data**](#using-skyflow-elements-to-reveal-data)
-  [**UI Error for Reveal Elements**](#ui-error-for-reveal-elements)
-  [**Set token for Reveal Elements**](#set-token-for-reveal-elements)
-  [**Set and clear altText for Reveal Elements**](#set-and-clear-alttext-for-reveal-elements)

## Using Skyflow Elements to reveal data
Skyflow Elements can be used to securely reveal data in an application without exposing your front end to the sensitive data. This is great for use-cases like card issuance where you may want to reveal the card number to a user without increasing your PCI compliance scope.
### Step 1: Create a container
To start, create a container using the `skyflowClient.container(Skyflow.ContainerType.REVEAL)` method as shown below.
```kt
val container = skyflowClient.container(type = Skyflow.ContainerType.REVEAL)
```

### Step 2: Create a reveal Element
Next, define a `RevealElementInput` for each element to reveal:

```kotlin
val revealElementInput = RevealElementInput(
    token = "<TOKEN>",                   // token of the data to reveal
    inputStyles = Skyflow.Styles(),      // optional styles for the element
    labelStyles = Skyflow.Styles(),      // optional styles for the label
    errorTextStyles = Skyflow.Styles(),  // optional styles for the error text
    label = "Card Number",               // optional label
    altText = "•••• •••• •••• ••••"     // optional placeholder shown before reveal
)
```


**Note:** Redaction is not set on individual reveal elements. To apply a redaction to a token group, use `tokenGroupRedactions` on `RevealOptions` passed to `reveal()`. See [Reveal with typed callbacks](#reveal-with-typed-callbacks).

The `inputStyles` parameter accepts a styles object as described in the [previous section](#step-2-create-a-collect-element) for collecting data but the only state available for a reveal element is the base state.

The `labelStyles` and `errorTextStyles` fields accept the above mentioned `Skyflow.Styles` object as described in the [previous section](#step-2-create-a-collect-element), the only state available for a reveal element is the base state.

The `inputStyles`, `labelStyles` and  `errorTextStyles` parameters accepts a styles object as described in the [previous section](#step-2-create-a-collect-element) for collecting data but only a single variant is available i.e. base. 

An example of a inputStyles object:

```kt
var inputStyles = Skyflow.Styles(base = Skyflow.Style(
                      borderColor = Color.BLUE))
```

An example of a labelStyles object:

```kt
var labelStyles = Skyflow.Styles(base = 
                    Skyflow.Style(font = 12))
```

An example of a errorTextStyles object:

```kt
var labelStyles = Skyflow.Styles(base = 
                    Skyflow.Style(textColor = COLOR.RED))
```

Along with `RevealElementInput`, you can define other options in the `RevealElementOptions` object as described below:
```kotlin
Skyflow.RevealElementOptions(
  format: String, // Format for the element.
  translation: HashMap<Char, String> // Indicates the allowed data type value for format
  enableCopy: Boolean, // Indicates whether to enable the copy icon in reveal elements to copy text to clipboard. Defaults to 'false'
)
```
- `format`: A string value that indicates how the  element should display the value, including placeholder characters that map to keys `translation` If `translation` isn't specified, the `format` value is considered a string literal.

- `translation`: A hashmap of key/value pairs, where the key is a character that appears in `format` and the value is a regex pattern of acceptable inputs for that character. Each key can only appear once. Defaults to `hashmapOf('X' to "[0-9]")`.

`enableCopy`: Indicates whether to enable the copy icon in reveal elements to copy text to clipboard.

Reveal Element Options examples:

Example 1:
```kotlin
let element = container.create(input: revealElementInput)
Skyflow.RevealElementOptions(
  format: "(XXX) XXX-XXXX",
  translation: hashmapOf('X' to "[0-9]") 
)
```
Value from vault: "1234121234"

Value displayed in element: "(123) 412-1234"

Example 2:
```kotlin
Skyflow.RevealElementOptions(
  format: "XXXX-XXXXXX-XXXXX",
  translation: hashmapOf('X' to "[0-9]") 
)
```
Value from vault: "374200000000004"

Value displayed in element: "3742-000000-00004"

Once you've defined a `Skyflow.RevealElementInput` object and `Skyflow.RevealElementOptions`, you can use the `create()` method of the container to create the Element as shown below:

```kotlin
let element = container.create(input: revealElementInput, options: Skyflow.RevealElementOptions(format: "XXXX-XXXXXX-XXXXX",
translation: hashmapOf('X' to "[0-9]")
))
```

### Step 3: Mount Elements to the Screen

Elements used for revealing data are mounted to the screen the same way as Elements used for collecting data. Refer to Step 3 of the [section above](#step-3-mount-elements-to-the-screen).

### Step 4: Reveal data
When the sensitive data is ready to be retrieved and revealed, call the `reveal()` method on the container with a typed `RevealCallback`:

```kotlin
revealContainer.reveal(object : RevealCallback {
    override fun onSuccess(response: RevealResponse) {
        response.records.forEach { record ->
            if (record.httpCode == 200) {
                Log.d(TAG, "revealed: token=${record.token}, group=${record.tokenGroupName}")
            } else {
                Log.e(TAG, "partial error [${record.httpCode}]: ${record.error}")
            }
        }
    }
    override fun onFailure(error: SkyflowError) {
        Log.e(TAG, "reveal failed: ${error.message}")
    }
})
```


To apply redaction per token group, pass `RevealOptions`:

```kotlin
val options = RevealOptions(
    tokenGroupRedactions = listOf(
        TokenGroupRedaction(tokenGroupName = "<TOKEN_GROUP_NAME>", redaction = "<REDACTION_TYPE>")
    )
)
revealContainer.reveal(object : RevealCallback { ... }, options)
```


### UI Error for Reveal Elements

Helps to display custom error messages on the Skyflow Elements through the methods `setError` and `resetError` on the elements.

`setError(error : String)` method is used to set the error text for the element, when this method is trigerred, all the current errors present on the element will be overridden with the custom error message passed. This error will be displayed on the element until `resetError()` is trigerred on the same element.

`resetError()` method is used to clear the custom error message that is set using `setError`.


### Set token for Reveal Elements
The `setToken(value: String)` method can be used to set the token of the Reveal Element. If no altText is set, the set token will be displayed on the UI as well. If altText is set, then there will be no change in the UI but the token of the element will be internally updated.
### Set and Clear altText for Reveal Elements
The `setAltText(value: String)` method can be used to set the altText of the Reveal Element. This will cause the altText to be displayed in the UI regardless of whether the token or value is currently being displayed.
`clearAltText()` method can be used to clear the altText, this will cause the element to display the token or actual value of the element. If the element has no token, the element will be empty.


### End to end example of revealing data with Skyflow Elements
#### [Sample Code](https://github.com/skyflowapi/skyflow-android/blob/main/samples/src/main/java/com/Skyflow/RevealActivity.kt):
```kotlin
// Initialize skyflow configuration
val config = Configuration(
    vaultID = "<VAULT_ID>",
    vaultURL = "<VAULT_URL>",
    tokenProvider = demoTokenProvider
)

// Initialize skyflow client
val skyflowClient = init(config)

// Create a Reveal Container
val container = skyflowClient.container(ContainerType.REVEAL)

// Create Skyflow.Styles with individual Skyflow.Style variants
val baseStyle = Style(borderColor = Color.BLUE)
val baseTextStyle = Style(textColor = Color.BLACK)
val inputStyles = Styles(base = baseStyle)
val labelStyles = Styles(base = baseTextStyle)
val errorTextStyles = Styles(base = baseTextStyle)

// Create Reveal Elements — no redaction on individual elements
val cardNumberInput = RevealElementInput(
    token = "b63ec4e0-bbad-4e43-96e6-6bd50f483f75",
    inputStyles = inputStyles,
    labelStyles = labelStyles,
    errorTextStyles = errorTextStyles,
    label = "Card Number",
    altText = "XXXX XXXX XXXX XXXX"
)

val cardNumberElement = container.create(context = this, input = cardNumberInput)

val nameInput = RevealElementInput(
    token = "89024714-6a26-4256-b9d4-55ad69aa4047",
    inputStyles = inputStyles,
    labelStyles = labelStyles,
    errorTextStyles = errorTextStyles,
    label = "Full Name",
    altText = "XXX"
)

val nameElement = container.create(context = this, input = nameInput)

// Optionally set/reset custom error text on an element
nameElement.setError("custom error")
nameElement.resetError()

// Mount elements to the screen
parent.addView(cardNumberElement)
parent.addView(nameElement)

// Call reveal with typed RevealCallback
container.reveal(object : RevealCallback {
    override fun onSuccess(response: RevealResponse) {
        response.records.forEach { record ->
            if (record.httpCode == 200) {
                Log.d(TAG, "revealed: token=${record.token}")
            } else {
                Log.e(TAG, "partial error [${record.httpCode}]: ${record.error}")
            }
        }
    }
    override fun onFailure(error: SkyflowError) {
        Log.e(TAG, "reveal failed: ${error.message}")
    }
})
```

The `records` list contains both successful and failed tokens. Each record carries its own `httpCode` so you can handle mixed results in a single pass.

#### Sample Response
```json
{
    "records": [
        {
            "token": "b63ec4e0-bbad-4e43-96e6-6bd50f483f75",
            "tokenGroupName": "deterministic_string",
            "metadata": {
                "skyflowId": "3ac0424e-fe45-43a9-9193-2e6d2913cbd2",
                "tableName": "cards"
            },
            "httpCode": 200
        },
        {
            "token": "89024714-6a26-4256-b9d4-55ad69aa4047",
            "error": "Detokenize failed. Token 89024714-6a26-4256-b9d4-55ad69aa4047 is invalid. Specify a valid token.",
            "httpCode": 404
        }
    ]
}
```

A whole-request failure (e.g. auth error) skips `onSuccess` and delivers a `SkyflowError` to `onFailure`:
```json
{
    "grpcCode": 13,
    "httpCode": 500,
    "message": "Skyflow services experienced an internal error.",
    "httpStatus": "Internal Server Error",
    "details": []
}
```
---

# Typed callbacks and response handling

The SDK provides typed callbacks and typed response objects for collect and reveal operations. Both successes and partial errors are returned in the same `records` list — each record carries its own `httpCode`, so you can handle mixed results without exceptions.

---

## Collect with typed callbacks

### CollectCallback

Implement `CollectCallback` to receive typed collect results:

```kotlin
container.collect(object : CollectCallback {
    override fun onSuccess(response: CollectResponse) {
        response.records.forEach { record ->
            if (record.httpCode == 200) {
                Log.d(TAG, "insert success: ${record.tokens}")
            } else {
                Log.d(TAG, "insert error [${record.httpCode}]: ${record.error}")
            }
        }
    }
    override fun onFailure(error: SkyflowError) {
        Log.d(TAG, "collect failure: code=${error.httpCode}, message=${error.message}")
    }
})
```


### CollectOptions

#### Upsert support

Pass `CollectOptions` with `upsert` to insert-or-update based on a unique column:

```kotlin
val options = CollectOptions(
    upsert = listOf(
        UpsertOptions(
            tableName = "<TABLE_NAME>",
            updateType = UpdateType.UPDATE,
            uniqueColumns = listOf("<UNIQUE_COLUMN>")
        )
    )
)
container.collect(object : CollectCallback { ... }, options)
```


#### Additional fields (non-PCI data)

Pass non-PCI data alongside element values using `AdditionalFields`:

```kotlin
val options = CollectOptions(
    additionalFields = AdditionalFields(
        records = listOf(
            AdditionalFieldsRecord(
                tableName = "<TABLE_NAME>",
                data = mapOf("<COLUMN>" to "<VALUE>")
                // skyflowId = "<SKYFLOW_ID>"  // set this to update an existing record
            )
        )
    )
)
container.collect(object : CollectCallback { ... }, options)
```


**Note:** Set `skyflowId` on `AdditionalFieldsRecord` to update an existing record. Without it, a new record is inserted.

### CollectResponse

`CollectResponse.records` is a flat list of `CollectRecord` objects. Both successes and partial errors are included in the same list.

```kotlin
data class CollectRecord(
    val tableName: String?,
    val skyflowId: String?,
    val tokens: Map<String, Any?>?,
    val hashedData: Map<String, Any?>?,
    val error: String?,
    val httpCode: Int
)
```

#### Sample success response:
```json
{
    "records": [
        {
            "tableName": "cards",
            "skyflowId": "f1714ef8-8deb-489a-a18d-77e0e007f403",
            "fields": {
                "cardNumber": [{"token": "f3907186-e7e2-466f-91e5-48e12c2bcbc1", "tokenGroupName": "deterministic_string"}]
            },
            "httpCode": 200
        }
    ]
}
```

#### Sample partial error response:
```json
{
    "records": [
        {
            "tableName": "cards",
            "skyflowId": "f1714ef8-8deb-489a-a18d-77e0e007f403",
            "fields": {
                "cardNumber": [{"token": "f3907186-e7e2-466f-91e5-48e12c2bcbc1", "tokenGroupName": "deterministic_string"}]
            },
            "httpCode": 200
        },
        {
            "error": "Invalid request. Table name table not present for record.",
            "skyflowId": null,
            "tableName": "",
            "httpCode": 400
        }
    ]
}
```

#### If the entire request fails, `onFailure` delivers a `SkyflowError`:

```kotlin
override fun onFailure(error: SkyflowError) {
    Log.d(TAG, "httpCode=${error.httpCode}, message=${error.message}")
}
```


#### Sample Code:
[CollectActivity.kt](https://github.com/skyflowapi/skyflow-android/blob/main/samples/src/main/java/com/Skyflow/CollectActivity.kt)

---

## Reveal with typed callbacks

### RevealCallback

Implement `RevealCallback` to receive typed reveal results:

```kotlin
revealContainer.reveal(object : RevealCallback {
    override fun onSuccess(response: RevealResponse) {
        response.records.forEach { record ->
            if (record.httpCode == 200) {
                Log.d(TAG, "reveal success: token=${record.token}")
            } else {
                Log.d(TAG, "reveal error [${record.httpCode}]: ${record.error}")
            }
        }
    }
    override fun onFailure(error: SkyflowError) {
        Log.d(TAG, "reveal failure: code=${error.httpCode}, message=${error.message}")
    }
})
```


### RevealOptions

Apply a redaction to an entire token group using `RevealOptions.tokenGroupRedactions`. This is a request-level setting — the redaction applies to every token in the named group, not to individual reveal elements.

```kotlin
val options = RevealOptions(
    tokenGroupRedactions = listOf(
        TokenGroupRedaction(
            tokenGroupName = "<TOKEN_GROUP_NAME>",
            redaction = "<REDACTION_TYPE>"
        )
    )
)
revealContainer.reveal(object : RevealCallback { ... }, options)
```


### RevealResponse

`RevealResponse.records` is a flat list of `RevealRecord` objects. Both successes and partial errors are included in the same list.

```kotlin
data class RevealRecord(
    val token: String,
    val tokenGroupName: String?,
    val metadata: Map<String, Any?>?,   // includes skyflowId, tableName
    val error: String?,
    val httpCode: Int
)
```

#### Sample success response:
```json
{
    "records": [
        {
            "token": "b63ec4e0-bbad-4e43-96e6-6bd50f483f75",
            "tokenGroupName": "deterministic_string",
            "metadata": {
                "skyflowId": "3ac0424e-fe45-43a9-9193-2e6d2913cbd2",
                "tableName": "cards"
            },
            "httpCode": 200
        }
    ]
}
```

#### Sample partial error response:
```json
{
    "records": [
        {
            "token": "b63ec4e0-bbad-4e43-96e6-6bd50f483f75",
            "tokenGroupName": "deterministic_string",
            "metadata": {
                "skyflowId": "3ac0424e-fe45-43a9-9193-2e6d2913cbd2",
                "tableName": "cards"
            },
            "httpCode": 200
        },
        {
            "token": "a4b24714-6a26-4256-b9d4-55ad69aa4047",
            "error": "Tokens not found for a4b24714-6a26-4256-b9d4-55ad69aa4047",
            "httpCode": 404
        }
    ]
}
```

#### Sample Code:
[RevealActivity.kt](https://github.com/skyflowapi/skyflow-android/blob/main/samples/src/main/java/com/Skyflow/RevealActivity.kt)

---

## Limitation
Currently the skyflow collect elements and reveal elements can't be used in the XML layout definition, we have to add them to the views programatically.



