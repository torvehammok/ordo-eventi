package io.github.torvehammok.domain

import io.github.torvehammok.OrdoEventiTest
import io.github.torvehammok.domain.schema.RegistryClient
import io.github.torvehammok.domain.schema.SchemaService
import io.github.torvehammok.domain.schema.SchemaUpdatePlanPrinter
import io.github.torvehammok.infra.config.YamlFileConfigmap
import io.github.torvehammok.infra.ctx.DefaultCtx
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SchemasServiceTest : OrdoEventiTest() {

    companion object {

        private lateinit var schemaService: SchemaService
        private lateinit var registryClient: RegistryClient

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            val configmap = YamlFileConfigmap(ClassLoader.getSystemResource("configmap-test.yaml").openStream())

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
    fun testPlanSchemasCreation() {
        val plan = schemaService.planSchemaUpdates()

        val printedPlan = SchemaUpdatePlanPrinter().print(plan)

        assertThat(printedPlan).isEqualToIgnoringWhitespace(
            """
              + Schema 'purchases/PurchaseStatus.proto needs to be created
              + Schema 'purchases/Product.proto needs to be created
              + Schema 'purchases/PaymentStatus.proto needs to be created
              + Schema 'purchases/Payment.proto needs to be created
              + Schema 'purchases/CustomerStatus.proto needs to be created
              + Schema 'purchases/CustomerDeleteProto.proto needs to be created
              + Schema 'purchases/BooFoo.proto needs to be created
              + Schema 'common/Money.proto needs to be created
              + Schema 'purchases/Offer.proto needs to be created
              + Schema 'private.topic-3-value needs to be created
              + Schema 'common/DateRange.proto needs to be created
              + Schema 'common/BookingAmounts.proto needs to be created
              + Schema 'common/Address.proto needs to be created
              + Schema 'purchases/Purchase.proto needs to be created
              + Schema 'private.topic-1-value needs to be created
              + Schema 'purchases/CustomerUpdateProto.proto needs to be created
              + Schema 'private.topic-2-value needs to be created
              + Schema 'private.payment-intent-created-value needs to be created
            """.trimIndent()
        )
    }

    @Test
    fun testExecuteSchemasCreationPlan() {
        schemaService.applySchemaUpdates()

        val schemas = registryClient.listSchemas()

        val schemaSubjects = schemas.map { it.subject }

        assertThat(schemaSubjects).containsExactlyInAnyOrder(
            "common/DateRange.proto",
            "purchases/BooFoo.proto",
            "purchases/PurchaseStatus.proto",
            "purchases/CustomerDeleteProto.proto",
            "purchases/CustomerStatus.proto",
            "purchases/PaymentStatus.proto",
            "purchases/Payment.proto",
            "purchases/Product.proto",
            "common/Address.proto",
            "common/Money.proto",
            "private.topic-3-value",
            "purchases/Offer.proto",
            "purchases/Purchase.proto",
            "private.topic-1-value",
            "purchases/CustomerUpdateProto.proto",
            "private.topic-2-value",
            "common/BookingAmounts.proto",
            "private.payment-intent-created-value"
        )
    }

    @Test
    fun testUpdateSchemasWithDeps() {
        // given:
        schemaService.applySchemaUpdates()

        // and:
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

        // when:
        val plan = schemaService.applySchemaUpdates()

        // then:
        val printedPlan = SchemaUpdatePlanPrinter().print(plan)

        assertThat(printedPlan).isEqualToIgnoringWhitespace(
            """
              # Schema 'common/Address.proto' is in sync
              # Schema 'common/DateRange.proto' is in sync
              # Schema 'private.topic-1-value' is in sync
              # Schema 'private.topic-2-value' is in sync
              # Schema 'purchases/BooFoo.proto' is in sync
              # Schema 'purchases/CustomerDeleteProto.proto' is in sync
              # Schema 'purchases/CustomerStatus.proto' is in sync
              # Schema 'purchases/Payment.proto' is in sync
              # Schema 'purchases/PaymentStatus.proto' is in sync
              # Schema 'purchases/Product.proto' is in sync
              # Schema 'purchases/PurchaseStatus.proto' is in sync
            
              ~ Schema 'common/Money.proto' needs to be updated due to changes in definition
              ~ Schema 'purchases/Offer.proto' needs to be updated due to changes in references
              ~ Schema 'purchases/Purchase.proto' needs to be updated due to changes in references
                ~ Schema 'private.topic-1-value' refs will be updated due to upstream
              ~ Schema 'purchases/CustomerUpdateProto.proto' needs to be updated due to changes in references
                ~ Schema 'private.topic-2-value' refs will be updated due to upstream
              ~ Schema 'private.topic-3-value' needs to be updated due to changes in references
              ~ Schema 'private.payment-intent-created-value' needs to be updated due to changes in references
              ~ Schema 'common/BookingAmounts.proto' needs to be updated due to changes in references
            """.trimIndent()
        )

        // and:
        val allSchemas = registryClient.listSchemas()
        val updatedMoneySchema = allSchemas.first { it.subject == "common/Money.proto" }
        assertThat(updatedMoneySchema.definition).contains("int32 foo = 3;")

        val updatedTopic3Schema = allSchemas.first { it.subject == "private.topic-3-value" }
        assertThat(updatedTopic3Schema.refs.map { it.subject }).contains("common/Money.proto")
    }


}

