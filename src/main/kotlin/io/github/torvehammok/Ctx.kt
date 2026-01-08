package io.github.torvehammok

import java.lang.AutoCloseable

interface Ctx : AutoCloseable {
    fun <T> get(clazz: Class<T>): T
}