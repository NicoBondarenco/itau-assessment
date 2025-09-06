package com.itau.authorizer.validation.application.adapter.out.metrics

import org.springframework.stereotype.Component
import com.itau.authorizer.validation.infrastructure.configuration.MetricsConfiguration.Companion.TAG_ERROR_TYPE
import com.itau.authorizer.validation.infrastructure.configuration.MetricsConfiguration.Companion.TAG_RESULT
import com.itau.authorizer.validation.infrastructure.configuration.MetricsConfiguration.Companion.TRANSACTION_PROCESSING_ERROR_TOTAL
import com.itau.authorizer.validation.infrastructure.configuration.MetricsConfiguration.Companion.TRANSACTION_PROCESSING_SUCCESS_TOTAL
import com.itau.authorizer.validation.infrastructure.configuration.MetricsConfiguration.Companion.TRANSACTION_THROUGHPUT_TOTAL
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.beans.factory.annotation.Qualifier
import java.time.Duration
import java.util.concurrent.TimeUnit.MILLISECONDS


@Component
class MetricsRegistrer(
    private val meterRegistry: MeterRegistry,
    @Qualifier("transactionProcessingTimer")
    private val processingTimer: Timer,
    @Qualifier("totalProcessedCounter")
    private val totalProcessedCounter: Counter,
) {

    fun recordTransactionProcessingSuccess(duration: Duration) {
        processingTimer.record(duration.toMillis(), MILLISECONDS)
        meterRegistry.counter(TRANSACTION_PROCESSING_SUCCESS_TOTAL).increment()
    }

    fun recordTransactionProcessingError(errorType: String, duration: Duration) {
        processingTimer.record(duration.toMillis(), MILLISECONDS)
        meterRegistry.counter(
            TRANSACTION_PROCESSING_ERROR_TOTAL,
            TAG_ERROR_TYPE, errorType,
        ).increment()
    }

    fun recordMessageProcessed() {
        totalProcessedCounter.increment(1.0)
    }

    fun incrementTransactionThroughput(result: String) {
        meterRegistry.counter(TRANSACTION_THROUGHPUT_TOTAL,
            TAG_RESULT, result
        ).increment()
    }

}
