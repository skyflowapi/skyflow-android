package Skyflow

import Skyflow.core.Messages
import android.content.res.Resources


enum class SkyflowErrorCode(val code:Int, var messageId:Int)
{

    INVALID_VAULT_ID(101, Messages.INVALID_VAULT_ID.messageId),
    INVALID_VAULT_URL(102,Messages.INVALID_VAULT_URL.messageId),
    EMPTY_VAULT_ID(103,Messages.EMPTY_VAULT_ID.messageId),
    EMPTY_VAULT_URL(104,Messages.EMPTY_VAULT_URL.messageId),
    INVALID_BEARER_TOKEN(105,Messages.INVALID_BEARER_TOKEN.messageId),
    INVALID_TABLE_NAME(106,Messages.INVALID_TABLE_NAME.messageId),
    EMPTY_TABLE_NAME(107,Messages.EMPTY_TABLE_NAME.messageId),
    RECORDS_KEY_NOT_FOUND(108,Messages.RECORDS_KEY_NOT_FOUND.messageId),
    EMPTY_RECORDS(109,Messages.EMPTY_RECORDS.messageId),
    TABLE_KEY_ERROR(108,Messages.TABLE_KEY_ERROR.messageId),
    FIELDS_KEY_ERROR(109,Messages.FIELDS_KEY_ERROR.messageId),
    INVALID_COLUMN_NAME(109,Messages.INVALID_COLUMN_NAME.messageId),
    EMPTY_COLUMN_NAME(109,Messages.EMPTY_COLUMN_NAME.messageId),
    INVALID_TOKEN_ID(112,Messages.INVALID_TOKEN_ID.messageId),  // response is in success only, getting both successful and unsuccessful records
    EMPTY_TOKEN_ID(113,Messages.EMPTY_TOKEN_ID.messageId),
    ID_KEY_ERROR(114,Messages.ID_KEY_ERROR.messageId),
    REDACTION_KEY_ERROR(115,Messages.REDACTION_KEY_ERROR.messageId),
    INVALID_REDACTION_TYPE(116,Messages.INVALID_REDACTION_TYPE.messageId),
    INVALID_FIELD(117,Messages.INVALID_FIELD.messageId),
    MISSING_TOKEN(118,Messages.MISSING_TOKEN.messageId),
    MISSING_IDS(119,Messages.MISSING_IDS.messageId),
    EMPTY_RECORD_IDS(120,Messages.EMPTY_RECORD_IDS.messageId),
    INVALID_RECORD_ID_TYPE(130,Messages.INVALID_RECORD_ID_TYPE.messageId),
    MISSING_TABLE(123,Messages.MISSING_TABLE.messageId),
    INVALID_RECORD_TABLE_VALUE(124,Messages.INVALID_RECORD_TABLE_VALUE.messageId),
    INVALID_GATEWAY_URL(12,Messages.INVALID_GATEWAY_URL.messageId),
    EMPTY_GATEWAY_URL(1,Messages.EMPTY_GATEWAY_URL.messageId),
    INVALID_INPUT(12,Messages.INVALID_INPUT.messageId),
    REQUIRED_INPUTS_NOT_PROVIDED(12,Messages.REQUIRED_INPUTS_NOT_PROVIDED.messageId),
    INVALID_EVENT_TYPE(12,Messages.INVALID_EVENT_TYPE.messageId),
    INVALID_EVENT_LISTENER(12,Messages.INVALID_EVENT_LISTENER.messageId),
    UNKNOWN_ERROR(12,Messages.UNKNOWN_ERROR.messageId),
    TRANSACTION_ERROR(1,Messages.TRANSACTION_ERROR.messageId),
    CONNECTION_ERROR(12,Messages.CONNECTION_ERROR.messageId),
    MISSING_REDACTION_VALUE(1,Messages.MISSING_REDACTION_VALUE.messageId),
    ELEMENT_NOT_MOUNTED(1,Messages.ELEMENT_NOT_MOUNTED.messageId),
    DUPLICATE_COLUMN_FOUND(1,Messages.DUPLICATE_COLUMN_FOUND.messageId),
    DUPLICATE_ELEMENT_FOUND(1,Messages.DUPLICATE_ELEMENT_FOUND.messageId),
    INVALID_RECORDS(1,Messages.INVALID_RECORDS.messageId),
    INVALID_RECORD_IDS(1,Messages.INVALID_RECORD_IDS.messageId),
    MISSING_REDACTION(1,Messages.MISSING_REDACTION.messageId),
    EMPTY_KEY_IN_QUERY_PARAMS(1,Messages.EMPTY_KEY_IN_QUERY_PARAMS.messageId),
    EMPTY_KEY_IN_PATH_PARAMS(1,Messages.EMPTY_KEY_IN_PATH_PARAMS.messageId),
    EMPTY_KEY_IN_REQUEST_HEADER_PARAMS(1,Messages.EMPTY_KEY_IN_REQUEST_HEADER_PARAMS.messageId),
    INVALID_FIELD_IN_PATH_PARAMS(1,Messages.INVALID_FIELD_IN_PATH_PARAMS.messageId),
    INVALID_FIELD_IN_QUERY_PARAMS(1,Messages.INVALID_FIELD_IN_QUERY_PARAMS.messageId),
    INVALID_FIELD_IN_REQUEST_HEADER_PARAMS(1,Messages.INVALID_FIELD_IN_REQUEST_HEADER_PARAMS.messageId),
    FAILED_TO_REVEAL(1,Messages.FAILED_TO_REVEAL.messageId),
    NOT_FOUND_IN_RESPONSE(1,Messages.NOT_FOUND_IN_RESPONSE.messageId),
    BAD_REQUEST(400,Messages.BAD_REQUEST.messageId);

    @JvmName("getCode1")
    fun getCode() : Int
    {
        return this.code
    }

    @JvmName("getMessage1")
    fun getMessage() : String
    {
        return Resources.getSystem().getString(this.messageId)
    }
}
