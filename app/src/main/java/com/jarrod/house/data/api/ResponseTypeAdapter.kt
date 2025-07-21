package com.jarrod.house.data.api

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import retrofit2.Response

/**
 * Custom TypeAdapter to handle Response<T> deserialization issues in release builds
 * This prevents ClassCastException: java.lang.Class cannot be cast to ParameterizedType
 */
class ResponseTypeAdapter<T>(private val dataTypeAdapter: TypeAdapter<T>) : TypeAdapter<Response<T>>() {
    
    override fun write(out: JsonWriter, value: Response<T>?) {
        if (value == null) {
            out.nullValue()
            return
        }
        
        out.beginObject()
        out.name("body")
        dataTypeAdapter.write(out, value.body())
        out.name("code")
        out.value(value.code())
        out.name("message")
        out.value(value.message())
        out.endObject()
    }
    
    override fun read(`in`: JsonReader): Response<T> {
        `in`.beginObject()
        var body: T? = null
        var code = 200
        var message = "OK"
        
        while (`in`.hasNext()) {
            when (`in`.nextName()) {
                "body" -> body = dataTypeAdapter.read(`in`)
                "code" -> code = `in`.nextInt()
                "message" -> message = `in`.nextString()
                else -> `in`.skipValue()
            }
        }
        `in`.endObject()
        
        return Response.success(body)
    }
}