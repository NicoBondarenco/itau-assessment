package com.itau.authorizer.common.application.mapper

import com.itau.authorizer.common.application.model.dynamodb.TransactionDynamoDB
import com.itau.authorizer.common.application.model.grpc.TransactionExecutionRequest
import com.itau.authorizer.common.domain.exception.InvalidTransactionException
import com.itau.authorizer.common.domain.model.entity.AccountTransactionEntity
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import com.itau.authorizer.common.domain.model.value.TransactionType
import com.itau.authorizer.common.util.extension.toIsoFormat
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TransactionMapperTest {

    @Test
    fun `toTransactionEntity should convert gRPC request to TransactionEntity successfully`() {
        val transactionId = UUID.randomUUID()
        val accountId = UUID.randomUUID()
        val amount = BigDecimal("100.50")
        val timestamp = ZonedDateTime.now()

        val grpcRequest = TransactionExecutionRequest.newBuilder()
            .setTransactionId(transactionId.toString())
            .setAccountId(accountId.toString())
            .setAmount("100.50")
            .setType("DEBIT")
            .setTimestamp(timestamp.toString())
            .build()

        val result = grpcRequest.toTransactionEntity()

        assertEquals(transactionId, result.transactionId)
        assertEquals(accountId, result.accountId)
        assertEquals(amount, result.amount)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(timestamp, result.timestamp)
    }

    @Test
    fun `toTransactionEntity should handle CREDIT transaction type`() {
        val transactionId = UUID.randomUUID()
        val accountId = UUID.randomUUID()
        val amount = BigDecimal("250.75")
        val timestamp = ZonedDateTime.now()

        val grpcRequest = TransactionExecutionRequest.newBuilder()
            .setTransactionId(transactionId.toString())
            .setAccountId(accountId.toString())
            .setAmount("250.75")
            .setType("CREDIT")
            .setTimestamp(timestamp.toString())
            .build()

        val result = grpcRequest.toTransactionEntity()

        assertEquals(transactionId, result.transactionId)
        assertEquals(accountId, result.accountId)
        assertEquals(amount, result.amount)
        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(timestamp, result.timestamp)
    }

    @Test
    fun `toTransactionEntity should throw InvalidTransactionException for invalid UUID`() {
        val grpcRequest = TransactionExecutionRequest.newBuilder()
            .setTransactionId("invalid-uuid")
            .setAccountId(UUID.randomUUID().toString())
            .setAmount("100.00")
            .setType("DEBIT")
            .setTimestamp(ZonedDateTime.now().toString())
            .build()

        val exception = assertThrows<InvalidTransactionException> {
            grpcRequest.toTransactionEntity()
        }

        assertTrue(exception.message!!.contains(grpcRequest.toString()))
    }

    @Test
    fun `toTransactionEntity should throw InvalidTransactionException for invalid amount`() {
        val grpcRequest = TransactionExecutionRequest.newBuilder()
            .setTransactionId(UUID.randomUUID().toString())
            .setAccountId(UUID.randomUUID().toString())
            .setAmount("invalid-amount")
            .setType("DEBIT")
            .setTimestamp(ZonedDateTime.now().toString())
            .build()

        val exception = assertThrows<InvalidTransactionException> {
            grpcRequest.toTransactionEntity()
        }

        assertTrue(exception.message!!.contains(grpcRequest.toString()))
    }

    @Test
    fun `toTransactionEntity should throw InvalidTransactionException for invalid transaction type`() {
        val grpcRequest = TransactionExecutionRequest.newBuilder()
            .setTransactionId(UUID.randomUUID().toString())
            .setAccountId(UUID.randomUUID().toString())
            .setAmount("100.00")
            .setType("INVALID_TYPE")
            .setTimestamp(ZonedDateTime.now().toString())
            .build()

        val exception = assertThrows<InvalidTransactionException> {
            grpcRequest.toTransactionEntity()
        }

        assertTrue(exception.message!!.contains(grpcRequest.toString()))
    }

    @Test
    fun `toTransactionEntity should throw InvalidTransactionException for invalid timestamp`() {
        val grpcRequest = TransactionExecutionRequest.newBuilder()
            .setTransactionId(UUID.randomUUID().toString())
            .setAccountId(UUID.randomUUID().toString())
            .setAmount("100.00")
            .setType("DEBIT")
            .setTimestamp("invalid-timestamp")
            .build()

        val exception = assertThrows<InvalidTransactionException> {
            grpcRequest.toTransactionEntity()
        }

        assertTrue(exception.message!!.contains(grpcRequest.toString()))
    }

    @Test
    fun `toTransactionEntity from TransactionDynamoDB should convert successfully`() {
        val transactionId = UUID.randomUUID()
        val accountId = UUID.randomUUID()
        val amount = BigDecimal("150.25")
        val timestamp = ZonedDateTime.now()

        val transactionDynamoDB = TransactionDynamoDB(
            transactionId = transactionId,
            accountId = accountId,
            amount = amount,
            type = TransactionType.DEBIT,
            timestamp = timestamp
        )

        val result = transactionDynamoDB.toTransactionEntity()

        assertEquals(transactionId, result.transactionId)
        assertEquals(accountId, result.accountId)
        assertEquals(amount, result.amount)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(timestamp, result.timestamp)
    }

    @Test
    fun `toTransactionDynamoDB from TransactionEntity should convert successfully`() {
        val transactionId = UUID.randomUUID()
        val accountId = UUID.randomUUID()
        val amount = BigDecimal("200.00")
        val timestamp = ZonedDateTime.now()

        val transactionEntity = TransactionEntity(
            transactionId = transactionId,
            accountId = accountId,
            amount = amount,
            type = TransactionType.CREDIT,
            timestamp = timestamp
        )

        val result = transactionEntity.toTransactionDynamoDB()

        assertEquals(transactionId, result.transactionId)
        assertEquals(accountId, result.accountId)
        assertEquals(amount, result.amount)
        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(timestamp, result.timestamp)
    }

    @Test
    fun `toTransactionDynamoDB from AccountTransactionEntity should convert successfully`() {
        val transactionId = UUID.randomUUID()
        val accountId = UUID.randomUUID()
        val amount = BigDecimal("300.75")
        val currentBalance = BigDecimal("1500.25")
        val timestamp = ZonedDateTime.now()

        val accountTransactionEntity = AccountTransactionEntity(
            transactionId = transactionId,
            accountId = accountId,
            amount = amount,
            type = TransactionType.DEBIT,
            timestamp = timestamp,
            currentBalance = currentBalance
        )

        val result = accountTransactionEntity.toTransactionDynamoDB()

        assertEquals(transactionId, result.transactionId)
        assertEquals(accountId, result.accountId)
        assertEquals(amount, result.amount)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(timestamp, result.timestamp)
    }

    @Test
    fun `toBalanceDynamoDB should convert AccountTransactionEntity to BalanceDynamoDB successfully`() {

        val transactionId = UUID.randomUUID()
        val accountId = UUID.randomUUID()
        val amount = BigDecimal("100.00")
        val currentBalance = BigDecimal("900.00")
        val timestamp = ZonedDateTime.now().minusMinutes(5)

        val accountTransactionEntity = AccountTransactionEntity(
            transactionId = transactionId,
            accountId = accountId,
            amount = amount,
            type = TransactionType.DEBIT,
            timestamp = timestamp,
            currentBalance = currentBalance
        )

        val result = accountTransactionEntity.toBalanceDynamoDB()

        assertEquals(accountId, result.accountId)
        assertEquals(currentBalance, result.amount)
        assertTrue(result.lastUpdate.isAfter(timestamp))
    }

    @Test
    fun `toTransactionExecutionRequest should convert TransactionEntity to gRPC request successfully`() {
        val transactionId = UUID.randomUUID()
        val accountId = UUID.randomUUID()
        val amount = BigDecimal("450.50")
        val timestamp = ZonedDateTime.now()

        val transactionEntity = TransactionEntity(
            transactionId = transactionId,
            accountId = accountId,
            amount = amount,
            type = TransactionType.CREDIT,
            timestamp = timestamp
        )

        val result = transactionEntity.toTransactionExecutionRequest()

        assertEquals(transactionId.toString(), result.transactionId)
        assertEquals(accountId.toString(), result.accountId)
        assertEquals("450.50", result.amount)
        assertEquals("CREDIT", result.type)
        assertEquals(timestamp.toIsoFormat(), result.timestamp)
    }

    @Test
    fun `should maintain data integrity in round-trip conversion for TransactionEntity`() {
        val originalEntity = TransactionEntity(
            transactionId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            amount = BigDecimal("123.45"),
            type = TransactionType.CREDIT,
            timestamp = ZonedDateTime.now()
        )

        val dynamoDb = originalEntity.toTransactionDynamoDB()
        val backToEntity = dynamoDb.toTransactionEntity()

        assertEquals(originalEntity.transactionId, backToEntity.transactionId)
        assertEquals(originalEntity.accountId, backToEntity.accountId)
        assertEquals(originalEntity.amount, backToEntity.amount)
        assertEquals(originalEntity.type, backToEntity.type)
        assertEquals(originalEntity.timestamp, backToEntity.timestamp)
    }

    @Test
    fun `should maintain data integrity in round-trip conversion for gRPC request`() {
        val originalEntity = TransactionEntity(
            transactionId = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            amount = BigDecimal("678.90"),
            type = TransactionType.DEBIT,
            timestamp = ZonedDateTime.now()
        )

        val grpcRequest = originalEntity.toTransactionExecutionRequest()
        val backToEntity = grpcRequest.toTransactionEntity()

        assertEquals(originalEntity.transactionId, backToEntity.transactionId)
        assertEquals(originalEntity.accountId, backToEntity.accountId)
        assertEquals(originalEntity.amount, backToEntity.amount)
        assertEquals(originalEntity.type, backToEntity.type)
        assertEquals(originalEntity.timestamp, backToEntity.timestamp)
    }

}
