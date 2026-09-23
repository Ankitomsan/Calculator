package com.example.calculator

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationRecord
import com.example.data.CalculationRepository
import com.example.data.CalculatorDatabase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val liveResult: String = "",
    val finalResult: String? = null,
    val errorMessage: String? = null,
    val isRad: Boolean = false,
    val isScientificExpanded: Boolean = false,
    val isHistoryOpen: Boolean = false,
    val historyList: List<CalculationRecord> = emptyList(),
    val isResultEvaluated: Boolean = false
)

sealed class CalculatorUiEvent {
    data class ShowToast(val message: String) : CalculatorUiEvent()
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalculationRepository

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CalculatorUiEvent>()
    val events: SharedFlow<CalculatorUiEvent> = _events.asSharedFlow()

    init {
        val database = CalculatorDatabase.getDatabase(application)
        repository = CalculationRepository(database.calculationDao())

        viewModelScope.launch {
            repository.allHistory.collect { history ->
                _uiState.update { it.copy(historyList = history) }
            }
        }
    }

    fun onDigit(digit: String) {
        _uiState.update { state ->
            val newExpr = if (state.isResultEvaluated) {
                digit
            } else {
                state.expression + digit
            }
            state.copy(
                expression = newExpr,
                finalResult = null,
                errorMessage = null,
                isResultEvaluated = false,
                liveResult = computeLivePreview(newExpr, state.isRad)
            )
        }
    }

    fun onOperator(op: String) {
        _uiState.update { state ->
            val baseExpr = if (state.isResultEvaluated && state.finalResult != null) {
                state.finalResult.replace(",", "")
            } else {
                state.expression
            }

            if (baseExpr.isEmpty()) {
                if (op == "−") {
                    return@update state.copy(
                        expression = "−",
                        finalResult = null,
                        errorMessage = null,
                        isResultEvaluated = false,
                        liveResult = ""
                    )
                }
                return@update state
            }

            val lastChar = baseExpr.last()
            val operators = listOf('+', '−', '×', '÷', '^')

            val newExpr = if (lastChar in operators) {
                baseExpr.dropLast(1) + op
            } else {
                baseExpr + op
            }

            state.copy(
                expression = newExpr,
                finalResult = null,
                errorMessage = null,
                isResultEvaluated = false,
                liveResult = computeLivePreview(newExpr, state.isRad)
            )
        }
    }

    fun onDecimal() {
        _uiState.update { state ->
            val currentExpr = if (state.isResultEvaluated) "0" else state.expression
            if (currentExpr.isEmpty()) {
                val newExpr = "0."
                return@update state.copy(
                    expression = newExpr,
                    finalResult = null,
                    errorMessage = null,
                    isResultEvaluated = false,
                    liveResult = computeLivePreview(newExpr, state.isRad)
                )
            }

            // Find current number token
            val lastToken = currentExpr.takeLastWhile { it !in "+−×÷^() " }
            if (lastToken.contains('.')) {
                return@update state // Already has decimal point
            }

            val newExpr = if (lastToken.isEmpty() || currentExpr.last() in "+−×÷^(") {
                currentExpr + "0."
            } else {
                currentExpr + "."
            }

            state.copy(
                expression = newExpr,
                finalResult = null,
                errorMessage = null,
                isResultEvaluated = false,
                liveResult = computeLivePreview(newExpr, state.isRad)
            )
        }
    }

    fun onParenthesis() {
        _uiState.update { state ->
            val expr = if (state.isResultEvaluated) "" else state.expression
            val openCount = expr.count { it == '(' }
            val closeCount = expr.count { it == ')' }

            val nextParen = if (expr.isEmpty()) {
                "("
            } else {
                val last = expr.last()
                if (openCount > closeCount && (last.isDigit() || last == ')' || last == 'π' || last == 'e' || last == '%')) {
                    ")"
                } else if (last.isDigit() || last == ')' || last == 'π' || last == 'e') {
                    "×("
                } else {
                    "("
                }
            }

            val newExpr = expr + nextParen
            state.copy(
                expression = newExpr,
                finalResult = null,
                errorMessage = null,
                isResultEvaluated = false,
                liveResult = computeLivePreview(newExpr, state.isRad)
            )
        }
    }

    fun onPercentage() {
        _uiState.update { state ->
            if (state.expression.isEmpty()) return@update state
            val last = state.expression.last()
            if (!last.isDigit() && last != ')' && last != 'π' && last != 'e') return@update state

            val newExpr = state.expression + "%"
            state.copy(
                expression = newExpr,
                finalResult = null,
                errorMessage = null,
                isResultEvaluated = false,
                liveResult = computeLivePreview(newExpr, state.isRad)
            )
        }
    }

    fun onToggleSign() {
        _uiState.update { state ->
            val expr = if (state.isResultEvaluated && state.finalResult != null) {
                state.finalResult.replace(",", "")
            } else {
                state.expression
            }

            if (expr.isEmpty()) return@update state

            // Check if entire expression is a single signed number
            if (expr.startsWith("−") && expr.drop(1).all { it.isDigit() || it == '.' }) {
                val newExpr = expr.drop(1)
                return@update state.copy(
                    expression = newExpr,
                    isResultEvaluated = false,
                    liveResult = computeLivePreview(newExpr, state.isRad)
                )
            } else if (expr.all { it.isDigit() || it == '.' }) {
                val newExpr = "−$expr"
                return@update state.copy(
                    expression = newExpr,
                    isResultEvaluated = false,
                    liveResult = computeLivePreview(newExpr, state.isRad)
                )
            }

            // Wrap or unwrap last number token
            val index = expr.lastIndexOfAny(charArrayOf('+', '−', '×', '÷', '(', '^'))
            if (index == -1) {
                val newExpr = "−$expr"
                return@update state.copy(
                    expression = newExpr,
                    isResultEvaluated = false,
                    liveResult = computeLivePreview(newExpr, state.isRad)
                )
            }

            val newExpr = expr.substring(0, index + 1) + "−" + expr.substring(index + 1)
            state.copy(
                expression = newExpr,
                isResultEvaluated = false,
                liveResult = computeLivePreview(newExpr, state.isRad)
            )
        }
    }

