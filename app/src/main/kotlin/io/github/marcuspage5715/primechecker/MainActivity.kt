package io.github.marcuspage5715.primechecker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.marcuspage5715.primechecker.ui.PrimeCheckerScreen
import io.github.marcuspage5715.primechecker.ui.PrimeCheckerViewModel
import io.github.marcuspage5715.primechecker.ui.theme.PrimeCheckerTheme

/**
 * The app's single activity.
 *
 * It holds no logic of its own: state lives in [PrimeCheckerViewModel], which
 * survives configuration changes (rotation, theme switch, keyboard) so a
 * half-typed number is never lost. The activity only wires the state stream to
 * the composable tree.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw behind the system bars and let Scaffold apply the insets. This is
        // what Android 15 expects by default for apps targeting SDK 35.
        enableEdgeToEdge()

        setContent {
            PrimeCheckerTheme {
                val viewModel: PrimeCheckerViewModel = viewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                PrimeCheckerScreen(
                    state = state,
                    onQueryChange = viewModel::onQueryChange,
                    onCheck = viewModel::onCheck,
                    onClear = viewModel::onClear,
                )
            }
        }
    }
}
