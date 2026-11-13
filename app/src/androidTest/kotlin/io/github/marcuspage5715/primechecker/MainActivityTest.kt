package io.github.marcuspage5715.primechecker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.marcuspage5715.primechecker.ui.PrimeCheckerTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end test against the real activity, ViewModel and core: type a number,
 * tap Check, read the answer off the screen.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun checking_a_prime_end_to_end() {
        composeTestRule.onNodeWithTag(PrimeCheckerTags.INPUT).performTextInput("97")
        composeTestRule.onNodeWithTag(PrimeCheckerTags.CHECK).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodesWithText("97 is prime").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("97 is prime").assertIsDisplayed()
    }

    @Test
    fun checking_a_composite_end_to_end() {
        composeTestRule.onNodeWithTag(PrimeCheckerTags.INPUT).performTextInput("91")
        composeTestRule.onNodeWithTag(PrimeCheckerTags.CHECK).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodesWithText("91 is not prime").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Divisible by 7").assertIsDisplayed()
    }
}
