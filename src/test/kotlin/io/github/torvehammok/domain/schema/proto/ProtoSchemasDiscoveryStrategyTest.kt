package io.github.torvehammok.domain.schema.proto

import io.github.torvehammok.domain.sandbox.SandboxProps
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Test
import java.nio.file.FileSystems
import java.nio.file.Paths

private val EMPTY_REFS = emptyList<String>()

class ProtoSchemasDiscoveryStrategyTest {

    @Test
    fun testDiscoverProtoSchemas() {
        // given
        val sandboxProps = SandboxProps().also {
            it.enabled = true
            it.prefix = "proto-"
        }

        val strategy = ProtoSchemasDiscoveryStrategy(sandboxProps, Paths.get("src/test/proto"))
        val matcher = FileSystems.getDefault().getPathMatcher("glob:common/**")

        // when
        val schemas = strategy.discoverSchemas(matcher)

        // then

        assertThat(schemas)
            .extracting({ it.def.subject }, { it.def.filename }, { ref -> ref.refs.map { it.subject } })
            .containsExactlyInAnyOrder(
                tuple("proto-common/Money.proto", "common/Money.proto", EMPTY_REFS),
                tuple("proto-common/DateRange.proto", "common/DateRange.proto", EMPTY_REFS),
                tuple("proto-common/Address.proto", "common/Address.proto", EMPTY_REFS),
                tuple(
                    "proto-common/BookingAmounts.proto",
                    "common/BookingAmounts.proto",
                    listOf("proto-common/Money.proto")
                )
            )
    }

    @Test
    fun testDiscoverProtoSchemasInGamingCommonModule() {
        // given
        val sandboxProps = SandboxProps().also {
            it.enabled = true
            it.prefix = "proto-"
        }

        val strategy = ProtoSchemasDiscoveryStrategy(sandboxProps, Paths.get("src/test/proto"))
        val matcher = FileSystems.getDefault().getPathMatcher("glob:gaming-common/**")

        // when
        val schemas = strategy.discoverSchemas(matcher)

        // then
        assertThat(schemas)
            .extracting({ it.def.subject }, { it.def.filename }, { ref -> ref.refs.map { it.subject } })
            .containsExactlyInAnyOrder(
                tuple("proto-gaming-common/Player.proto", "gaming-common/Player.proto", EMPTY_REFS),
                tuple(
                    "proto-gaming-common/Bet.proto",
                    "gaming-common/Bet.proto",
                    listOf("proto-common/Money.proto", "proto-gaming-common/Player.proto")
                )
            )
    }

    @Test
    fun testDiscoverAvroSchemas() {
        // given
        val sandboxProps = SandboxProps()
        val strategy = ProtoSchemasDiscoveryStrategy(sandboxProps, Paths.get("avro"))

        val matcher = FileSystems.getDefault().getPathMatcher("glob:common/**")

        // when
        val schemas = strategy.discoverSchemas(matcher)

        // then
        assertThat(schemas).isEmpty()
    }
}