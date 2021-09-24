package Skyflow.utils

import Skyflow.Element
import Skyflow.Label
import android.util.Log
import android.webkit.URLUtil
import org.json.JSONObject
import java.lang.Exception

class Utils {

    companion object {
        fun checkUrl(url: String): Boolean {
            if (!URLUtil.isValidUrl(url) || !URLUtil.isHttpsUrl(url))
                return false
            return true
        }

        fun gateWayConstructBody(records:JSONObject) : JSONObject
        {
            val list = JSONObject()
            try {
                val keys = records.names()
                for (j in 0 until keys!!.length()) {
                    var value:Any
                    if(records.get(keys.getString(j)) is Element )
                    {
                        value = (records.get(keys.getString(j)) as Element).getOutput()
                    }
                    else if(records.get(keys.getString(j)) is Label)
                    {
                        value = (records.get(keys.getString(j)) as Label).revealInput.token
                    }
                    else if(records.get(keys.getString(j)) is JSONObject)
                    {
                       constructJsonKey(records.get(keys.getString(j)) as JSONObject).toString()
                       value = JSONObject(records.get(keys.getString(j)).toString())
                    }
                    else
                        value = records.get(keys.getString(j)).toString()
                    list.put(keys.getString(j), value)
                }
                return list
            }
            catch (e:Exception)
            {
                return JSONObject()
            }

        }
        private fun constructJsonKey(records: JSONObject) {
            try {
                val keys = records.names()
                for (j in 0 until keys!!.length()) {
                    var value:Any
                    if(records.get(keys.getString(j)) is Element )
                    {
                        value = (records.get(keys.getString(j)) as Element).getOutput()
                    }
                    else if(records.get(keys.getString(j)) is Label)
                    {
                        value = (records.get(keys.getString(j)) as Label).revealInput.token
                    }
                    else if(records.get(keys.getString(j)) is JSONObject)
                    {
                        constructJsonKey(records.get(keys.getString(j)) as JSONObject).toString()
                        value = JSONObject(records.get(keys.getString(j)).toString())
                    }
                    else
                        value = records.get(keys.getString(j)).toString()
                    records.put(keys.getString(j),value)
                }
            }
            catch (e:Exception)
            {
                Log.d("exception in recursion",e.toString())
            }
        }
    }
}