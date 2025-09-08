package com.itau.authorizer.authorization.infrastructure.configuration

import com.itau.authorizer.authorization.application.adapter.mock.dynamodb.MockDynamoDbEnhancedClient
import com.itau.authorizer.authorization.application.adapter.mock.schemaregistry.MockSchemaRegistryClient
import com.itau.authorizer.authorization.application.model.kafka.TransactionExecutedKafka
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cloud.stream.schema.registry.avro.AvroSchemaMessageConverter
import org.springframework.cloud.stream.schema.registry.avro.AvroSchemaServiceManager
import org.springframework.cloud.stream.schema.registry.client.SchemaRegistryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@TestConfiguration
@Profile("test")
class MockTestConfiguration {

    @Bean
    @Primary
    fun dynamoDbEnhancedClient(): MockDynamoDbEnhancedClient = MockDynamoDbEnhancedClient.INSTANCE

    @Bean
    @Primary
    fun schemaRegistryClient(): SchemaRegistryClient = MockSchemaRegistryClient.INSTANCE

    @Bean
    @Primary
    fun kafkaDeserializer(
        manager: AvroSchemaServiceManager,
    ): AvroSchemaMessageConverter = AvroSchemaMessageConverter(manager).apply {
        schema = TransactionExecutedKafka.getClassSchema()
    }

}
