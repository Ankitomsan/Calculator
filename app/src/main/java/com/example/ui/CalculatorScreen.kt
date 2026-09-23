package com.example.ui

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.CalculatorUiEvent
import com.example.calculator.CalculatorViewModel
import com.example.ui.components.ButtonType
import com.example.ui.components.CalculatorButton
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.HistoryBottomSheet

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CalculatorUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            if (isLandscape) {
                LandscapeCalculatorLayout(
                    uiState = uiState,
                    viewModel = viewModel,
                    onCopyResult = { text -> viewModel.onCopyResult(context, text) }
                )
            } else {
                PortraitCalculatorLayout(
                    uiState = uiState,
                    viewModel = viewModel,
                    onCopyResult = { text -> viewModel.onCopyResult(context, text) }
                )
            }

            if (uiState.isHistoryOpen) {
                HistoryBottomSheet(
                    historyList = uiState.historyList,
                    onDismiss = { viewModel.onToggleHistory(false) },
                    onSelectRecord = { viewModel.onSelectHistoryItem(it) },
                    onReuseExpression = { viewModel.onReuseExpression(it) },
                    onDeleteRecord = { viewModel.onDeleteHistoryItem(it) },
                    onClearAll = { viewModel.onClearHistory() }
                )
            }
        }
    }
}

