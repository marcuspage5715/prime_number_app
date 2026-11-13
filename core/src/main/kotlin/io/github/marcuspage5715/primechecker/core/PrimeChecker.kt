package io.github.marcuspage5715.primechecker.core

/** How a number should be described to a human. */
enum class Verdict {
    /** The number is prime. */
    PRIME,

    /** Composite, and its smallest prime factor was found. */
    COMPOSITE_WITH_FACTOR,

    /** Composite, but no factor was found within the search budget. */
    COMPOSITE_NO_SMALL_FACTOR,

    /** Below 2, so not prime by definition (negatives, 0 and 1). */
    BELOW_TWO,
}

/** Everything the UI needs to know about one checked number. */
data class PrimeAnalysis(
    val number: Long,
    val isPrime: Boolean,
    val smallestFactor: Long?,
    val factorSearchLimit: Long,
) {
    val verdict: Verdict
        get() = when {
            isPrime -> Verdict.PRIME
            number < 2L -> Verdict.BELOW_TWO
            smallestFactor != null -> Verdict.COMPOSITE_WITH_FACTOR
            else -> Verdict.COMPOSITE_NO_SMALL_FACTOR
        }

    val digitCount: Int get() = Primality.digitCount(number)
}

/**
 * The one place that answers "is this number prime, and why".
 *
 * Both front ends (the Android app and the CLI) call this, so the answer can
 * never drift between them.
 */
class PrimeChecker(private val factorSearchLimit: Long = FactorSearch.DEFAULT_LIMIT) {

    init {
        require(factorSearchLimit >= 4L) { "factorSearchLimit must be at least 4, was $factorSearchLimit" }
    }

    /**
     * @param shouldContinue polled during the factor search so a caller can
     *   cancel a long search. Primality itself is always fast enough to run to
     *   completion.
     */
    fun analyze(number: Long, shouldContinue: () -> Boolean = { true }): PrimeAnalysis {
        val prime = Primality.isPrime(number)
        val factor = if (prime) {
            null
        } else {
            FactorSearch.smallestFactor(number, factorSearchLimit, shouldContinue)
        }
        return PrimeAnalysis(
            number = number,
            isPrime = prime,
            smallestFactor = factor,
            factorSearchLimit = factorSearchLimit,
        )
    }
}
