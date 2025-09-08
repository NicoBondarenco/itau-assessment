package com.itau.authorizer.authorization.domain.usecase

import com.itau.authorizer.authorization.application.adapter.`in`.grpc.TestCurrentBalanceRetrieverRPC
import com.itau.authorizer.authorization.util.RandomGenerator.randomInteger
import com.itau.authorizer.authorization.util.RandomGenerator.randomZonedDateTime
import io.grpc.Status
import io.grpc.StatusException
import java.time.LocalDate
import java.util.UUID.randomUUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired

class RetrieveCurrentBalanceTest : BaseContextTest() {

    @Autowired
    private lateinit var rpc: TestCurrentBalanceRetrieverRPC

    @BeforeEach
    fun beforeEach() {
        client.clear()
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1])
    fun `retrieve current balance with transactions`(days: Int) = runTest(StandardTestDispatcher()) {
        val accountId = randomUUID()
        val balance = client.generateBalance(accountId)
        val dayTransaction = (days..2).map { minusDay ->
            val day = LocalDate.now().minusDays(minusDay.toLong()).dayOfYear
            (1..randomInteger(1, 3)).map {
                client.generateTransaction(
                    accountId = accountId,
                    timestamp = randomZonedDateTime().withDayOfYear(day)
                )
            }
        }.flatten().filter { it.timestamp.toLocalDate().dayOfYear == LocalDate.now().dayOfYear }

        val currentBalance = rpc.accountCurrentBalance(accountId)
        assertEquals(balance.amount, currentBalance.currentBalance)
        assertEquals(0, dayTransaction.sumOf { it.amount }.compareTo(currentBalance.dailyTransacted))
    }

    @Test
    fun `retrieve current balance without balance`() = runTest(StandardTestDispatcher()) {
        val accountId = randomUUID()
        val exception = assertThrows<StatusException> {
            rpc.accountCurrentBalance(accountId)
        }
        Assertions.assertEquals(Status.NOT_FOUND.code, exception.status.code)
        Assertions.assertEquals("Account $accountId has no balance", exception.status.description)
    }

}
