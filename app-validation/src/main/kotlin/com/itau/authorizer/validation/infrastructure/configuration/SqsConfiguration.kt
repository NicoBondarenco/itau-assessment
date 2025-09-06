package com.itau.authorizer.validation.infrastructure.configuration

import com.itau.authorizer.validation.infrastructure.properties.SqsClientProperties
import com.itau.authorizer.validation.infrastructure.properties.SqsListenerProperties
import io.awspring.cloud.autoconfigure.sqs.SqsProperties
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory
import io.awspring.cloud.sqs.operations.SqsTemplate
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder


@Configuration
@EnableConfigurationProperties(
    value = [
        SqsListenerProperties::class,
        SqsClientProperties::class,
    ]
)
class SqsConfiguration {

    @Bean
    fun sqsAsyncHttpClient(
        sqsClientProperties: SqsClientProperties,
    ): SdkAsyncHttpClient = NettyNioAsyncHttpClient.builder()
        .maxConcurrency(sqsClientProperties.maxConcurrency)
        .maxPendingConnectionAcquires(sqsClientProperties.maxPendingConnectionAcquires)
        .connectionAcquisitionTimeout(sqsClientProperties.connectionAcquisitionTimeoutDuration)
        .connectionTimeout(sqsClientProperties.connectionTimeoutDuration)
        .connectionTimeToLive(sqsClientProperties.connectionTimeToLiveDuration)
        .build()

    @Bean
    fun sqsAsyncClientBuilder(
        sqsProperties: SqsProperties,
        sqsAsyncHttpClient: SdkAsyncHttpClient,
    ): SqsAsyncClientBuilder = SqsAsyncClient
        .builder()
        .httpClient(sqsAsyncHttpClient)
        .region(Region.of(sqsProperties.region))

    @Bean
    @Qualifier("accountTransactionQueueContainerFactory")
    fun accountTransactionQueueContainerFactory(
        sqsAsyncClient: SqsAsyncClient,
        sqsProperties: SqsListenerProperties,
    ): SqsMessageListenerContainerFactory<Any> = SqsMessageListenerContainerFactory.builder<Any>()
        .configure { options ->
            options.maxConcurrentMessages(sqsProperties.maxConcurrentMessages)
            options.maxMessagesPerPoll(sqsProperties.maxMessagesPerPoll)
            options.pollTimeout(sqsProperties.pollTimeout)
            options.messageVisibility(sqsProperties.messageVisibility)
            options.acknowledgementMode(sqsProperties.acknowledgementMode)
            options.acknowledgementInterval(sqsProperties.acknowledgementInterval)
            options.acknowledgementThreshold(sqsProperties.acknowledgementThreshold)
            options.backPressureMode(sqsProperties.backPressureMode)
        }
        .sqsAsyncClient(sqsAsyncClient)
        .build()

    @Bean
    @Primary
    fun sqsTemplate(
        sqsAsyncClient: SqsAsyncClient,
    ): SqsTemplate = SqsTemplate.builder()
        .sqsAsyncClient(sqsAsyncClient)
        .build()

    @Bean("accountTransactionDispatcher")
    fun accountTransactionDispatcher(): CoroutineDispatcher {
        val factory = Thread.ofVirtual().name("sqs-processor-", 0).factory()
        return Executors.newThreadPerTaskExecutor(factory).asCoroutineDispatcher()
    }

}
