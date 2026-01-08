package io.github.torvehammok

interface Env {
    fun get(key: String): String?
}