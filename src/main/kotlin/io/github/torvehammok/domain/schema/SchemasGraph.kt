package io.github.torvehammok.domain.schema

interface SchemasGraph {

    fun listNamespaces(namespace: String? = null): List<NamespaceSchemas>

    fun dependencyGraphItems(): List<SchemaDef>

    fun findDepsForSubject(subject: String): List<SchemaDef>

    fun allSchemas(): Set<SchemaDef>

    fun traverse(changedSchemas: List<String>): List<SchemaDef>

}

data class SchemaDef(val subject: String, val filename: String, val packageName: String)

data class NamespaceSchemas(val namespace: String, val schemas: List<Item>) {

    data class Item(val name: String, val deps: List<Dep> = emptyList())

    data class Dep(val name: String, val namespace: String? = null)

}