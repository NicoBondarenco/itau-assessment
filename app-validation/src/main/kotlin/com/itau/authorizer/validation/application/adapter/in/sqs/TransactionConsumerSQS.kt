package com.itau.authorizer.validation.application.adapter.`in`.sqs

import com.fasterxml.jackson.databind.ObjectMapper
import com.itau.authorizer.validation.application.adapter.out.metrics.MetricsRegistrer
import com.itau.authorizer.validation.application.adapter.out.sqs.TransactionDQLProducerSQS
import com.itau.authorizer.validation.application.exception.TransactionDlqException
import com.itau.authorizer.validation.application.mapper.toTransactionEntity
import com.itau.authorizer.validation.application.model.metrics.ExecutionResult
import com.itau.authorizer.validation.application.model.sqs.TransactionCommandSQS
import com.itau.authorizer.validation.domain.exception.InvalidAmountException
import com.itau.authorizer.validation.domain.exception.InvalidPayloadException
import com.itau.authorizer.validation.domain.exception.InvalidTransactionTypeException
import com.itau.authorizer.validation.domain.exception.LimitReachedException
import com.itau.authorizer.validation.domain.usecase.TransactionUsecase
import com.itau.authorizer.validation.infrastructure.configuration.MetricsConfiguration.Companion.TRANSACTION_THROUGHPUT_FAILURE
import com.itau.authorizer.validation.infrastructure.configuration.MetricsConfiguration.Companion.TRANSACTION_THROUGHPUT_SUCCESS
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue
import kotlin.time.toJavaDuration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.messaging.Message
import org.springframework.stereotype.Component


@Component
class TransactionConsumerSQS(
    @Qualifier("accountTransactionDispatcher")
    private val processingDispatcher: CoroutineDispatcher,
    private val objectMapper: ObjectMapper,
    private val usecase: TransactionUsecase,
    private val dlqProducer: TransactionDQLProducerSQS,
    private val metricsRegistrer: MetricsRegistrer,
) {

    companion object {
        private val UNPROCESSABLE_EXCEPTIONS = listOf(
            InvalidPayloadException::class,
            InvalidAmountException::class,
            LimitReachedException::class,
            InvalidTransactionTypeException::class,
        )
    }

    private val logger = KotlinLogging.logger {}

    @SqsListener(
        value = ["\${spring.cloud.aws.sqs.queue.account-transaction-queue-name}"],
        factory = "accountTransactionQueueContainerFactory",
    )
    fun receive(
        message: Message<String>,
        acknowledgement: Acknowledgement
    ): Unit = runBlocking(processingDispatcher) {
        measureTimedValue {
            try {
                message.readCommand().apply {
                    usecase.executeTransaction(this.toTransactionEntity())
                }
                acknowledgement.acknowledge()
                logger.info { "Message ${message.headers.id} -> ${message.payload} processed successfully" }
                ExecutionResult.success()
            } catch (e: Exception) {
                e.toErrorResult(message, acknowledgement)
            }
        }.registerMetrics().throwsOnError()
    }

    private suspend fun TimedValue<ExecutionResult>.registerMetrics() = this.apply {
        metricsRegistrer.recordMessageProcessed()
        val throughputResult: String = if (this.value.isSuccess) {
            metricsRegistrer.recordTransactionProcessingSuccess(this.duration.toJavaDuration())
            TRANSACTION_THROUGHPUT_SUCCESS
        } else {
            metricsRegistrer.recordTransactionProcessingError(
                this.value.errorType!!.simpleName!!,
                this.duration.toJavaDuration()
            )
            TRANSACTION_THROUGHPUT_FAILURE
        }
        metricsRegistrer.incrementTransactionThroughput(throughputResult)
    }

    private suspend fun TimedValue<ExecutionResult>.throwsOnError() = this.value.errorException?.let { throw it }

    private suspend fun Exception.toErrorResult(
        message: Message<String>,
        acknowledgement: Acknowledgement,
    ): ExecutionResult = this.let { e ->
        val errorException = e.handleException(message, acknowledgement)
        val errorType = errorException?.let { it::class } ?: e::class
        ExecutionResult.error(errorType, errorException)
    }

    private suspend fun Exception.handleException(
        message: Message<String>,
        acknowledgement: Acknowledgement,
    ): Exception? {
        logger.error(this) { "Error processing message ${message.headers.id} -> ${message.payload}" }
        return if (UNPROCESSABLE_EXCEPTIONS.contains(this::class)) {
            try {
                dlqProducer.send(message)
                acknowledgement.acknowledge()
                null
            } catch (e: Exception) {
                TransactionDlqException(e).apply {
                    logger.error(this) { "Error sending message to dlq ${message.headers.id} -> ${message.payload}" }
                }
            }
        } else {
            this
        }
    }

    private fun Message<String>.readCommand(): TransactionCommandSQS = try {
        objectMapper.readValue(this.payload, TransactionCommandSQS::class.java)
    } catch (e: Exception) {
        throw InvalidPayloadException(e.message)
    }

}

