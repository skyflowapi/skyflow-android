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

# Installation

## Requirements
- Android Gradle plugin 4.2.0 and above
- Android 5.0 (API level 21) and above

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
  implementation 'com.skyflowapi.android:skyflow-android-sdk:1.24.0'
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
   <version>1.24.0</version>
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
-  [**Inserting data into the vault**](#inserting-data-into-the-vault)
-  [**Using Skyflow Elements to collect data**](#using-skyflow-elements-to-collect-data)
-  [**Event Listener on Collect Elements**](#event-listener-on-collect-elements)
- [**UI Error for Collect Eements**](#ui-error-for-collect-elements)
-  [**Set and Clear value for Collect Elements (DEV ENV ONLY)**](#set-and-clear-value-for-collect-elements-dev-env-only)


## Inserting data into the vault

To insert data into the vault from the integrated application, use the ```insert(records: JSONObject, options: InsertOptions?= InsertOptions() , callback: Skyflow.Callback)``` method of the Skyflow client. The records parameter takes a JSON object of the records to be inserted in the below format. The options parameter takes a object of optional parameters for the insertion. `insert` method also support upsert operations. See below:

```json5
{
  "records": [
    {
        table: "string",  //table into which record should be inserted
        fields: {
            column1: "value",  //column names should match vault column names
            ///... additional fields
        }
    },
    ///...additional records
  ]
}
```

An example of an insert call is given below:

```kt
//Upsert options
val upsertArray = JSONArray()
val upsertColumn = JSONObject()
upsertColumn.put("table", "cards")
upsertColumn.put("column", "card_number")
upsertArray.put(upsertColumn)
val insertOptions = Skyflow.InsertOptions(tokens= false,upsert= upsertArray) /*indicates whether or not tokens should be returned for the inserted data. Defaults to 'true'*/
val insertCallback = InsertCallback()    //Custom callback - implementation of Skyflow.Callback
val records = JSONObject()
val recordsArray = JSONArray()
val record = JSONObject()
record.put("table", "cards")
val fields = JSONObject()
fields.put("expiry_date", "12/2028")
fields.put("cardNumber", "41111111111")
record.put("fields", fields)
recordsArray.put(record)
records.put("records", recordsArray)
skyflowClient.insert(records = records, options = insertOptions, callback = insertCallback);
```

**Response :**
```json
{
  "records": [
    {
     "table": "cards",
     "fields":{
        "cardNumber": "f3907186-e7e2-466f-91e5-48e12c2bcbc1",
        "expiry_date": "1989cb56-63da-4482-a2df-1f74cd0dd1a5"
      }
    }
  ]
}
```

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


### Step 4 :  Collect data from Elements
When the form is ready to be submitted, call the collect(options: Skyflow.CollectOptions? = nil, callback: Skyflow.Callback) method on the container object. The options parameter takes `Skyflow.CollectOptions` object.

`Skyflow.CollectOptions` takes two optional fields
- `tokens`: indicates whether tokens for the collected data should be returned or not. Defaults to 'true'
- `additionalFields`: Non-PCI elements data to be inserted into the vault which should be in the `records` object format as described in the above [Inserting data into vault](#Inserting-data-into-the-vault) section.

```kt
// NON-PCI fields object creation
val nonPCIRecords = JSONObject()
val recordsArray = JSONArray()
val record = JSONObject()
record.put("table", "persons")
val fields = JSONObject()
fields.put("gender", "MALE")
record.put("fields", fields)
recordsArray.put(record)
nonPCIRecords.put("records", recordsArray)

val options = Skyflow.CollectOptions(tokens = true, additonalFields = nonPCIRecords)
val insertCallback = InsertCallback() //Custom callback - implementation of Skyflow.callback
container.collect(options, insertCallback)
```
### End to end example of collecting data with Skyflow Elements

#### [Sample Code](https://github.com/skyflowapi/skyflow-android/blob/main/samples/src/main/java/com/Skyflow/CollectActivity.kt):
```kt
//Initialize skyflow configuration
val config = Skyflow.Configuration(vaultId = VAULT_ID, vaultURL = VAULT_URL, tokenProvider = demoTokenProvider)

//Initialize skyflow client
val skyflowClient = Skyflow.initialize(config)

//Create a CollectContainer
val container = skyflowClient.container(type = Skyflow.ContainerType.COLLECT)

//Initialize and set required options
val options = Skyflow.CollectElementOptions(required = true)

//Create Skyflow.Styles with individual Skyflow.Style variants
val baseStyle = Skyflow.Style(borderColor = Color.BLUE)
val baseTextStyle = Skyflow.Style(textColor = Color.BLACK)
val completedStyle = Skyflow.Style(textColor = Color.GREEN)
val focusTextStyle = Skyflow.Style(textColor = Color.RED)
val inputStyles = Skyflow.Styles(base = baseStyle, complete = completedStyle)
val labelStyles = Skyflow.Styles(base = baseTextStyle, focus = focusTextStyle)
val errorTextStyles = Skyflow.Styles(base = baseTextStyle)

//Create a CollectElementInput
val input = Skyflow.CollectElementInput(
       table = "cards",
       column = "cardNumber",
       type = Skyflow.ElementType.CARD_NUMBER
       inputStyles = inputStyles,
       labelStyles = labelStyles,
       errorTextStyles = errorTextStyles,
       label = "card number",
       placeholder = "card number",
)

//Create a CollectElementOptions instance
val options = Skyflow.CollectElementOptions(required = true)

//Create a Collect Element from the Collect Container
val skyflowElement = container.create(context = Context,input, options)

//Can interact with this object as a normal UIView Object and add to View

// Non-PCI fields data
val nonPCIRecords = JSONObject()
val recordsArray = JSONArray()
val record = JSONObject()
record.put("table", "persons")
val fields = JSONObject()
fields.put("gender", "MALE")
record.put("fields", fields)
recordsArray.put(record)
nonPCIRecords.put("records", recordsArray)

//Initialize and set required options for insertion
val collectOptions = Skyflow.CollectOptions(tokens = true, additionalFields = nonPCIRecords)

//Implement a custom Skyflow.Callback to be called on Insertion success/failure
public class InsertCallback: Skyflow.Callback {
    override fun onSuccess(responseBody: Any) {
        print(responseBody)
    }
    override fun onFailure(_ error: Error) {
        print(error)
    }
}

//Initialize InsertCallback which is an implementation of Skyflow.Callback interface
val insertCallback = InsertCallback()

//Call collect method on CollectContainer
container.collect(options = collectOptions, callback = insertCallback)

```
#### Sample Response :
```
{
  "records": [
    {
      "table": "cards",
      "fields": {
        "cardNumber": "f3907186-e7e2-466f-91e5-48e12c2bcbc1"
      }
    },
    {
      "table": "persons",
      "fields": {
        "gender": "12f670af-6c7d-4837-83fb-30365fbc0b1e",
      }
    }
  ]
}

```

### End to end example of upsert support with Skyflow Elements

#### [Sample Code](https://github.com/skyflowapi/skyflow-android/blob/main/samples/src/main/java/com/Skyflow/UpsertFeature.kt):
```kt
val config = Skyflow.Configuration(vaultId = VAULT_ID, vaultURL = VAULT_URL, tokenProvider = demoTokenProvider)
val skyflowClient = Skyflow.initialize(config)
val container = skyflowClient.container(type = Skyflow.ContainerType.COLLECT)
val options = Skyflow.CollectElementOptions(required = true)
val baseStyle = Skyflow.Style(borderColor = Color.BLUE)
val baseTextStyle = Skyflow.Style(textColor = Color.BLACK)
val completedStyle = Skyflow.Style(textColor = Color.GREEN)
val focusTextStyle = Skyflow.Style(textColor = Color.RED)
val inputStyles = Skyflow.Styles(base = baseStyle, complete = completedStyle)
val labelStyles = Skyflow.Styles(base = baseTextStyle, focus = focusTextStyle)
val errorTextStyles = Skyflow.Styles(base = baseTextStyle)

val cardNumberInput = Skyflow.CollectElementInput(
       table = "cards",
       column = "card_number",
       type = Skyflow.ElementType.CARD_NUMBER
       inputStyles = inputStyles,
       labelStyles = labelStyles,
       errorTextStyles = errorTextStyles,
       label = "Card number",
       placeholder = "enter your card number",
)

val nameInput = Skyflow.CollectElementInput(
       table = "cards",
       column = "full_name",
       type = Skyflow.ElementType.CARD_NUMBER
       inputStyles = inputStyles,
       labelStyles = labelStyles,
       errorTextStyles = errorTextStyles,
       label = "Full name",
       placeholder = "enter your name",
)
val cardNumberElement = container.create(context = Context,cardNumberInput, options)
val nameElement = container.create(context = Context,namerInput, options)

//Upsert options
val upsertArray = JSONArray()
val upsertColumn = JSONObject()
upsertColumn.put("table", "cards")
upsertColumn.put("column", "card_number")
upsertArray.put(upsertColumn)

val collectOptions = Skyflow.CollectOptions(tokens = true,upsert = upsertArray)

public class InsertCallback: Skyflow.Callback {
    override fun onSuccess(responseBody: Any) {
        print(responseBody)
    }
    override fun onFailure(_ error: Error) {
        print(error)
    }
}

val insertCallback = InsertCallback()
container.collect(options = collectOptions, callback = insertCallback)

```
#### Sample Response :
```
{
  "records": [
    {
      "table": "cards",
      "fields": {
        "card_number": "f3907186-e7e2-466f-91e5-48e12c2bcbc1",
        "name": "f3907186-e7e2-464f-91e5-48e12c2bfsi9"
      }
    }
  ]
}

```

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

- [**Collecting data through Composable Elements**](#collecting-data-through-composable-elements)
- [**Event Listeners on Composable Elements**](#event-listeners-on-composable-elements)
- [**Update Composable Elements**](#update-composable-elements)
- [**Event Listeners on Composable Container**](#event-listeners-on-composable-container)

## Collecting data through Composable Elements
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

When the form is ready to be submitted, call the `collect(options: Skyflow.CollectOptions? = CollectOptions(), callback: Skyflow.Callback)` method on the container object. The options parameter takes `Skyflow.CollectOptions` object.

`Skyflow.CollectOptions` takes three optional fields
- `tokens`: indicates whether tokens for the collected data should be returned or not. Defaults to 'true'
- `additionalFields`: Non-PCI elements data to be inserted into the vault which should be in the `records` object format as described in the above [Inserting data into vault](#Inserting-data-into-the-vault) section.
- `upsert`: To support upsert operations, the table containing the data and a column marked as unique in that table.

```kotlin
// NON-PCI fields object creation
val nonPCIRecords = JSONObject()
val recordsArray = JSONArray()

val record = JSONObject()
record.put("table", "persons")

val fields = JSONObject()
fields.put("gender", "MALE")

record.put("fields", fields)
recordsArray.put(record)

nonPCIRecords.put("records", recordsArray)

//Upsert options
val upsertArray = JSONArray()

val upsertColumn = JSONObject()
upsertColumn.put("table", "cards")
upsertColumn.put("column", "card_number")

upsertArray.put(upsertColumn)

val options = Skyflow.CollectOptions(tokens = true, additonalFields = nonPCIRecords, upsert = upsertArray)
val insertCallback = InsertCallback() //Custom callback - implementation of Skyflow.callback
container.collect(options, insertCallback)
```
#### End to end example of collecting data with Composable Elements

##### [Sample Code](https://github.com/skyflowapi/skyflow-android/blob/main/samples/src/main/java/com/Skyflow/ComposableActivity.kt):
```kotlin
//Initialize skyflow configuration
val config = Skyflow.Configuration(vaultID = VAULT_ID, vaultURL = VAULT_URL, tokenProvider = demoTokenProvider)

//Initialize skyflow client
val skyflowClient = Skyflow.init(config)

//Create a ComposableContainer
val container = skyflowClient.container(
  type = Skyflow.ContainerType.COMPOSABLE,
  options = ContainerOptions(layout = arrayOf(1, 2))
)

//Initialize and set required options
val options = Skyflow.CollectElementOptions(required = true)

//Create Skyflow.Styles with individual Skyflow.Style variants
val baseCardStyle = Skyflow.Style(borderColor = Color.TRANSPARENT)
val baseDateStyle = Skyflow.Style(borderColor = Color.TRANSPARENT, width = 300)
val baseCvvStyle = Skyflow.Style(borderColor = Color.TRANSPARENT, width = 200)
val completedStyle = Skyflow.Style(textColor = Color.TRANSPARENT)
val baseTextStyle = Skyflow.Style(textColor = Color.BLACK)
val focusTextStyle = Skyflow.Style(textColor = Color.RED)
val cardStyles = Skyflow.Styles(base = baseCardStyle, complete = completedStyle)
val dateStyles = Skyflow.Styles(base = baseDateStyle, complete = completedStyle)
val cvvStyles = Skyflow.Styles(base = baseCvvStyle, complete = completedStyle)
val labelStyles = Skyflow.Styles(base = baseTextStyle, focus = focusTextStyle)
val errorTextStyles = Skyflow.Styles(base = baseTextStyle)

//Create a CollectElementInput
val cardNumber = Skyflow.CollectElementInput(
  table = "cards",
  column = "cardNumber",
  type = Skyflow.ElementType.CARD_NUMBER
  inputStyles = cardStyles,
  labelStyles = labelStyles,
  errorTextStyles = errorTextStyles,
  label = "card number",
  placeholder = "card number",
)

val expDate = Skyflow.CollectElementInput(
  table = "cards",
  column = "expiryDate",
  type = Skyflow.ElementType.EXPIRATION_DATE
  inputStyles = dateStyles,
  labelStyles = labelStyles,
  errorTextStyles = errorTextStyles,
  label = "Expiry Date",
  placeholder = "mm/yy",
)

val cvv = Skyflow.CollectElementInput(
  table = "cards",
  column = "cvv",
  type = Skyflow.ElementType.CVV
  inputStyles = cvvStyles,
  labelStyles = labelStyles,
  errorTextStyles = errorTextStyles,
  label = "CVV",
  placeholder = "***",
)

//Create a CollectElementOptions instance
val options = Skyflow.CollectElementOptions(required = true)

//Create a Composable Element from the Composable Container
val cardNumberElement = container.create(context = Context, cardNumber, options)
val expDateElement = container.create(context = Context, expDate, options)
val cvvElement = container.create(context = Context, cvv, options)

//Fetch composable layout and add to main view
try {
  val composableLayout = container.getComposableLayout()
  parent.addView(composableLayout)
} catch (error: Exception) {
  println(error)
}

// Non-PCI fields data
val nonPCIRecords = JSONObject()
val recordsArray = JSONArray()

val record = JSONObject()
record.put("table", "persons")

val fields = JSONObject()
fields.put("gender", "MALE")

record.put("fields", fields)
recordsArray.put(record)

nonPCIRecords.put("records", recordsArray)

//Upsert options
val upsertArray = JSONArray()

val upsertColumn = JSONObject()
upsertColumn.put("table", "cards")
upsertColumn.put("column", "card_number")

upsertArray.put(upsertColumn)

//Initialize and set required options for insertion
val collectOptions = Skyflow.CollectOptions(tokens = true, additionalFields = nonPCIRecords, upsert = upsertArray)

//Implement a custom Skyflow.Callback to be called on Insertion success/failure
class InsertCallback: Skyflow.Callback {
  override fun onSuccess(responseBody: Any) {
    print(responseBody)
  }
  override fun onFailure(_ error: Error) {
    print(error)
  }
}

//Initialize InsertCallback which is an implementation of Skyflow.Callback interface
val insertCallback = InsertCallback()

//Call collect method on CollectContainer
container.collect(options = collectOptions, callback = insertCallback)
```
##### Sample Response :
```json
{
  "records": [
    {
      "table": "cards",
      "fields": {
        "cardNumber": "f3907186-e7e2-466f-91e5-48e12c2bcbc1",
        "expiryDate": "d0369871-91e5-466f-e7e2-48e12c2bcbc2",
        "cvv": "c7093186-466f-e7e2-91e5-48e12c2bcbc3",
      }
    },
    {
      "table": "persons",
      "fields": {
        "gender": "12f670af-6c7d-4837-83fb-30365fbc0b1e",
      }
    }
  ]
}
```

[For information on validations, see validations.](#validations)

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
-  [**Retrieving data from the vault**](#retrieving-data-from-the-vault)
-  [**Using Skyflow Elements to reveal data**](#using-skyflow-elements-to-reveal-data)
-  [**UI Error for Reveal Elements**](#ui-error-for-reveal-elements)
-  [**Set token for Reveal Elements**](#set-token-for-reveal-elements)
-  [**Set and clear altText for Reveal Elements**](#set-and-clear-alttext-for-reveal-elements)

## Retrieving data from the vault
For non-PCI use-cases, retrieving data from the vault and revealing it in the mobile can be done either using the SkyflowID's or tokens as described below

- ### Using tokens
    To retrieve record data using tokens, use the `detokenize(records)` method. The `records` parameter takes a JSON object that contains tokens for record values to fetch:

    ```json5
    {
      "records":[
        {
          "token": "string",                 // token for the record to be fetched
          "redaction": Skyflow.RedactionType // Optional. Redaction to apply for retrieved data. E.g. RedactionType.MASKED 
        }
      ]
    }
   ```
  
  Note: `redaction` defaults to [`RedactionType.PLAIN_TEXT`](#redaction-types).

  The following example code makes a detokenize call to reveal the masked value of a token:
  ```kt
  val getCallback = GetCallback() //Custom callback - implementation of Skyflow.Callback

  val records = JSONObject()
  val recordsArray = JSONArray()
  val recordObj = JSONObject()
  recordObj.put("token", "45012507-f72b-4f5c-9bf9-86b133bae719")
  recordObj.put("redaction", RedactionType.MASKED)
  recordsArray.put(recordObj)
  records.put("records", recordsArray)

  skyflowClient.detokenize(records = records, callback = getCallback)
  ```
  The sample response:
  ```json
  {
    "records": [
      {
        "token": "131e70dc-6f76-4319-bdd3-96281e051051",
        "value": "j***oe"
      }
    ]
  }
  ```

- ### Using Skyflow ID's or Unique Column Values
    For retrieving data from the vault, use the `get(records: JSONObject, options: GetOptions? = GetOptions(), callback: Skyflow.Callback)` method.
    
    The `records` parameter takes a JSON object that contains an array of the records to fetch. Each object inside array should contain:

    - Either an array of Skyflow IDs to fetch
    - Or a column name and an array of column values

    The second parameter, `options`, is a `GetOptions` object that retrieves tokens of Skyflow IDs. 

    Notes: 
    - You can use either Skyflow IDs or unique values to retrieve records. You can't use both at the same time.
    - GetOptions parameter is applicable only for retrieving tokens using Skyflow ID.
    - You can't pass GetOptions along with the redaction type.
    - `tokens` defaults to false.

    ```json5
    {
      "records":[
        {
          "ids": JSONArray(),            // Array of SkyflowID's of the records to be fetched
          "table": String,                       // name of table holding the above skyflow_id's
          "redaction": Skyflow.RedactionType     // redaction to be applied to retrieved data
        },
        {
          "table": String,                      // name of table from where records are to be fetched
          "redaction": Skyflow.RedactionType,   // redaction to be applied to retrieved data
          "columnName": String,                 // a unique column name
          "colunmnValues": JSONArray()  // Array of Column Values of the records to be fetched
        }
      ]
    }
    ```

    An example of get call to fetch records:
    ```kotlin
    val getCallback = GetCallback() //Custom callback - implementation of Skyflow.Callback

    val recordsArray = JSONArray()

    val record = JSONObject()
    val skyflowIDs = JSONArray()
    skyflowIDs.put("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
    skyflowIDs.put("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
    
    record.put("ids", skyflowIDs)
    record.put("table", "cards")
    record.put("redaction", RedactionType.PLAIN_TEXT)

    val record1 = JSONObject()
    val recordSkyflowIDs = JSONArray()
    recordSkyflowIDs.put("invalid skyflow id")   // invalid skyflow ID
    
    record1.put("ids", recordSkyflowIDs)
    record1.put("table", "cards")
    record1.put("redaction", RedactionType.PLAIN_TEXT)

    val record2 = JSONObject()
    val columnValues = JSONArray()
    columnValues.put("john.doe@gmail.com")
    columnValues.put("jane.doe@gmail.com")
    
    record2.put("table", "customers")
    record2.put("redaction", RedactionType.PLAIN_TEXT)
    record2.put("columnName", "email")
    record2.put("columnValues", columnValues)
    
    val record3 = JSONObject()
    val columnValues1 = JSONArray()
    columnValues1.put("invalid column value")    // invalid column value
    
    record3.put("table", "customers")
    record3.put("redaction", RedactionType.PLAIN_TEXT)
    record3.put("columnName", "email")
    record3.put("columnValues", columnValues1)

    recordsArray.put(record)
    recordsArray.put(record1)
    recordsArray.put(record2)
    recordsArray.put(record3)

    val records = JSONObject()
    records.put("records", recordsArray)

    skyflowClient.getById(records = records, GetOptions(), callback = getCallback)
    ```

  The sample response:
  ```json
  {
    "records": [
      {
        "fields": {
          "card_number": "4111111111111111",
          "expiry_date": "11/35",
          "fullname": "myname",
          "id": "f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9"
        },
        "table": "cards"
      },
      {
        "fields": {
          "card_number": "4111111111111111",
          "expiry_date": "10/23",
          "fullname": "sam",
          "id": "da26de53-95d5-4bdb-99db-8d8c66a35ff9"
        },
        "table": "cards"
      },
      {
        "fields": {
          "card_number": "4111111111111111",
          "email": "john@doe@gmail.com",
          "name": "john",
          "id": "f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9"
        },
        "table": "customers"
      },
      {
        "fields": {
          "card_number": "4111111111111111",
          "email": "jane@doe@gmail.com",
          "name": "jane",
          "id": "f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9"
        },
        "table": "customers"
      }
    ],
    "errors": [
      {
        "error": {
          "code": "404",
          "description": "No Records Found"
        },
        "ids": ["invalid skyflow id"]
      },
      {
        "error": {
          "code": "404",
          "description": "No Records Found"
        },
        "columnName": "customers",
        "columnValues": ["invalid column value"]
      }
    ]
  }
  ```

  An example of get call to fetch tokens:
  ```kotlin
  val getCallback = GetCallback() //Custom callback - implementation of Skyflow.Callback

  val recordsArray = JSONArray()

  val validRecord = JSONObject()
  val validSkyflowIDs = JSONArray()
  validSkyflowIDs.put("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
  validSkyflowIDs.put("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
  
  validRecord.put("ids", validSkyflowIDs)
  validRecord.put("table", "cards")

  val invalidRecord = JSONObject()
  val invalidRecordSkyflowIDs = JSONArray()
  invalidRecordSkyflowIDs.put("invalid skyflow id")   // invalid skyflow ID
  
  invalidRecord.put("ids", invalidRecordSkyflowIDs)
  invalidRecord.put("table", "cards")
  
  recordsArray.put(validRecord)
  recordsArray.put(invalidRecord)
  
  val records = JSONObject()
  records.put("records", recordsArray)

  skyflowClient.getById(records = records, GetOptions(true), callback = getCallback)
  ```

  The sample Response:
  ```json
  {
    "records": [
      {
        "fields": {
          "card_number": "9802-3257-3113-0294",
          "expiry_date": "45012507-f72b-4f5c-9bf9-86b133bae719",
          "fullname": "131e2507-f72b-4f5c-9bf9-86b133bae719",
        },
        "table": "cards"
      },
      {
        "fields": {
          "card_number": "0294-3213-3157-9802",
          "expiry_date": "131e2507-f72b-4f5c-9bf9-86b133bae719",
          "fullname": "45012507-f72b-4f5c-9bf9-86b133bae719",
        },
        "table": "cards"
      }
    ],
    "errors": [
      {
        "error": {
          "code": "404",
          "description": "No Records Found"
        },
        "ids": ["invalid skyflow id"]
      }
    ]
  }
  ```
### Redaction types
  There are four enum values in Skyflow.RedactionType:
  - `PLAIN_TEXT`
  - `MASKED`
  - `REDACTED`
  - `DEFAULT`


## Using Skyflow Elements to reveal data
Skyflow Elements can be used to securely reveal data in an application without exposing your front end to the sensitive data. This is great for use-cases like card issuance where you may want to reveal the card number to a user without increasing your PCI compliance scope.
### Step 1: Create a container
To start, create a container using the `skyflowClient.container(Skyflow.ContainerType.REVEAL)` method as shown below.
```kt
val container = skyflowClient.container(type = Skyflow.ContainerType.REVEAL)
```

### Step 2: Create a reveal Element
Next, define a Skyflow Element to reveal data as shown below:
```kt
val revealElementInput = Skyflow.RevealElementInput(
        token = "string",
        redaction = Skyflow.RedactionType,   // optional. Redaction to apply for retrieved data. E.g. RedactionType.MASKED   
        inputStyles = Skyflow.Styles(),      //optional, styles to be applied to the element
        labelStyles = Skyflow.Styles(),      //optional, styles to be applied to the label of the reveal element
        errorTextStyles = Skyflow.Styles(),  //optional styles that will be applied to the errorText of the reveal element
        label = "cardNumber"                 //optional, label for the element,
        altText = "XXXX XXXX XXXX XXXX"      //optional, string that is shown before reveal, will show token if altText is not provided 
    )

```

`Notes`: 
- `token` is optional only if it is being used in invokeConnection()
- `redaction` defaults to [`RedactionType.PLAIN_TEXT`](#redaction-types)

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
When the sensitive data is ready to be retrieved and revealed, call the `reveal()` method on the container as shown below:
```kt
val revealCallback = RevealCallback()  //Custom callback - implementation of Skyflow.Callback
container.reveal(callback = revealCallback)
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
```kt
//Initialize skyflow configuration
val config = Skyflow.Configuration(vaultId = <VAULT_ID>, vaultURL = <VAULT_URL>, tokenProvider = demoTokenProvider)

//Initialize skyflow client
val skyflowClient = Skyflow.initialize(config)

//Create a Reveal Container
val container = skyflowClient.container(type = Skyflow.ContainerType.REVEAL)


//Create Skyflow.Styles with individual Skyflow.Style variants
val baseStyle = Skyflow.Style(borderColor = Color.BLUE)
val baseTextStyle = Skyflow.Style(textColor = Color.BLACK)
val inputStyles = Skyflow.Styles(base = baseStyle)
val labelStyles = Skyflow.Styles(base = baseTextStyle)
val errorTextStyles = Skyflow.Styles(base = baseTextStyle)

//Create Reveal Elements
val cardNumberInput = Skyflow.RevealElementInput(
        token = "b63ec4e0-bbad-4e43-96e6-6bd50f483f75",
        redaction = RedactionType.MASKED,
        inputStyles = inputStyles,
        labelStyles = labelStyles,
        errorTextStyles = errorTextStyles,
        label = "cardnumber",
        altText = "XXXX XXXX XXXX XXXX"
)

val cardNumberElement = container.create(context = Context, input = cardNumberInput)

val nameInput = Skyflow.RevealElementInput(
        token = "89024714-6a26-4256-b9d4-55ad69aa4047",
        redaction = RedactionType.DEFAULT,
        inputStyles = inputStyles,
        labelStyles = labelStyles,
        errorTextStyles = errorTextStyles,
        label = "fullname",
        altText = "XXX"
)

val nameElement = container.create(context = Context,input = nameInput)

//set error to the element
nameElement.setError("custom error")

//reset error to the element
nameElement.resetError()

//Can interact with these objects as a normal UIView Object and add to View


//Implement a custom Skyflow.Callback to be called on Reveal success/failure
public class RevealCallback: Skyflow.Callback {
    override fun onSuccess(responseBody: Any) {
        print(responseBody)
    }
    override fun onFailure(exception: Exception) {
        print(exception)
    }
}

//Initialize custom Skyflow.Callback
val revealCallback = RevealCallback()

//Call reveal method on RevealContainer
container.reveal(callback = revealCallback)

```
The response below shows that some tokens assigned to the reveal elements get revealed successfully, while others fail and remain unrevealed.


#### Sample Response:Callback
```json
{
  "success": [
    {
      "token": "b63ec4e0-bbad-4e43-96e6-6bd50f483f75"
    }
  ],
 "errors": [
    {
       "id": "89024714-6a26-4256-b9d4-55ad69aa4047",
       "error": {
         "code": 404,
         "description": "Tokens not found for 89024714-6a26-4256-b9d4-55ad69aa4047"
       }
   }
  ]
}
```
## Limitation
Currently the skyflow collect elements and reveal elements can't be used in the XML layout definition, we have to add them to the views programatically.



