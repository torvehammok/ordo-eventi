package io.github.torvehammok.domain.schema

interface SchemasGraph {

    fun listNamespaces(namespace: String? = null): List<NamespaceSchemas>

    fun traverseWholeGraph(): List<SchemaDef>

    fun findDirectPredecessors(subject: String): List<SchemaDef>

    fun allSchemas(): Set<SchemaDef>

    fun traversePredecessors(subject: String): List<SchemaDef>

    fun traverseSuccessors(subjects: List<String>): List<SchemaDef>
}

data class SchemaDef(val subject: String, val filename: String, val packageName: String, val name: String)

data class NamespaceSchemas(val namespace: String, val schemas: List<Item>) {

    data class Item(val name: String, val deps: List<Dep> = emptyList())

    data class Dep(val name: String, val namespace: String? = null)

}