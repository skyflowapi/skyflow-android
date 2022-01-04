
package Skyflow.soap

import Skyflow.*
import Skyflow.utils.Utils
import android.os.Handler
import android.util.Log
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import android.os.Looper
class SoapValueCallback(
	var client: Client,
	var soapConnectionConfig: SoapConnectionConfig,
	var callback: Callback,
	var logLevel: LogLevel,
) : Callback {
	private val tag = SoapValueCallback::class.qualifiedName
	override fun onSuccess(responseBody: Any) {
		try {
			val document = doResponse(responseBody.toString())
			if (document == null) {
				return
			}
			callback.onSuccess(getXmlFromDocument(document))
		}
		catch (e:Exception){
			callback.onFailure(SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, tag = tag, logLevel = this.logLevel, arrayOf(e.message.toString())))
		}
	}

	override fun onFailure(exception: Any) {
		callback.onFailure(exception)
	}

	fun getXmlFromDocument(document: Document): String{
		val tf: TransformerFactory = TransformerFactory.newInstance()
		val transformer: Transformer = tf.newTransformer()
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
		val writer = StringWriter()
		transformer.transform(DOMSource(document), StreamResult(writer))
		val output: String = writer.getBuffer().toString() //.replaceAll("\n|\r", "")
		return output
	}

	fun doResponse(actualXml: String) : Document? {
		val factory = DocumentBuilderFactory.newInstance()
		val builder = factory.newDocumentBuilder()
		val actualXmlDocument: Document = builder.parse(InputSource(StringReader(actualXml)))
		val userXmlDocument = builder.parse(InputSource(StringReader(soapConnectionConfig.responseXML)))
		constructLookup(userXmlDocument.documentElement,"")
		lookup.forEach{
			parseActualResponse(actualXmlDocument.documentElement,it.key,it.key)
		}
		actualValues.forEach {
			val element = client.elementMap[it.key.trim()]
			if (element == null) {
				val error = SkyflowError(SkyflowErrorCode.INVALID_ID_IN_RESPONSE_XML,
					tag, logLevel, arrayOf(it.key.trim()))
				callback.onFailure(error)
				return null
			} else if (element is TextField) {
				if (!Utils.checkIfElementsMounted(element)) {
					val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
						tag, logLevel, arrayOf(element.label.text.toString()))
					callback.onFailure(error)
					return null
				}
			} else if (element is Label) {
				if (!Utils.checkIfElementsMounted(element)) {
					val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
						tag, logLevel, arrayOf(element.label.text.toString()))
					callback.onFailure(error)
					return null
				}
			}
		}
		Handler(Looper.getMainLooper()).post(Runnable {
			actualValues.forEach{
				val element = client.elementMap[it.key.trim()]
				if(element is TextField){
					element.setText(it.value.trim())
				}
				else if(element is Label){
					element.setText(it.value.trim())
				}
			}
		})
		return actualXmlDocument
	}


	var actualValues = HashMap<String,String>()
	var lookup = HashMap<String,Any>()
	var lookupEntry = HashMap<String,Any>()
	//var checkforDuplicates = false


	fun constructLookup(element: Element, path: String) {
		if(element.childNodes.length != 0 && element.firstChild.nodeValue.trim().isEmpty()) {
			var pathTemp = ""
			if(path.isEmpty())
			{
				pathTemp = element.nodeName.trim()
			}
			else
				pathTemp = path+"."+element.nodeName.trim()
			val rootList: NodeList = element.getChildNodes()
			for (i in 0 until rootList.length) {
				val child = element.childNodes.item(i)
				if (child.nodeName.trim().equals("skyflow")) {
					addLookupEntry(element, path, "")
					if (lookup[path] is MutableList<*>) {
						val entryArr = (lookup[path] as MutableList<Any>)
						lookupEntry["isFound"] = false
						entryArr.add(lookupEntry)
						lookup[path] = entryArr
					} else {
						lookupEntry["isFound"] = false
						lookup[path] = mutableListOf(lookupEntry)
					}
					lookupEntry = HashMap()
					return
				} else if (child.childNodes.length == 1 && child.firstChild.nodeValue.trim().isNotEmpty()) {
					addLookupEntry(element, pathTemp, "")
					if (lookup[pathTemp] is MutableList<*>) {
						val entryArr = (lookup[pathTemp] as MutableList<Any>)
						lookupEntry["isFound"] = false
						entryArr.add(lookupEntry)
						lookup[pathTemp] = entryArr

					} else {
						lookupEntry["isFound"] = false
						lookup[pathTemp] = mutableListOf(lookupEntry)
					}
					lookupEntry = HashMap()
					return
				}
			}
			for (i in 0 until element.childNodes.length) {
				val child = element.childNodes.item(i)
				if(child is Element)
					constructLookup(child, pathTemp)
			}

		}
		else {
			//unreachable block
		}
	}


	fun addLookupEntry(element: Element, path:String, pathFromParent:String)
	{
		for (i in 0 until element.childNodes.length) {
			val child = element.childNodes.item(i)
			if (child.nodeName.trim().equals("skyflow")) {
				if (lookupEntry["values"] != null) {
					val valuesMap = lookupEntry["values"] as HashMap<String, Any>
					val pathFromParentArr = pathFromParent.split(".").toMutableList()//to
					pathFromParentArr.removeAt(0) //to
					var pathFromParentTemp = pathFromParentArr.joinToString(".")
					if (pathFromParentTemp.isEmpty())
						pathFromParentTemp = element.nodeName.trim()
					else
						pathFromParentTemp = pathFromParentTemp + "." + element.nodeName.trim()
					valuesMap[pathFromParentTemp] = child.firstChild.nodeValue.trim()
					lookupEntry["values"] = valuesMap
				} else {
					val valuesMap = HashMap<String, Any>()
					val pathFromParentArr = pathFromParent.split(".").toMutableList() //to
					pathFromParentArr.removeAt(0) //to
					var pathFromParentTemp = pathFromParentArr.joinToString(".").trim()
					if (pathFromParentTemp.isEmpty())
						pathFromParentTemp = element.nodeName.trim()
					else
						pathFromParentTemp = pathFromParentTemp + "." + element.nodeName.trim()
					valuesMap[pathFromParentTemp] = child.firstChild.nodeValue.trim()
					lookupEntry["values"] = valuesMap

				}
			}
			else if (child.childNodes.length == 1 && child.firstChild.nodeValue.trim()
					.isNotEmpty()
			) {
				if (lookupEntry["identifiers"] != null) {
					val identifiersMap = lookupEntry["identifiers"] as HashMap<String, Any>
					var pathFromParentTemp = ""
					if (pathFromParent.isEmpty())
						pathFromParentTemp = child.nodeName.trim()
					else
						pathFromParentTemp = pathFromParent + "." + child.nodeName.trim()
					identifiersMap[pathFromParentTemp] = child.firstChild.nodeValue.trim()
					lookupEntry["identifiers"] = identifiersMap

				} else {
					val identifiersMap = HashMap<String, Any>()
					var pathFromParentTemp = ""
					if (pathFromParent.isEmpty())
						pathFromParentTemp = child.nodeName.trim()
					else
						pathFromParentTemp = pathFromParent + "." + child.nodeName.trim()
					identifiersMap[pathFromParentTemp] = child.firstChild.nodeValue.trim()
					lookupEntry["identifiers"] = identifiersMap
				}

			} else {
				var pathFromParentTemp = ""
				if (pathFromParent.isEmpty())
					pathFromParentTemp = element.nodeName.trim()
				else
					pathFromParentTemp = pathFromParent + "." + element.nodeName.trim()
				if (child is Element)
					addLookupEntry(child, path, pathFromParentTemp)
			}
		}
	}

	fun checkIfMapIsSubset(subMap:HashMap<String,String>,map:HashMap<String,String>) : Boolean{

		subMap.forEach{
			if(map[it.key] == null) return false
			if(subMap[it.key] != map[it.key])
			{
				return false
			}
		}
		return true
	}

	var tempMap = HashMap<String,String>()
	var elementMap = HashMap<String,Any>()
	fun removeElements(map:HashMap<String,String>, valuesMap:HashMap<String,String>, elementMap:HashMap<String,Any>)
	{
		valuesMap.forEach{
			if(map[it.key] != null){
				val xml = elementMap[it.key] as Element
				xml.parentNode.removeChild(xml)
				if(valuesMap[it.key] !=null)
					actualValues[(valuesMap[it.key]).toString()] = xml.firstChild.nodeValue
			}
		}
	}

	fun constructElementMap(element: Element, path: String, parent:Boolean)
	{
		if(element.childNodes.length > 0) {
			for (i in 0 until element.childNodes.length) {
				val child = element.childNodes.item(i)
				var pathTemp = ""
				if (!parent) {
					if (path.isEmpty())
						pathTemp = element.nodeName.trim()
					else
						pathTemp = path + "." + element.nodeName.trim()
				}
				if(child.firstChild ==null || child.firstChild.nodeValue==null) continue
				if (child.childNodes.length != 0 && child.firstChild.nodeValue.trim().isEmpty())
					constructElementMap(child as Element, pathTemp, false)
				else {
					if (pathTemp.isEmpty()) {
						elementMap[child.nodeName.trim()] = child
					} else
						elementMap[pathTemp + "." + child.nodeName.trim()] = child
				}
			}
		}
	}

	fun constructMap(element: Element, path: String, parent: Boolean)
	{
		if(element.childNodes.length > 0) {
			for (i in 0 until element.childNodes.length) {
				val child = element.childNodes.item(i)
				if(child.firstChild ==null || child.firstChild.nodeValue==null) continue
				var pathTemp = ""
				if (!parent) {
					if (path.isEmpty())
						pathTemp = element.nodeName.trim()
					else
						pathTemp = path + "." + element.nodeName.trim()
				}
				if (child.childNodes.length != 0 && child.firstChild.nodeValue.trim().isEmpty()) {
					constructMap(child as Element, pathTemp, false)
				}
				else {
					if (pathTemp.isEmpty()) {
						tempMap[child.nodeName.trim()] = child.firstChild.nodeValue.trim()
					} else
						tempMap[pathTemp + "." + child.nodeName.trim()] = child.firstChild.nodeValue.trim()
				}
			}
		}
	}

	fun comparisonHelper(element: Element, identifiersMap:HashMap<String,String>) : Boolean {
		tempMap = HashMap()
		constructMap(element,"",true)
		return checkIfMapIsSubset(identifiersMap,tempMap)
	}

	fun newHelper(element: Element, completePath: String){
		var detailsArr = mutableListOf<Any>()
		if(lookup[completePath] is MutableList<*>) {
			detailsArr = lookup[completePath] as MutableList<Any>
		}
		for(i in 0 until detailsArr.size) {
			val details = detailsArr.get(i)
			var detailsMap = HashMap<String,Any>()
			if(details is HashMap<*,*>)
			{
				detailsMap = details as HashMap<String, Any>
			}
			var identifierMap = HashMap<String,String>()
			if(detailsMap["identifiers"] is HashMap<*, *>)
			{
				identifierMap = detailsMap["identifiers"] as HashMap<String, String>
			}

			var valuesMap = HashMap<String,String>()
			if(detailsMap["values"] is HashMap<*,*>)
			{
				valuesMap = detailsMap["values"] as HashMap<String, String>
			}
			if(identifierMap.isEmpty() || comparisonHelper(element, identifierMap)) {
				if(!((detailsMap["isFound"] is Boolean) && (detailsMap["isFound"] as Boolean)))
				{
					detailsMap["isFound"] = true
					if(lookup[completePath] is MutableList<*>)
					{
						val tempArr = (lookup[completePath] as MutableList<Any>)
						tempArr[i] = detailsMap
						lookup[completePath] = tempArr
						elementMap = HashMap()
						constructElementMap(element,"",true)
						tempMap = HashMap()
						constructMap(element,"",true)
						removeElements(tempMap,valuesMap,elementMap)
						elementMap = HashMap()
					}
				}
			}
		}
	}

	fun parseActualResponse(element: Element, targetPath: String, completePath: String)
	{
		val tparr = targetPath.split(".").toMutableList()
		if(tparr.isNotEmpty() && tparr[0].equals(element.nodeName.trim()))
		{
			tparr.removeAt(0)
			val tp = tparr.joinToString(".")
			if(tp.isEmpty())
			{
				newHelper(element,completePath)
			}
			else
			{
				if(element.childNodes.length > 0)
				{
					for(i in 0 until  element.childNodes.length) {
						val child = element.childNodes.item(i)
						if(child is Element)
							parseActualResponse(child,tp,completePath)
					}
				}
				else {
					//unreachable code
				}
			}
		}
		else {
			//throw error
		}
	}


}