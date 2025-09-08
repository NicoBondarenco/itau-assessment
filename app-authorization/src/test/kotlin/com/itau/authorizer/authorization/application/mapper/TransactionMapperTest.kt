package com.itau.authorizer.authorization.application.mapper

import com.itau.authorizer.authorization.application.model.kafka.TransactionExecutedKafka
import com.itau.authorizer.common.domain.model.entity.AccountTransactionEntity
import com.itau.authorizer.common.domain.model.value.TransactionType
import com.itau.authorizer.common.util.extension.toIsoFormat
import java.math.BigDecimal
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class TransactionMapperTest {

    @ParameterizedTest
    @EnumSource(TransactionType::class)
    fun `toTransactionExecutedKafka should convert AccountTransactionEntity for all transaction types`(transactionType: TransactionType) {
        val transactionId = UUID.randomUUID()
        val accountId = UUID.randomUUID()
        val amount = BigDecimal("150.75")
        val currentBalance = BigDecimal("849.25")
        val timestamp = ZonedDateTime.now()

        val accountTransaction = AccountTransactionEntity(
            transactionId = transactionId,
            accountId = accountId,
            amount = amount,
            type = transactionType,
            timestamp = timestamp,
            currentBalance = currentBalance
        )

        val result = accountTransaction.toTransactionExecutedKafka()

        assertValidKafkaConversion(accountTransaction, result)
        assertEquals(transactionType.name, result.type)
    }

    @ParameterizedTest
    @CsvSource(
        "0.00, 1000.00",
        "100.50, 900.50",
        "999999999.99, 1000000000.00",
        "0.01, 999.99",
        "123.456789, 876.543211"
    )
    fun `toTransactionExecutedKafka should handle different amount and balance combinations`(
        amount: BigDecimal,
        currentBalance: BigDecimal
    ) {
        val accountTransaction = createTestAccountTransaction(amount, currentBalance)

        val result = accountTransaction.toTransactionExecutedKafka()

        assertValidKafkaConversion(accountTransaction, result)
        assertEquals(amount, result.amount)
        assertEquals(currentBalance, result.currentBalance)
    }

    @ParameterizedTest
    @ValueSource(strings = ["-500.00", "0.00", "1500.25"])
    fun `toTransactionExecutedKafka should handle different current balance values`(balanceValue: String) {
        val currentBalance = BigDecimal(balanceValue)
        val accountTransaction = createTestAccountTransaction(currentBalance = currentBalance)

        val result = accountTransaction.toTransactionExecutedKafka()

        assertValidKafkaConversion(accountTransaction, result)
        assertEquals(currentBalance, result.currentBalance)
    }

    @ParameterizedTest
    @CsvSource(
        "UTC, 2024-01-15T10:30:45Z",
        "America/New_York, 2024-06-15T14:45:30-04:00",
        "Europe/London, 2024-12-15T15:00:00Z"
    )
    fun `toTransactionExecutedKafka should format timestamp correctly for different timezones`(
        zoneId: String,
        expectedFormat: String
    ) {
        val timestamp = when (zoneId) {
            "UTC" -> ZonedDateTime.of(2024, 1, 15, 10, 30, 45, 0, ZoneId.of("UTC"))
            "America/New_York" -> ZonedDateTime.of(2024, 6, 15, 14, 45, 30, 0, ZoneId.of("America/New_York"))
            else -> ZonedDateTime.of(2024, 12, 15, 15, 0, 0, 0, ZoneId.of("Europe/London"))
        }

        val accountTransaction = createTestAccountTransaction(timestamp = timestamp)

        val result = accountTransaction.toTransactionExecutedKafka()

        assertEquals(timestamp.toIsoFormat(), result.timestamp)
    }

    @Test
    fun `toTransactionExecutedKafka should return properly constructed AVRO object`() {
        val accountTransaction = createTestAccountTransaction()

        val result = accountTransaction.toTransactionExecutedKafka()

        assertEquals(TransactionExecutedKafka::class.java, result.javaClass)
        assertEquals(6, result.schema.fields.size)
        assertValidKafkaConversion(accountTransaction, result)
    }

    @Test
    fun `toTransactionExecutedKafka should be consistent across multiple calls`() {
        val accountTransaction = createTestAccountTransaction()

        val result1 = accountTransaction.toTransactionExecutedKafka()
        val result2 = accountTransaction.toTransactionExecutedKafka()

        assertEquals(result1.transactionId, result2.transactionId)
        assertEquals(result1.accountId, result2.accountId)
        assertEquals(result1.amount, result2.amount)
        assertEquals(result1.type, result2.type)
        assertEquals(result1.timestamp, result2.timestamp)
        assertEquals(result1.currentBalance, result2.currentBalance)
    }

    private fun createTestAccountTransaction(
        amount: BigDecimal = BigDecimal("100.00"),
        currentBalance: BigDecimal = BigDecimal("900.00"),
        transactionType: TransactionType = TransactionType.DEBIT,
        timestamp: ZonedDateTime = ZonedDateTime.now()
    ) = AccountTransactionEntity(
        transactionId = UUID.randomUUID(),
        accountId = UUID.randomUUID(),
        amount = amount,
        type = transactionType,
        timestamp = timestamp,
        currentBalance = currentBalance
    )

    private fun assertValidKafkaConversion(
        original: AccountTransactionEntity,
        result: TransactionExecutedKafka
    ) {
        assertEquals(original.transactionId, result.transactionId)
        assertEquals(original.accountId, result.accountId)
        assertEquals(original.amount, result.amount)
        assertEquals(original.type.name, result.type)
        assertEquals(original.timestamp.toIsoFormat(), result.timestamp)
        assertEquals(original.currentBalance, result.currentBalance)
    }

}
