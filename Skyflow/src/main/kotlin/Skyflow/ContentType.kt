/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package Skyflow

enum class ContentType(var type:String) {
    APPLICATIONORJSON("application/json"),
    TEXTORPLAIN("text/plain"),
    TEXTORXML("text/xml"),
    FORMURLENCODED("application/x-www-form-urlencoded"),
    FORMDATA("multipart/form-data")
}