package com.itau.authorizer.authorization.infrastructure.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.schema.registry.client.ConfluentSchemaRegistryClient
import org.springframework.cloud.stream.schema.registry.client.SchemaRegistryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class SchemaRegistryConfiguration {

    @Bean
    fun schemaRegistryClient(
        @Value("\${spring.cloud.stream.kafka.binder.configuration.schema.registry.url}")
        endPoint: String,
    ): SchemaRegistryClient = ConfluentSchemaRegistryClient().apply {
        this.setEndpoint(endPoint)
    }

}
