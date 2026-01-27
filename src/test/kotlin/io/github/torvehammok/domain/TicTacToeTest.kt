package io.github.torvehammok.domain

import io.github.torvehammok.OrdoEventiTest
import io.github.torvehammok.domain.schema.RegistryClient
import io.github.torvehammok.domain.schema.RegistrySchemaRef
import io.github.torvehammok.domain.schema.SchemaService
import io.github.torvehammok.domain.schema.SchemaUpdatePlanPrinter
import io.github.torvehammok.domain.schema.NamespaceSchemas
import io.github.torvehammok.infra.config.YamlFileConfigmap
import io.github.torvehammok.infra.ctx.DefaultCtx
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TicTacToeTest : OrdoEventiTest() {

    companion object {

        private lateinit var schemaService: SchemaService
        private lateinit var registryClient: RegistryClient

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            val systemResource = ClassLoader.getSystemResource("configmap-tictactoetest.yaml")
            val configmap = YamlFileConfigmap(systemResource.openStream())

            val ctx = DefaultCtx(configmap)
            registryClient = ctx.get(RegistryClient::class.java)
            schemaService = ctx.get(SchemaService::class.java)
        }
    }

    @BeforeEach
    fun setUp() {
        schemaService.destroySchemas()
    }

    @Test
    fun testPrintSchemasForGamingNamespace() {
        val deps = schemaService.listSchemasNamespaces(namespace = "io.github.torvehammok.ordoeventi.proto")

        assertThat(deps).containsExactly(
            NamespaceSchemas(
                namespace = "io.github.torvehammok.ordoeventi.proto",
                schemas = listOf(
                    NamespaceSchemas.Item("purchases/PurchaseStatus.proto"),
                    NamespaceSchemas.Item(
                        name = "purchases/Offer.proto",
                        deps = listOf(
                            NamespaceSchemas.Dep(
                                name = "common/Money.proto",
                                namespace = "io.github.torvehammok.proto.common"
                            ),
                            NamespaceSchemas.Dep("purchases/PurchaseStatus.proto")
                        )
                    ),
                    NamespaceSchemas.Item("purchases/Product.proto"),
                    NamespaceSchemas.Item(
                        name = "purchases/private.payment-intent-created-value.proto",
                        deps = listOf(
                            NamespaceSchemas.Dep(
                                name = "common/Address.proto",
                                namespace = "io.github.torvehammok.proto.common"
                            ),
                            NamespaceSchemas.Dep(
                                name = "common/Money.proto",
                                namespace = "io.github.torvehammok.proto.common"
                            ),
                            NamespaceSchemas.Dep("purchases/Offer.proto"),
                            NamespaceSchemas.Dep("purchases/Product.proto")
                        )
                    ),
                    NamespaceSchemas.Item("purchases/PaymentStatus.proto"),
                    NamespaceSchemas.Item(
                        name = "purchases/Payment.proto",
                        deps = listOf(
                            NamespaceSchemas.Dep("purchases/PaymentStatus.proto")
                        )
                    ),
                    NamespaceSchemas.Item(
                        name = "purchases/Purchase.proto",
                        deps = listOf(
                            NamespaceSchemas.Dep(
                                name = "common/Address.proto",
                                namespace = "io.github.torvehammok.proto.common"
                            ),
                            NamespaceSchemas.Dep(
                                name = "common/Money.proto",
                                namespace = "io.github.torvehammok.proto.common"
                            ),
                            NamespaceSchemas.Dep("purchases/Offer.proto"),
                            NamespaceSchemas.Dep("purchases/Payment.proto"),
                            NamespaceSchemas.Dep("purchases/Product.proto")
                        )
                    ),
                    NamespaceSchemas.Item(
                        name = "purchases/private.topic-1-value.proto",
                        deps = listOf(
                            NamespaceSchemas.Dep("purchases/Purchase.proto")
                        )
                    ),
                    NamespaceSchemas.Item("purchases/CustomerStatus.proto"),
                    NamespaceSchemas.Item(
                        name = "purchases/CustomerUpdateProto.proto",
                        deps = listOf(
                            NamespaceSchemas.Dep(
                                name = "common/Address.proto",
                                namespace = "io.github.torvehammok.proto.common"
                            ),
                            NamespaceSchemas.Dep(
                                name = "common/Money.proto",
                                namespace = "io.github.torvehammok.proto.common"
                            ),
                            NamespaceSchemas.Dep("purchases/CustomerStatus.proto")
                        )
                    ),
                    NamespaceSchemas.Item(
                        name = "purchases/private.topic-2-value.proto",
                        deps = listOf(
                            NamespaceSchemas.Dep("purchases/CustomerUpdateProto.proto"),
                        )
                    ),
                    NamespaceSchemas.Item(name="purchases/CustomerDeleteProto.proto"),
                    NamespaceSchemas.Item(name="purchases/BooFoo.proto"),
                )
            )
        )
    }

    @Test
    fun testUpdateSchemasWithDeps() {
        // given:
        registryClient.updateSchema(
            "common/Money.proto",
            """
                syntax = "proto3";
                package io.github.torvehammok.proto.common;

                option java_multiple_files = true;
                
                message MoneyProto {
                    int32 foo = 3;
                    int32 bar = 4;
                    reserved 1, 2;
                }
            """.trimIndent(),
        )

        registryClient.updateSchema(
            "gaming-common/Bet.proto",
            """
                syntax = "proto3";
                
                package io.github.torvehammok.proto.gaming;
                
                option java_multiple_files = true;
                
                import "common/Money.proto";
                
                message BetProto {
                  string id = 1;
                }
            """.trimIndent(),
            directReferences = listOf(
                RegistrySchemaRef(
                    subject = "common/Money.proto",
                    version = -1,
                    name = "common/Money.proto"
                )
            )
        )

        // when:
        val plan = schemaService.applySchemaUpdates()

        // then:
        val printedPlan = SchemaUpdatePlanPrinter().print(plan)

        assertThat(printedPlan).isEqualToIgnoringWhitespace(
            """
              + Schema 'tictactoe/PlayerRole.proto needs to be created
              + Schema 'tictactoe/GetGameStateQuery.proto needs to be created
              + Schema 'tictactoe/GetGameHistoryQuery.proto needs to be created
              + Schema 'tictactoe/GameStatus.proto needs to be created
              + Schema 'tictactoe/GameFilter.proto needs to be created
              + Schema 'tictactoe/GameEndReason.proto needs to be created
              + Schema 'tictactoe/GameCommandResponse.proto needs to be created
              + Schema 'gaming-common/Player.proto needs to be created
              + Schema 'tictactoe/TicTacToeMove.proto needs to be created
              + Schema 'tictactoe/MakeMoveCommand.proto needs to be created
              + Schema 'tictactoe/JoinGameCommand.proto needs to be created
              + Schema 'tictactoe/GetPlayerGamesQuery.proto needs to be created
              + Schema 'tictactoe/ForfeitGameCommand.proto needs to be created
              + Schema 'tictactoe.player-joined-value needs to be created
              + Schema 'tictactoe.game-abandoned-value needs to be created
              ~ Schema 'common/Money.proto' needs to be updated due to changes in definition
              + Schema 'tictactoe/GetAvailableGamesQuery.proto needs to be created
              + Schema 'tictactoe/CreateGameCommand.proto needs to be created
              + Schema 'tictactoe.game-finished-value needs to be created
              + Schema 'tictactoe.game-created-value needs to be created
              ~ Schema 'gaming-common/Bet.proto' needs to be updated due to changes in definition & references
              + Schema 'tictactoe/TicTacToeGameState.proto needs to be created
              + Schema 'tictactoe/GamesListResponse.proto needs to be created
              + Schema 'tictactoe/GameStateResponse.proto needs to be created
              + Schema 'tictactoe/GameHistoryResponse.proto needs to be created
              + Schema 'tictactoe.move-made-value needs to be created
              + Schema 'tictactoe.game-snapshot-value needs to be created
              + Schema 'common/DateRange.proto needs to be created
              + Schema 'common/BookingAmounts.proto needs to be created
              + Schema 'common/Address.proto needs to be created
            """.trimIndent()
        )

        // and:
        val allSchemas = registryClient.listSchemas()

        assertThat(allSchemas).extracting({ it.subject }).contains(
            tuple("common/Money.proto"),
            tuple("gaming-common/Bet.proto"),
            tuple("tictactoe/PlayerRole.proto"),
            tuple("tictactoe/GetGameStateQuery.proto"),
            tuple("tictactoe/GetGameHistoryQuery.proto"),
            tuple("tictactoe/GameStatus.proto"),
            tuple("tictactoe/GameFilter.proto"),
            tuple("tictactoe/GameEndReason.proto"),
            tuple("tictactoe/GameCommandResponse.proto"),
            tuple("gaming-common/Player.proto"),
            tuple("tictactoe/JoinGameCommand.proto"),
            tuple("tictactoe.game-abandoned-value"),
            tuple("tictactoe/GetPlayerGamesQuery.proto"),
            tuple("tictactoe.player-joined-value"),
            tuple("tictactoe/MakeMoveCommand.proto"),
            tuple("tictactoe/TicTacToeMove.proto"),
            tuple("tictactoe/ForfeitGameCommand.proto"),
            tuple("tictactoe/TicTacToeGameState.proto"),
            tuple("tictactoe/GameHistoryResponse.proto"),
            tuple("tictactoe.move-made-value"),
            tuple("tictactoe/GameStateResponse.proto"),
            tuple("tictactoe.game-snapshot-value"),
            tuple("tictactoe/GamesListResponse.proto"),
            tuple("tictactoe.game-created-value"),
            tuple("tictactoe/GetAvailableGamesQuery.proto"),
            tuple("tictactoe.game-finished-value"),
            tuple("tictactoe/CreateGameCommand.proto"),
            tuple("common/DateRange.proto"),
            tuple("common/BookingAmounts.proto"),
        )
    }

    @Test
    fun testPrintSchemasTreeForCommonNamespace() {
        val deps = schemaService.listSchemasNamespaces(namespace = "io.github.torvehammok.proto.common")

        assertThat(deps).containsExactly(
            NamespaceSchemas(
                namespace = "io.github.torvehammok.proto.common",
                schemas = listOf(
                    NamespaceSchemas.Item("common/Money.proto"),
                    NamespaceSchemas.Item(
                        name = "common/BookingAmounts.proto",
                        deps = listOf(
                            NamespaceSchemas.Dep("common/Money.proto")
                        )
                    ),
                    NamespaceSchemas.Item("common/DateRange.proto"),
                    NamespaceSchemas.Item("common/Address.proto")
                )
            )
        )
    }
}