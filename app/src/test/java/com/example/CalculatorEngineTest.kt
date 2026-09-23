package com.example

import com.example.calculator.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

    @Test
    fun testBasicArithmetic() {
        val res1 = CalculatorEngine.evaluate("2+3")
        assertTrue(res1 is CalculatorEngine.EvalResult.Success)
        assertEquals("5", (res1 as CalculatorEngine.EvalResult.Success).formatted)

        val res2 = CalculatorEngine.evaluate("10-4")
        assertTrue(res2 is CalculatorEngine.EvalResult.Success)
        assertEquals("6", (res2 as CalculatorEngine.EvalResult.Success).formatted)

        val res3 = CalculatorEngine.evaluate("6*7")
        assertTrue(res3 is CalculatorEngine.EvalResult.Success)
        assertEquals("42", (res3 as CalculatorEngine.EvalResult.Success).formatted)

        val res4 = CalculatorEngine.evaluate("15/3")
        assertTrue(res4 is CalculatorEngine.EvalResult.Success)
        assertEquals("5", (res4 as CalculatorEngine.EvalResult.Success).formatted)
    }

    @Test
    fun testOperatorPrecedenceAndParentheses() {
        val res = CalculatorEngine.evaluate("2+3*4")
        assertTrue(res is CalculatorEngine.EvalResult.Success)
        assertEquals("14", (res as CalculatorEngine.EvalResult.Success).formatted)

        val resParens = CalculatorEngine.evaluate("(2+3)*4")
        assertTrue(resParens is CalculatorEngine.EvalResult.Success)
        assertEquals("20", (resParens as CalculatorEngine.EvalResult.Success).formatted)
    }

    @Test
    fun testDecimalAndPercentage() {
        val res = CalculatorEngine.evaluate("50*20%")
        assertTrue(res is CalculatorEngine.EvalResult.Success)
        assertEquals("10", (res as CalculatorEngine.EvalResult.Success).formatted)
    }

    @Test
    fun testScientificFunctions() {
        // sqrt
        val resSqrt = CalculatorEngine.evaluate("sqrt(144)")
        assertTrue(resSqrt is CalculatorEngine.EvalResult.Success)
        assertEquals("12", (resSqrt as CalculatorEngine.EvalResult.Success).formatted)

        // power
        val resPower = CalculatorEngine.evaluate("2^8")
        assertTrue(resPower is CalculatorEngine.EvalResult.Success)
        assertEquals("256", (resPower as CalculatorEngine.EvalResult.Success).formatted)

        // sin 90 degrees
        val resSinDeg = CalculatorEngine.evaluate("sin(90)", isRad = false)
        assertTrue(resSinDeg is CalculatorEngine.EvalResult.Success)
        assertEquals("1", (resSinDeg as CalculatorEngine.EvalResult.Success).formatted)

        // factorial
        val resFact = CalculatorEngine.evaluate("5!")
        assertTrue(resFact is CalculatorEngine.EvalResult.Success)
        assertEquals("120", (resFact as CalculatorEngine.EvalResult.Success).formatted)
    }

    @Test
    fun testDivisionByZero() {
        val res = CalculatorEngine.evaluate("10/0")
        assertTrue(res is CalculatorEngine.EvalResult.Error)
        assertEquals("Cannot divide by zero", (res as CalculatorEngine.EvalResult.Error).message)
    }
}
