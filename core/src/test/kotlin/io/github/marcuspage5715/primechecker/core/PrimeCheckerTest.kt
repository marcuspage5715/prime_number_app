package io.github.marcuspage5715.primechecker.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PrimeCheckerTest {

    private val checker = PrimeChecker()

    @Test
    fun `prime numbers get the prime verdict`() {
        val analysis = checker.analyze(97L)
        assertTrue(analysis.isPrime)
        assertEquals(Verdict.PRIME, analysis.verdict)
        assertNull(analysis.smallestFactor)
        assertEquals(2, analysis.digitCount)
    }

    @Test
    fun `composites report their smallest factor`() {
        val analysis = checker.analyze(91L)
        assertEquals(Verdict.COMPOSITE_WITH_FACTOR, analysis.verdict)
        assertEquals(7L, analysis.smallestFactor)
    }

    @Test
    fun `numbers below two are neither prime nor factorable`() {
        for (n in longArrayOf(-9L, -1L, 0L, 1L)) {
            val analysis = checker.analyze(n)
            assertEquals("verdict for $n", Verdict.BELOW_TWO, analysis.verdict)
            assertNull(analysis.smallestFactor)
        }
    }

    @Test
    fun `a composite with only large factors reports the search budget`() {
        val tight = PrimeChecker(factorSearchLimit = 100L)
        val analysis = tight.analyze(1009L * 1013L)
        assertEquals(Verdict.COMPOSITE_NO_SMALL_FACTOR, analysis.verdict)
        assertEquals(100L, analysis.factorSearchLimit)
        assertNull(analysis.smallestFactor)
    }

    @Test
    fun `rejects a nonsensical factor search budget`() {
        assertThrows(IllegalArgumentException::class.java) { PrimeChecker(factorSearchLimit = 1L) }
    }

    @Test
    fun `cancellation yields the composite verdict without a factor`() {
        val analysis = checker.analyze(1_000_003L * 1_000_033L) { false }
        assertEquals(Verdict.COMPOSITE_NO_SMALL_FACTOR, analysis.verdict)
        assertNull(analysis.smallestFactor)
    }
}
