package io.github.torvehammok.domain.schema

interface SchemasGraph {

    fun dependencyGraph(namespace: String? = null): String

    fun dependencyGraphItems(): List<SchemaDef>

    fun listDeps(subject: String): List<SchemaDef>

    fun allSchemas(): Set<SchemaDef>

    fun traverse(changedSchemas: List<String>): List<SchemaDef>

}

data class SchemaDef(val subject: String, val filename: String, val packageName: String)