@Composable
private fun PortraitCalculatorLayout(
    uiState: com.example.calculator.CalculatorUiState,
    viewModel: CalculatorViewModel,
    onCopyResult: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display area
        CalculatorDisplay(
            uiState = uiState,
            onToggleAngleMode = { viewModel.onToggleAngleMode() },
            onToggleScientific = { viewModel.onToggleScientific() },
            onOpenHistory = { viewModel.onToggleHistory(true) },
            onCopyResult = onCopyResult,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Keypad Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Expandable Scientific Keypad
            AnimatedVisibility(
                visible = uiState.isScientificExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalculatorButton("sin", { viewModel.onFunction("sin") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_sin", 18.sp)
                        CalculatorButton("cos", { viewModel.onFunction("cos") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_cos", 18.sp)
                        CalculatorButton("tan", { viewModel.onFunction("tan") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_tan", 18.sp)
                        CalculatorButton("ln", { viewModel.onFunction("ln") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_ln", 18.sp)
                        CalculatorButton("log", { viewModel.onFunction("log") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_log", 18.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalculatorButton("√", { viewModel.onFunction("√") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_sqrt", 20.sp)
                        CalculatorButton("^", { viewModel.onOperator("^") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_power", 20.sp)
                        CalculatorButton("π", { viewModel.onConstant("π") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_pi", 20.sp)
                        CalculatorButton("e", { viewModel.onConstant("e") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_e", 20.sp)
                        CalculatorButton("!", { viewModel.onFunction("!") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_fact", 20.sp)
                    }
                }
            }

            // Standard Keypad Rows
            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val clearText = if (uiState.expression.isNotEmpty()) "C" else "AC"
                CalculatorButton(clearText, { viewModel.onClear() }, Modifier.weight(1f), ButtonType.FUNCTION, "btn_clear", 22.sp)
                CalculatorButton("⌫", { viewModel.onBackspace() }, Modifier.weight(1f), ButtonType.FUNCTION, "btn_backspace", 22.sp)
                CalculatorButton("%", { viewModel.onPercentage() }, Modifier.weight(1f), ButtonType.FUNCTION, "btn_percent", 22.sp)
                CalculatorButton("÷", { viewModel.onOperator("÷") }, Modifier.weight(1f), ButtonType.OPERATOR, "btn_divide", 26.sp)
            }

            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalculatorButton("7", { viewModel.onDigit("7") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_7", 24.sp)
                CalculatorButton("8", { viewModel.onDigit("8") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_8", 24.sp)
                CalculatorButton("9", { viewModel.onDigit("9") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_9", 24.sp)
                CalculatorButton("×", { viewModel.onOperator("×") }, Modifier.weight(1f), ButtonType.OPERATOR, "btn_multiply", 26.sp)
            }

            // Row 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalculatorButton("4", { viewModel.onDigit("4") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_4", 24.sp)
                CalculatorButton("5", { viewModel.onDigit("5") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_5", 24.sp)
                CalculatorButton("6", { viewModel.onDigit("6") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_6", 24.sp)
                CalculatorButton("−", { viewModel.onOperator("−") }, Modifier.weight(1f), ButtonType.OPERATOR, "btn_subtract", 26.sp)
            }

            // Row 4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalculatorButton("1", { viewModel.onDigit("1") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_1", 24.sp)
                CalculatorButton("2", { viewModel.onDigit("2") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_2", 24.sp)
                CalculatorButton("3", { viewModel.onDigit("3") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_3", 24.sp)
                CalculatorButton("+", { viewModel.onOperator("+") }, Modifier.weight(1f), ButtonType.OPERATOR, "btn_add", 26.sp)
            }

            // Row 5
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalculatorButton("( )", { viewModel.onParenthesis() }, Modifier.weight(1f), ButtonType.FUNCTION, "btn_parenthesis", 20.sp)
                CalculatorButton("0", { viewModel.onDigit("0") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_0", 24.sp)
                CalculatorButton(".", { viewModel.onDecimal() }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_dot", 26.sp)
                CalculatorButton("=", { viewModel.onCalculate() }, Modifier.weight(1f), ButtonType.EQUALS, "btn_equals", 28.sp)
            }
        }
    }
}

@Composable
private fun LandscapeCalculatorLayout(
    uiState: com.example.calculator.CalculatorUiState,
    viewModel: CalculatorViewModel,
    onCopyResult: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Display and quick tools
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CalculatorDisplay(
                uiState = uiState,
                onToggleAngleMode = { viewModel.onToggleAngleMode() },
                onToggleScientific = { viewModel.onToggleScientific() },
                onOpenHistory = { viewModel.onToggleHistory(true) },
                onCopyResult = onCopyResult,
                modifier = Modifier.weight(1f)
            )

            // Scientific keys grid in landscape
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculatorButton("sin", { viewModel.onFunction("sin") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_land_sin", 16.sp)
                    CalculatorButton("cos", { viewModel.onFunction("cos") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_land_cos", 16.sp)
                    CalculatorButton("tan", { viewModel.onFunction("tan") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_land_tan", 16.sp)
                    CalculatorButton("ln", { viewModel.onFunction("ln") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_land_ln", 16.sp)
                    CalculatorButton("log", { viewModel.onFunction("log") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_land_log", 16.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculatorButton("√", { viewModel.onFunction("√") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_land_sqrt", 18.sp)
                    CalculatorButton("^", { viewModel.onOperator("^") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_land_power", 18.sp)
                    CalculatorButton("π", { viewModel.onConstant("π") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_land_pi", 18.sp)
                    CalculatorButton("e", { viewModel.onConstant("e") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_land_e", 18.sp)
                    CalculatorButton("!", { viewModel.onFunction("!") }, Modifier.weight(1f), ButtonType.SCIENTIFIC, "btn_land_fact", 18.sp)
                }
            }
        }

        // Right Column: Numeric & operator keypad
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val clearText = if (uiState.expression.isNotEmpty()) "C" else "AC"
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(clearText, { viewModel.onClear() }, Modifier.weight(1f), ButtonType.FUNCTION, "btn_land_clear", 18.sp)
                CalculatorButton("⌫", { viewModel.onBackspace() }, Modifier.weight(1f), ButtonType.FUNCTION, "btn_land_backspace", 18.sp)
                CalculatorButton("%", { viewModel.onPercentage() }, Modifier.weight(1f), ButtonType.FUNCTION, "btn_land_percent", 18.sp)
                CalculatorButton("÷", { viewModel.onOperator("÷") }, Modifier.weight(1f), ButtonType.OPERATOR, "btn_land_divide", 20.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton("7", { viewModel.onDigit("7") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_land_7", 18.sp)
                CalculatorButton("8", { viewModel.onDigit("8") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_land_8", 18.sp)
                CalculatorButton("9", { viewModel.onDigit("9") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_land_9", 18.sp)
                CalculatorButton("×", { viewModel.onOperator("×") }, Modifier.weight(1f), ButtonType.OPERATOR, "btn_land_multiply", 20.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton("4", { viewModel.onDigit("4") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_land_4", 18.sp)
                CalculatorButton("5", { viewModel.onDigit("5") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_land_5", 18.sp)
                CalculatorButton("6", { viewModel.onDigit("6") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_land_6", 18.sp)
                CalculatorButton("−", { viewModel.onOperator("−") }, Modifier.weight(1f), ButtonType.OPERATOR, "btn_land_subtract", 20.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton("1", { viewModel.onDigit("1") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_land_1", 18.sp)
                CalculatorButton("2", { viewModel.onDigit("2") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_land_2", 18.sp)
                CalculatorButton("3", { viewModel.onDigit("3") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_land_3", 18.sp)
                CalculatorButton("+", { viewModel.onOperator("+") }, Modifier.weight(1f), ButtonType.OPERATOR, "btn_land_add", 20.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton("( )", { viewModel.onParenthesis() }, Modifier.weight(1f), ButtonType.FUNCTION, "btn_land_paren", 16.sp)
                CalculatorButton("0", { viewModel.onDigit("0") }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_land_0", 18.sp)
                CalculatorButton(".", { viewModel.onDecimal() }, Modifier.weight(1f), ButtonType.NUMERIC, "btn_land_dot", 20.sp)
                CalculatorButton("=", { viewModel.onCalculate() }, Modifier.weight(1f), ButtonType.EQUALS, "btn_land_equals", 22.sp)
            }
        }
    }
}
