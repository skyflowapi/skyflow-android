package Skyflow.soap

internal class SoapConnectionConfig(var connectionURL:String,var httpHeaders:HashMap<String,String> = HashMap(),var requestXML:String,var responseXML:String = "") {}