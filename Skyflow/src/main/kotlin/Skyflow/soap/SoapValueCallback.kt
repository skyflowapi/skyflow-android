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
		val skyflowMap = HashMap<String,String>()
		val valuesForSkyflowElements = HashMap<String,String>()
		var arrayElementsMap = HashMap<String,HashMap<String,String>>()
		override fun onSuccess(responseBody: Any) {
			val document = doResponse(responseBody.toString())
			if(document == null){
				return
			}
			callback.onSuccess(getXmlFromDocument(document))
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
			if(!getPathForSkyflowElementsInUserResponse(userXmlDocument.documentElement,userXmlDocument.documentElement.nodeName))return null
			skyflowMap.forEach {
				val tempPath = it.key
				getValueForSkyflowElements(actualXmlDocument.documentElement,tempPath,tempPath,it.value)
				Log.d(it.key,it.value)

			}
			arrayElementsMap.forEach{
				Log.d(it.key,it.value.toString())
			}

			if(skyflowMap.size != valuesForSkyflowElements.size)
			{
				val error = SkyflowError(SkyflowErrorCode.NOT_FOUND_IN_RESPONSE,
					tag, logLevel, params = arrayOf("some element"))
				callback.onFailure(error)
				return null
			}
			valuesForSkyflowElements.map {
				val element = client.elementMap[it.key.trim()]
				if (element == null) {
					val error = SkyflowError(SkyflowErrorCode.INVALID_ID_IN_RESPONSE_XML,
						tag,
						logLevel,
						params = arrayOf(it.key.trim()))
					callback.onFailure(error)
					return null
				}
				if (element is TextField) {
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
				valuesForSkyflowElements.map {

					val element = client.elementMap[it.key.trim()]
					if(element is TextField){
						element.setText(it.value.trim())
					}
					else if(element is Label){
						element.setText(it.value.trim())
					}
			} })

			return actualXmlDocument
		}

		fun getPathForSkyflowElementsInUserResponse(root: Element, path:String) : Boolean {

			if(root.nodeName.equals("skyflow")) {
				if(root.firstChild.nodeValue.trim().isEmpty())
				{
					val error = SkyflowError(SkyflowErrorCode.EMPTY_ID_IN_RESPONSE_XML,tag,logLevel)
					callback.onFailure(error)
					return false
				}
	//            if(skyflowMap.containsKey(path.removeSuffix(".skyflow"))) {
	//                //already in map - duplicate
	//                val error = SkyflowError(SkyflowErrorCode.DUPLICATE_ID_IN_RESPONSE_XML,tag,logLevel, params = arrayOf(root.firstChild.nodeValue))
	//                callback.onFailure(error)
	//                return false
	//            }
				skyflowMap[path.removeSuffix(".skyflow")] = root.firstChild.nodeValue
			}
			else if(root.childNodes.length != 0 && root.firstChild.nodeValue.trim().isEmpty()) {
				val rootList: NodeList = root.getChildNodes()
				for (childIndex in 0 until rootList.getLength()) {
					val childNode: Node = rootList.item(childIndex)
					if(childNode is Element)
					{
						if(!getPathForSkyflowElementsInUserResponse(childNode, path + "." + childNode.nodeName))return false//add path here
					}

				}
			}
			else{
				val pathForArrayElements = path.removeSuffix("."+root.nodeName)
				if(arrayElementsMap[pathForArrayElements] == null ){
					val tempMap = HashMap<String,String>()
					tempMap[root.nodeName] = root.firstChild.nodeValue
					arrayElementsMap[pathForArrayElements] = tempMap
				}
				else {
					val tempMap = arrayElementsMap[pathForArrayElements]
	//                if(tempDict!!.containsKey(root.nodeName)){
	//                    //already in map - duplicate
	//                    val error = SkyflowError(SkyflowErrorCode.DUPLICATE_ID_IN_RESPONSE_XML,tag,logLevel, params = arrayOf(root.firstChild.nodeValue))
	//                    callback.onFailure(error)
	//                    return false
	//                }
					tempMap!!.set(root.nodeName, root.firstChild.nodeValue)
					arrayElementsMap[pathForArrayElements] = tempMap
				}
			}
			return true
		}

		fun getValueForSkyflowElements(element: Element, targetPath: String, completePath:String, elementId: String)
		{
			var temp = replacingFirstOccurrence(targetPath,element.nodeName,"")
			if(temp.isEmpty()){
				var tempString = completePath
				if(tempString.endsWith("."+element.nodeName))
				{
					if(tempString.length > (element.nodeName.length+1)  )
						tempString = tempString.substring(0,tempString.length-(element.nodeName.length+1))
					if(arrayElementsMap[tempString] !=null){
						val toSearch = arraySearchHelper(element,tempString)
						if(toSearch) {
							valuesForSkyflowElements[elementId] = element.firstChild.nodeValue
							element.parentNode.removeChild(element)
						}
					}
					else {
						valuesForSkyflowElements[elementId] = element.firstChild.nodeValue
                        element.parentNode.removeChild(element)
					}
				}
			}
			else {
				temp = replacingFirstOccurrence(temp,".","")
				for(child in 0 until element.childNodes.length)
				{
					if(element.childNodes.item(child) is Element)
						getValueForSkyflowElements(element.childNodes.item(child) as Element,temp,completePath,elementId)
				}
			}

		}

		fun replacingFirstOccurrence(myString:String,target: String,with:String) : String {
			val range = myString.indexOf(target)
			if(range ==-1) return myString
			return myString.replaceFirst(target,with)
		}


		fun arraySearchHelper(element: Element, path: String) : Boolean{
			val parent = element.parentNode
			var result = true
			for(i in 0 until  parent.childNodes.length){
				val child = parent.childNodes.item(i)
				if(child.nodeName.equals("#text")) continue
				val tempMap = arrayElementsMap[path]
				if(tempMap !=null) {
					if(tempMap[child.nodeName] !=null && !child.firstChild.nodeValue.trim().equals(tempMap[child.nodeName]!!.trim()))
					{
						result = false
					}
				}
			}
			return result
		}

	}