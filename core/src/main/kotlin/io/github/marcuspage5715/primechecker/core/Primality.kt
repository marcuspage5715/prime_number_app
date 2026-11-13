package io.github.marcuspage5715.primechecker.core

import java.math.BigInteger

/**
 * Exact primality testing for any [Long].
 *
 * The original Python CLI divided by every 6k +/- 1 candidate up to sqrt(n).
 * That is exact but costs up to ~1.5 billion iterations for a 19-digit input,
 * which on a phone means a frozen UI and an ANR dialog. This port keeps the
 * exactness and removes the hang by using a deterministic Miller-Rabin test.
 *
 * Miller-Rabin is normally probabilistic, but the witness set below is proven to
 * be *deterministic* for every n < 3,317,044,064,679,887,385,961,981
 * (~3.3 * 10^24), which is far above [Long.MAX_VALUE] (~9.2 * 10^18). So for
 * every value this API accepts, the answer is a proof, not a probability.
 *
 * Modular multiplication is done with [BigInteger] so that intermediate values
 * can never overflow a 64-bit [Long]; correctness is worth more here than the
 * last few microseconds.
 */
object Primality {

    /**
     * Small primes used for a fast divisibility screen. They double as the
     * Miller-Rabin witness set, and 37 is the largest base needed for the
     * deterministic bound quoted above.
     */
    private val SMALL_PRIMES: LongArray = longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)

    private val BASES: List<BigInteger> = SMALL_PRIMES.map { BigInteger.valueOf(it) }

    private val ONE = BigInteger.ONE

    /**
     * Returns true if [n] is prime.
     *
     * Negative numbers, 0 and 1 are not prime by definition, so they return
     * false rather than throwing: the caller decides how to explain that.
     */
    fun isPrime(n: Long): Boolean {
        if (n < 2L) return false

        // Constant-time screen: catches 2 and 3, and every multiple of the
        // small primes (the vast majority of real inputs) without any bignum
        // work at all.
        for (p in SMALL_PRIMES) {
            if (n == p) return true
            if (n % p == 0L) return false
        }

        // Write n - 1 as d * 2^s with d odd. n is odd here, so s >= 1.
        var d = n - 1L
        var s = 0
        while ((d and 1L) == 0L) {
            d = d shr 1
            s++
        }

        val nBig = BigInteger.valueOf(n)
        val nMinusOne = nBig.subtract(ONE)
        val dBig = BigInteger.valueOf(d)

        for (base in BASES) {
            // Because of the screen above, base is always coprime to n, so a
            // zero result cannot occur and no gcd step is needed.
            var x = base.modPow(dBig, nBig)
            if (x == ONE || x == nMinusOne) continue

            // Square up to s - 1 times looking for -1; if it never appears,
            // this base proves n composite.
            var reachedMinusOne = false
            var round = 1
            while (round < s) {
                x = x.multiply(x).mod(nBig)
                if (x == nMinusOne) {
                    reachedMinusOne = true
                    break
                }
                round++
            }
            if (!reachedMinusOne) return false
        }

        return true
    }

    /**
     * Number of digits in the decimal form of [n]. Kept here (rather than in
     * the UI) so it can be unit-tested and reused by the CLI.
     */
    fun digitCount(n: Long): Int {
        if (n == 0L) return 1
        // -Long.MIN_VALUE overflows, but Long.MIN_VALUE and Long.MAX_VALUE both
        // have 19 digits, so clamping to the positive maximum is exact here.
        var remaining = when {
            n == Long.MIN_VALUE -> Long.MAX_VALUE
            n < 0L -> -n
            else -> n
        }
        var digits = 0
        while (remaining > 0L) {
            remaining /= 10L
            digits++
        }
        return digits
    }
}
