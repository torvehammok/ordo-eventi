package io.github.torvehammok.infra.config

import io.github.torvehammok.Env

class DefaultEnv : Env {
    override fun get(key: String): String? {
        return System.getenv(key) ?: System.getProperty(key)
    }
}