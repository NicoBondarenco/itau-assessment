package com.itau.authorizer.validation.application.adapter.`in`.sqs

import com.fasterxml.jackson.databind.ObjectMapper
import com.itau.authorizer.validation.application.adapter.out.sqs.TransactionDQLProducerSQS
import com.itau.authorizer.validation.application.mapper.toTransactionEntity
import com.itau.authorizer.validation.application.model.sqs.TransactionCommandSQS
import com.itau.authorizer.validation.domain.exception.InvalidAmountException
import com.itau.authorizer.validation.domain.exception.InvalidPayloadException
import com.itau.authorizer.validation.domain.exception.LimitReachedException
import com.itau.authorizer.validation.domain.usecase.TransactionUsecase
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.collections.contains
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
) {

    companion object {
        private val UNPROCESSABLE_EXCEPTIONS = listOf(
            InvalidPayloadException::class,
            InvalidAmountException::class,
            LimitReachedException::class,
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
        try {
            message.readCommand().apply {
                usecase.executeTransaction(this.toTransactionEntity())
            }
            acknowledgement.acknowledge()
        } catch (e: Exception) {
            e.handleException(message, acknowledgement)
        }
    }

    private suspend fun Exception.handleException(
        message: Message<String>,
        acknowledgement: Acknowledgement,
    ) {
        logger.error(this) { "Error processing message ${message.headers.id} -> ${message.payload}" }
        if (UNPROCESSABLE_EXCEPTIONS.contains(this::class)) {
            dlqProducer.send(message)
            acknowledgement.acknowledge()
        } else {
            throw this
        }
    }

    private fun Message<String>.readCommand(): TransactionCommandSQS = try {
        objectMapper.readValue(this.payload, TransactionCommandSQS::class.java)
    } catch (e: Exception) {
        throw InvalidPayloadException(e.message)
    }

}

