package io.github.marcuspage5715.primechecker.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberParserTest {

    private fun number(raw: String): Long {
        val result = NumberParser.parse(raw)
        assertTrue("expected success for <$raw>, got $result", result is ParseResult.Success)
        return (result as ParseResult.Success).number
    }

    private fun reason(raw: String): ParseResult.Reason {
        val result = NumberParser.parse(raw)
        assertTrue("expected failure for <$raw>, got $result", result is ParseResult.Invalid)
        return (result as ParseResult.Invalid).reason
    }

    @Test
    fun `accepts plain and signed integers`() {
        assertEquals(97L, number("97"))
        assertEquals(97L, number("  97  "))
        assertEquals(7L, number("+7"))
        assertEquals(-13L, number("-13"))
        assertEquals(0L, number("0"))
        assertEquals(-0L, number("-0"))
    }

    @Test
    fun `accepts the full 64 bit range`() {
        assertEquals(Long.MAX_VALUE, number("9223372036854775807"))
        assertEquals(Long.MIN_VALUE, number("-9223372036854775808"))
    }

    @Test
    fun `rejects one past the 64 bit range`() {
        assertEquals(ParseResult.Reason.OUT_OF_RANGE, reason("9223372036854775808"))
        assertEquals(ParseResult.Reason.OUT_OF_RANGE, reason("-9223372036854775809"))
    }

    @Test
    fun `blank input is reported as blank`() {
        assertEquals(ParseResult.Blank, NumberParser.parse(""))
        assertEquals(ParseResult.Blank, NumberParser.parse("    "))
    }

    @Test
    fun `rejects anything that is not an ascii digit`() {
        assertEquals(ParseResult.Reason.NOT_A_NUMBER, reason("abc"))
        assertEquals(ParseResult.Reason.NOT_A_NUMBER, reason("12a"))
        assertEquals(ParseResult.Reason.NOT_A_NUMBER, reason("1 2"))
        assertEquals(ParseResult.Reason.NOT_A_NUMBER, reason("1,234"))
        assertEquals(ParseResult.Reason.NOT_A_NUMBER, reason("1.5"))
        assertEquals(ParseResult.Reason.NOT_A_NUMBER, reason("--5"))
        assertEquals(ParseResult.Reason.NOT_A_NUMBER, reason("+"))
        assertEquals(ParseResult.Reason.NOT_A_NUMBER, reason("-"))
        // Arabic-Indic digits are digits, but not the ones this parser accepts.
        assertEquals(ParseResult.Reason.NOT_A_NUMBER, reason("\u0661\u0662\u0663"))
    }

    @Test
    fun `rejects over-long input without parsing it`() {
        assertEquals(ParseResult.Reason.TOO_LONG, reason("1".repeat(20)))
        assertEquals(ParseResult.Reason.TOO_LONG, reason("-" + "1".repeat(20)))
        // A hostile paste must be rejected instantly, not converted.
        assertEquals(ParseResult.Reason.TOO_LONG, reason("9".repeat(1_000_000)))
    }
}
