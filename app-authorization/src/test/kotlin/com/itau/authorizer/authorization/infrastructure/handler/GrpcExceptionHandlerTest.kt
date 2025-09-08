package com.itau.authorizer.authorization.infrastructure.handler

import com.itau.authorizer.common.domain.exception.BalanceNotFoundException
import io.grpc.Status
import java.util.UUID.randomUUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class GrpcExceptionHandlerTest {

    private val handler = GrpcExceptionHandler()

    @Test
    fun `should handle BalanceNotFoundException and return NOT_FOUND status`() {
        val accountId = randomUUID()
        val exception = BalanceNotFoundException(accountId)

        val result = handler.handleException(exception)

        assertEquals(Status.NOT_FOUND.code, result.status.code)
        assertEquals("Account $accountId has no balance", result.status.description)
        assertSame(exception, result.cause)
    }

    @Test
    fun `should handle IllegalArgumentException and return INTERNAL status`() {
        val exception = IllegalArgumentException("Invalid argument provided")
        val result = handler.handleException(exception)
        assertEquals(Status.INTERNAL.code, result.status.code)
        assertEquals("Invalid argument provided", result.status.description)
        assertSame(exception, result.cause)
    }

    @Test
    fun `should handle RuntimeException and return INTERNAL status`() {
        val exception = RuntimeException("Unexpected runtime error")
        val result = handler.handleException(exception)
        assertEquals(Status.INTERNAL.code, result.status.code)
        assertEquals("Unexpected runtime error", result.status.description)
        assertSame(exception, result.cause)
    }

}
