package com.itau.authorizer.web.domain.usecase

import com.itau.authorizer.common.domain.model.value.TransactionType
import com.itau.authorizer.common.domain.model.entity.AccountDataEntity
import com.itau.authorizer.common.domain.model.entity.AccountEntity
import com.itau.authorizer.common.domain.model.entity.BalanceEntity
import com.itau.authorizer.common.domain.model.entity.TransactionEntity
import com.itau.authorizer.web.domain.port.`in`.AccountBatchSaver
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID
import java.util.UUID.randomUUID
import kotlin.random.Random
import org.springframework.stereotype.Service

@Service
class DataUsecase(
    private val accountBatchSaver: AccountBatchSaver,
) {

    suspend fun generateData(quantity: Int) {
        val accounts = (0..quantity).map { generateAccountData() }
        accountBatchSaver.saveAll(accounts)
    }

    private fun generateAccountData() = randomUUID().let { accountId ->
        AccountDataEntity(
            account = generateAccount(accountId),
            balance = generateBalance(accountId),
            transactions = (0..7).map { minusDays ->
                (0..randomLong(0, 5)).map { count ->
                    generateTransaction(accountId, minusDays.toLong())
                }
            }.flatten(),
        )
    }

    private fun generateAccount(
        accountId: UUID,
    ) = AccountEntity(
        accountId = accountId,
        createdAt = randomZonedDateTime(8),
        isActive = (1..10).random() < 9,
        dailyLimit = randomBigDecimal(500.0, 2000.0),
    )

    private fun generateBalance(
        accountId: UUID,
    ) = BalanceEntity(
        accountId = accountId,
        amount = randomBigDecimal(5000.0, 10000.0),
        lastUpdate = ZonedDateTime.now(),
    )

    private fun generateTransaction(
        accountId: UUID,
        minusDays: Long,
    ) = TransactionEntity(
        transactionId = randomUUID(),
        accountId = accountId,
        amount = randomBigDecimal(100.0, 250.0),
        type = TransactionType.entries.random(),
        timestamp = randomZonedDateTime(minusDays),
    )

    private fun randomZonedDateTime(
        minusDays: Long = 0
    ): ZonedDateTime = randomLong(0, (24 * 60 * 60 * 1_000_000_000L)).let {
        LocalDate.now().atStartOfDay(ZoneOffset.UTC).plusNanos(it)
    }.minusDays(minusDays)

    fun randomLong(
        start: Long = 0,
        end: Long = 9999999,
    ): Long = Random.nextLong(start, end)

    private fun randomBigDecimal(
        start: Double = 0.0,
        end: Double = 9999.9999,
    ): BigDecimal = Random.nextDouble(start, end).let { BigDecimal.valueOf(it) }

}
