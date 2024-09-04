package Skyflow.core

import Skyflow.utils.Utils
import com.skyflow_android.BuildConfig

private const val SDK_NAME_VERSION = "Android SDK v${BuildConfig.SDK_VERSION}"

enum class Messages(val message: String) {
    INVALID_URL("Invalid client credentials. Expecting \"https://XYZ\" for vaultURL"),
    INITIALIZE_CLIENT("Initializing skyflow client"),
    CLIENT_INITIALIZED("Initialized skyflow client successfully"),
    CREATE_COLLECT_CONTAINER("Creating Collect container"),
    COLLECT_CONTAINER_CREATED("Created Collect container successfully"),
    CREATE_REVEAL_CONTAINER("Creating Reveal container"),
    REVEAL_CONTAINER_CREATED("Created Reveal container successfully"),
    COMPOSABLE_CONTAINER_CREATED("Created Composable container successfully"),
    VALIDATE_RECORDS("Validating insert records"),
    VALIDATE_DETOKENIZE_INPUT("Validating detokenize input"),
    VALIDATE_GET_BY_ID_INPUT("Validating getByID input"),
    VALIDATE_CONNECTION_CONFIG("Validating connection config"),
    VALIDATE_COLLECT_RECORDS("Validating collect element input"),
    VALIDATE_REVEAL_RECORDS("Validating reveal element input"),
    CREATED_COLLECT_ELEMENT("Created collect element %s"),
    CREATED_REVEAL_ELEMENT("Created reveal element %s"),

    RETRIEVING_BEARER_TOKEN("Retrieving bearer token."),
    BEARER_TOKEN_RECEIVED("BearerToken received successfully."),
    RETRIEVING_BEARER_TOKEN_FAILED("Retrieving bearer token failed"),

    ELEMENT_MOUNTED("%s1 Element mounted"),
    ELEMENT_REVEALED("%s1 Element revealed"),
    COLLECT_SUBMIT_SUCCESS("Data has been collected successfully."),
    REVEAL_SUBMIT_SUCCESS("Data has been revealed successfully."),
    INSERT_DATA_SUCCESS("Data has been inserted successfully."),
    DETOKENIZE_SUCCESS("Data has been revealed successfully."),
    GET_BY_ID_SUCCESS("Data has been revealed successfully"),
    BEARER_TOKEN_LISTENER("Listening to GetBearerToken event"),
    BEARER_TOKEN_EMITTER("Emitted GetBearerToken event"),

    INSERT_CALLED("Insert method triggered"),
    INSERTING_RECORDS("Inserting records into vault with id %s"),
    INSERTING_RECORDS_SUCCESS("Successfully inserted records into vault with id %s"),
    INSERTING_RECORDS_FAILED("Failed inserting records into vault with id %s"),
    DETOKENIZE_CALLED("Detokenize method called"),
    DETOKENIZING_RECORDS("Detokenizing records"),
    DETOKENIZING_FAILED("Failed revealed data from vault with id %s"),
    GET_BY_ID_CALLED("getById method called"),
    GET_CALLED("get method called"),
    GETTING_RECORDS("retrieving data using get called"),
    GETTING_RECORDS_BY_ID_CALLED("retrieving records using skyflow ids"),
    INVOKE_CONNECTION_CALLED("invokeConnection method called"),

    // client config validations
    INVALID_VAULT_ID("vault id invalid cannot be found"),
    EMPTY_VAULT_ID("$SDK_NAME_VERSION Initialization failed. Invalid credentials. Specify a valid 'vaultID'."),
    INVALID_VAULT_URL("$SDK_NAME_VERSION Initialization failed. Invalid client credentials. 'vaultURL' must be begin with 'https://'."),
    EMPTY_VAULT_URL("$SDK_NAME_VERSION Initialization failed. Invalid credentials. Specify a valid 'vaultURL'."),
    INVALID_BEARER_TOKEN("$SDK_NAME_VERSION Token generated from 'getBearerToken' callback function is invalid. Make sure the implementation of 'getBearerToken' is correct."),
    BEARER_TOKEN_REJECTED("$SDK_NAME_VERSION 'getBearerToken' callback function call failed with rejected promise. Make sure the implementation of 'getBearerToken' is correct."),

