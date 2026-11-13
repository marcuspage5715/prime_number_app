package io.github.marcuspage5715.primechecker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.marcuspage5715.primechecker.R
import io.github.marcuspage5715.primechecker.core.ParseResult
import io.github.marcuspage5715.primechecker.core.PrimeAnalysis
import io.github.marcuspage5715.primechecker.core.Verdict
import io.github.marcuspage5715.primechecker.ui.theme.PrimeCheckerTheme
import java.text.NumberFormat

/** Test tags, so instrumented tests never depend on user-visible wording. */
object PrimeCheckerTags {
    const val INPUT = "prime_input"
    const val CHECK = "prime_check_button"
    const val CLEAR = "prime_clear_button"
    const val RESULT = "prime_result_card"
}

/**
 * Stateless screen: everything it draws comes from [state], and every
 * interaction leaves through a callback. That makes it previewable and testable
 * without a ViewModel or an emulator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimeCheckerScreen(
    state: PrimeUiState,
    onQueryChange: (String) -> Unit,
    onCheck: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val submit = {
        keyboardController?.hide()
        onCheck()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.screen_intro),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.semantics { heading() },
            )

            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(PrimeCheckerTags.INPUT),
                label = { Text(text = stringResource(R.string.input_label)) },
                singleLine = true,
                isError = state.result is CheckResult.Empty ||
                    state.result is CheckResult.Invalid,
                supportingText = { Text(text = stringResource(R.string.input_helper)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = submit,
                    enabled = state.canCheck,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(PrimeCheckerTags.CHECK),
                ) {
                    Text(text = stringResource(R.string.action_check))
                }
                OutlinedButton(
                    onClick = {
                        keyboardController?.hide()
                        onClear()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag(PrimeCheckerTags.CLEAR),
                ) {
                    Text(text = stringResource(R.string.action_clear))
                }
            }

            ResultCard(result = state.result, isChecking = state.isChecking)

            HorizontalDivider()

            Text(
                text = stringResource(R.string.offline_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ResultCard(
    result: CheckResult,
    isChecking: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(PrimeCheckerTags.RESULT),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                isChecking -> Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                    Text(text = stringResource(R.string.state_checking))
                }

                result is CheckResult.Analyzed -> AnalyzedContent(result)

                result is CheckResult.Empty -> Text(
                    text = stringResource(R.string.error_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )

                result is CheckResult.Invalid -> Text(
                    text = invalidMessage(result.reason),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )

                else -> Text(
                    text = stringResource(R.string.result_idle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AnalyzedContent(result: CheckResult.Analyzed) {
    val analysis = result.analysis
    val number = remember(analysis.number) { formatNumber(analysis.number) }

    val headline = verdictHeadline(analysis, number)

    Text(
        text = headline,
        // A 19-digit number with grouping is ~35 characters, which overflows a
        // 360dp screen at headline size and wraps mid-group ("...693,95" /
        // "1 is prime"). Step the style down with length instead, so the number
        // always stays readable as one unit.
        style = headlineStyle(headline.length),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Start,
    )

    verdictDetail(analysis)?.let { detail ->
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }


    Text(
        text = stringResource(R.string.elapsed_ms, result.elapsedMillis),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun headlineStyle(length: Int) = when {
    length <= 18 -> MaterialTheme.typography.headlineSmall
    length <= 30 -> MaterialTheme.typography.titleLarge
    else -> MaterialTheme.typography.titleMedium
}

@Composable
private fun verdictHeadline(analysis: PrimeAnalysis, number: String): String = when (analysis.verdict) {
    Verdict.PRIME -> stringResource(R.string.result_prime, number)
    Verdict.COMPOSITE_WITH_FACTOR,
    Verdict.COMPOSITE_NO_SMALL_FACTOR,
    Verdict.BELOW_TWO,
    -> stringResource(R.string.result_not_prime, number)
}

@Composable
private fun verdictDetail(analysis: PrimeAnalysis): String? = when (analysis.verdict) {
    Verdict.PRIME -> null
    Verdict.BELOW_TWO -> stringResource(R.string.detail_below_two)
    Verdict.COMPOSITE_WITH_FACTOR -> {
        val factor = analysis.smallestFactor
        if (factor == null) {
            null
        } else {
            stringResource(R.string.detail_factor, formatNumber(factor))
        }
    }
    Verdict.COMPOSITE_NO_SMALL_FACTOR ->
        stringResource(R.string.detail_no_factor, formatNumber(analysis.factorSearchLimit))
}

@Composable
private fun invalidMessage(reason: ParseResult.Reason): String = when (reason) {
    ParseResult.Reason.NOT_A_NUMBER -> stringResource(R.string.error_not_a_number)
    ParseResult.Reason.TOO_LONG -> stringResource(R.string.error_too_long)
    ParseResult.Reason.OUT_OF_RANGE -> stringResource(R.string.error_out_of_range)
}

/** Locale-aware grouping (e.g. 1,234,567) without ever changing the value. */
private fun formatNumber(value: Long): String =
    NumberFormat.getIntegerInstance().format(value)

@Preview(name = "Empty", showBackground = true)
@Composable
private fun PreviewEmpty() {
    PrimeCheckerTheme {
        PrimeCheckerScreen(
            state = PrimeUiState(),
            onQueryChange = {},
            onCheck = {},
            onClear = {},
        )
    }
}

@Preview(name = "Prime result", showBackground = true)
@Composable
private fun PreviewPrime() {
    PrimeCheckerTheme {
        PrimeCheckerScreen(
            state = PrimeUiState(
                query = "97",
                result = CheckResult.Analyzed(
                    analysis = PrimeAnalysis(
                        number = 97L,
                        isPrime = true,
                        smallestFactor = null,
                        factorSearchLimit = 1_000_000L,
                    ),
                    elapsedMillis = 1L,
                ),
            ),
            onQueryChange = {},
            onCheck = {},
            onClear = {},
        )
    }
}
