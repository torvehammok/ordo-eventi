package io.github.torvehammok.domain.schema.avro

import com.fasterxml.jackson.databind.JsonNode
import kotlin.collections.component1
import kotlin.collections.component2

internal fun traverseAvroJson(avroJson: JsonNode, currentField: String = "", onTypeDetectedFn: (String, JsonNode) -> Unit) {
    fun onTypeDetectedInternal(typeName: String, node: JsonNode) {
        if (typeName !in RESERVED_AVRO_WORDS) {
            onTypeDetectedFn(typeName, node)
        }
    }

    if (currentField == "type" && avroJson.isTextual) {
        val typeName = avroJson.asText()
        onTypeDetectedInternal(typeName, avroJson)
    } else if (currentField == "values" && avroJson.isTextual) {
        val typeName = avroJson.asText()
        onTypeDetectedInternal(typeName, avroJson)
    } else if (currentField == "type" && avroJson.isObject && avroJson.has("name")) {
        val typeName = avroJson.get("name").asText()
        onTypeDetectedInternal(typeName, avroJson)
    } else if (currentField == "values" && avroJson.isObject && avroJson.has("name")) {
        val typeName = avroJson.get("name").asText()
        onTypeDetectedInternal(typeName, avroJson)
    }

    when {
        avroJson.isObject -> {
            val fields = avroJson.properties()

            for ((fieldName, childNode) in fields) {
                traverseAvroJson(childNode, fieldName, onTypeDetectedFn)
            }
        }

        avroJson.isArray -> {
            for (childNode in avroJson) {
                traverseAvroJson(childNode, currentField, onTypeDetectedFn)
            }
        }
    }
}