    // record validations
    RECORDS_KEY_NOT_FOUND("$SDK_NAME_VERSION Validation error. Missing 'records' key. Provide a valid 'records' key."),
    EMPTY_RECORDS("$SDK_NAME_VERSION Validation error. 'records' key cannot be empty. Provide a non-empty value instead."),
    INVALID_RECORDS_TYPE("$SDK_NAME_VERSION Validation error. Invalid 'records' key found. Specify a value of type array instead."),
    EMPTY_RECORD_OBJECT("$SDK_NAME_VERSION Validation error. 'records' key cannot be an array of empty objects at index %s. Specify non-empty objects instead."),

    // ids validations
    IDS_KEY_NOT_FOUND("$SDK_NAME_VERSION Validation error. Missing 'ids' key in records at index %s. Provide a valid 'ids' key."),
    INVALID_IDS("$SDK_NAME_VERSION Validation error. Invalid 'ids' key found in records at index %s. Specify a value of type array instead."),
    EMPTY_RECORD_IDS("$SDK_NAME_VERSION Validation error. 'ids' key cannot be an empty array in records at index %s. Make sure to provide at least one id in array."),
    EMPTY_ID_IN_RECORD_IDS("$SDK_NAME_VERSION Validation error. 'id' cannot be empty in 'ids' array in 'records' at index %s. Specify non-empty values instead."),
    INVALID_ID_IN_RECORD_IDS("$SDK_NAME_VERSION Validation error. Invalid 'id' found in 'ids' array in 'records' at index %s. Specify a value of type string instead."),
    EMPTY_RECORD_IDS_IN_GET("$SDK_NAME_VERSION Validation error. 'ids' key cannot be an empty array in records at index %s. Make sure to provide at least one id in array."),

    // table validations
    TABLE_KEY_NOT_FOUND("$SDK_NAME_VERSION Validation error.'table' key not found in records at index %s. Provide a valid 'table' key."),
    EMPTY_TABLE_KEY("$SDK_NAME_VERSION Validation error.'table' cannot be empty in records at index %s. Specify a non-empty value instead."),
    INVALID_TABLE_NAME("$SDK_NAME_VERSION Validation error. Invalid 'table' key in records at index %s. Specify a value of type string instead."),

    // redaction validations
    REDACTION_KEY_NOT_FOUND("$SDK_NAME_VERSION Validation error. Missing 'redaction' key in records at index %s. Provide a valid 'redaction' key."),
    EMPTY_REDACTION_VALUE("$SDK_NAME_VERSION Validation error. 'redaction' key cannot be empty in records at index %s. Specify a non-empty value instead."),
    INVALID_REDACTION_TYPE("$SDK_NAME_VERSION Validation error. Invalid 'redaction' key in records at index %s. Specify a valid redaction type."),

    // special case validations - get
    REDACTION_WITH_TOKENS_NOT_SUPPORTED("$SDK_NAME_VERSION Get failed. Redaction cannot be applied when 'tokens' are set to true in get options. Either remove redaction or set 'tokens' to false."),
    TOKENS_NOT_SUPPORTED_WITH_COLUMN_DETAILS("$SDK_NAME_VERSION Validation error. 'columnName' and 'columnValues' cannot be used when 'tokens' are set to true in get options. Either set 'tokens' to false or use 'ids' instead."),
    NEITHER_IDS_NOR_COLUMN_DETAILS_SPECIFIED("$SDK_NAME_VERSION Validation error. Both 'ids' or 'columnValues' keys are missing. Either provide 'ids' or 'columnValues' with 'columnName' to fetch records."),
    BOTH_IDS_AND_COLUMN_DETAILS_SPECIFIED("$SDK_NAME_VERSION Validation error. 'ids' cannot be used when 'columnName' and 'columnValues' are passed in records at index %s. Either use 'ids' or 'columnName' and 'columnValues'."),

