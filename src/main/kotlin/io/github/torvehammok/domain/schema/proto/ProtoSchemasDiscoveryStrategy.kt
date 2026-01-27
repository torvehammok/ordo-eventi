package io.github.torvehammok.domain.schema.proto

import com.squareup.wire.schema.internal.parser.ProtoFileElement
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchema
import io.github.torvehammok.domain.sandbox.SandboxProps
import io.github.torvehammok.domain.schema.DiscoveredSchema
import io.github.torvehammok.domain.schema.SchemaDef
import io.github.torvehammok.domain.schema.SchemasDiscoveryStrategy
import io.github.torvehammok.domain.schema.toSubjectName
import java.nio.file.Files
import java.nio.file.Path

class ProtoSchemasDiscoveryStrategy(
    private val sandboxProps: SandboxProps,
    private val schemasDir: Path
) : SchemasDiscoveryStrategy {

    override fun discoverSchemas(): List<DiscoveredSchema> {
        val discoveredSchemas = mutableListOf<DiscoveredSchema>()

        Files.walk(schemasDir).use { paths ->
            for (path in paths) {
                if (!isProtoFile(path)) {
                    continue
                }

                val relativeProtoFile = schemasDir.relativize(path)

                val protobufSchema = ProtobufSchema(Files.readString(path))
                val protoFile = protobufSchema.rawSchema()

                val allImports = toAllImports(protoFile)

                val packageName = protoFile.packageName ?: "default"

                val schemaDef = SchemaDef(
                    subject = toSubjectName(relativeProtoFile.toString(), sandboxProps),
                    filename = relativeProtoFile.toString(),
                    packageName = packageName,
                    name = relativeProtoFile.toString()
                )

                val refs = allImports.map { importDef ->
                    val importedPath = schemasDir.resolve(importDef)

                    if (!Files.exists(importedPath)) {
                        throw IllegalStateException("Imported proto file not found: $importDef in $relativeProtoFile")
                    }

                    val rawProto = Files.readString(importedPath)
                    val importedSchema = ProtobufSchema(rawProto)

                    SchemaDef(
                        subject = toSubjectName(importDef, sandboxProps),
                        filename = importDef,
                        name = importDef,
                        packageName = importedSchema.rawSchema().packageName ?: "default"
                    )
                }

                discoveredSchemas.add(
                    DiscoveredSchema(def = schemaDef, refs = refs)
                )
            }
        }

        return discoveredSchemas
    }

}

private fun isProtoFile(path: Path): Boolean {
    return Files.isRegularFile(path) && path.toString().endsWith(".proto")
}

private fun toAllImports(protoFile: ProtoFileElement): List<String> {
    return protoFile.imports
        .plus(protoFile.publicImports)
        .plus(protoFile.weakImports)
        .filter { !it.startsWith("google/protobuf/") }
}
