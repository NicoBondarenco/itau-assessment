package com.itau.authorizer.validation.domain.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import com.itau.authorizer.common.application.model.dynamodb.AccountDynamoDB
import com.itau.authorizer.common.application.model.grpc.CurrentBalanceResponse
import com.itau.authorizer.common.domain.exception.AccountNotFoundException
import com.itau.authorizer.common.domain.model.value.TransactionType
import com.itau.authorizer.common.util.extension.toAmountFormat
import com.itau.authorizer.common.util.extension.toIsoFormat
import com.itau.authorizer.validation.application.adapter.`in`.sqs.TransactionConsumerSQS
import com.itau.authorizer.validation.application.adapter.mock.dynamodb.MockDynamoDbEnhancedClient
import com.itau.authorizer.validation.application.adapter.mock.grpc.currentBalancesMock
import com.itau.authorizer.validation.application.adapter.mock.grpc.dlqMessages
import com.itau.authorizer.validation.application.adapter.mock.grpc.executedTransactionsMock
import com.itau.authorizer.validation.application.exception.TransactionDlqException
import com.itau.authorizer.validation.application.model.sqs.TransactionCommandSQS
import com.itau.authorizer.validation.domain.exception.InactiveAccountException
import com.itau.authorizer.validation.domain.exception.InsufficientFundsException
import com.itau.authorizer.validation.infrastructure.configuration.MockTestConfiguration
import com.itau.authorizer.validation.util.RandomGenerator.randomBigDecimal
import com.itau.authorizer.validation.util.RandomGenerator.randomString
import com.itau.authorizer.validation.util.RandomGenerator.randomTransactionType
import com.itau.authorizer.validation.util.RandomGenerator.randomZonedDateTime
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID
import java.util.UUID.randomUUID
import java.util.concurrent.CompletableFuture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import org.springframework.context.annotation.Import
import org.springframework.grpc.test.AutoConfigureInProcessTransport
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.GenericMessage
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = NONE)
@ActiveProfiles("test")
@Import(MockTestConfiguration::class)
@AutoConfigureInProcessTransport
class TransactionTest {

    @Autowired
    private lateinit var client: MockDynamoDbEnhancedClient

    @Autowired
    private lateinit var consumer: TransactionConsumerSQS

    @Autowired
    private lateinit var mapper: ObjectMapper

    @BeforeEach
    fun beforeEach() {
        client.clear()
        executedTransactionsMock.clear()
        currentBalancesMock.clear()
        dlqMessages.clear()
    }

    @Test
    fun `execute transaction successfully`() {
        val accountId = randomUUID()

        client.generateAccount(accountId = accountId)
        generateCurrentBalance(accountId = accountId)

        val command = generateTransactionCommandSQS(accountId = accountId)
        val message = generateTransactionMessage(command)
        val acknowledgement = TestAcknowledgement()
        consumer.receive(message, acknowledgement)

        assertTrue(acknowledgement.acknowledged)

        val transaction = executedTransactionsMock[accountId]
        assertNotNull(transaction)
        assertEquals(command.accountId.toString(), transaction!!.accountId)
        assertEquals(command.transactionId.toString(), transaction.transactionId)
        assertEquals(command.amount.toAmountFormat(), transaction.amount)
        assertEquals(command.timestamp.toIsoFormat(), transaction.timestamp)
        assertEquals(command.type.name, transaction.type)
    }

