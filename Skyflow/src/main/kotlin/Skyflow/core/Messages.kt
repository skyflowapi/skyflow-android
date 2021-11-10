package Skyflow.core

import Skyflow.utils.Utils
import com.skyflow_android.R

enum class Messages(val message: String) {
    INVALID_URL("URL %s is invalid"),
    INITIALIZE_CLIENT("Initializing skyflow client"),
    CLIENT_INITIALIZED("Initialized skyflow client successfully"),
    CREATE_COLLECT_CONTAINER("Creating Collect container"),
    COLLECT_CONTAINER_CREATED("Created Collect container successfully"),
    CREATE_REVEAL_CONTAINER("Creating Reveal container"),
    REVEAL_CONTAINER_CREATED("Created Reveal container successfully"),
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
    GETTING_RECORDS_BY_ID_CALLED("retrieving records using skyflow ids"),
    INVOKE_CONNECTION_CALLED("invokeConnection method called"),


    INVALID_VAULT_ID("vault id invalid cannot be found"),
    INVALID_VAULT_URL("vault url %s is invalid or not secure"),
    EMPTY_VAULT_ID("vaultid is empty."),
    EMPTY_VAULT_URL("vault url is empty."),
    INVALID_BEARER_TOKEN("bearer token is invalid or expired"),
    INVALID_TABLE_NAME("Key 'table' doesn't have a value of type String"),
    EMPTY_TABLE_NAME("Table Name is empty"),
    RECORDS_KEY_NOT_FOUND("records object key value not found"),
    EMPTY_RECORDS("records object is empty"),
    TABLE_KEY_ERROR("key \'table\' is missing or payload is incorrectly formatted"),
    FIELDS_KEY_ERROR("key \'fields\' is missing or payload is incorrectly formatted"),
    INVALID_COLUMN_NAME("column with given name is not present in the vault"),
    EMPTY_COLUMN_NAME("column name is empty"),
    INVALID_TOKEN_ID("token provided is invalid  "),
    EMPTY_TOKEN_ID("tokenid is empty"),
    ID_KEY_ERROR("key \'id\' is missing in the payload provided"),
    REDACTION_KEY_ERROR("key \'redaction\' is missing in the payload provided"),
    INVALID_REDACTION_TYPE("provided redaction type value doesn’t match with one of : \'plain_text\', \'redacted\' ,\'default\' or \'masked\'"),
    INVALID_FIELD("invalid field %s"),
    MISSING_TOKEN("missing token property"),
    MISSING_KEY_IDS("Key 'ids' is not present in the JSON object passed."),
    EMPTY_RECORD_IDS("record ids cannot be empty"),
    INVALID_RECORD_ID_TYPE("invalid type of records id"),
    MISSING_TABLE("missing table property"),
    INVALID_RECORD_TABLE_VALUE("invalid record table value"),
    INVALID_CONNECTION_URL("invalid connection url %s"),
    INVALID_INPUT("%s"),
    REQUIRED_INPUTS_NOT_PROVIDED("required inputs are not provided"),
    INVALID_EVENT_TYPE("provide a valid event type"),
    INVALID_EVENT_LISTENER("provide valid event listener"),
    UNKNOWN_ERROR("%s"),
    TRANSACTION_ERROR("an error occurred during transaction"),
    CONNECTION_ERROR("error while initializing the connection"),
    MISSING_REDACTION_VALUE("missing redaction value"),
    ELEMENT_NOT_MOUNTED("element %s not mounted"),
    DUPLICATE_COLUMN_FOUND("Duplicate element with <TABLE_NAME> and <COLUMN_NAME> found in container"),
    DUPLICATE_ELEMENT_FOUND("Duplicate Element found in response body"),
    INVALID_RECORDS_TYPE("Key 'records' is of invalid type"),
    INVALID_RECORD_IDS("ids are not valid"),
    MISSING_REDACTION("redaction is missing"),
    EMPTY_KEY_IN_REQUEST_BODY("empty key present in request body"),
    EMPTY_KEY_IN_QUERY_PARAMS("empty key present in query parameters"),
    EMPTY_KEY_IN_PATH_PARAMS("empty key present in path parameter"),
    EMPTY_KEY_IN_REQUEST_HEADER_PARAMS("empty key present in request header"),
    INVALID_FIELD_IN_PATH_PARAMS("invalid data type %s present in path parameters"),
    INVALID_FIELD_IN_QUERY_PARAMS("invalid data type %s present in query parameters"),
    INVALID_FIELD_IN_REQUEST_HEADER_PARAMS("invalid data type %s present in request header"),
    INVALID_FIELD_IN_REQUEST_BODY("invalid data type %s present in request body"),
    FAILED_TO_REVEAL("Failed to reveal"),
    EMPTY_CONNECTION_URL("Empty connection url is passed"),
    NOT_FOUND_IN_RESPONSE("%s is not found in response"),
    BAD_REQUEST("bad request"),
    MISSING_COLUMN("column name is missing"),
    EMPTY_FIELDS("fields is empty"),
    SERVER_ERROR("Server error %s"),
}

fun Messages.getMessage(vararg values: String?): String{
    return Utils.constructMessage(this.message, *values)
}