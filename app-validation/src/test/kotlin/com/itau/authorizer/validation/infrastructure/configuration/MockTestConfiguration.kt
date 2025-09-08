package com.itau.authorizer.validation.infrastructure.configuration

import com.itau.authorizer.common.application.adapter.`in`.grpc.ExecuteTransactionGrpcKt.ExecuteTransactionCoroutineStub
import com.itau.authorizer.common.application.adapter.out.grpc.RetrieveCurrentBalanceGrpcKt.RetrieveCurrentBalanceCoroutineStub
import com.itau.authorizer.validation.application.adapter.mock.dynamodb.MockDynamoDbEnhancedClient
import com.itau.authorizer.validation.application.adapter.mock.grpc.mockExecuteTransactionCoroutineStub
import com.itau.authorizer.validation.application.adapter.mock.grpc.mockRetrieveCurrentBalanceCoroutineStub
import com.itau.authorizer.validation.application.adapter.mock.grpc.mockSqsTemplate
import io.awspring.cloud.sqs.operations.SqsTemplate
import io.mockk.mockk
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.boot.test.context.TestConfiguration
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
    fun balanceChannel(): RetrieveCurrentBalanceCoroutineStub = mockRetrieveCurrentBalanceCoroutineStub()

    @Bean
    @Primary
    fun transactionChannel(): ExecuteTransactionCoroutineStub = mockExecuteTransactionCoroutineStub()

    @Bean
    @Primary
    fun sqsTemplate(): SqsTemplate = mockSqsTemplate()

    @Bean("accountTransactionDispatcher")
    fun accountTransactionDispatcher(): CoroutineDispatcher {
        val factory = Thread.ofVirtual().name("sqs-processor-", 0).factory()
        return Executors.newThreadPerTaskExecutor(factory).asCoroutineDispatcher()
    }

}
