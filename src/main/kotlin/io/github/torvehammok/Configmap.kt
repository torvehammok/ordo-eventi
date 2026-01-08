package io.github.torvehammok

interface Configmap {

    fun <T> get(prefix: String, clazz: Class<T>): T

    fun get(): Map<String, Any>

}

/**
 * Recursively replaces environment variable placeholders in the given map.
 * Placeholders are in the format `$env:VAR_NAME`.
 *
 * @param map The input map with potential environment variable placeholders.
 * @param env The environment interface to fetch variable values.
 * @return A new map with all placeholders replaced by their corresponding environment variable values.
 */
fun replaceEnvs(map: Map<String, Any>, env: Env): Map<String, Any> {
    return map.mapValues { entry ->
        when (val value = entry.value) {
            is String -> replaceEnvInString(value, env)
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                replaceEnvs(value as Map<String, Any>, env)
            }

            is Iterable<*> -> {
                value.map { item ->
                    when (item) {
                        is String -> replaceEnvInString(item, env)
                        is Map<*, *> -> {
                            @Suppress("UNCHECKED_CAST")
                            replaceEnvs(item as Map<String, Any>, env)
                        }

                        else -> item
                    }
                }
            }

            else -> value
        }
    }

}

fun replaceEnvInString(value: String, env: Env): String {
    if (!value.startsWith("\$env:")) {
        return value
    }

    val envKey = value.removePrefix("\$env:")
    return env.get(envKey) ?: throw IllegalArgumentException("Environment variable $envKey not found")
}