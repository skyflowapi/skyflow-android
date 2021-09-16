# skyflow-android
---
Skyflow’s android SDK can be used to securely collect, tokenize, and display sensitive data in the mobile without exposing your front-end infrastructure to sensitive data.

# Table of Contents
- [**Installing Skyflow-android**](#installing-skyflow-android)
- [**Initializing Skyflow-android**](#initializing-skyflow-android)
- [**Securely collecting data client-side**](#securely-collecting-data-client-side)
- [**Securely revealing data client-side**](#securely-revealing-data-client-side)

# Installing skyflow-android
---

## Step 1: Generate a Personal Access Token for GitHub
- Inside you GitHub account:
- Settings -> Developer Settings -> Personal Access Tokens -> Generate new token
- Make sure you select the following scopes (“read:packages”) and Generate a token
- After Generating make sure to copy your new personal access token. You cannot see it again! The only option is to generate a new key.

## Step 2: Store your GitHub — Personal Access Token details
- Create a github.properties file within your root Android project
- In case of a public repository make sure you add this file to .gitignore for keep the token private
- Add properties gpr.usr=GITHUB_USER_NAME and gpr.key=PERSONAL_ACCESS_TOKEN
- Replace GITHUB_USER_NAME with personal / organisation Github user NAME and PERSONAL_ACCESS_TOKEN with the token generated in #Step 1

Alternatively you can also add the GPR_USER and GPR_API_KEY values to your environment variables on you local machine or build server to avoid creating a github properties file

## Step 3: Adding the dependency to the project

### Using gradle

- Add the JitPack repository to your root project build.gradle file

  ```java
  def githubProperties = new Properties() githubProperties.load(new FileInputStream(rootProject.file(“github.properties”)))
  allprojects {
     repositories {
	    ...
	      maven {
            url "https://maven.pkg.github.com/skyflowapi/skyflow-android-sdk"
            credentials
                    {
                        username = githubProperties['gpr.usr'] ?: System.getenv("GPR_USER")
                        password = githubProperties['gpr.key'] ?: System.getenv("GPR_API_KEY")
                    }
        }
     } gradle
  ```

- Add the dependency to your application's build.gradle file

  ```java
  implementation 'com.skyflowapi.android:skyflow-android-sdk:1.0.0'
  ```

### Using maven
- Add the github repository in the repositories tag and the github userName, token collected from  [Step1](#Step-1-:-Generate-a-Personal-Access-Token-for-GitHub) in the server tag to your project's settings.xml file. Make sure that the id's for both these tags are the same.

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
      <username>USERNAME</username>
      <password>TOKEN</password>
    </server>
</servers>
  ```

- Add the package dependencies to the dependencies element of your project pom.xml file
```xml
<dependency>
   <groupId>com.skyflowapi.android</groupId>
   <artifactId>skyflow-android-sdk</artifactId>
   <version>1.0.0</version>
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
    tokenProvider = demoTokenProvider
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


---
# Securely collecting data client-side
-  [**Inserting data into the vault**](#inserting-data-into-the-vault)
-  [**Using Skyflow Elements to collect data**](#using-skyflow-elements-to-collect-data)

## Inserting data into the vault

To insert data into the vault from the integrated application, use the ```insert(records: JSONObject, options: InsertOptions?= InsertOptions() , callback: Skyflow.Callback)``` method of the Skyflow client. The records parameter takes a JSON object of the records to be inserted in the below format. The options parameter takes a object of optional parameters for the insertion. See below:

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
val insertOptions = Skyflow.InsertOptions(tokens: false) /*indicates whether or not tokens should be returned for the inserted data. Defaults to 'true'*/
val insertCallback = InsertCallback()    //Custom callback - implementation of Skyflow.Callback
val records = JSONObject()
val recordsArray = JSONArray()
val record = JSONObject()
record.put("table", "cards")
val fields = JSONObject()
fields.put("cvv", "123")
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
        "cvv": "1989cb56-63da-4482-a2df-1f74cd0dd1a5"
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
   altText: String,          //optional string that acts as an initial value for the collect element
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

```kt
Skyflow.Style(
    borderColor: Int            //optional
    cornerRadius: Float         //optional
    padding: Skyflow.Padding    //optional
    borderWidth: Int            //optional
    font:  Int                  //optional
    textAlignment: Int          //optional
    textColor: Int              //optional
)
```
Here Skyflow.Padding is a class which can be used to set the padding for the collect element which takes all the left, top, right, bottom padding values.

```kt
Skyflow.Padding(left: Int, top: Int, right: Int, bottom: Int)
```

An example Skyflow.Styles object
```kt
val inputStyles = Skyflow.Styles(
        base = Skyflow.Style(),        //optional
        complete  = Skyflow.Style(),   //optional
        empty = Skyflow.Style(),       //optional
        focus = Skyflow.Style(),       //optional
        invalid = Skyflow.Style()      //optional
    )
```

The `labelStyles` and `errorTextStyles` fields accept the above mentioned `Skyflow.Styles` object which are applied to the `label` and `errorText` text views respectively.

The states that are available for `labelStyles` are `base` and `focus`.

The state that is available for `errorTextStyles` is only the `base` state, it shows up when there is some error in the collect element.

The parameters in `Skyflow.Style` object that are respected for `label` and `errorText` text views are
- padding
- font
- textColor
- textAlignment

Other parameters in the `Skyflow.Style` object are ignored for `label` and `errorText` text views.

Finally, the `type` field takes a Skyflow ElementType. Each type applies the appropriate regex and validations to the form element. There are currently 4 types:
- `CARDHOLDER_NAME`
- `CARD_NUMBER`
- `EXPIRATION_DATE`
- `CVV`

Once the `Skyflow.CollectElementInput` and `Skyflow.CollectElementOptions` objects are defined, add to the container using the ```create(context:Context,input: CollectElementInput, options: CollectElementOptions)``` method as shown below. The `input` param takes a `Skyflow.CollectElementInput` object as defined above and the `options` parameter takes a `Skyflow.CollectElementOptions`, 
the `context` param takes android `Context` object as described below:

```kt
val collectElementInput =  Skyflow.CollectElementInput(
        table = "string",            //the table this data belongs to
        column = "string",           //the column into which this data should be inserted
        type = Skyflow.ElementType.CARD_NUMBER,   //Skyflow.ElementType enum
        inputStyles = Skyflow.Styles(),     /*optional styles that should be applied to the form element*/
        labelStyles = Skyflow.Styles(), //optional styles that will be applied to the label of the collect element
        errorTextStyles = Skyflow.Styles(),  //optional styles that will be applied to the errorText of the collect element
        label = "string",            //optional label for the form element
        placeholder = "string",      //optional placeholder for the form element
        altText = String,          //optional string that acts as an initial value for the collect element
)

val collectElementOptions = Skyflow.CollectElementOptions(required: false)  //indicates whether the field is marked as required. Defaults to 'false'

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

The Skyflow Element is an implementation of native android View so it can be used/mounted similarly.

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

#### Sample Code:
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
---
# Securely revealing data client-side
-  [**Retrieving data from the vault**](#retrieving-data-from-the-vault)
-  [**Using Skyflow Elements to reveal data**](#using-skyflow-elements-to-reveal-data)

## Retrieving data from the vault
For non-PCI use-cases, to retrieve data from the vault and reveal it in the mobile, use the `get(records)` method. The records parameter takes a JSON object that contains `records` to be fetched as shown below.
```json5
{
    "records":[
        {
          token: "string",                 // token for the record to be fetched
          redaction: Skyflow.RedactionType    //redaction to be applied to retrieved data
        }
    ]
}

```
There are four enum values in Skyflow.RedactionType:
- `PLAIN_TEXT`
- `MASKED`
- `REDACTED`
- `DEFAULT`

An example of a get call:
```kt
val getCallback = GetCallback() //Custom callback - implementation of Skyflow.Callback

val recods = JSONObject()
val recordsArray = JSONArray()
val recordObj = JSONObject()
recordObj.put("token", "45012507-f72b-4f5c-9bf9-86b133bae719")
recordObj.put("redaction", RedactionType.PLAIN_TEXT)
recordsArray.put(recordObj)
records.put("records", recordsArray)

skyflowClient.get(records = records, callback = getCallback)
```
The sample response:
```json
{
  "records": [
    {
      "token": "131e70dc-6f76-4319-bdd3-96281e051051",
      "date_of_birth": "1990-01-01",
    }
  ]
}
```

## Using Skyflow Elements to reveal data
Skyflow Elements can be used to securely reveal data in an application without exposing your front end to the sensitive data. This is great for use-cases like card issuance where you may want to reveal the card number to a user without increasing your PCI compliance scope.
### Step 1: Create a container
To start, create a container using the `skyflowClient.container(Skyflow.ContainerType.REVEAL)` method as shown below.
```kt
val container = skyflowClient.container(type = Skyflow.ContainerType.REVEAL)
```

### Step 2: Create a reveal Element
Then define a Skyflow Element to reveal data as shown below.
```kt
val revealElementInput = Skyflow.RevealElementInput(
        token = "string",
        redaction = Skyflow.RedactionType.DEFAULT,
        inputStyles = Skyflow.Styles(),        //optional, styles to be applied to the element
        labelStyles = Skyflow.Styles(),  //optional, styles to be applied to the label of the reveal element
        errorTextStyles = Skyflow.Styles(),  //optional styles that will be applied to the errorText of the reveal element
        label = "cardNumber"            //optional, label for the element,
        altText = "XXXX XXXX XXXX XXXX" //optional, string that is shown before reveal, will show token if altText is not provided
    )
```
The `inputStyles` parameter accepts a styles object as described in the [previous section](#step-2-create-a-collect-element) for collecting data but the only state available for a reveal element is the base state. For a list of acceptable redaction types, see the [section above](#Retrieving-data-from-the-vault).

The `labelStyles` and `errorTextStyles` fields accept the above mentioned `Skyflow.Styles` object as described in the [previous section](#step-2-create-a-collect-element), the only state available for a reveal element is the base state.

Once you've defined a `Skyflow.RevealElementInput` object, you can use the `create()` method of the container to create the Element as shown below:

```kt
val element = container.create(context = Context, input = revealElementInput)
```

### Step 3: Mount Elements to the Screen

Elements used for revealing data are mounted to the screen the same way as Elements used for collecting data. Refer to Step 3 of the [section above](#step-3-mount-elements-to-the-screen).

### Step 4: Reveal data
When the sensitive data is ready to be retrieved and revealed, call the `reveal()` method on the container as shown below:
```kt
val revealCallback = RevealCallback()  //Custom callback - implementation of Skyflow.Callback
container.reveal(callback = revealCallback)
```

### End to end example of revealing data with Skyflow Elements
#### Sample Code:
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
        redaction = Skyflow.RedactionType.PLAIN_TEXT,
        inputStyles = inputStyles,
        labelStyles = labelStyles,
        errorTextStyles = errorTextStyles,
        label = "cardnumber",
        altText = "XXXX XXXX XXXX XXXX"
)

val cardNumberElement = container.create(context = Context, input = cardNumberInput)

val cvvInput = Skyflow.RevealElementInput(
        token = "89024714-6a26-4256-b9d4-55ad69aa4047",
        redaction = Skyflow.RedactionType.PLAIN_TEXT
        inputStyles = inputStyles,
        labelStyles = labelStyles,
        errorTextStyles = errorTextStyles,
        label = "cvv",
        altText = "XXX"
)

val cvvElement = container.create(context = Context,input = cvvInput)

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


#### Sample Response:
```json
{
  "success": [
    {
      "id": "b63ec4e0-bbad-4e43-96e6-6bd50f483f75"
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