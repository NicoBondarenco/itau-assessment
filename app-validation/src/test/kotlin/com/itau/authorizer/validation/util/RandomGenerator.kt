package com.itau.authorizer.validation.util

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
    private val CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toList()
    private val NUMBERS = "0123456789".toList()

    fun randomString(
        length: Int = 10,
        numbers: Boolean = true,
        spaces: Boolean = true,
    ): String = ((NUMBERS.takeIf { numbers } ?: listOf()) + (listOf(" ").takeIf { spaces } ?: listOf()) + CHARS).let { chars ->
        (1..length).map { chars.shuffled().first() }.joinToString("")
    }

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
