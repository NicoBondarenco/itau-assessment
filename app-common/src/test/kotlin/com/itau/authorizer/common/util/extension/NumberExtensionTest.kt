package com.itau.authorizer.common.util.extension

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NumberExtensionTest {

    @Test
    fun `toAmountFormat should format BigDecimal with two decimal places`() {
        val amount = BigDecimal("100.5")

        val result = amount.toAmountFormat()

        assertEquals("100.50", result)
    }

    @Test
    fun `toAmountFormat should format whole number with two decimal places`() {
        val amount = BigDecimal("100")

        val result = amount.toAmountFormat()

        assertEquals("100.00", result)
    }

    @Test
    fun `toAmountFormat should format zero with two decimal places`() {
        val amount = BigDecimal.ZERO

        val result = amount.toAmountFormat()

        assertEquals("0.00", result)
    }

    @Test
    fun `toAmountFormat should format negative amount with two decimal places`() {
        val amount = BigDecimal("-50.25")

        val result = amount.toAmountFormat()

        assertEquals("-50.25", result)
    }

    @Test
    fun `toAmountFormat should format large amount with two decimal places`() {
        val amount = BigDecimal("999999999.99")

        val result = amount.toAmountFormat()

        assertEquals("999999999.99", result)
    }

    @Test
    fun `toAmountFormat should round up using HALF_EVEN rounding for 0_5 ending`() {
        val amount = BigDecimal("10.125")

        val result = amount.toAmountFormat()

        assertEquals("10.12", result)
    }

    @Test
    fun `toAmountFormat should round up using HALF_EVEN rounding for 1_5 ending`() {
        val amount = BigDecimal("10.135")

        val result = amount.toAmountFormat()

        assertEquals("10.14", result)
    }

    @Test
    fun `toAmountFormat should round down for values less than 0_5`() {
        val amount = BigDecimal("10.124")

        val result = amount.toAmountFormat()

        assertEquals("10.12", result)
    }

    @Test
    fun `toAmountFormat should round up for values greater than 0_5`() {
        val amount = BigDecimal("10.126")

        val result = amount.toAmountFormat()

        assertEquals("10.13", result)
    }

    @Test
    fun `toAmountFormat should handle HALF_EVEN rounding for even digits`() {
        val amount1 = BigDecimal("10.225") // rounds to 10.22 (even)
        val amount2 = BigDecimal("10.235") // rounds to 10.24 (even)

        val result1 = amount1.toAmountFormat()
        val result2 = amount2.toAmountFormat()

        assertEquals("10.22", result1)
        assertEquals("10.24", result2)
    }

}
