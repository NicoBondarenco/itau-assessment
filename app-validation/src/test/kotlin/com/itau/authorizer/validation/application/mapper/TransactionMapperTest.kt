package com.itau.authorizer.validation.application.mapper

import com.itau.authorizer.common.domain.model.value.TransactionType
import com.itau.authorizer.validation.application.model.sqs.TransactionCommandSQS
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID.randomUUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TransactionMapperTest {

    @Test
    fun `should map TransactionCommandSQS to TransactionEntity correctly`() {
        val transactionId = randomUUID()
        val accountId = randomUUID()
        val amount = BigDecimal("150.75")
        val type = TransactionType.entries.random()
        val timestamp = ZonedDateTime.now()

        val transactionSQS = TransactionCommandSQS(
            transactionId = transactionId,
            accountId = accountId,
            amount = amount,
            type = type,
            timestamp = timestamp
        )

        val result = transactionSQS.toTransactionEntity()

        assertEquals(transactionId, result.transactionId)
        assertEquals(accountId, result.accountId)
        assertEquals(amount, result.amount)
        assertEquals(type, result.type)
        assertEquals(timestamp, result.timestamp)
    }

}
