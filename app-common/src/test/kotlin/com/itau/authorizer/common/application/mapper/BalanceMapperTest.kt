package com.itau.authorizer.common.application.mapper

import com.itau.authorizer.common.application.model.dynamodb.BalanceDynamoDB
import com.itau.authorizer.common.domain.model.entity.BalanceEntity
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BalanceMapperTest {

    @Test
    fun `toBalanceDynamoDB should convert BalanceEntity to BalanceDynamoDB successfully`() {
        val accountId = UUID.randomUUID()
        val amount = BigDecimal("1500.75")
        val lastUpdate = ZonedDateTime.now()

        val balanceEntity = BalanceEntity(
            accountId = accountId,
            amount = amount,
            lastUpdate = lastUpdate
        )

        val result = balanceEntity.toBalanceDynamoDB()

        assertEquals(accountId, result.accountId)
        assertEquals(amount, result.amount)
        assertEquals(lastUpdate, result.lastUpdate)
    }

    @Test
    fun `toBalanceEntity should convert BalanceDynamoDB to BalanceEntity successfully`() {
        val accountId = UUID.randomUUID()
        val amount = BigDecimal("2500.25")
        val lastUpdate = ZonedDateTime.now()

        val balanceDynamoDB = BalanceDynamoDB(
            accountId = accountId,
            amount = amount,
            lastUpdate = lastUpdate
        )

        val result = balanceDynamoDB.toBalanceEntity()

        assertEquals(accountId, result.accountId)
        assertEquals(amount, result.amount)
        assertEquals(lastUpdate, result.lastUpdate)
    }

    @Test
    fun `should maintain data integrity in bidirectional conversion`() {
        val originalBalanceEntity = BalanceEntity(
            accountId = UUID.randomUUID(),
            amount = BigDecimal("1234.56"),
            lastUpdate = ZonedDateTime.now()
        )

        val convertedDynamoDB = originalBalanceEntity.toBalanceDynamoDB()
        val backToOriginal = convertedDynamoDB.toBalanceEntity()

        assertEquals(originalBalanceEntity.accountId, backToOriginal.accountId)
        assertEquals(originalBalanceEntity.amount, backToOriginal.amount)
        assertEquals(originalBalanceEntity.lastUpdate, backToOriginal.lastUpdate)
    }

}
