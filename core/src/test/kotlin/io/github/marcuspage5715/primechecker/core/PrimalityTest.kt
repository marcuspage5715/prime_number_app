package io.github.marcuspage5715.primechecker.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrimalityTest {

    /**
     * The original Python algorithm, reimplemented independently. Two different
     * algorithms agreeing on every small input is strong evidence that the port
     * is faithful.
     */
    private fun referenceIsPrime(n: Long): Boolean {
        if (n < 2L) return false
        if (n < 4L) return true
        if (n % 2L == 0L || n % 3L == 0L) return false
        var i = 5L
        while (i * i <= n) {
            if (n % i == 0L || n % (i + 2L) == 0L) return false
            i += 6L
        }
        return true
    }

    @Test
    fun `matches trial division for every value in a dense range`() {
        for (n in -20L..20_000L) {
            assertEquals("mismatch at $n", referenceIsPrime(n), Primality.isPrime(n))
        }
    }

    @Test
    fun `negatives zero and one are not prime`() {
        assertFalse(Primality.isPrime(-7L))
        assertFalse(Primality.isPrime(-1L))
        assertFalse(Primality.isPrime(0L))
        assertFalse(Primality.isPrime(1L))
    }

    @Test
    fun `smallest primes are prime`() {
        for (n in longArrayOf(2L, 3L, 5L, 7L, 11L, 13L, 97L, 7919L, 104_729L)) {
            assertTrue("$n should be prime", Primality.isPrime(n))
        }
    }

    @Test
    fun `Carmichael numbers are not prime`() {
        // These pass a naive Fermat test, so they catch a broken Miller-Rabin.
        val carmichael = longArrayOf(
            561L, 1105L, 1729L, 2465L, 2821L, 6601L, 8911L, 10_585L, 15_841L,
            29_341L, 41_041L, 52_633L, 62_745L, 63_973L, 75_361L, 101_101L,
        )
        for (n in carmichael) {
            assertFalse("$n is composite", Primality.isPrime(n))
        }
    }

    @Test
    fun `strong pseudoprimes to base two are not prime`() {
        val pseudoprimes = longArrayOf(
            2047L, 3277L, 4033L, 4681L, 8321L, 15_841L, 29_341L, 42_799L,
            49_141L, 52_633L, 65_281L, 74_665L, 80_581L, 85_489L, 88_357L,
            90_751L, 104_653L, 130_561L, 196_093L, 220_729L, 233_017L,
        )
        for (n in pseudoprimes) {
            assertFalse("$n is composite", Primality.isPrime(n))
        }
    }

    @Test
    fun `perfect powers are never prime`() {
        for (base in 2L..60L) {
            var value = base * base
            while (value in 0..(Long.MAX_VALUE / base)) {
                assertFalse("$base^k = $value is composite", Primality.isPrime(value))
                value *= base
            }
        }
    }

    @Test
    fun `sixty four bit boundaries are handled exactly`() {
        // 2^63 - 1 is NOT prime: it is 7 * 7 * 73 * 127 * 337 * 92737 * 649657.
        assertFalse(Primality.isPrime(Long.MAX_VALUE))
        assertFalse(Primality.isPrime(Long.MIN_VALUE))
        assertFalse(Primality.isPrime(Long.MAX_VALUE - 1L))
        // Largest prime below 2^63, and a Mersenne prime.
        assertTrue(Primality.isPrime(9_223_372_036_854_775_783L))
        assertTrue(Primality.isPrime(2_305_843_009_213_693_951L))
        // 10^18 + 3 and 10^18 + 9 are the first primes past 10^18.
        assertTrue(Primality.isPrime(1_000_000_000_000_000_003L))
        assertTrue(Primality.isPrime(1_000_000_000_000_000_009L))
        // Product of two large primes.
        assertFalse(Primality.isPrime(1_000_000_007L * 1_000_000_009L))
    }

    @Test
    fun `digit count is correct at the edges`() {
        assertEquals(1, Primality.digitCount(0L))
        assertEquals(1, Primality.digitCount(-9L))
        assertEquals(2, Primality.digitCount(10L))
        assertEquals(3, Primality.digitCount(-100L))
        assertEquals(19, Primality.digitCount(Long.MAX_VALUE))
        assertEquals(19, Primality.digitCount(Long.MIN_VALUE))
    }
}