    // column name validations - get
    RECORD_COLUMN_NAME_NOT_FOUND("$SDK_NAME_VERSION Validation error. Missing 'columnName' in records at index %s. Provide a valid 'columnName' key."),
    EMPTY_RECORD_COLUMN_NAME("$SDK_NAME_VERSION Validation error. 'columnName' cannot be empty in records at index %s. Specify a non-empty value for instead."),
    INVALID_RECORD_COLUMN_NAME_TYPE("$SDK_NAME_VERSION Validation error. Invalid 'columnName' found in records at index %s. Specify a value of type string instead."),

    // column values validations - get
    RECORD_COLUMN_VALUES_NOT_FOUND("$SDK_NAME_VERSION Validation error. Missing 'columnValues' in records at index %s. Provide a valid 'columnValues' key."),
    EMPTY_RECORD_COLUMN_VALUES("$SDK_NAME_VERSION Validation error. 'columnValues' cannot be an empty array in records at index %s. Make sure to provide at least one columnValue in array."),
    INVALID_RECORD_COLUMN_VALUES_TYPE("$SDK_NAME_VERSION Validation error. Invalid 'columnValues' key found. Specify a value of type array instead."),
    EMPTY_COLUMN_VALUE("$SDK_NAME_VERSION Validation error. 'columnValue' cannot be empty in 'columnValues' array in 'records' at index %s. Specify non-empty values instead."),
    INVALID_COLUMN_VALUE_TYPE("$SDK_NAME_VERSION Validation error. Invalid 'columnValue' found in 'columnValues' array in 'records' at index %s. Specify a value of type string instead."),

    // token validations - detokenize
    TOKEN_KEY_NOT_FOUND("$SDK_NAME_VERSION Validation error. Missing 'token' key in records at index %s. Provide a valid 'token' key."),
    EMPTY_TOKEN("$SDK_NAME_VERSION Validation error. 'token' key cannot be empty in records at index %s. Specify a non-empty value instead."),

    // fields object validations - insert
    FIELDS_KEY_NOT_FOUND("$SDK_NAME_VERSION Validation error. Missing 'fields' key in records at index %s. Provide a valid 'fields' key."),
    EMPTY_FIELDS("$SDK_NAME_VERSION Validation error. Missing 'fields' key in records at index %s. Provide a valid 'fields' key."),
    EMPTY_FIELD_IN_FIELDS("$SDK_NAME_VERSION Validation error. 'field' cannot be empty in 'fields' in 'records' at index %s. Specify non-empty values instead."),

    // upsert options validations - insert
    EMPTY_UPSERT_OPTIONS_ARRAY("$SDK_NAME_VERSION Validation error. 'upsert' key cannot be an empty array in insert options. Make sure to add at least one table column object in upsert array."),
    ALLOW_JSON_OBJECT_IN_UPSERT("$SDK_NAME_VERSION Validation error. Invalid value in upsert array at index %s in insert options. Specify objects with 'table' and 'column' keys instead."),
    NO_TABLE_KEY_IN_UPSERT("$SDK_NAME_VERSION Validation error. Missing 'table' key in upsert array at index %s. Provide a valid 'table' key."),
    NO_COLUMN_KEY_IN_UPSERT("$SDK_NAME_VERSION Validation error. Missing 'column' key in upsert array at index %s. Provide a valid 'column' key."),
    INVALID_TABLE_IN_UPSERT_OPTION("$SDK_NAME_VERSION Validation error. Invalid 'table' key in upsert array at index %s. Specify a value of type string instead."),
    INVALID_COLUMN_IN_UPSERT_OPTION("$SDK_NAME_VERSION Validation error. Invalid 'column' key in upsert array at index %s. Specify a value of type string instead."),

