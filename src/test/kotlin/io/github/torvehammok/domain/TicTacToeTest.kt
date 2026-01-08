package io.github.torvehammok.domain

import io.github.torvehammok.OrdoEventiTest
import io.github.torvehammok.domain.schema.RegistryClient
import io.github.torvehammok.domain.schema.SchemaService
import io.github.torvehammok.domain.schema.SchemaUpdatePlanPrinter
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
                import "gaming-common/Player.proto";
                
                message BetProto {
                  string id = 1;
                }
            """.trimIndent(),
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
                + Schema 'tictactoe/JoinGameCommand.proto needs to be created
                + Schema 'tictactoe.game-abandoned-value needs to be created
                + Schema 'tictactoe/GetPlayerGamesQuery.proto needs to be created
                + Schema 'tictactoe.player-joined-value needs to be created
                + Schema 'tictactoe/MakeMoveCommand.proto needs to be created
                + Schema 'tictactoe/TicTacToeMove.proto needs to be created
                + Schema 'tictactoe/ForfeitGameCommand.proto needs to be created
                ~ Schema 'common/Money.proto' needs to be updated due to changes in definition
                ~ Schema 'gaming-common/Bet.proto' needs to be updated due to changes in definition & references
                + Schema 'tictactoe/TicTacToeGameState.proto needs to be created
                + Schema 'tictactoe/GameHistoryResponse.proto needs to be created
                + Schema 'tictactoe.move-made-value needs to be created
                + Schema 'tictactoe/GameStateResponse.proto needs to be created
                + Schema 'tictactoe.game-snapshot-value needs to be created
                + Schema 'tictactoe/GamesListResponse.proto needs to be created
                + Schema 'tictactoe.game-created-value needs to be created
                + Schema 'tictactoe/GetAvailableGamesQuery.proto needs to be created
                + Schema 'tictactoe.game-finished-value needs to be created
                + Schema 'tictactoe/CreateGameCommand.proto needs to be created
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
    fun testPrintSchemasTree() {
        val deps = schemaService.findSchemasGraph()

        val dependencyGraph = deps.dependencyGraph()

        assertThat(dependencyGraph).isEqualToIgnoringWhitespace(
            """
            ---
            namespace: io.github.torvehammok.proto.common
            schemas:
              - name: common/Money.proto
              - name: common/BookingAmounts.proto
                deps:
                  - name: common/Money.proto
              - name: common/DateRange.proto
              - name: common/Address.proto
            ---
            namespace: io.github.torvehammok.proto.gaming
            schemas:
              - name: gaming-common/Player.proto
              - name: gaming-common/Bet.proto
                deps:
                  - name: common/Money.proto
                    namespace: io.github.torvehammok.proto.common
                  - name: gaming-common/Player.proto
            ---
            namespace: io.github.torvehammok.ordoeventi.proto
            schemas:
              - name: purchases/PurchaseStatus.proto
              - name: purchases/Offer.proto
                deps:
                  - name: common/Money.proto
                    namespace: io.github.torvehammok.proto.common
                  - name: purchases/PurchaseStatus.proto
              - name: purchases/Product.proto
              - name: purchases/private.payment-intent-created-value.proto
                deps:
                  - name: common/Address.proto
                    namespace: io.github.torvehammok.proto.common
                  - name: common/Money.proto
                    namespace: io.github.torvehammok.proto.common
                  - name: purchases/Offer.proto
                  - name: purchases/Product.proto
              - name: purchases/PaymentStatus.proto
              - name: purchases/Payment.proto
                deps:
                  - name: purchases/PaymentStatus.proto
              - name: purchases/Purchase.proto
                deps:
                  - name: common/Address.proto
                    namespace: io.github.torvehammok.proto.common
                  - name: common/Money.proto
                    namespace: io.github.torvehammok.proto.common
                  - name: purchases/Offer.proto
                  - name: purchases/Payment.proto
                  - name: purchases/Product.proto
              - name: purchases/private.topic-1-value.proto
                deps:
                  - name: purchases/Purchase.proto
              - name: purchases/CustomerStatus.proto
              - name: purchases/CustomerUpdateProto.proto
                deps:
                  - name: common/Address.proto
                    namespace: io.github.torvehammok.proto.common
                  - name: common/Money.proto
                    namespace: io.github.torvehammok.proto.common
                  - name: purchases/CustomerStatus.proto
              - name: purchases/private.topic-2-value.proto
                deps:
                  - name: purchases/CustomerUpdateProto.proto
              - name: purchases/CustomerDeleteProto.proto
              - name: purchases/BooFoo.proto
            ---
            namespace: io.github.torvehammok.proto.tictactoe
            schemas:
              - name: tictactoe/PlayerRole.proto
              - name: tictactoe/tictactoe.player-joined-value.proto
                deps:
                  - name: gaming-common/Player.proto
                    namespace: io.github.torvehammok.proto.gaming
                  - name: tictactoe/PlayerRole.proto
              - name: tictactoe/GetGameStateQuery.proto
              - name: tictactoe/GetGameHistoryQuery.proto
              - name: tictactoe/GameStatus.proto
              - name: tictactoe/TicTacToeGameState.proto
                deps:
                  - name: gaming-common/Bet.proto
                    namespace: io.github.torvehammok.proto.gaming
                  - name: gaming-common/Player.proto
                    namespace: io.github.torvehammok.proto.gaming
                  - name: tictactoe/GameStatus.proto
              - name: tictactoe/GameHistoryResponse.proto
                deps:
                  - name: tictactoe/TicTacToeGameState.proto
                  - name: tictactoe/TicTacToeMove.proto
              - name: tictactoe/tictactoe.move-made-value.proto
                deps:
                  - name: tictactoe/TicTacToeGameState.proto
                  - name: tictactoe/TicTacToeMove.proto
              - name: tictactoe/GameStateResponse.proto
                deps:
                  - name: tictactoe/TicTacToeGameState.proto
              - name: tictactoe/tictactoe.game-snapshot-value.proto
                deps:
                  - name: tictactoe/TicTacToeGameState.proto
              - name: tictactoe/GamesListResponse.proto
                deps:
                  - name: tictactoe/TicTacToeGameState.proto
              - name: tictactoe/GameFilter.proto
              - name: tictactoe/GetPlayerGamesQuery.proto
                deps:
                  - name: gaming-common/Player.proto
                    namespace: io.github.torvehammok.proto.gaming
                  - name: tictactoe/GameFilter.proto
              - name: tictactoe/GameEndReason.proto
              - name: tictactoe/tictactoe.game-finished-value.proto
                deps:
                  - name: common/Money.proto
                    namespace: io.github.torvehammok.proto.common
                  - name: gaming-common/Player.proto
                    namespace: io.github.torvehammok.proto.gaming
                  - name: tictactoe/GameEndReason.proto
              - name: tictactoe/GameCommandResponse.proto

            """.trimIndent()
        )
    }
}