    fun onFunction(fn: String) {
        _uiState.update { state ->
            val baseExpr = if (state.isResultEvaluated) "" else state.expression
            val formattedFn = when (fn) {
                "sqrt", "√" -> "√("
                "sin" -> "sin("
                "cos" -> "cos("
                "tan" -> "tan("
                "log" -> "log("
                "ln" -> "ln("
                "!" -> "!"
                else -> "$fn("
            }

            val prefix = if (baseExpr.isNotEmpty()) {
                val last = baseExpr.last()
                if (last.isDigit() || last == ')' || last == 'π' || last == 'e') {
                    if (fn == "!") "" else "×"
                } else ""
            } else ""

            val newExpr = baseExpr + prefix + formattedFn
            state.copy(
                expression = newExpr,
                finalResult = null,
                errorMessage = null,
                isResultEvaluated = false,
                liveResult = computeLivePreview(newExpr, state.isRad)
            )
        }
    }

    fun onConstant(constant: String) {
        _uiState.update { state ->
            val baseExpr = if (state.isResultEvaluated) "" else state.expression
            val prefix = if (baseExpr.isNotEmpty()) {
                val last = baseExpr.last()
                if (last.isDigit() || last == ')' || last == 'π' || last == 'e') "×" else ""
            } else ""

            val newExpr = baseExpr + prefix + constant
            state.copy(
                expression = newExpr,
                finalResult = null,
                errorMessage = null,
                isResultEvaluated = false,
                liveResult = computeLivePreview(newExpr, state.isRad)
            )
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            if (state.isResultEvaluated) {
                return@update state.copy(isResultEvaluated = false, finalResult = null)
            }
            if (state.expression.isEmpty()) return@update state

            val expr = state.expression
            // Check for multi-character functions at end
            val multiChars = listOf("sin(", "cos(", "tan(", "log(", "ln(", "√(")
            var charsToRemove = 1
            for (fn in multiChars) {
                if (expr.endsWith(fn)) {
                    charsToRemove = fn.length
                    break
                }
            }

            val newExpr = expr.dropLast(charsToRemove)
            state.copy(
                expression = newExpr,
                errorMessage = null,
                finalResult = null,
                liveResult = computeLivePreview(newExpr, state.isRad)
            )
        }
    }

    fun onClear() {
        _uiState.update { state ->
            state.copy(
                expression = "",
                liveResult = "",
                finalResult = null,
                errorMessage = null,
                isResultEvaluated = false
            )
        }
    }

    fun onCalculate() {
        val state = _uiState.value
        if (state.expression.isBlank()) return

        when (val eval = CalculatorEngine.evaluate(state.expression, state.isRad, isPreview = false)) {
            is CalculatorEngine.EvalResult.Success -> {
                val formatted = eval.formatted
                _uiState.update {
                    it.copy(
                        finalResult = formatted,
                        errorMessage = null,
                        isResultEvaluated = true,
                        liveResult = ""
                    )
                }

                // Save to Room history
                viewModelScope.launch {
                    repository.saveCalculation(state.expression, formatted)
                }
            }
            is CalculatorEngine.EvalResult.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage = eval.message.ifEmpty { "Error" },
                        finalResult = null,
                        isResultEvaluated = true
                    )
                }
            }
        }
    }

    fun onToggleAngleMode() {
        _uiState.update { state ->
            val newRad = !state.isRad
            state.copy(
                isRad = newRad,
                liveResult = computeLivePreview(state.expression, newRad)
            )
        }
    }

    fun onToggleScientific() {
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    fun onToggleHistory(open: Boolean? = null) {
        _uiState.update { state ->
            state.copy(isHistoryOpen = open ?: !state.isHistoryOpen)
        }
    }

    fun onSelectHistoryItem(record: CalculationRecord) {
        _uiState.update { state ->
            state.copy(
                expression = record.result.replace(",", ""),
                finalResult = null,
                errorMessage = null,
                isResultEvaluated = false,
                isHistoryOpen = false,
                liveResult = ""
            )
        }
    }

    fun onReuseExpression(record: CalculationRecord) {
        _uiState.update { state ->
            state.copy(
                expression = record.expression,
                finalResult = record.result,
                errorMessage = null,
                isResultEvaluated = true,
                isHistoryOpen = false,
                liveResult = ""
            )
        }
    }

    fun onDeleteHistoryItem(record: CalculationRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record.id)
        }
    }

    fun onClearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun onCopyResult(context: Context, text: String) {
        if (text.isBlank()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Calculator Result", text)
        clipboard.setPrimaryClip(clip)
        viewModelScope.launch {
            _events.emit(CalculatorUiEvent.ShowToast("Copied to clipboard"))
        }
    }

    private fun computeLivePreview(expr: String, isRad: Boolean): String {
        if (expr.length < 2) return ""
        val eval = CalculatorEngine.evaluate(expr, isRad, isPreview = true)
        return if (eval is CalculatorEngine.EvalResult.Success) {
            eval.formatted
        } else {
            ""
        }
    }
}
