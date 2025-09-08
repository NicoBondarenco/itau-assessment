package com.itau.authorizer.common.util.extension

import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StringExtensionTest {

    @Test
    fun `toSnakeCase should convert simple camelCase to snake_case`() {
        val result = "camelCase".toSnakeCase()

        assertEquals("camel_case", result)
    }

    @Test
    fun `toSnakeCase should convert multiple words to snake_case`() {
        val result = "thisIsALongCamelCase".toSnakeCase()

        assertEquals("this_is_a_long_camel_case", result)
    }

    @Test
    fun `toSnakeCase should handle single word`() {
        val result = "word".toSnakeCase()

        assertEquals("word", result)
    }

    @Test
    fun `toSnakeCase should handle single uppercase word`() {
        val result = "Word".toSnakeCase()

        assertEquals("word", result)
    }


    @Test
    fun `toSnakeCase should handle empty string`() {
        val result = "".toSnakeCase()

        assertEquals("", result)
    }

    @Test
    fun `toUUID should convert valid UUID string to UUID`() {
        val uuidString = "550e8400-e29b-41d4-a716-446655440000"

        val result = uuidString.toUUID()

        assertEquals(UUID.fromString(uuidString), result)
    }

    @Test
    fun `toUUID should throw IllegalArgumentException for invalid UUID format`() {
        val invalidUuid = "invalid-uuid-string"

        assertThrows<IllegalArgumentException> {
            invalidUuid.toUUID()
        }
    }

    @Test
    fun `toUUID should throw IllegalArgumentException for malformed UUID`() {
        val malformedUuid = "5550e8400-e29b-41d4g-a716-44665544000"

        assertThrows<IllegalArgumentException> {
            malformedUuid.toUUID()
        }
    }

    @Test
    fun `toUUID should throw IllegalArgumentException for empty string`() {
        assertThrows<IllegalArgumentException> {
            "".toUUID()
        }
    }

    @Test
    fun `toZonedDateTime should parse valid ISO zoned date time`() {
        val dateTimeString = "2024-01-15T10:30:45Z"

        val result = dateTimeString.toZonedDateTime()

        assertEquals(ZonedDateTime.parse(dateTimeString), result)
    }

    @Test
    fun `toZonedDateTime should parse zoned date time with timezone`() {
        val dateTimeString = "2024-01-15T10:30:45+02:00"

        val result = dateTimeString.toZonedDateTime()

        assertEquals(ZonedDateTime.parse(dateTimeString), result)
    }

    @Test
    fun `toZonedDateTime should throw DateTimeParseException for invalid format`() {
        val invalidDateTime = "invalid-date-time"

        assertThrows<DateTimeParseException> {
            invalidDateTime.toZonedDateTime()
        }
    }

    @Test
    fun `toZonedDateTime should throw DateTimeParseException for malformed date`() {
        val malformedDate = "2024-13-45T25:70:90Z"

        assertThrows<DateTimeParseException> {
            malformedDate.toZonedDateTime()
        }
    }

    @Test
    fun `toZonedDateTime should throw DateTimeParseException for empty string`() {
        assertThrows<DateTimeParseException> {
            "".toZonedDateTime()
        }
    }

}
