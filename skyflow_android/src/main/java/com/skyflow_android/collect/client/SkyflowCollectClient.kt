package com.skyflowandroid.collect.client

import org.json.JSONArray
import org.json.JSONObject

class SkyflowCollectClient(
    workspaceUrl: String,
    vaultId: String,
    private val tokenProvider: TokenProvider
) {
    private val skyflowVault = SkyflowVault(workspaceUrl, vaultId)
    private val httpClient = SkyflowHttpClient(skyflowVault.vaultUrl, tokenProvider)

    fun tokenize(record: SkyflowRecord, callback: ApiCallback) {
        httpClient.post(
            JSONObject().put("records", record.getTokenizeRequestObject()).toString(),
            callback
        );
    }

    fun tokenize(records: List<SkyflowRecord>, callback: ApiCallback) {
        val requestJson = JSONArray();
        records.forEachIndexed { index, skyflowRecord ->
            val arr = skyflowRecord.getTokenizeRequestObject(index);
            requestJson.put(arr[0]).put(arr[1]);
        }
        httpClient.post(JSONObject().put("records", requestJson).toString(), callback);
    }

}