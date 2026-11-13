# Architecture

## Module graph

```
        +-----------------+
        |      :core      |   pure Kotlin/JVM, no Android APIs
        |  Primality      |   deterministic Miller-Rabin
        |  FactorSearch   |   bounded, cancellable trial division
        |  NumberParser   |   bounded, strict input parsing
        |  PrimeChecker   |   use case: number -> PrimeAnalysis
        +--------+--------+
                 |
      +----------+-----------+
      |                      |
+-----+------+        +------+------+
|    :app    |        |    :cli    |
| Compose UI |        |  terminal   |
| ViewModel  |        |  exit codes |
+------------+        +-------------+
```

`:core` depends on nothing but the Kotlin standard library and `java.math`. That
is what lets its tests run on a plain JVM in about a second, and what lets the
same code answer in the app and in a terminal.

## Data flow

```
keystroke ──▶ PrimeCheckerViewModel.onQueryChange ──▶ PrimeUiState (query, Idle)
                                                        │
tap Check ──▶ onCheck ──▶ NumberParser.parse ──▶ ParseResult
                                                        │
                        ┌───────────────────────────────┴───────────────┐
                        │ Success                     Blank / Invalid   │
                        ▼                                               ▼
        Dispatchers.Default: PrimeChecker.analyze            CheckResult.Empty / .Invalid
                        │                                               │
                        ▼                                               │
                PrimeAnalysis                                           │
                        │                                               │
                        └──────────────▶ PrimeUiState ◀─────────────────┘
                                              │
                                       Compose recomposes
```

The screen is stateless: `PrimeCheckerScreen(state, onQueryChange, onCheck,
onClear)`. It holds no logic and reads no globals, so a `@Preview` or an
instrumented test can render any state directly. All state lives in the
`ViewModel`, which survives rotation and theme changes.

## Key decisions

**Why a separate `:core` module instead of `util/` inside `:app`.**
Keeping the maths free of Android types means its tests are fast JVM tests, its
API is enforced by a module boundary, and the CLI can reuse it. Android
instrumented tests are slow and flaky; arithmetic should not depend on them.

**Why deterministic Miller–Rabin instead of trial division.**
Exactness was non-negotiable (it is the product), and so was not freezing the
UI. The fixed witness set {2,…,37} is proven for n < 3.3 × 10²⁴, comfortably
beyond `Long.MAX_VALUE`. `BigInteger.modPow` supplies overflow-free modular
arithmetic, so correctness does not rest on hand-rolled 128-bit tricks.

**Why `Long` rather than `BigInteger` as the input type.**
A 64-bit bound gives the UI something honest to say ("up to 19 digits"), keeps
the parser trivially safe, and matches what a phone keyboard can type. Arbitrary
precision would need progress reporting and cancellation everywhere.

**Why the factor search is budgeted but primality is not.**
Primality is fast for any input in range, so it always completes. Finding the
smallest factor of a semiprime near 2⁶³ is genuinely expensive, so the budget is
explicit and the result says when it stopped. A wrong "not prime, no factor
found" would be worse than an honest limit.

**Why cancellation is a lambda, not a coroutine dependency.**
`shouldContinue: () -> Boolean` keeps `:core` free of coroutine types while still
letting the ViewModel stop a long search. In the ViewModel it is wired to
`isActive`, so cancelling the job stops the loop within one candidate.

**Why the ViewModel drops stale results.**
Two taps in quick succession cancel the first job. `ensureActive()` before
publishing means a cancelled computation cannot overwrite newer state — the
classic race in "async check on tap" screens.

**Why errors are a sealed type, not an exception or a string.**
`ParseResult` and `CheckResult` make every outcome explicit at compile time, keep
user-facing wording in `strings.xml` (so it is translatable), and let tests assert
on structure rather than on prose.

## Threading

- `viewModelScope` runs on `Dispatchers.Main.immediate` for state updates.
- The computation itself runs on `Dispatchers.Default` via `withContext`.
- Nothing blocks the main thread, so no ANR is possible from the algorithm; the
  factor search additionally yields to cancellation between candidates.

## Extending it

| Idea | Where it goes |
| --- | --- |
| Prime factorisation of the whole number | `:core` `Factorizer`, reusing `FactorSearch` |
| Check history | `DataStore` in `:app`, a repository interface in `:core`'s consumers |
| Batch / CSV import | New use case in `:core`, new screen in `:app` |
| Arbitrary precision | Change `PrimeAnalysis.number` to `BigInteger`; parser and UI follow |

Anything that adds storage, network or a new permission changes the security
model in `SECURITY.md` — update it in the same pull request.
