package com.example.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object CalculatorEngine {

    sealed class EvalResult {
        data class Success(val value: Double, val formatted: String) : EvalResult()
        data class Error(val message: String) : EvalResult()
    }

    /**
     * Evaluates a mathematical expression string.
     * @param expr The mathematical expression
     * @param isRad If true, trigonometric functions use radians; if false, degrees.
     * @param isPreview If true, auto-balances open parentheses to give live feedback.
     */
    fun evaluate(expr: String, isRad: Boolean = false, isPreview: Boolean = false): EvalResult {
        if (expr.isBlank()) return EvalResult.Error("")

        var sanitized = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace(" ", "")

        if (sanitized.isEmpty()) return EvalResult.Error("")

        if (isPreview) {
            // Remove trailing operator if user is still typing
            while (sanitized.isNotEmpty() && (sanitized.last() in "+-*/^(")) {
                sanitized = sanitized.dropLast(1)
            }
            if (sanitized.isEmpty()) return EvalResult.Error("")

            // Auto-close open parentheses
            val openCount = sanitized.count { it == '(' }
            val closeCount = sanitized.count { it == ')' }
            if (openCount > closeCount) {
                sanitized += ")".repeat(openCount - closeCount)
            }
        }

        return try {
            val parser = Parser(sanitized, isRad)
            val result = parser.parse()
            if (result.isNaN()) {
                EvalResult.Error("Undefined")
            } else if (result.isInfinite()) {
                EvalResult.Error("Cannot divide by zero")
            } else {
                EvalResult.Success(result, formatResult(result))
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Math error")
        } catch (e: Exception) {
            EvalResult.Error("Invalid expression")
        }
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "Undefined"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        // Handle exact integers or very small floating point noise
        val rounded = if (kotlin.math.abs(value - kotlin.math.round(value)) < 1e-12) {
            kotlin.math.round(value)
        } else {
            value
        }

        // Use BigDecimal for high precision display and to strip scientific notation where reasonable
        val bd = try {
            BigDecimal(rounded).round(MathContext(12, RoundingMode.HALF_UP))
        } catch (e: Exception) {
            BigDecimal.valueOf(rounded)
        }

        val absVal = kotlin.math.abs(rounded)
        return if (absVal >= 1e12 || (absVal > 0 && absVal < 1e-6)) {
            val symbols = DecimalFormatSymbols(Locale.US)
            val df = DecimalFormat("0.######E0", symbols)
            df.format(rounded).lowercase(Locale.US)
        } else {
            val symbols = DecimalFormatSymbols(Locale.US).apply {
                groupingSeparator = ','
                decimalSeparator = '.'
            }
            val df = DecimalFormat("#,##0.##########", symbols)
            df.format(bd.stripTrailingZeros())
        }
    }

    private class Parser(val input: String, val isRad: Boolean) {
        var pos = -1
        var ch = ' '

        fun nextChar() {
            pos++
            ch = if (pos < input.length) input[pos] else '\u0000'
        }

        fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < input.length && ch != '\u0000') {
                throw IllegalArgumentException("Unexpected character: $ch")
            }
            return x
        }

        // Expression = Term ('+' Term | '-' Term)*
        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        // Term = Factor ('*' Factor | '/' Factor)*
        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Cannot divide by zero")
                        x /= divisor
                    }
                    else -> return x
                }
            }
        }

        // Factor = Power
        fun parseFactor(): Double {
            var x = parseUnary()
            if (eat('^')) {
                val exponent = parseFactor()
                x = x.pow(exponent)
            }
            return x
        }

        // Unary = ('+' | '-')? Base ('!' | '%')*
        fun parseUnary(): Double {
            if (eat('+')) return parseUnary()
            if (eat('-')) return -parseUnary()

            var x = parseBase()

            // Postfix operators: ! and %
            while (true) {
                when {
                    eat('!') -> {
                        x = factorial(x)
                    }
                    eat('%') -> {
                        x /= 100.0
                    }
                    else -> break
                }
            }
            return x
        }

        // Base = Number | Constants | Parenthesized | Function
        fun parseBase(): Double {
            var x: Double
            val startPos = this.pos

            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if ((ch in '0'..'9') || ch == '.') {
                while ((ch in '0'..'9') || ch == '.') nextChar()
                val numStr = input.substring(startPos, this.pos)
                x = numStr.toDouble()
            } else if (ch == 'π') {
                nextChar()
                x = Math.PI
            } else if (ch == 'e') {
                nextChar()
                x = Math.E
            } else if (ch == '√') {
                nextChar()
                val arg = parseBase()
                if (arg < 0) throw ArithmeticException("Square root of negative number")
                x = sqrt(arg)
            } else if (ch in 'a'..'z') {
                while (ch in 'a'..'z') nextChar()
                val func = input.substring(startPos, this.pos)
                val arg = if (eat('(')) {
                    val a = parseExpression()
                    eat(')')
                    a
                } else {
                    parseBase()
                }

                x = when (func) {
                    "sqrt" -> {
                        if (arg < 0) throw ArithmeticException("Square root of negative number")
                        sqrt(arg)
                    }
                    "sin" -> {
                        val rad = if (isRad) arg else Math.toRadians(arg)
                        sin(rad)
                    }
                    "cos" -> {
                        val rad = if (isRad) arg else Math.toRadians(arg)
                        cos(rad)
                    }
                    "tan" -> {
                        val rad = if (isRad) arg else Math.toRadians(arg)
                        if (!isRad && (kotlin.math.abs(arg % 180) == 90.0)) {
                            throw ArithmeticException("Tangent undefined")
                        }
                        tan(rad)
                    }
                    "log" -> {
                        if (arg <= 0) throw ArithmeticException("Log of non-positive number")
                        log10(arg)
                    }
                    "ln" -> {
                        if (arg <= 0) throw ArithmeticException("Ln of non-positive number")
                        ln(arg)
                    }
                    else -> throw IllegalArgumentException("Unknown function: $func")
                }
            } else {
                throw IllegalArgumentException("Unexpected: $ch")
            }

            // Check for implicit multiplication like 2(3), 2π, (2)(3), 2sin(30)
            if (ch == '(' || ch == 'π' || ch == 'e' || ch == '√' || (ch in 'a'..'z')) {
                x *= parseFactor()
            }

            return x
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || n != kotlin.math.floor(n)) {
                throw ArithmeticException("Factorial is only defined for non-negative integers")
            }
            if (n > 170) return Double.POSITIVE_INFINITY
            var res = 1.0
            val limit = n.toInt()
            for (i in 2..limit) {
                res *= i
            }
            return res
        }
    }
}