    // reveal validations
    TOKEN_KEY_NOT_FOUND_REVEAL("$SDK_NAME_VERSION Validation error. Missing 'token' key for reveal element. Specify a valid value for token."),
    EMPTY_TOKEN_REVEAL("$SDK_NAME_VERSION Validation error. 'token' key cannot be empty for reveal element. Specify a non-empty value instead."),
    ELEMENT_NOT_MOUNTED_REVEAL("$SDK_NAME_VERSION Reveal failed. Make sure to mount all elements before invoking 'reveal' function."),
    ERROR_STATE_REVEAL("$SDK_NAME_VERSION Reveal failed. 'setError' is invoked on one or more elements. Make sure to reset any custom errors on all elements before invoking 'reveal' function."),

    // collect validations
    MISSING_TABLE_IN_ELEMENT("$SDK_NAME_VERSION Validation error. Missing 'table' key for %s collect element. Specify a valid value for 'table' key."),
    MISSING_COLUMN("$SDK_NAME_VERSION Validation error. Missing 'column' key for %s collect element. Specify a valid value for 'column' key."),
    ELEMENT_EMPTY_TABLE_NAME("$SDK_NAME_VERSION Validation error. 'table' cannot be empty for %s collect element. Specify a non-empty value for 'table'."),
    EMPTY_COLUMN_NAME("$SDK_NAME_VERSION Validation error. 'column' cannot be empty for %s collect element. Specify a non-empty value for 'column'."),
    DUPLICATE_COLUMN_FOUND("$SDK_NAME_VERSION Validation error. Duplicate for column %s is found for table %s. Please ensure each column within a record is unique."),
    ELEMENT_NOT_MOUNTED("$SDK_NAME_VERSION Collect failed. Make sure all elements are mounted before calling 'collect' on the container."),

    // additional fields validations - collect
    ADDITIONAL_FIELDS_RECORDS_KEY_NOT_FOUND("$SDK_NAME_VERSION Validation error.'records' key not found in additionalFields. Specify a 'records' key in additionalFields."),
    ADDITIONAL_FIELDS_INVALID_RECORDS_TYPE("$SDK_NAME_VERSION Validation error.'records' must be an array within additionalFields."),
    ADDITIONAL_FIELDS_EMPTY_RECORDS("$SDK_NAME_VERSION Validation error.'records' object cannot be empty within additionalFields. Specify a non-empty value instead."),
    ADDITIONAL_FIELDS_TABLE_KEY_NOT_FOUND("$SDK_NAME_VERSION Validation error.'table' key not found in additionalFields record at index %s. Specify a 'table' key in additionalFields record."),
    ADDITIONAL_FIELDS_INVALID_TABLE_NAME("$SDK_NAME_VERSION Validation error. Invalid 'table' key value in additionalFields record at index %s. Specify a value of type string for 'table' key."),
    ADDITIONAL_FIELDS_EMPTY_TABLE_KEY("$SDK_NAME_VERSION Validation error.'table' field cannot be empty in additionalFields record at index %s. Specify a non-empty value instead."),
    ADDITIONAL_FIELDS_FIELDS_KEY_NOT_FOUND("$SDK_NAME_VERSION Validation error.'fields' key not found in additionalFields record at index %s. Specify a 'fields' key in additionalFields record."),
    ADDITIONAL_FIELDS_EMPTY_FIELDS("$SDK_NAME_VERSION Validation error.'fields' object cannot be empty in additionalFields record at index %s. Specify a non-empty value instead."),

