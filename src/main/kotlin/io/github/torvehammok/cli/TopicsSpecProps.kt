package io.github.torvehammok.cli

data class TopicsSpecDefaultsProps(
    var partitions: Int = 1,
    var config: Map<String, String> = emptyMap()
)

data class TopicsSpecProps(
    var topics: List<TopicSpecProps> = emptyList(),
    var topicDefaults: TopicsSpecDefaultsProps = TopicsSpecDefaultsProps()
)


data class TopicSpecProps(
    var name: String = "",
    var partitions: Int = 0,
    var configOverrides: Map<String, String> = emptyMap(),
    var deleteTopic: Boolean = false
) {
    fun merge(defaults: TopicsSpecDefaultsProps): TopicSpecProps {
        return TopicSpecProps(
            name = this.name,
            partitions = if (this.partitions != 0) this.partitions else defaults.partitions,
            configOverrides = mergeMaps(defaults.config, this.configOverrides),
            deleteTopic = this.deleteTopic
        )
    }
}

private fun mergeMaps(defaults: Map<String, String>, overrides: Map<String, String>): Map<String, String> {
    val merged = mutableMapOf<String, String>()
    merged.putAll(defaults)
    merged.putAll(overrides)
    return merged
}
