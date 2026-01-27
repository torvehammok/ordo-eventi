package io.github.torvehammok.domain.schema.registry

import io.github.torvehammok.domain.schema.DiscoveredSchema
import io.github.torvehammok.domain.schema.RegistrySchema
import io.github.torvehammok.domain.schema.SchemaDef
import io.github.torvehammok.domain.schema.SchemasDiscoveryStrategy

class RegistrySchemaDiscoveryStrategy(private val schemas: List<RegistrySchema>) : SchemasDiscoveryStrategy {

    override fun discoverSchemas(): List<DiscoveredSchema> {
        val discoveredSchemas = mutableListOf<DiscoveredSchema>()

        for (schema in schemas) {
            val def = SchemaDef(
                subject = schema.subject,
                filename = schema.subject,
                name = schema.subject,
                packageName = "default"
            )

            val refs = schema.refs.map { ref ->
                val refSchema = schemas.find { it.subject == ref.subject }
                    ?: throw IllegalStateException("Referenced schema not found: ${ref.subject} in ${schema.subject}")

                SchemaDef(
                    subject = refSchema.subject,
                    filename = ref.name,
                    name = ref.name,
                    packageName = "default"
                )
            }

            discoveredSchemas.add(
                DiscoveredSchema(def = def, refs = refs)
            )
        }

        return discoveredSchemas
    }

}