    TABLE_KEY_ERROR("key \'table\' is missing or payload is incorrectly formatted"),
    INVALID_COLUMN_NAME("column with given name is not present in the vault"),
    INVALID_TOKEN_ID("token provided is invalid  "),
    ID_KEY_ERROR("key \'id\' is missing in the payload provided"),
    INVALID_FIELD("invalid field %s"),
    EMPTY_COLUMN_KEY("column key cannot be empty"),
    MISSING_TOKEN_IN_CONNECTION_REQUEST("element for %s must have token"),
    INVALID_RECORD_TABLE_VALUE("invalid record table value"),
    INVALID_CONNECTION_URL("connectionURL %s is invalid "),
    INVALID_INPUT("%s"),
    REQUIRED_INPUTS_NOT_PROVIDED("required inputs are not provided"),
    INVALID_EVENT_TYPE("$SDK_NAME_VERSION Invalid event type. Specify a valid event type."),
    INVALID_EVENT_LISTENER("$SDK_NAME_VERSION  Invalid event listener. Please specify a valid event listener."),
    UNKNOWN_ERROR("%s"),
    TRANSACTION_ERROR("an error occurred during transaction"),
    CONNECTION_ERROR("error while initializing the connection"),
    DUPLICATE_ELEMENT_FOUND("Duplicate Element found in response body"),
    MISSING_REDACTION("redaction is missing"),
    EMPTY_KEY_IN_REQUEST_BODY("empty key present in request body"),
    EMPTY_KEY_IN_QUERY_PARAMS("empty key present in query parameters"),
    EMPTY_KEY_IN_PATH_PARAMS("empty key present in path parameters"),
    EMPTY_KEY_IN_REQUEST_HEADER_PARAMS("empty key present in request header"),
    INVALID_FIELD_IN_PATH_PARAMS("invalid data type %s present in path parameters"),
    INVALID_FIELD_IN_QUERY_PARAMS("invalid data type %s present in query parameters"),
    INVALID_FIELD_IN_REQUEST_HEADER_PARAMS("invalid data type %s present in request header"),
    INVALID_FIELD_IN_REQUEST_BODY("invalid data type %s present in request body"),
    FAILED_TO_REVEAL("$SDK_NAME_VERSION Reveal failed. Some errors were encountered."),
    EMPTY_CONNECTION_URL("Empty connection url is passed"),
    NOT_FOUND_IN_RESPONSE("%s is not found in response"),
    BAD_REQUEST("bad request"),
    SERVER_ERROR("Server error %s"),
    EMPTY_REQUEST_XML("RequestXML is empty"),
    INVALID_REQUEST_XML("Invalid RequestXML in SoapConnection - %s"),
    INVALID_RESPONSE_XML("Invalid ResponseXML in SoapConnection - %s"),
    NOT_FOUND_IN_RESPONSE_XML("Invalid path in responseXML. Element present under %s path is not found in response"),
    AMBIGUOUS_ELEMENT_FOUND_IN_RESPONSE_XML("Ambiguous Element found in responseXML"),
    INVALID_ID_IN_REQUEST_XML("Invalid elementId %s present in RequestXML"),
    EMPTY_ID_IN_REQUEST_XML("empty elementId present in RequestXml"),
    INVALID_ID_IN_RESPONSE_XML("Invalid elementId %s present in ResponseXML"),
    EMPTY_ID_IN_RESPONSE_XML("empty elementId present in ResponseXML"),
    DUPLICATE_ID_IN_RESPONSE_XML("duplicate Id present in ResponseXML"),
    INVALID_FORMAT_REGEX("$SDK_NAME_VERSION Validation error. Invalid value for 'regex' param found for regex in validations array at index %s. Provide a valid value regular expression for regex param."),
    NOT_VALID_TOKENS("following tokens are not valid - %s"),

    EMPTY_TOKEN_ID("token key cannot be empty"),
    MISSING_TOKEN("token key is required"),

    FAILED_TO_GET("$SDK_NAME_VERSION Get failed. Get request is rejected."),

    VALIDATE_INPUT_FORMAT_OPTIONS("Validated Input Format Options for %s"),

    INPUT_FORMATTING_NOT_SUPPORTED("$SDK_NAME_VERSION Mount failed. Format must be a non-empty string. Specify a valid format."),
    INVALID_INPUT_TRANSLATION("$SDK_NAME_VERSION Mount failed. Translation must be a non-empty object. Specify a valid translation."),
    EMPTY_INPUT_TRANSLATION("translation not passed. Switching to default translation %s"),

    MISMATCH_ELEMENT_COUNT_LAYOUT_SUM("$SDK_NAME_VERSION Mount failed. Invalid layout array values. Make sure all values in the layout array are positive numbers.")
}

fun Messages.getMessage(vararg values: String?): String {
    return Utils.constructMessage(this.message, *values)
}
