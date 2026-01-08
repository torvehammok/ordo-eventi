package io.github.torvehammok.cli

data class SchemasDefaultsProps(
    var compatibility: String = "BACKWARD",
)

data class SchemasSpecProps(
    var dir: String = "src/main/proto",
    var defaults: SchemasDefaultsProps = SchemasDefaultsProps(),
    var includeOnly : List<SchemasSpecInclusionProps> = emptyList()
)

data class SchemasSpecInclusionProps(
    var glob: String = "",
)
