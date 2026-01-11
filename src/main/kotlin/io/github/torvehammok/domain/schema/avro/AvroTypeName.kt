package io.github.torvehammok.domain.schema.avro

internal data class AvroTypeName(val name: String, val namespace: String?) {
    override fun toString(): String {
        return if (namespace != null) {
            "$namespace.$name"
        } else {
            name
        }
    }

}

internal val RESERVED_AVRO_WORDS = setOf(
    "null",
    "boolean",
    "int",
    "long",
    "float",
    "double",
    "bytes",
    "string",
    "record",
    "enum",
    "array",
    "map",
    "union",
    "fixed"
)
