/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package Skyflow.soap

class SoapConnectionConfig(var connectionURL:String,var httpHeaders:HashMap<String,String> = HashMap(),var requestXML:String,var responseXML:String = "") {}