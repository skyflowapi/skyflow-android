package Skyflow.core

import Skyflow.utils.Utils
import android.content.res.Resources
import com.skyflow_android.R

enum class Messages(val messageId: Int) {
    INVALID_URL(R.string.invalid_url),
    INITIALIZE_CLIENT(R.string.initialize_client),
    CLIENT_INITIALIZED(R.string.client_initialized),
    CREATE_COLLECT_CONTAINER(R.string.create_collect_container),
    COLLECT_CONTAINER_CREATED(R.string.collect_container_created),
    CREATE_REVEAL_CONTAINER(R.string.create_reveal_container),
    REVEAL_CONTAINER_CREATED(R.string.reveal_container_created),
    VALIDATE_RECORDS(R.string.validate_records),
    VALIDATE_DETOKENIZE_INPUT(R.string.validate_detokenize_input),
    VALIDATE_GET_BY_ID_INPUT(R.string.validate_get_by_id_input),
    VALIDATE_GATEWAY_CONFIG(R.string.validate_gateway_config),
    VALIDATE_COLLECT_RECORDS(R.string.validate_collect_records),
    VALIDATE_REVEAL_RECORDS(R.string.validate_reveal_records),
    CREATED_COLLECT_ELEMENT(R.string.created_collect_element),
    CREATED_REVEAL_ELEMENT(R.string.created_reveal_element),
    RETRIEVING_BEARER_TOKEN(R.string.tokenprovider_called),
    BEARER_TOKEN_RECEIVED(R.string.bearer_token_received),
    RETRIEVING_BEARER_TOKEN_FAILED(R.string.retrieving_bearer_token_failed),
    ELEMENT_MOUNTED(R.string.element_mounted),
    ELEMENT_REVEALED(R.string.element_revealed),
    COLLECT_SUBMIT_SUCCESS(R.string.collect_submit_success),
    REVEAL_SUBMIT_SUCCESS(R.string.reveal_submit_success),
    INSERT_DATA_SUCCESS(R.string.insert_data_success),
    DETOKENIZE_SUCCESS(R.string.detokenize_success),
    GET_BY_ID_SUCCESS(R.string.get_by_id_success),
    BEARER_TOKEN_LISTENER(R.string.bearer_token_listener),
    BEARER_TOKEN_EMITTER(R.string.bearer_token_emitter),

    INSERT_CALLED(R.string.insert_called),
    INSERTING_RECORDS(R.string.inserting_records),
    INSERTING_RECORDS_SUCCESS(R.string.insert_records_success),
    INSERTING_RECORDS_FAILED(R.string.insert_records_failed),
    DETOKENIZE_CALLED(R.string.detokenize_called),
    DETOKENIZING_RECORDS(R.string.detokenizing_records),
    DETOKENIZING_FAILED(R.string.detokenize_failed),
    GET_BY_ID_CALLED(R.string.get_by_id_called),
    GETTING_RECORDS_BY_ID_CALLED(R.string.retrieving_records_using_skyflow_ids),
    INVOKE_GATEWAY_CALLED(R.string.invoked_gateway_called),
//    EMIT_PURE_JS_REQUEST: 'Emitted %s1 request',
//    LISTEN_PURE_JS_REQUEST: 'Listening to %s1  event',
//    FETCH_RECORDS_RESOLVED: 'Detokenize request is resolved',
//    FETCH_RECORDS_REJECTED: 'Detokenize request is rejected',
//    INSERT_RECORDS_RESOLVED: 'Insert request is resolved',
//    INSERT_RECORDS_REJECTED: 'Insert request is rejected',
//    GET_BY_SKYFLOWID_RESOLVED: 'GetById request is resolved',
//    GET_BY_SKYFLOWID_REJECTED: 'GetById request is rejected',
//    SEND_INVOKE_GATEWAY_RESOLVED: 'Invoke gateway request resolved',
//    SEND_INVOKE_GATEWAY_REJECTED: 'Invoke gateway request rejected',
//    EMIT_EVENT: '%s1 event emitted',
//    LISTEN_EVENT: 'Listening to %s1',
}

fun Messages.getMessage(vararg values: String?): String{
    return Utils.constructMessage(this.messageId, *values)
}