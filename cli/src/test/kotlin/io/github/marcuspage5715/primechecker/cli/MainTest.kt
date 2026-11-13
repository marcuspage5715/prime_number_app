package io.github.marcuspage5715.primechecker.cli

import org.junit.Assert.assertEquals
import org.junit.Test

class MainTest {

    private class Console(private vararg val inputs: String?) {
        private var index = 0
        val lines = mutableListOf<String>()

        fun read(): String? = if (index < inputs.size) inputs[index++] else null
        fun write(line: String) {
            lines.add(line)
        }
    }

    private fun run(vararg args: String): Pair<Int, List<String>> {
        val console = Console()
        val code = runCli(args.toList(), console::read, console::write)
        return code to console.lines
    }

    private fun runInteractive(vararg inputs: String?): Pair<Int, List<String>> {
        val console = Console(*inputs)
        val code = runCli(emptyList(), console::read, console::write)
        return code to console.lines
    }

    @Test
    fun `one-shot prime exits 0`() {
        val (code, lines) = run("97")
        assertEquals(0, code)
        assertEquals(listOf("97 is prime"), lines)
    }

    @Test
    fun `one-shot composite exits 1 and names a factor`() {
        val (code, lines) = run("91")
        assertEquals(1, code)
        assertEquals(listOf("91 is not prime (divisible by 7)"), lines)
    }

    @Test
    fun `one-shot non-integer exits 2`() {
        val (code, lines) = run("abc")
        assertEquals(2, code)
        assertEquals(listOf("Not an integer: abc"), lines)
    }

    @Test
    fun `numbers below two are not prime`() {
        val (code, lines) = run("1")
        assertEquals(1, code)
        assertEquals(listOf("1 is not prime"), lines)
    }

    @Test
    fun `too many arguments is a usage error`() {
        val (code, lines) = run("1", "2")
        assertEquals(2, code)
        assertEquals(listOf("Usage: prime-checker [NUMBER]"), lines)
    }

    @Test
    fun `interactive mode checks several numbers then says goodbye`() {
        val (code, lines) = runInteractive("97", "91", "q")
        assertEquals(0, code)
        assertEquals(
            listOf(
                "Prime number checker. Enter a number, or 'q' to quit.",
                "97 is prime",
                "91 is not prime (divisible by 7)",
                "Goodbye.",
            ),
            lines,
        )
    }

    @Test
    fun `interactive mode stops at end of input`() {
        val (code, lines) = runInteractive("13", null)
        assertEquals(0, code)
        assertEquals(listOf(
            "Prime number checker. Enter a number, or 'q' to quit.",
            "13 is prime",
            "Goodbye.",
        ), lines)
    }
}
