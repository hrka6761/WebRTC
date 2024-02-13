package ir.srp.webrtc.data_converters

import com.google.gson.Gson
import java.lang.reflect.Type

object JsonConverter {

    private val gson = Gson()


    fun convertObjectToJsonString(obj: Any): String = gson.toJson(obj)

    fun convertJsonStringToObject(jsonString: String, clazz: Type): Any =
        gson.fromJson(jsonString, clazz)
}