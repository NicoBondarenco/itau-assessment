package com.itau.authorizer.common.application.mapper

import com.itau.authorizer.common.application.model.grpc.CurrentBalanceResponse
import com.itau.authorizer.common.domain.model.entity.CurrentBalanceEntity
import java.math.BigDecimal
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CurrentBalanceMapperTest {

    @Test
    fun `toCurrentBalanceResponse should convert CurrentBalanceEntity to gRPC response successfully`() {
        val accountId = UUID.randomUUID()
        val currentBalance = BigDecimal("1000.50")
        val dailyTransacted = BigDecimal("250.75")

        val currentBalanceEntity = CurrentBalanceEntity(
            accountId = accountId,
            currentBalance = currentBalance,
            dailyTransacted = dailyTransacted
        )

        val result = currentBalanceEntity.toCurrentBalanceResponse()

        assertEquals(accountId.toString(), result.accountId)
        assertEquals("1000.50", result.currentBalance)
        assertEquals("250.75", result.dailyTransacted)
    }

    @Test
    fun `toCurrentBalanceRequest should convert UUID to gRPC request successfully`() {
        val accountId = UUID.randomUUID()

        val result = accountId.toCurrentBalanceRequest()

        assertEquals(accountId.toString(), result.accountId)
    }

    @Test
    fun `toCurrentBalanceEntity should convert gRPC response to CurrentBalanceEntity successfully`() {
        val accountId = UUID.randomUUID()
        val currentBalanceResponse = CurrentBalanceResponse.newBuilder()
            .setAccountId(accountId.toString())
            .setCurrentBalance("750.25")
            .setDailyTransacted("123.45")
            .build()

        val result = currentBalanceResponse.toCurrentBalanceEntity()

        assertEquals(accountId, result.accountId)
        assertEquals(BigDecimal("750.25"), result.currentBalance)
        assertEquals(BigDecimal("123.45"), result.dailyTransacted)
    }

    @Test
    fun `toCurrentBalanceEntity should throw exception for invalid UUID`() {
        val currentBalanceResponse = CurrentBalanceResponse.newBuilder()
            .setAccountId("invalid-uuid")
            .setCurrentBalance("100.00")
            .setDailyTransacted("50.00")
            .build()

        assertThrows<IllegalArgumentException> {
            currentBalanceResponse.toCurrentBalanceEntity()
        }
    }

    @Test
    fun `toCurrentBalanceEntity should throw exception for invalid decimal amounts`() {
        val accountId = UUID.randomUUID()
        val currentBalanceResponse = CurrentBalanceResponse.newBuilder()
            .setAccountId(accountId.toString())
            .setCurrentBalance("invalid-amount")
            .setDailyTransacted("50.00")
            .build()

        assertThrows<NumberFormatException> {
            currentBalanceResponse.toCurrentBalanceEntity()
        }
    }

    @Test
    fun `should maintain data integrity in round-trip conversion`() {
        val originalEntity = CurrentBalanceEntity(
            accountId = UUID.randomUUID(),
            currentBalance = BigDecimal("1500.75"),
            dailyTransacted = BigDecimal("300.25")
        )

        val grpcResponse = originalEntity.toCurrentBalanceResponse()
        val backToEntity = grpcResponse.toCurrentBalanceEntity()

        assertEquals(originalEntity.accountId, backToEntity.accountId)
        assertEquals(originalEntity.currentBalance, backToEntity.currentBalance)
        assertEquals(originalEntity.dailyTransacted, backToEntity.dailyTransacted)
    }

}
