package io.github.marcuspage5715.primechecker.core

/**
 * Finds the smallest prime factor of a composite number, with a hard budget so
 * the UI can never be blocked for long.
 *
 * Why a budget: a 19-digit number can have a smallest factor near 3 * 10^9, and
 * scanning that far would take minutes. [DEFAULT_LIMIT] settles every factor up
 * to one million (which covers the overwhelming majority of inputs) in a few
 * milliseconds, and the UI states plainly when the search stopped early.
 */
object FactorSearch {

    /** Largest candidate divisor the default search will try. */
    const val DEFAULT_LIMIT: Long = 1_000_000L

    /**
     * Smallest factor of [n] found by trial division by 6k +/- 1 up to
     * `min(limit, sqrt(n))`, or null if none was found (including when the
     * search was cancelled or [n] is below 4).
     *
     * [shouldContinue] is polled between candidates; pass a coroutine-aware
     * predicate to make the search cancellable from a ViewModel.
     */
    fun smallestFactor(
        n: Long,
        limit: Long = DEFAULT_LIMIT,
        shouldContinue: () -> Boolean = { true },
    ): Long? {
        if (n < 4L) return null
        if (n % 2L == 0L) return 2L
        if (n % 3L == 0L) return 3L

        val ceiling = minOf(limit, isqrt(n))
        var candidate = 5L
        while (candidate <= ceiling) {
            if (!shouldContinue()) return null
            if (n % candidate == 0L) return candidate
            val twin = candidate + 2L
            if (twin <= ceiling && n % twin == 0L) return twin
            candidate += 6L
        }
        return null
    }

    /**
     * Integer square root by Newton's method, exact for every non-negative
     * [Long]. Written without floating point so there is no rounding drift at
     * the top of the range.
     */
    fun isqrt(n: Long): Long {
        if (n < 0L) throw IllegalArgumentException("isqrt needs a non-negative value, got $n")
        if (n < 2L) return n
        // Seed from a double sqrt, then correct with division-only arithmetic
        // (never multiplication, which would overflow) until it is exact. The
        // seed can only ever be off by one, and both loops are monotone, so
        // this terminates immediately in practice and is exact always.
        var root = Math.sqrt(n.toDouble()).toLong()
        if (root < 1L) root = 1L
        while (root > n / root) root--
        while (root + 1L <= n / (root + 1L)) root++
        return root
    }
}
