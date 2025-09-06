package com.itau.authorizer.validation.infrastructure.configuration

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.time.Duration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsConfiguration {

    companion object {

        private const val TRANSACTION_PROCESSING_TIMER = "transaction.processing.duration"
        private const val SQS_MESSAGE_PROCESSED_COUNTER = "sqs.message.processed.total"

        const val TRANSACTION_PROCESSING_SUCCESS_TOTAL = "transaction.processing.success.total"
        const val TRANSACTION_PROCESSING_ERROR_TOTAL = "transaction.processing.error.total"
        const val TRANSACTION_THROUGHPUT_TOTAL = "transaction.throughput.total"

        const val TAG_RESULT = "result"
        const val TAG_ERROR_TYPE = "error_type"

        const val TRANSACTION_THROUGHPUT_SUCCESS = "SUCCESS"
        const val TRANSACTION_THROUGHPUT_FAILURE = "FAILURE"
    }

    @Bean
    fun simpleMeterRegistry(): SimpleMeterRegistry = SimpleMeterRegistry()

    @Bean("transactionProcessingTimer")
    fun transactionProcessingTimer(meterRegistry: MeterRegistry): Timer =
        Timer.builder(TRANSACTION_PROCESSING_TIMER)
            .description("Duration of complete transaction processing")
            .publishPercentiles(0.5, 0.95, 0.99)
            .publishPercentileHistogram(true)
            .distributionStatisticExpiry(Duration.ofMinutes(10))
            .distributionStatisticBufferLength(1000)
            .register(meterRegistry)

    @Bean("totalProcessedCounter")
    fun totalProcessedCounter(meterRegistry: MeterRegistry): Counter =
        Counter.builder(SQS_MESSAGE_PROCESSED_COUNTER)
            .description("Total number of SQS messages processed successfully")
            .register(meterRegistry)

}
