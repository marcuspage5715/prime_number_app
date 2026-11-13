package io.github.marcuspage5715.primechecker.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class FactorSearchTest {

    private fun referenceSmallestFactor(n: Long): Long? {
        if (n < 4L) return null
        var i = 2L
        while (i * i <= n) {
            if (n % i == 0L) return i
            i++
        }
        return null
    }

    @Test
    fun `finds the same smallest factor as naive division`() {
        for (n in -10L..20_000L) {
            assertEquals("mismatch at $n", referenceSmallestFactor(n), FactorSearch.smallestFactor(n))
        }
    }

    @Test
    fun `returns two and three without scanning`() {
        assertEquals(2L, FactorSearch.smallestFactor(1_000_000L))
        assertEquals(3L, FactorSearch.smallestFactor(999_999L))
    }

    @Test
    fun `primes and values below four have no factor`() {
        assertNull(FactorSearch.smallestFactor(97L))
        assertNull(FactorSearch.smallestFactor(2L))
        assertNull(FactorSearch.smallestFactor(3L))
        assertNull(FactorSearch.smallestFactor(1L))
        assertNull(FactorSearch.smallestFactor(0L))
        assertNull(FactorSearch.smallestFactor(-15L))
        assertNull(FactorSearch.smallestFactor(9_223_372_036_854_775_783L))
    }

    @Test
    fun `finds a factor just below the limit`() {
        assertEquals(999_983L, FactorSearch.smallestFactor(999_983L * 1_000_003L))
    }

    @Test
    fun `stops at the limit instead of scanning forever`() {
        // Both factors sit above the default budget, so the answer is "none found".
        assertNull(FactorSearch.smallestFactor(1_000_003L * 1_000_033L))
        // An explicit smaller budget behaves the same way.
        assertNull(FactorSearch.smallestFactor(1009L * 1013L, limit = 100L))
        assertEquals(1009L, FactorSearch.smallestFactor(1009L * 1013L, limit = 2000L))
    }

    @Test
    fun `cancellation stops the search and returns null`() {
        var polls = 0
        val result = FactorSearch.smallestFactor(1_000_003L * 1_000_033L) {
            polls++
            polls < 100
        }
        assertNull(result)
        assertEquals(100, polls)
    }

    @Test
    fun `integer square root is exact across the range`() {
        for (n in 0L..10_000L) {
            val root = FactorSearch.isqrt(n)
            assertEquals("isqrt($n)", Math.floor(Math.sqrt(n.toDouble())).toLong(), root)
        }
        // Perfect squares, and their neighbours, at the top of the range.
        val bigRoot = 3_037_000_499L
        assertEquals(bigRoot, FactorSearch.isqrt(bigRoot * bigRoot))
        assertEquals(bigRoot - 1L, FactorSearch.isqrt(bigRoot * bigRoot - 1L))
        assertEquals(bigRoot, FactorSearch.isqrt(Long.MAX_VALUE))
        assertEquals(0L, FactorSearch.isqrt(0L))
        assertEquals(1L, FactorSearch.isqrt(1L))
    }

    @Test
    fun `integer square root rejects negatives`() {
        assertThrows(IllegalArgumentException::class.java) { FactorSearch.isqrt(-1L) }
    }
}
