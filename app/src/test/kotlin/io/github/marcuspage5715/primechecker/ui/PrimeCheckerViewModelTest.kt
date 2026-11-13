package io.github.marcuspage5715.primechecker.ui

import io.github.marcuspage5715.primechecker.core.ParseResult
import io.github.marcuspage5715.primechecker.core.PrimeChecker
import io.github.marcuspage5715.primechecker.core.Verdict
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrimeCheckerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(limit: Long = 1_000_000L) =
        PrimeCheckerViewModel(PrimeChecker(factorSearchLimit = limit))

    @Test
    fun `checks a prime and publishes the verdict`() = runTest {
        val viewModel = viewModel()
        viewModel.onQueryChange("97")
        viewModel.onCheck()

        val state = viewModel.uiState.first { it.result is CheckResult.Analyzed }
        val result = state.result as CheckResult.Analyzed

        assertEquals(97L, result.analysis.number)
        assertEquals(Verdict.PRIME, result.analysis.verdict)
        assertFalse(state.isChecking)
        assertTrue(result.elapsedMillis >= 0L)
    }

    @Test
    fun `checks a composite and names its factor`() = runTest {
        val viewModel = viewModel()
        viewModel.onQueryChange("91")
        viewModel.onCheck()

        val state = viewModel.uiState.first { it.result is CheckResult.Analyzed }
        val result = state.result as CheckResult.Analyzed

        assertEquals(Verdict.COMPOSITE_WITH_FACTOR, result.analysis.verdict)
        assertEquals(7L, result.analysis.smallestFactor)
    }

    @Test
    fun `empty input is reported without running a check`() = runTest {
        val viewModel = viewModel()
        viewModel.onCheck()

        assertEquals(CheckResult.Empty, viewModel.uiState.value.result)
        assertFalse(viewModel.uiState.value.isChecking)
    }

    @Test
    fun `non numeric input is reported as invalid`() = runTest {
        val viewModel = viewModel()
        viewModel.onQueryChange("twelve")
        viewModel.onCheck()

        assertEquals(
            CheckResult.Invalid(ParseResult.Reason.NOT_A_NUMBER),
            viewModel.uiState.value.result,
        )
    }

    @Test
    fun `editing the number clears the previous result`() = runTest {
        val viewModel = viewModel()
        viewModel.onQueryChange("97")
        viewModel.onCheck()
        viewModel.uiState.first { it.result is CheckResult.Analyzed }

        viewModel.onQueryChange("9")
        assertEquals(CheckResult.Idle, viewModel.uiState.value.result)
    }

    @Test
    fun `input longer than the parser allows is truncated at the edge`() = runTest {
        val viewModel = viewModel()
        viewModel.onQueryChange("1".repeat(500))

        assertEquals(io.github.marcuspage5715.primechecker.core.NumberParser.MAX_INPUT_LENGTH, viewModel.uiState.value.query.length)
    }

    @Test
    fun `clear resets the screen`() = runTest {
        val viewModel = viewModel()
        viewModel.onQueryChange("97")
        viewModel.onCheck()
        viewModel.uiState.first { it.result is CheckResult.Analyzed }

        viewModel.onClear()

        assertEquals(PrimeUiState(), viewModel.uiState.value)
    }
}
