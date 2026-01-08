package io.github.torvehammok.domain.topic

import io.github.torvehammok.infra.ctx.KafkaAdminProps
import org.apache.kafka.clients.admin.AdminClient
import java.lang.AutoCloseable

class AdminClientsPool(private val kafkaAdminProps: KafkaAdminProps) : AutoCloseable {

    private lateinit var cachedAdminClient: AdminClient

    fun <T> withAdminClient(action: (AdminClient) -> T): T {
        synchronized(this) {
            if (!::cachedAdminClient.isInitialized) {
                cachedAdminClient = AdminClient.create(kafkaAdminProps.config)
            }

            return action(cachedAdminClient)
        }
    }

    override fun close() {
        if (::cachedAdminClient.isInitialized) {
            cachedAdminClient.close()
        }
    }

}