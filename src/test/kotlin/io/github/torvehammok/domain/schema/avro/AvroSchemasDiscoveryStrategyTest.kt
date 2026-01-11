package io.github.torvehammok.domain.schema.avro

import io.github.torvehammok.domain.sandbox.SandboxProps
import io.github.torvehammok.domain.schema.avro.AvroSchemasDiscoveryStrategy
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Test
import java.nio.file.FileSystems
import java.nio.file.Paths

class AvroSchemasDiscoveryStrategyTest {

    @Test
    fun testDiscoverAvroSchemas() {
        // given
        val sandboxProps = SandboxProps().also {
            it.enabled = true
            it.prefix = "avro-"
        }

        val strategy = AvroSchemasDiscoveryStrategy(sandboxProps, Paths.get("avro"))
        val matcher = FileSystems.getDefault().getPathMatcher("glob:common/**")

        // when
        val schemas = strategy.discoverSchemas(matcher)

        // then
        assertThat(schemas)
            .extracting({ it.def.subject }, { it.def.filename }, { ref -> ref.refs.map { it.subject } })
            .containsExactlyInAnyOrder(
                tuple("avro-common/DateRangeAvro.avsc", "common/DateRangeAvro.avsc", emptyList<String>()),
                tuple("avro-common/AddressAvro.avsc", "common/AddressAvro.avsc", emptyList<String>()),
                tuple(
                    "avro-common/CrazyAvro.avsc",
                    "common/CrazyAvro.avsc",
                    listOf("avro-common/AddressAvro.avsc", "avro-common/MoneyAvro.avsc")
                ),
                tuple(
                    "avro-common/BookingAmountsAvro.avsc",
                    "common/BookingAmountsAvro.avsc",
                    listOf("avro-common/AddressAvro.avsc", "avro-common/MoneyAvro.avsc")
                ),
                tuple("avro-common/MoneyAvro.avsc", "common/MoneyAvro.avsc", emptyList<String>())
            )
    }

    @Test
    fun testDiscoverAvroSchemasInGamingCommonModule() {
        // given
        val sandboxProps = SandboxProps().also {
            it.enabled = true
            it.prefix = "avro-"
        }

        val strategy = AvroSchemasDiscoveryStrategy(sandboxProps, Paths.get("avro"))
        val matcher = FileSystems.getDefault().getPathMatcher("glob:gaming-common/**")

        // when
        val schemas = strategy.discoverSchemas(matcher)

        // then
        assertThat(schemas)
            .extracting({ it.def.subject }, { it.def.filename }, { ref -> ref.refs.map { it.subject } })
            .containsExactlyInAnyOrder(
                tuple("avro-gaming-common/PlayerAvro.avsc", "gaming-common/PlayerAvro.avsc", emptyList<String>()),
                tuple(
                    "avro-gaming-common/BetAvro.avsc",
                    "gaming-common/BetAvro.avsc",
                    listOf("avro-common/MoneyAvro.avsc", "avro-gaming-common/PlayerAvro.avsc")
                )
            )
    }

    @Test
    fun testDiscoverProtoSchemas() {
        // given
        val sandboxProps = SandboxProps()
        val strategy = AvroSchemasDiscoveryStrategy(sandboxProps, Paths.get("src/test/proto"))


        // when
        val schemas = strategy.discoverSchemas { true }

        // then
        assertThat(schemas).isEmpty()
    }
}