    @Test
    fun `execute transaction with exact limit`() {
        val accountId = randomUUID()

        val account = client.generateAccount(accountId = accountId)
        val balance = generateCurrentBalance(accountId = accountId, currentBalance = randomBigDecimal(100000.0, 500000.0))
        val amount = account.dailyLimit.minus(BigDecimal(balance.dailyTransacted))

        val command = generateTransactionCommandSQS(accountId = accountId, amount = amount)
        val message = generateTransactionMessage(command)
        val acknowledgement = TestAcknowledgement()
        consumer.receive(message, acknowledgement)

        assertTrue(acknowledgement.acknowledged)

        val transaction = executedTransactionsMock[accountId]
        assertNotNull(transaction)
        assertEquals(command.accountId.toString(), transaction!!.accountId)
        assertEquals(command.transactionId.toString(), transaction.transactionId)
        assertEquals(command.amount.toAmountFormat(), transaction.amount)
        assertEquals(command.timestamp.toIsoFormat(), transaction.timestamp)
        assertEquals(command.type.name, transaction.type)
    }

    @Test
    fun `execute transaction invalid payload`() {
        val accountId = randomUUID()

        client.generateAccount(accountId = accountId, isActive = false)
        generateCurrentBalance(accountId = accountId)

        val message = generateInvalidPayload()
        val acknowledgement = TestAcknowledgement()
        consumer.receive(message, acknowledgement)

        assertTrue(acknowledgement.acknowledged)

        val transaction = executedTransactionsMock[accountId]
        assertNull(transaction)

        val dlq = dlqMessages.firstOrNull()
        assertNotNull(dlq)
        assertEquals(message.payload, dlq!!.payload)
    }

    @Test
    fun `execute transaction with negative amount`() {
        val accountId = randomUUID()

        client.generateAccount(accountId = accountId)
        generateCurrentBalance(accountId = accountId)

        val command = generateTransactionCommandSQS(accountId = accountId, amount = randomBigDecimal(negative = true))
        val message = generateTransactionMessage(command)
        val acknowledgement = TestAcknowledgement()
        consumer.receive(message, acknowledgement)

        assertTrue(acknowledgement.acknowledged)

        val transaction = executedTransactionsMock[accountId]
        assertNull(transaction)

        val dlq = dlqMessages.firstOrNull()
        assertNotNull(dlq)
        assertEquals(message.payload, dlq!!.payload)
    }

    @Test
    fun `execute transaction with zero amount`() {
        val accountId = randomUUID()

        client.generateAccount(accountId = accountId)
        generateCurrentBalance(accountId = accountId)

        val command = generateTransactionCommandSQS(accountId = accountId, amount = BigDecimal.ZERO)
        val message = generateTransactionMessage(command)
        val acknowledgement = TestAcknowledgement()
        consumer.receive(message, acknowledgement)

        assertTrue(acknowledgement.acknowledged)

        val transaction = executedTransactionsMock[accountId]
        assertNull(transaction)

        val dlq = dlqMessages.firstOrNull()
        assertNotNull(dlq)
        assertEquals(message.payload, dlq!!.payload)
    }

    @Test
    fun `execute transaction with invalid type`() {
        val accountId = randomUUID()

        client.generateAccount(accountId = accountId)
        generateCurrentBalance(accountId = accountId)

        val command = generateTransactionCommandSQS(accountId = accountId, type = TransactionType.UNKNOWN)
        val message = generateTransactionMessage(command)
        val acknowledgement = TestAcknowledgement()
        consumer.receive(message, acknowledgement)

        assertTrue(acknowledgement.acknowledged)

        val transaction = executedTransactionsMock[accountId]
        assertNull(transaction)

        val dlq = dlqMessages.firstOrNull()
        assertNotNull(dlq)
        assertEquals(message.payload, dlq!!.payload)
    }

    @Test
    fun `execute transaction with exceeded limit`() {
        val accountId = randomUUID()

        val account = client.generateAccount(accountId = accountId)
        generateCurrentBalance(accountId = accountId, currentBalance = randomBigDecimal(100000.0, 500000.0))

        val command = generateTransactionCommandSQS(accountId = accountId, amount = account.dailyLimit.plus(BigDecimal.ONE))
        val message = generateTransactionMessage(command)
        val acknowledgement = TestAcknowledgement()
        consumer.receive(message, acknowledgement)

        assertTrue(acknowledgement.acknowledged)

        val transaction = executedTransactionsMock[accountId]
        assertNull(transaction)

        val dlq = dlqMessages.firstOrNull()
        assertNotNull(dlq)
        assertEquals(message.payload, dlq!!.payload)
    }

