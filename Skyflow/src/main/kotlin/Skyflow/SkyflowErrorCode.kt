package Skyflow

import Skyflow.core.Messages
import android.content.res.Resources


enum class SkyflowErrorCode(val code:Int, var message:String)
{

    INVALID_VAULT_ID(101, Messages.INVALID_VAULT_ID.message),
    INVALID_VAULT_URL(102,Messages.INVALID_VAULT_URL.message),
    EMPTY_VAULT_ID(103,Messages.EMPTY_VAULT_ID.message),
    EMPTY_VAULT_URL(104,Messages.EMPTY_VAULT_URL.message),
    INVALID_BEARER_TOKEN(105,Messages.INVALID_BEARER_TOKEN.message),
    INVALID_TABLE_NAME(106,Messages.INVALID_TABLE_NAME.message),
    EMPTY_TABLE_NAME(107,Messages.EMPTY_TABLE_NAME.message),
    RECORDS_KEY_NOT_FOUND(108,Messages.RECORDS_KEY_NOT_FOUND.message),
    EMPTY_RECORDS(109,Messages.EMPTY_RECORDS.message),
    TABLE_KEY_ERROR(108,Messages.TABLE_KEY_ERROR.message),
    FIELDS_KEY_ERROR(109,Messages.FIELDS_KEY_ERROR.message),
    INVALID_COLUMN_NAME(109,Messages.INVALID_COLUMN_NAME.message),
    EMPTY_COLUMN_NAME(109,Messages.EMPTY_COLUMN_NAME.message),
    INVALID_TOKEN_ID(112,Messages.INVALID_TOKEN_ID.message),  // response is in success only, getting both successful and unsuccessful records
    EMPTY_TOKEN_ID(113,Messages.EMPTY_TOKEN_ID.message),
    ID_KEY_ERROR(114,Messages.ID_KEY_ERROR.message),
    REDACTION_KEY_ERROR(115,Messages.REDACTION_KEY_ERROR.message),
    INVALID_REDACTION_TYPE(116,Messages.INVALID_REDACTION_TYPE.message),
    INVALID_FIELD(117,Messages.INVALID_FIELD.message),
    MISSING_TOKEN(118,Messages.MISSING_TOKEN.message),
    MISSING_IDS(119,Messages.MISSING_IDS.message),
    EMPTY_RECORD_IDS(120,Messages.EMPTY_RECORD_IDS.message),
    INVALID_RECORD_ID_TYPE(130,Messages.INVALID_RECORD_ID_TYPE.message),
    MISSING_TABLE(123,Messages.MISSING_TABLE.message),
    INVALID_RECORD_TABLE_VALUE(124,Messages.INVALID_RECORD_TABLE_VALUE.message),
    INVALID_GATEWAY_URL(12,Messages.INVALID_GATEWAY_URL.message),
    EMPTY_GATEWAY_URL(1,Messages.EMPTY_GATEWAY_URL.message),
    INVALID_INPUT(12,Messages.INVALID_INPUT.message),
    REQUIRED_INPUTS_NOT_PROVIDED(12,Messages.REQUIRED_INPUTS_NOT_PROVIDED.message),
    INVALID_EVENT_TYPE(12,Messages.INVALID_EVENT_TYPE.message),
    INVALID_EVENT_LISTENER(12,Messages.INVALID_EVENT_LISTENER.message),
    UNKNOWN_ERROR(12,Messages.UNKNOWN_ERROR.message),
    TRANSACTION_ERROR(1,Messages.TRANSACTION_ERROR.message),
    CONNECTION_ERROR(12,Messages.CONNECTION_ERROR.message),
    MISSING_REDACTION_VALUE(1,Messages.MISSING_REDACTION_VALUE.message),
    ELEMENT_NOT_MOUNTED(1,Messages.ELEMENT_NOT_MOUNTED.message),
    DUPLICATE_COLUMN_FOUND(1,Messages.DUPLICATE_COLUMN_FOUND.message),
    DUPLICATE_ELEMENT_FOUND(1,Messages.DUPLICATE_ELEMENT_FOUND.message),
    INVALID_RECORDS(1,Messages.INVALID_RECORDS.message),
    INVALID_RECORD_IDS(1,Messages.INVALID_RECORD_IDS.message),
    MISSING_REDACTION(1,Messages.MISSING_REDACTION.message),
    EMPTY_KEY_IN_QUERY_PARAMS(1,Messages.EMPTY_KEY_IN_QUERY_PARAMS.message),
    EMPTY_KEY_IN_PATH_PARAMS(1,Messages.EMPTY_KEY_IN_PATH_PARAMS.message),
    EMPTY_KEY_IN_REQUEST_HEADER_PARAMS(1,Messages.EMPTY_KEY_IN_REQUEST_HEADER_PARAMS.message),
    INVALID_FIELD_IN_PATH_PARAMS(1,Messages.INVALID_FIELD_IN_PATH_PARAMS.message),
    INVALID_FIELD_IN_QUERY_PARAMS(1,Messages.INVALID_FIELD_IN_QUERY_PARAMS.message),
    INVALID_FIELD_IN_REQUEST_HEADER_PARAMS(1,Messages.INVALID_FIELD_IN_REQUEST_HEADER_PARAMS.message),
    FAILED_TO_REVEAL(1,Messages.FAILED_TO_REVEAL.message),
    NOT_FOUND_IN_RESPONSE(1,Messages.NOT_FOUND_IN_RESPONSE.message),
    BAD_REQUEST(400,Messages.BAD_REQUEST.message),
    MISSING_COLUMN(400,Messages.MISSING_COLUMN.message),
    EMPTY_FIELDS(400,Messages.EMPTY_FIELDS.message);

    @JvmName("getCode1")
    fun getCode() : Int
    {
        return this.code
    }

    @JvmName("getMessage1")
    fun getMessage() : String
    {
        return this.message
    }
}
