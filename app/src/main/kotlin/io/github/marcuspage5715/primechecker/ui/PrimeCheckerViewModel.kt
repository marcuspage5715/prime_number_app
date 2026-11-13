package io.github.marcuspage5715.primechecker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.marcuspage5715.primechecker.core.NumberParser
import io.github.marcuspage5715.primechecker.core.ParseResult
import io.github.marcuspage5715.primechecker.core.PrimeAnalysis
import io.github.marcuspage5715.primechecker.core.PrimeChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Immutable snapshot of everything the screen draws. */
data class PrimeUiState(
    val query: String = "",
    val isChecking: Boolean = false,
    val result: CheckResult = CheckResult.Idle,
) {
    /**
     * Only blocked while a check is running: tapping Check on an empty field is
     * allowed so the app can explain what is wrong, which a greyed-out button
     * cannot do.
     */
    val canCheck: Boolean get() = !isChecking
}

/** The outcome of the last check, or the reason there wasn't one. */
sealed interface CheckResult {
    data object Idle : CheckResult
    data object Empty : CheckResult
    data class Invalid(val reason: ParseResult.Reason) : CheckResult
    data class Analyzed(val analysis: PrimeAnalysis, val elapsedMillis: Long) : CheckResult
}

/**
 * Holds screen state and runs the check off the main thread.
 *
 * The all-default constructor is intentional: it lets `viewModel()` create this
 * class with no factory, while tests can inject a [PrimeChecker] configured with
 * a small factor-search budget.
 */
class PrimeCheckerViewModel(
    private val checker: PrimeChecker = PrimeChecker(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrimeUiState())
    val uiState: StateFlow<PrimeUiState> = _uiState.asStateFlow()

    private var checkJob: Job? = null

    /**
     * Called on every keystroke. The text is bounded here as well as in the
     * parser so a paste of enormous length never reaches the state object.
     */
    fun onQueryChange(newQuery: String) {
        val bounded = if (newQuery.length > NumberParser.MAX_INPUT_LENGTH) {
            newQuery.take(NumberParser.MAX_INPUT_LENGTH)
        } else {
            newQuery
        }
        _uiState.update { state ->
            if (state.query == bounded) {
                state
            } else {
                // Any edit invalidates the previous verdict.
                state.copy(query = bounded, result = CheckResult.Idle)
            }
        }
    }

    fun onCheck() {
        // A second tap while a check runs replaces the first one rather than
        // queueing behind it.
        checkJob?.cancel()

        val parsed = NumberParser.parse(_uiState.value.query)
        if (parsed !is ParseResult.Success) {
            _uiState.update { state ->
                state.copy(
                    isChecking = false,
                    result = when (parsed) {
                        ParseResult.Blank -> CheckResult.Empty
                        is ParseResult.Invalid -> CheckResult.Invalid(parsed.reason)
                        is ParseResult.Success -> CheckResult.Idle
                    },
                )
            }
            return
        }

        _uiState.update { it.copy(isChecking = true, result = CheckResult.Idle) }

        checkJob = viewModelScope.launch {
            val startedAt = System.nanoTime()
            val analysis = withContext(Dispatchers.Default) {
                // isActive is read from the coroutine scope, so cancelling the
                // job also stops the factor search mid-loop.
                checker.analyze(parsed.number) { isActive }
            }
            // If the job was cancelled while computing, drop the stale result
            // instead of publishing it over newer state.
            ensureActive()
            val elapsedMillis = (System.nanoTime() - startedAt) / NANOS_PER_MILLI
            _uiState.update { state ->
                state.copy(
                    isChecking = false,
                    result = CheckResult.Analyzed(analysis, elapsedMillis),
                )
            }
        }
    }

    fun onClear() {
        checkJob?.cancel()
        checkJob = null
        _uiState.value = PrimeUiState()
    }

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
    }
}