    @Test
    fun `execute transaction with inactive account`() {
        val accountId = randomUUID()

        client.generateAccount(accountId = accountId, isActive = false)
        generateCurrentBalance(accountId = accountId)

        val command = generateTransactionCommandSQS(accountId = accountId)
        val message = generateTransactionMessage(command)
        val acknowledgement = TestAcknowledgement()
        val exception = assertThrows<InactiveAccountException> {
            consumer.receive(message, acknowledgement)
        }
        assertEquals("Account $accountId is inactive", exception.message)

        assertFalse(acknowledgement.acknowledged)

        val transaction = executedTransactionsMock[accountId]
        assertNull(transaction)

        val dlq = dlqMessages.firstOrNull()
        assertNull(dlq)
    }

    @Test
    fun `execute transaction with zero balance`() {
        val accountId = randomUUID()

        client.generateAccount(accountId = accountId, dailyLimit = randomBigDecimal(100000.0, 500000.0))
        val balance = generateCurrentBalance(accountId = accountId, dailyTransacted = BigDecimal.ZERO)

        val command = generateTransactionCommandSQS(accountId = accountId, amount = BigDecimal(balance.currentBalance))
        val message = generateTransactionMessage(command)
        val acknowledgement = TestAcknowledgement()
        consumer.receive(message, acknowledgement)

        assertTrue(acknowledgement.acknowledged)

        val transaction = executedTransactionsMock[accountId]
        assertNotNull(transaction)
        assertEquals(command.accountId.toString(), transaction!!.accountId)
        assertEquals(command.transactionId.toString(), transaction.transactionId)
        assertEquals(command.amount.toAmountFormat(), transaction.amount)
        assertEquals(command.timestamp.toIsoFormat(), transaction.timestamp)
        assertEquals(command.type.name, transaction.type)
    }

    @Test
    fun `execute transaction with negative balance`() {
        val accountId = randomUUID()

        client.generateAccount(accountId = accountId)
        val balance = generateCurrentBalance(accountId = accountId, dailyTransacted = BigDecimal.ZERO)

        val command = generateTransactionCommandSQS(
            accountId = accountId,
            amount = BigDecimal(balance.currentBalance).add(BigDecimal.TEN)
        )
        val message = generateTransactionMessage(command)
        val acknowledgement = TestAcknowledgement()
        val exception = assertThrows<InsufficientFundsException> {
            consumer.receive(message, acknowledgement)
        }
        assertEquals("Insufficient funds for transaction ${command.transactionId} on account $accountId", exception.message)

        assertFalse(acknowledgement.acknowledged)

        val transaction = executedTransactionsMock[accountId]
        assertNull(transaction)

        val dlq = dlqMessages.firstOrNull()
        assertNull(dlq)
    }

    @Test
    fun `execute transaction with error on dlq`() {
        val accountId = randomUUID()

        client.generateAccount(accountId = accountId)
        generateCurrentBalance(accountId = accountId)

        val message = generateInvalidPayload("exception")
        val acknowledgement = TestAcknowledgement()
        val exception = assertThrows<TransactionDlqException> {
            consumer.receive(message, acknowledgement)
        }
        assertEquals("Error sending message to transaction dlq. Cause: exception", exception.message)

        assertFalse(acknowledgement.acknowledged)

        val transaction = executedTransactionsMock[accountId]
        assertNull(transaction)

        val dlq = dlqMessages.firstOrNull()
        assertNull(dlq)
    }

