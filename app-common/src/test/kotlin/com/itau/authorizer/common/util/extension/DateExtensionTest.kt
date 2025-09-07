package com.itau.authorizer.common.util.extension

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class DateExtensionTest {

    @Test
    fun `toIsoFormat should format ZonedDateTime to ISO string`() {
        val zonedDateTime = ZonedDateTime.of(2024, 1, 15, 10, 30, 45, 123456789, ZoneId.of("UTC"))

        val result = zonedDateTime.toIsoFormat()

        assertEquals("2024-01-15T10:30:45.123456789Z[UTC]", result)
    }

    @Test
    fun `toIsoFormat should format ZonedDateTime with timezone offset`() {
        val zonedDateTime = ZonedDateTime.of(2024, 1, 15, 10, 30, 45, 0, ZoneId.of("+02:00"))

        val result = zonedDateTime.toIsoFormat()

        assertEquals("2024-01-15T10:30:45+02:00", result)
    }

    @Test
    fun `toIsoFormat should format ZonedDateTime with zone id`() {
        val zonedDateTime = ZonedDateTime.of(2024, 1, 15, 10, 30, 45, 0, ZoneId.of("America/New_York"))

        val result = zonedDateTime.toIsoFormat()

        assertNotNull(result)
        assertEquals(zonedDateTime, ZonedDateTime.parse(result, zonedDateTimeFormatter))
    }

    @Test
    fun `atEndOfDay should return end of day for LocalDate in UTC`() {
        val localDate = LocalDate.of(2024, 1, 15)
        val zone = ZoneId.of("UTC")

        val result = localDate.atEndOfDay(zone)

        val expectedEndOfDay = ZonedDateTime.of(2024, 1, 15, 23, 59, 59, 999999999, zone)
        assertEquals(expectedEndOfDay, result)
    }

}
