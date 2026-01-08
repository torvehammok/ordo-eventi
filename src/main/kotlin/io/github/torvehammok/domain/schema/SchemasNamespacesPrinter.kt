package io.github.torvehammok.domain.schema

class SchemasNamespacesPrinter {

    fun print(namespaces: List<NamespaceSchemas>): String {
        val sb = StringBuilder()

        for (schemasNamespace in namespaces) {
            sb.append("---\n")
            sb.append("namespace: ${schemasNamespace.namespace}\n")
            sb.append("schemas:\n")

            for (schema in schemasNamespace.schemas) {
                sb.append("  - name: ${schema.name}\n")
                if (schema.deps.isNotEmpty()) {
                    sb.append("    deps:\n")
                    for (dep in schema.deps) {
                        sb.append("      - name: ${dep.name}\n")
                        if (dep.namespace != null) {
                            sb.append("        namespace: ${dep.namespace}\n")
                        }
                    }
                }
            }
        }

        return sb.toString()
    }

}