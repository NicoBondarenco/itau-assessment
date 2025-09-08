package com.itau.authorizer.authorization.util

import com.itau.authorizer.common.domain.model.value.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import kotlin.random.Random


object RandomGenerator {

    private val startZonedDateTime: ZonedDateTime = ZonedDateTime.now().minusDays(10)
    private val endZonedDateTime: ZonedDateTime = ZonedDateTime.now()

    fun randomBigDecimal(
        start: Double = 1.0,
        end: Double = 9999.99,
        negative: Boolean = false
    ): BigDecimal = Random.nextDouble(start, end).let { value ->
        BigDecimal.valueOf(value).setScale(2, HALF_EVEN).let {
            if (negative) it.negate() else it
        }
    }

    fun randomTransactionType(
        exclude: List<TransactionType> = listOf()
    ): TransactionType = TransactionType.entries
        .filter { !exclude.contains(it) }
        .random()

    fun randomZonedDateTime(
        start: ZonedDateTime = startZonedDateTime,
        end: ZonedDateTime = endZonedDateTime,
    ): ZonedDateTime {
        val millis = Random.nextLong(
            start.toInstant().toEpochMilli(),
            end.toInstant().toEpochMilli()
        )
        return Instant.ofEpochMilli(millis).let { ZonedDateTime.ofInstant(it, UTC) }
    }

    fun randomInteger(
        start: Int = 0,
        end: Int = 9999,
    ): Int = Random.nextInt(start, end)

}
