package com.example.myapplication

import com.example.myapplication.document.Category
import com.example.myapplication.document.DocumentCategory
import com.example.myapplication.document.NamedCategory
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import com.google.gson.Gson



class CategoryDeserializer: JsonDeserializer<Category> {

    private val NAMED_CATEGORY = "namedCategory"
    private val DOCUMENT_CATEGORY = "documentCategory"

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Category {
        if (json.isJsonObject && json.asJsonObject.has("type")) {
            val jsonObject = json.asJsonObject
            val type = jsonObject.get("type").asString
            if (NAMED_CATEGORY == type) {
                return context.deserialize(json, NamedCategory::class.java)
            } else if (DOCUMENT_CATEGORY == type) {
                return context.deserialize(json, DocumentCategory::class.java)
            }
            return Gson().fromJson(json, typeOfT)
        } else {
            return Category()
        }
    }

}