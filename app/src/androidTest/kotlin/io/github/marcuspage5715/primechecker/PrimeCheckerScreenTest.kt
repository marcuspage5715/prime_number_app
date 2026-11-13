package io.github.marcuspage5715.primechecker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.marcuspage5715.primechecker.core.ParseResult
import io.github.marcuspage5715.primechecker.core.PrimeAnalysis
import io.github.marcuspage5715.primechecker.ui.CheckResult
import io.github.marcuspage5715.primechecker.ui.PrimeCheckerScreen
import io.github.marcuspage5715.primechecker.ui.PrimeCheckerTags
import io.github.marcuspage5715.primechecker.ui.PrimeUiState
import io.github.marcuspage5715.primechecker.ui.theme.PrimeCheckerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests the screen in isolation, with state supplied by the test itself. */
@RunWith(AndroidJUnit4::class)
class PrimeCheckerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun typing_a_number_and_checking_shows_the_verdict() {
        composeTestRule.setContent {
            var state by remember { mutableStateOf(PrimeUiState()) }
            PrimeCheckerTheme {
                PrimeCheckerScreen(
                    state = state,
                    onQueryChange = { typed -> state = state.copy(query = typed) },
                    onCheck = {
                        state = state.copy(
                            isChecking = false,
                            result = CheckResult.Analyzed(
                                analysis = PrimeAnalysis(
                                    number = 97L,
                                    isPrime = true,
                                    smallestFactor = null,
                                    factorSearchLimit = 1_000_000L,
                                ),
                                elapsedMillis = 1L,
                            ),
                        )
                    },
                    onClear = { state = PrimeUiState() },
                )
            }
        }

        composeTestRule.onNodeWithTag(PrimeCheckerTags.INPUT).performTextInput("97")
        composeTestRule.onNodeWithTag(PrimeCheckerTags.CHECK).performClick()

        composeTestRule.onNodeWithText("97 is prime").assertIsDisplayed()
    }

    @Test
    fun invalid_input_shows_an_explanation() {
        composeTestRule.setContent {
            PrimeCheckerTheme {
                PrimeCheckerScreen(
                    state = PrimeUiState(
                        query = "abc",
                        result = CheckResult.Invalid(ParseResult.Reason.NOT_A_NUMBER),
                    ),
                    onQueryChange = {},
                    onCheck = {},
                    onClear = {},
                )
            }
        }

        composeTestRule
            .onNodeWithText("Use digits only, for example 97 or 1234.")
            .assertIsDisplayed()
    }

    @Test
    fun clear_button_is_reachable_and_labelled() {
        composeTestRule.setContent {
            PrimeCheckerTheme {
                PrimeCheckerScreen(
                    state = PrimeUiState(query = "97"),
                    onQueryChange = {},
                    onCheck = {},
                    onClear = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(PrimeCheckerTags.CLEAR).assertIsDisplayed()
    }
}
