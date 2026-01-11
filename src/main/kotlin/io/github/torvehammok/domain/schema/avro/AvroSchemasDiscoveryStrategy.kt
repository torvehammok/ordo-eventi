package io.github.torvehammok.domain.schema.avro

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.torvehammok.domain.sandbox.SandboxProps
import io.github.torvehammok.domain.schema.DiscoveredSchema
import io.github.torvehammok.domain.schema.SchemaDef
import io.github.torvehammok.domain.schema.SchemasDiscoveryStrategy
import io.github.torvehammok.domain.schema.toSubjectName
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.stream.Collectors

class AvroSchemasDiscoveryStrategy(
    private val sandboxProps: SandboxProps,
    private val schemasDir: Path
) : SchemasDiscoveryStrategy {

    private val objectMapper = ObjectMapper()

    override fun discoverSchemas(pathMatcher: PathMatcher): List<DiscoveredSchema> {
        val discoveredSchemas = mutableListOf<DiscoveredSchema>()

        val typesIndex = indexFilesByDeclaredType(schemasDir, objectMapper)

        Files.walk(schemasDir).use { visitedPaths ->
            for (schemaFile in visitedPaths) {
                if (!isAvscSchemaFile(schemaFile)) {
                    continue
                }

                val relativeFile = schemasDir.relativize(schemaFile)

                if (!pathMatcher.matches(relativeFile)) {
                    continue
                }

                val schemaJson = objectMapper.readTree(Files.readString(schemaFile))
                val declaredAvroType = toTypeName(schemaJson, relativeFile)

                val dependentTypes = findDependentTypeNames(
                    schemaJson = schemaJson,
                    declaredTypeName = declaredAvroType,
                    typesIndex = typesIndex
                )

                val dependentSchemaDefs = dependentTypes
                    .map { avroType ->
                        val dependentSchemaFile = typesIndex[avroType]
                            ?: throw IllegalStateException("Cannot find file for Avro type $avroType, required by $declaredAvroType")

                        val relativeDependencyFile = schemasDir.relativize(dependentSchemaFile)

                        SchemaDef(
                            subject = toSubjectName(relativeDependencyFile.toString(), sandboxProps),
                            filename = relativeDependencyFile.toString(),
                            packageName = avroType.namespace ?: "default"
                        )
                    }
                    .sortedBy { it.filename }

                discoveredSchemas.add(
                    DiscoveredSchema(
                        def = SchemaDef(
                            subject = toSubjectName(relativeFile.toString(), sandboxProps),
                            filename = relativeFile.toString(),
                            packageName = declaredAvroType.namespace ?: "default"
                        ),
                        refs = dependentSchemaDefs
                    )
                )

            }
        }

        return discoveredSchemas
    }

}

private fun findDependentTypeNames(
    schemaJson: JsonNode,
    declaredTypeName: AvroTypeName,
    typesIndex: Map<AvroTypeName, Path>
): Set<AvroTypeName> {
    val dependentTypes = mutableSetOf<AvroTypeName>()

    traverseAvroJson(schemaJson) { typeName, _ ->
        val qualifiedTypeName =
            if (typeName.contains('.'))
                AvroTypeName(
                    name = typeName.substringAfterLast('.'),
                    namespace = typeName.substringBeforeLast('.')
                )
            else AvroTypeName(name = typeName, namespace = declaredTypeName.namespace)

        if (typesIndex.containsKey(qualifiedTypeName)) {
            dependentTypes.add(qualifiedTypeName)
        } else {
            println("Warning: Cannot find declared type $qualifiedTypeName for schema")
        }
    }

    return dependentTypes
}

private fun indexFilesByDeclaredType(dir: Path, objectMapper: ObjectMapper): Map<AvroTypeName, Path> {
    return Files.walk(dir).use { paths ->
        paths
            .filter { isAvscSchemaFile(it) }
            .map {
                val avroJson = objectMapper.readTree(Files.readString(it))

                val relativeFile = dir.relativize(it)
                val declaredType = toTypeName(avroJson, relativeFile)

                declaredType to it
            }
            .collect(Collectors.toMap({ it.first }, { it.second }))
    }
}

private fun isAvscSchemaFile(path: Path): Boolean {
    return Files.isRegularFile(path) && path.fileName.toString().endsWith(".avsc")
}

private fun toTypeName(json: JsonNode, relativeFile: Path): AvroTypeName {
    return when {
        json.isObject -> {
            val name = json.get("name")
            val namespace = json.get("namespace")

            checkTypeNameIsText(name, relativeFile)
            checkNamespaceIsText(namespace, relativeFile)

            AvroTypeName(
                name = name.asText(),
                namespace = if (namespace.isNull) null else namespace.asText()
            )
        }

        json.isTextual -> AvroTypeName(name = json.asText(), namespace = null)

        else -> throw IllegalStateException("Avro schema in file $relativeFile is neither an object nor a string")
    }
}

private fun checkNamespaceIsText(namespace: JsonNode, relativeFile: Path) {
    if (namespace.isNull) {
        return
    }
    if (!namespace.isTextual) {
        throw IllegalStateException("Avro schema in file $relativeFile does not have a valid 'namespace' field")
    }
}

private fun checkTypeNameIsText(name: JsonNode, relativeFile: Path) {
    if (!name.isTextual || name.isNull) {
        throw IllegalStateException("Avro schema in file $relativeFile does not have a valid 'name' field")
    }
}
