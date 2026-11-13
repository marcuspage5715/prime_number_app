package io.github.marcuspage5715.primechecker.core

/** Outcome of turning raw text into a number. */
sealed interface ParseResult {

    /** The text was a valid 64-bit integer. */
    data class Success(val number: Long) : ParseResult

    /** The field was empty or only whitespace. */
    data object Blank : ParseResult

    /** The text could not be used; [reason] explains why. */
    data class Invalid(val reason: Reason) : ParseResult

    enum class Reason {
        /** Contains something other than an optional sign and digits. */
        NOT_A_NUMBER,

        /** Longer than any 64-bit integer, rejected before any parsing. */
        TOO_LONG,

        /** Digits only, but outside the range of a signed 64-bit integer. */
        OUT_OF_RANGE,
    }
}

/**
 * Strict, bounded parser for user input.
 *
 * Two deliberate choices:
 *  - The length is checked *before* any conversion, so a pasted megabyte of
 *    digits costs nothing (input is treated as hostile, even from the user).
 *  - Only ASCII digits 0-9 are accepted. Non-ASCII digit shapes and thousands
 *    separators are rejected instead of being silently guessed at, which keeps
 *    "what you typed" and "what was checked" identical.
 */
object NumberParser {

    /** Longest string that can still be a 64-bit integer (sign not counted). */
    const val MAX_DIGITS = 19

    /** Longest raw input accepted, sign included. */
    const val MAX_INPUT_LENGTH = MAX_DIGITS + 1

    fun parse(raw: String): ParseResult {
        val text = raw.trim()
        if (text.isEmpty()) return ParseResult.Blank
        if (text.length > MAX_INPUT_LENGTH) return ParseResult.Invalid(ParseResult.Reason.TOO_LONG)

        val hasSign = text[0] == '+' || text[0] == '-'
        val digits = if (hasSign) text.substring(1) else text
        if (digits.isEmpty()) return ParseResult.Invalid(ParseResult.Reason.NOT_A_NUMBER)
        if (digits.length > MAX_DIGITS) return ParseResult.Invalid(ParseResult.Reason.TOO_LONG)

        var i = 0
        while (i < digits.length) {
            val c = digits[i]
            if (c < '0' || c > '9') return ParseResult.Invalid(ParseResult.Reason.NOT_A_NUMBER)
            i++
        }

        val value = text.toLongOrNull() ?: return ParseResult.Invalid(ParseResult.Reason.OUT_OF_RANGE)
        return ParseResult.Success(value)
    }
}
