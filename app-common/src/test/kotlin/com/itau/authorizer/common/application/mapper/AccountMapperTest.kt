package com.itau.authorizer.common.application.mapper

import com.itau.authorizer.common.application.model.dynamodb.AccountDynamoDB
import com.itau.authorizer.common.domain.model.entity.AccountEntity
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AccountMapperTest {

    @Test
    fun `toAccountEntity should convert AccountDynamoDB to AccountEntity successfully`() {
        val accountId = UUID.randomUUID()
        val createdAt = ZonedDateTime.now()
        val isActive = true
        val dailyLimit = BigDecimal("1000.00")

        val accountDynamoDB = AccountDynamoDB(
            accountId = accountId,
            createdAt = createdAt,
            isActive = isActive,
            dailyLimit = dailyLimit
        )

        val result = accountDynamoDB.toAccountEntity()

        assertEquals(accountId, result.accountId)
        assertEquals(createdAt, result.createdAt)
        assertEquals(isActive, result.isActive)
        assertEquals(dailyLimit, result.dailyLimit)
    }

    @Test
    fun `toAccountDynamoDB should convert AccountEntity to AccountDynamoDB successfully`() {
        val accountId = UUID.randomUUID()
        val createdAt = ZonedDateTime.now()
        val isActive = true
        val dailyLimit = BigDecimal("500.00")

        val accountEntity = AccountEntity(
            accountId = accountId,
            createdAt = createdAt,
            isActive = isActive,
            dailyLimit = dailyLimit
        )

        val result = accountEntity.toAccountDynamoDB()

        assertEquals(accountId, result.accountId)
        assertEquals(createdAt, result.createdAt)
        assertEquals(isActive, result.isActive)
        assertEquals(dailyLimit, result.dailyLimit)
    }

    @Test
    fun `should maintain data integrity in bidirectional conversion`() {
        val originalAccountDynamoDB = AccountDynamoDB(
            accountId = UUID.randomUUID(),
            createdAt = ZonedDateTime.now(),
            isActive = true,
            dailyLimit = BigDecimal("750.50")
        )

        val convertedEntity = originalAccountDynamoDB.toAccountEntity()
        val backToOriginal = convertedEntity.toAccountDynamoDB()

        assertEquals(originalAccountDynamoDB.accountId, backToOriginal.accountId)
        assertEquals(originalAccountDynamoDB.createdAt, backToOriginal.createdAt)
        assertEquals(originalAccountDynamoDB.isActive, backToOriginal.isActive)
        assertEquals(originalAccountDynamoDB.dailyLimit, backToOriginal.dailyLimit)
    }

}
