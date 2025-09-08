package com.itau.authorizer.authorization.domain.model.mapper

import com.itau.authorizer.common.domain.model.entity.AccountTransactionEntity
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import com.itau.authorizer.common.domain.model.value.TransactionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class TransactionMapperTest {

    @ParameterizedTest
    @EnumSource(TransactionType::class)
    fun `toAccountTransactionEntity should convert TransactionEntity for all transaction types`(transactionType: TransactionType) {
        val currentBalance = BigDecimal("849.25")
        val transactionEntity = createTestTransaction(type = transactionType)

        val result = transactionEntity.toAccountTransactionEntity(currentBalance)

        assertValidConversion(transactionEntity, result, currentBalance)
        assertEquals(transactionType, result.type)
    }

    @ParameterizedTest
    @CsvSource(
        "0.00, 1000.00",
        "150.75, 849.25",
        "999999999.99, 1000000000.00",
        "0.01, 999.99",
        "123.456789, 876.543211",
        "500.00, 0.00",
        "100.00, -50.25"
    )
    fun `toAccountTransactionEntity should handle different amount and balance combinations`(
        amount: BigDecimal,
        currentBalance: BigDecimal
    ) {
        val transactionEntity = createTestTransaction(amount = amount)

        val result = transactionEntity.toAccountTransactionEntity(currentBalance)

        assertValidConversion(transactionEntity, result, currentBalance)
    }

    @ParameterizedTest
    @ValueSource(strings = ["-500.00", "0.00", "700.00", "1500.25", "999999.99"])
    fun `toAccountTransactionEntity should handle various current balance values`(balanceValue: String) {
        val currentBalance = BigDecimal(balanceValue)
        val transactionEntity = createTestTransaction()

        val result = transactionEntity.toAccountTransactionEntity(currentBalance)

        assertValidConversion(transactionEntity, result, currentBalance)
    }

    @ParameterizedTest
    @CsvSource(
        "UTC, 2024-01-15, 10, 30, 45, 0",
        "America/New_York, 2024-06-15, 14, 45, 30, 123456789",
        "Europe/London, 2024-12-31, 23, 59, 59, 999999999",
        "Asia/Tokyo, 2024-02-29, 12, 0, 0, 500000000"
    )
    fun `toAccountTransactionEntity should preserve timestamps with different timezones and precisions`(
        zoneId: String,
        date: String,
        hour: Int,
        minute: Int,
        second: Int,
        nanos: Int
    ) {
        val dateParts = date.split("-").map { it.toInt() }
        val timestamp = ZonedDateTime.of(
            dateParts[0], dateParts[1], dateParts[2],
            hour, minute, second, nanos,
            ZoneId.of(zoneId)
        )
        val currentBalance = BigDecimal("1200.50")
        val transactionEntity = createTestTransaction(timestamp = timestamp)

        val result = transactionEntity.toAccountTransactionEntity(currentBalance)

        assertValidConversion(transactionEntity, result, currentBalance)
        assertEquals(timestamp, result.timestamp)
        assertEquals(timestamp.zone, result.timestamp.zone)
        assertEquals(timestamp.nano, result.timestamp.nano)
    }

    @ParameterizedTest
    @CsvSource(
        "DEBIT, 100.00, 900.00",
        "CREDIT, 200.00, 1200.00",
        "UNKNOWN, 50.00, 950.00",
        "DEBIT, 0.00, 1000.00",
        "CREDIT, 999.99, 1999.99"
    )
    fun `toAccountTransactionEntity should handle type-amount-balance combinations correctly`(
        transactionType: TransactionType,
        amount: BigDecimal,
        currentBalance: BigDecimal
    ) {
        val transactionEntity = createTestTransaction(
            amount = amount,
            type = transactionType
        )

        val result = transactionEntity.toAccountTransactionEntity(currentBalance)

        assertValidConversion(transactionEntity, result, currentBalance)
        assertEquals(transactionType, result.type)
        assertEquals(amount, result.amount)
        assertEquals(currentBalance, result.currentBalance)
    }

    @Test
    fun `toAccountTransactionEntity should be consistent across multiple calls`() {
        val transactionEntity = createTestTransaction()
        val currentBalance = BigDecimal("700.00")

        val result1 = transactionEntity.toAccountTransactionEntity(currentBalance)
        val result2 = transactionEntity.toAccountTransactionEntity(currentBalance)

        assertEquals(result1.transactionId, result2.transactionId)
        assertEquals(result1.accountId, result2.accountId)
        assertEquals(result1.amount, result2.amount)
        assertEquals(result1.type, result2.type)
        assertEquals(result1.timestamp, result2.timestamp)
        assertEquals(result1.currentBalance, result2.currentBalance)
    }

    @Test
    fun `toAccountTransactionEntity should handle multiple balance values for same transaction`() {
        val transactionEntity = createTestTransaction()
        val balances = listOf(
            BigDecimal("900.00"),
            BigDecimal("800.00"),
            BigDecimal.ZERO,
            BigDecimal("-100.50")
        )

        val results = balances.map { balance ->
            transactionEntity.toAccountTransactionEntity(balance) to balance
        }

        results.forEach { (result, expectedBalance) ->
            assertValidConversion(transactionEntity, result, expectedBalance)
        }

        val allTransactionIds = results.map { it.first.transactionId }.toSet()
        assertEquals(1, allTransactionIds.size, "All results should have same transaction ID")
    }

    @Test
    fun `toAccountTransactionEntity should maintain complete data integrity in conversion`() {
        val originalTransaction = createTestTransaction(
            amount = BigDecimal("456.78"),
            type = TransactionType.CREDIT,
            timestamp = ZonedDateTime.of(2024, 3, 15, 16, 30, 45, 123456789, ZoneId.of("UTC"))
        )
        val currentBalance = BigDecimal("1543.22")

        val result = originalTransaction.toAccountTransactionEntity(currentBalance)

        assertValidConversion(originalTransaction, result, currentBalance)
        assertEquals(6, result.javaClass.declaredFields.size, "Result should have exactly 6 fields")
    }

    private fun createTestTransaction(
        amount: BigDecimal = BigDecimal("100.00"),
        type: TransactionType = TransactionType.DEBIT,
        timestamp: ZonedDateTime = ZonedDateTime.now()
    ) = TransactionEntity(
        transactionId = UUID.randomUUID(),
        accountId = UUID.randomUUID(),
        amount = amount,
        type = type,
        timestamp = timestamp
    )

    private fun assertValidConversion(
        original: TransactionEntity,
        result: AccountTransactionEntity,
        expectedBalance: BigDecimal
    ) {
        assertEquals(original.transactionId, result.transactionId)
        assertEquals(original.accountId, result.accountId)
        assertEquals(original.amount, result.amount)
        assertEquals(original.type, result.type)
        assertEquals(original.timestamp, result.timestamp)
        assertEquals(expectedBalance, result.currentBalance)
    }
}
