package io.github.marcuspage5715.primechecker.cli

import io.github.marcuspage5715.primechecker.core.NumberParser
import io.github.marcuspage5715.primechecker.core.ParseResult
import io.github.marcuspage5715.primechecker.core.PrimeAnalysis
import io.github.marcuspage5715.primechecker.core.PrimeChecker
import io.github.marcuspage5715.primechecker.core.Verdict
import kotlin.system.exitProcess

/**
 * Terminal front end, kept from the original Python script so the app's logic
 * can be exercised without an emulator.
 *
 * Exit codes match the original script exactly:
 *   0  the number is prime
 *   1  the number is not prime
 *   2  the input was not a usable integer
 */
private const val EXIT_PRIME = 0
private const val EXIT_NOT_PRIME = 1
private const val EXIT_BAD_INPUT = 2

private val QUIT_WORDS = setOf("q", "quit", "exit")

fun main(args: Array<String>) {
    exitProcess(
        runCli(
            args = args.toList(),
            readLine = { readlnOrNull() },
            writeLine = { line -> println(line) },
        ),
    )
}

/**
 * Pure entry point: all input and output is injected, so the whole CLI is
 * unit-testable without touching the real console.
 */
fun runCli(
    args: List<String>,
    readLine: () -> String?,
    writeLine: (String) -> Unit,
    checker: PrimeChecker = PrimeChecker(),
): Int {
    if (args.size > 1) {
        writeLine("Usage: prime-checker [NUMBER]")
        return EXIT_BAD_INPUT
    }

    // One-shot mode, like the original: a number was supplied on the command
    // line, so print one line and exit.
    if (args.size == 1) return checkOne(args[0], writeLine, checker)

    // Interactive mode: stay open until the user asks to leave.
    writeLine("Prime number checker. Enter a number, or 'q' to quit.")
    while (true) {
        val raw = readLine() ?: break
        val trimmed = raw.trim()
        if (trimmed.isEmpty() || trimmed.lowercase() in QUIT_WORDS) break
        checkOne(trimmed, writeLine, checker)
    }
    writeLine("Goodbye.")
    return EXIT_PRIME
}

private fun checkOne(raw: String, writeLine: (String) -> Unit, checker: PrimeChecker): Int =
    when (val parsed = NumberParser.parse(raw)) {
        is ParseResult.Success -> {
            val analysis = checker.analyze(parsed.number)
            writeLine(describe(analysis))
            if (analysis.isPrime) EXIT_PRIME else EXIT_NOT_PRIME
        }

        ParseResult.Blank -> {
            writeLine("Not an integer: ''")
            EXIT_BAD_INPUT
        }

        is ParseResult.Invalid -> {
            writeLine("Not an integer: ${raw.trim()}")
            EXIT_BAD_INPUT
        }
    }

private fun describe(analysis: PrimeAnalysis): String = when (analysis.verdict) {
    Verdict.PRIME -> "${analysis.number} is prime"
    Verdict.COMPOSITE_WITH_FACTOR ->
        "${analysis.number} is not prime (divisible by ${analysis.smallestFactor})"
    Verdict.COMPOSITE_NO_SMALL_FACTOR ->
        "${analysis.number} is not prime (no prime factor up to ${analysis.factorSearchLimit})"
    Verdict.BELOW_TWO -> "${analysis.number} is not prime"
}
