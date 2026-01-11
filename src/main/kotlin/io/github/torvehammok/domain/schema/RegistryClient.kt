package io.github.torvehammok.domain.schema

interface RegistryClient {

    fun listSchemas(): List<RegistrySchema>

    fun updateSchema(
        subject: String,
        schemaDefinition: String,
        refs: List<RegistrySchemaRef> = emptyList(),
        minVersion: Int = -1
    ): Int

    fun deleteSchema(subject: String)

    fun deleteAllSchemas()
}

data class RegistrySchema(
    val id: Int,
    val subject: String,
    var version: Int,
    val definition: String,
    val refs: List<RegistrySchemaRef>
)

data class RegistrySchemaRef(
    val subject: String,
    val version: Int,
    val name: String
)
