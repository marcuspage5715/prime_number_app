package io.github.marcuspage5715.primechecker.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.marcuspage5715.primechecker.core.ParseResult
import io.github.marcuspage5715.primechecker.core.PrimeChecker
import io.github.marcuspage5715.primechecker.ui.theme.PrimeCheckerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

/**
 * Renders the real screen to PNG files on the JVM — no emulator, no device.
 *
 * Two jobs:
 *  - produce the Play Store screenshots (1080 x 1920, a 9:16 phone canvas), and
 *  - catch layout regressions, because a diff in the rendered pixels fails the
 *    build in `verifyRoborazziDebug`.
 *
 * Numbers go through the real [PrimeChecker], so what is pictured is what the
 * app actually prints, and the elapsed time shown is genuinely measured.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-xxhdpi")
@LooperMode(LooperMode.Mode.PAUSED)
class PrimeCheckerScreenshotTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val checker = PrimeChecker()

    @Test
    fun emptyState() = render("01-empty", PrimeUiState())

    @Test
    fun primeNumber() = render("02-prime", PrimeUiState(query = "97", result = analyzed(97L)))

    @Test
    fun compositeNumber() = render("03-composite", PrimeUiState(query = "91", result = analyzed(91L)))

    @Test
    fun invalidInput() = render(
        "04-invalid",
        PrimeUiState(
            query = "twelve",
            result = CheckResult.Invalid(ParseResult.Reason.NOT_A_NUMBER),
        ),
    )

    @Test
    fun darkThemeLargePrime() = render(
        "05-dark-prime",
        PrimeUiState(query = "2305843009213693951", result = analyzed(2_305_843_009_213_693_951L)),
        darkTheme = true,
    )

    private fun analyzed(number: Long): CheckResult.Analyzed {
        val startedAt = System.nanoTime()
        val analysis = checker.analyze(number)
        val elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L
        return CheckResult.Analyzed(analysis, elapsedMillis)
    }

    private fun render(name: String, state: PrimeUiState, darkTheme: Boolean = false) {
        composeTestRule.setContent {
            // dynamicColor is off so screenshots are the same on every machine
            // instead of following a wallpaper.
            PrimeCheckerTheme(darkTheme = darkTheme, dynamicColor = false) {
                PrimeCheckerScreen(
                    state = state,
                    onQueryChange = {},
                    onCheck = {},
                    onClear = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage("build/screenshots/$name.png")
    }
}
