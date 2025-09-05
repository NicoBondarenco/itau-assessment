package com.itau.authorizer.validation.infrastructure.configuration

import com.itau.authorizer.validation.infrastructure.interceptor.SqsMessageInterceptor
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
import org.springframework.messaging.converter.AbstractMessageConverter
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder


@Configuration
@EnableConfigurationProperties(SqsListenerProperties::class)
class SqsConfiguration {

    @Bean
    fun sqsAsyncClientBuilder(
        sqsProperties: SqsProperties,
    ): SqsAsyncClientBuilder = SqsAsyncClient
        .builder()
        .region(Region.of(sqsProperties.region))

    @Bean
    @Qualifier("accountTransactionQueueContainerFactory")
    fun accountTransactionQueueContainerFactory(
        sqsAsyncClient: SqsAsyncClient,
        sqsProperties: SqsListenerProperties,
        sqsMessageInterceptor: SqsMessageInterceptor,
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
        .messageInterceptor(sqsMessageInterceptor)
        .build()

    @Bean
    @Primary
    fun sqsTemplate(
        sqsAsyncClient: SqsAsyncClient,
    ): SqsTemplate = SqsTemplate.builder()
        .sqsAsyncClient(sqsAsyncClient)
        .build()

    @Bean("accountTransactionDispatcher")
    fun accountTransactionDispatcher(): CoroutineDispatcher = Executors.newFixedThreadPool(2000) { runnable ->
        Thread(runnable, "sqs-processor-${Thread.currentThread().threadId()}")
    }.asCoroutineDispatcher()

    private fun <T : AbstractMessageConverter> T.applyDefaultConfiguration() = this.apply {
        serializedPayloadClass = String::class.java
    }

}