    @Test
    fun `execute transaction with account not found`() {
        val accountId = randomUUID()

        generateCurrentBalance(accountId = accountId)

        val command = generateTransactionCommandSQS(accountId = accountId)
        val message = generateTransactionMessage(command)
        val acknowledgement = TestAcknowledgement()
        val exception = assertThrows<AccountNotFoundException> {
            consumer.receive(message, acknowledgement)
        }
        assertEquals("Account not found with ID: $accountId", exception.message)

        assertFalse(acknowledgement.acknowledged)

        val transaction = executedTransactionsMock[accountId]
        assertNull(transaction)

        val dlq = dlqMessages.firstOrNull()
        assertNull(dlq)
    }

    private fun generateCurrentBalance(
        accountId: UUID = randomUUID(),
        currentBalance: BigDecimal = randomBigDecimal(5000.0, 10000.0),
        dailyTransacted: BigDecimal = randomBigDecimal(2000.0, 4000.0),
    ) = generateCurrentBalanceResponse(accountId, currentBalance, dailyTransacted).apply {
        currentBalancesMock[accountId] = this
    }

    private fun generateCurrentBalanceResponse(
        accountId: UUID = randomUUID(),
        currentBalance: BigDecimal = randomBigDecimal(5000.0, 10000.0),
        dailyTransacted: BigDecimal = randomBigDecimal(2000.0, 4000.0),
    ) = CurrentBalanceResponse.newBuilder()
        .setAccountId(accountId.toString())
        .setCurrentBalance(currentBalance.toAmountFormat())
        .setDailyTransacted(dailyTransacted.toAmountFormat())
        .build()

    private fun MockDynamoDbEnhancedClient.generateAccount(
        accountId: UUID = randomUUID(),
        createdAt: ZonedDateTime = randomZonedDateTime(),
        isActive: Boolean = true,
        dailyLimit: BigDecimal = randomBigDecimal(5000.0, 10000.0),
    ) = generateAccountDynamoDB(accountId, createdAt, isActive, dailyLimit).apply {
        put(this)
    }

    private fun generateAccountDynamoDB(
        accountId: UUID = randomUUID(),
        createdAt: ZonedDateTime = randomZonedDateTime(),
        isActive: Boolean = true,
        dailyLimit: BigDecimal = randomBigDecimal(5000.0, 1000.0),
    ) = AccountDynamoDB(
        accountId = accountId,
        createdAt = createdAt,
        isActive = isActive,
        dailyLimit = dailyLimit,
    )

    private fun generateTransactionMessage(
        command: TransactionCommandSQS,
    ) = GenericMessage(
        mapper.writeValueAsString(command),
        MessageHeaders(
            mapOf(
                "transactionId" to command.transactionId,
                "accountId" to command.accountId,
                "timestamp" to ZonedDateTime.now().toInstant().toEpochMilli()
            )
        )
    )

    private fun generateInvalidPayload(message: String = randomString()) = GenericMessage(
        message,
        MessageHeaders(
            mapOf(
                "transactionId" to randomUUID(),
                "accountId" to randomUUID(),
                "timestamp" to ZonedDateTime.now().toInstant().toEpochMilli()
            )
        )
    )

    private fun generateTransactionCommandSQS(
        transactionId: UUID = randomUUID(),
        accountId: UUID = randomUUID(),
        amount: BigDecimal = randomBigDecimal(100.0, 500.0),
        type: TransactionType = randomTransactionType(listOf(TransactionType.UNKNOWN)),
        timestamp: ZonedDateTime = randomZonedDateTime(),
    ) = TransactionCommandSQS(
        transactionId = transactionId,
        accountId = accountId,
        amount = amount,
        type = type,
        timestamp = timestamp,
    )

    private class TestAcknowledgement : Acknowledgement {

        var acknowledged: Boolean = false

        override fun acknowledge() {
            acknowledged = true
        }

        override fun acknowledgeAsync(): CompletableFuture<Void?> {
            acknowledged = true
            return CompletableFuture.completedFuture(null)
        }

    }
}
