package io.github.torvehammok.infra.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.torvehammok.Configmap
import io.github.torvehammok.replaceEnvs
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

private val log = LoggerFactory.getLogger(YamlFileConfigmap::class.java)

class YamlFileConfigmap(private val input: InputStream) : Configmap {

    private val objectMapper = ObjectMapper()
    private val yaml = Yaml(LoaderOptions().apply { isAllowDuplicateKeys = false })

    private lateinit var config: Map<String, Any>

    override fun <T> get(prefix: String, clazz: Class<T>): T {
        val fullMap = get()

        val subMap = fullMap[prefix] ?: emptyMap<String, Any>()

        if (subMap !is Map<*, *>) {
            throw IllegalArgumentException("Prefix $prefix is not a map in configmap")
        }

        return objectMapper.convertValue(subMap, clazz)
    }

    override fun get(): Map<String, Any> {
        if (::config.isInitialized) {
            return config
        }

        config = input.use {
            val load = yaml.load<Map<String, Any>>(it)
            replaceEnvs(load, DefaultEnv())
        }
        return config
    }
}

fun readYamlFileConfigmap(configmapLocation: String? = null): YamlFileConfigmap {
    val cm = if (configmapLocation != null)
        Paths.get(configmapLocation)
    else
        Paths.get("configmap.yaml")

    if (Files.notExists(cm)) {
        throw IllegalArgumentException("Configmap file not found at ${cm.toAbsolutePath()}")
    }

    log.info("Loading configmap from file: {}", cm.toAbsolutePath())
    return YamlFileConfigmap(Files.newInputStream(cm))
}