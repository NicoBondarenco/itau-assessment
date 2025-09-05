package com.itau.authorizer.validation.application.adapter.`in`.sqs

import com.fasterxml.jackson.databind.ObjectMapper
import com.itau.authorizer.validation.application.model.sqs.TransactionCommandSQS
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode
import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.time.measureTimedValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.messaging.Message
import org.springframework.stereotype.Component


@Component
class TransactionConsumerSQS(
    @Qualifier("accountTransactionDispatcher")
    private val processingDispatcher: CoroutineDispatcher,
    private val objectMapper: ObjectMapper,
) {

    companion object {
        private val counter = AtomicInteger(0)
        private val counterAlreadyProcessing = AtomicInteger(0)
        private val list = Collections.synchronizedList(mutableListOf<String>())
        private val counterMap = ConcurrentHashMap<String, Triple<String, Int, LocalDateTime>>()
    }

    private val logger = KotlinLogging.logger {}

    @SqsListener(
        value = ["\${spring.cloud.aws.sqs.listener.account-transaction-queue-name}"],
        factory = "accountTransactionQueueContainerFactory",
//        maxConcurrentMessages = "\${spring.cloud.aws.sqs.listener.max-concurrent-messages}",
//        maxMessagesPerPoll = "\${spring.cloud.aws.sqs.listener.max-messages-per-poll}",
//        pollTimeoutSeconds = "\${spring.cloud.aws.sqs.listener.poll-timeout}",
//        messageVisibilitySeconds = "\${spring.cloud.aws.sqs.listener.visibility-timeout}",
//        acknowledgementMode = "MANUAL",
    )
    fun receive(
        message: Message<String>,
        acknowledgement: Acknowledgement
    ): Unit = runBlocking(processingDispatcher) {
        try {
            val messageGroupId: String = message.headers["Sqs_Msa_MessageGroupId"]?.toString()
                ?: throw IllegalArgumentException("MessageGroupId is missing")
            val accountCounter: Int = message.headers["counter"]!!.toString().toInt()
            val mapKey = "${messageGroupId}-${accountCounter}"

            if (list.contains(messageGroupId)) {
                counterAlreadyProcessing.incrementAndGet()
                throw IllegalArgumentException("MessageGroupId already received")
            }

            list.add(messageGroupId)


            if (counterMap.containsKey(mapKey)) {
                list.remove(messageGroupId)
                throw IllegalArgumentException("AccountCounter already received -> $accountCounter $messageGroupId")
            }
            counterMap[mapKey] = Triple(messageGroupId, accountCounter, LocalDateTime.now())

            logger.info { "START MESSAGE ${message.headers.id} PROCESS -> ${message.payload}" }
            val result = measureTimedValue {
                delay(Random.nextLong(500, 2000))
                objectMapper.readValue(message.payload, TransactionCommandSQS::class.java)
            }

            val command = result.value
            if (command.amount <= BigDecimal.ZERO) throw IllegalArgumentException("Amount cannot be negative")

            list.remove(messageGroupId)

            logger.info { "MESSAGE ${counter.incrementAndGet()} ${message.headers.id} PROCESSED in ${result.duration.inWholeNanoseconds}ns -> ${message.payload}" }

            acknowledgement.acknowledge()
        } catch (e: Exception) {
            logger.error(e) { "ERROR MESSAGE ${message.headers.id} -> ${message.payload}" }
            throw e
        }
    }

    fun ConcurrentHashMap<String, Triple<String, Int, LocalDateTime>>.printResult() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        this.values.groupBy { it.first }.forEach { (key, value) ->
            println()
            println("-------------------------------------------------------------------------------------------")
            println("MessageGroupId: $key")
            value.toList().sortedBy { it.third }.forEach { (_, accountCounter, dateTime) ->
                println("AccountCounter: $accountCounter Received at: ${formatter.format(dateTime)}")
            }
            println("-------------------------------------------------------------------------------------------")
        }
        println()
        println("-------------------------------------------------------------------------------------------")
        println("Total: ${this.size}")
        println("-------------------------------------------------------------------------------------------")
    }

}

