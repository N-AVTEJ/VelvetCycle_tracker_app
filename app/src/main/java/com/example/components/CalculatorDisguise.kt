package com.example.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

object CalculatorEngine {
    fun evaluate(expression: String): String {
        if (expression.isBlank()) return "0"

        try {
            val normalized = expression
                .replace("x", "×")
                .replace("*", "×")
                .replace("/", "÷")
                .trim()

            val tokens = mutableListOf<String>()
            var currentNumber = StringBuilder()

            var i = 0
            while (i < normalized.length) {
                val c = normalized[i]
                if (c == ' ') {
                    i++
                    continue
                }
                if (c in '0'..'9' || c == '.') {
                    currentNumber.append(c)
                } else if (c in listOf('+', '-', '×', '÷')) {
                    if (c == '-' && currentNumber.isEmpty() && (tokens.isEmpty() || tokens.last() in listOf("+", "-", "×", "÷"))) {
                        currentNumber.append(c)
                    } else {
                        if (currentNumber.isNotEmpty()) {
                            tokens.add(currentNumber.toString())
                            currentNumber = StringBuilder()
                        }
                        tokens.add(c.toString())
                    }
                }
                i++
            }
            if (currentNumber.isNotEmpty()) {
                tokens.add(currentNumber.toString())
            }

            // Remove trailing operator if expression ends with one (e.g. "12 +")
            while (tokens.isNotEmpty() && tokens.last() in listOf("+", "-", "×", "÷")) {
                tokens.removeAt(tokens.size - 1)
            }

            if (tokens.isEmpty()) return "0"
            if (tokens.size == 1) {
                val num = tokens[0].toDoubleOrNull() ?: return tokens[0]
                return formatNumber(num)
            }

            // Pass 1: Multiplication and Division
            var idx = 0
            while (idx < tokens.size) {
                val token = tokens[idx]
                if (token == "×" || token == "÷") {
                    val prev = tokens.getOrNull(idx - 1)?.toDoubleOrNull()
                    val next = tokens.getOrNull(idx + 1)?.toDoubleOrNull()
                    if (prev != null && next != null) {
                        if (token == "÷" && next == 0.0) {
                            return "Error"
                        }
                        val calc = if (token == "×") prev * next else prev / next
                        tokens[idx - 1] = calc.toString()
                        tokens.removeAt(idx)
                        tokens.removeAt(idx)
                        idx--
                    } else {
                        return "Error"
                    }
                } else {
                    idx++
                }
            }

            // Pass 2: Addition and Subtraction
            var result = tokens.getOrNull(0)?.toDoubleOrNull() ?: return "Error"
            idx = 1
            while (idx < tokens.size) {
                val op = tokens[idx]
                val next = tokens.getOrNull(idx + 1)?.toDoubleOrNull()
                if (next != null) {
                    result = if (op == "+") result + next else result - next
                    idx += 2
                } else {
                    break
                }
            }

            return formatNumber(result)
        } catch (e: Exception) {
            return "Error"
        }
    }

    fun formatNumber(value: Double): String {
        if (value.isInfinite() || value.isNaN()) return "Error"
        val longVal = value.toLong()
        return if (value == longVal.toDouble()) {
            longVal.toString()
        } else {
            String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')
        }
    }
}

/**
 * A 100% realistic stock Calculator disguise interface.
 * Shows no VelvetCycle branding or hints anywhere.
 *
 * Secret Unlock Mechanism:
 * - Tracks the last 4 digits entered by the user.
 * - When '=' is pressed:
 *     - If last 4 digits match saved PIN -> calls onUnlock()
 *     - If not -> evaluates the expression normally and displays result (treating digits as numbers).
 */
@Composable
fun CalculatorDisguise(
    savedPin: String?,
    onUnlock: () -> Unit,
    onEmergencyWipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var last4Digits by remember { mutableStateOf("") }
    var justCalculated by remember { mutableStateOf(false) }

    val handleDigit: (String) -> Unit = { digit ->
        if (justCalculated) {
            expression = digit
            resultText = ""
            justCalculated = false
        } else {
            expression += digit
        }
        last4Digits = (last4Digits + digit).takeLast(4)
    }

    val handleOperator: (String) -> Unit = { op ->
        justCalculated = false
        if (expression.isEmpty()) {
            if (resultText.isNotEmpty() && resultText != "Error") {
                expression = "$resultText $op "
                resultText = ""
            } else if (op == "-") {
                expression = "-"
            }
        } else {
            val trimmed = expression.trimEnd()
            if (trimmed.endsWith("+") || trimmed.endsWith("-") || trimmed.endsWith("×") || trimmed.endsWith("÷")) {
                expression = trimmed.dropLast(1) + op + " "
            } else {
                expression = "$trimmed $op "
            }
        }
    }

    val handleClear: () -> Unit = {
        expression = ""
        resultText = ""
        last4Digits = ""
        justCalculated = false
    }

    val handleBackspace: () -> Unit = {
        if (expression.isNotEmpty()) {
            val lastChar = expression.last()
            expression = if (expression.endsWith(" ")) {
                expression.trimEnd().dropLast(1).trimEnd()
            } else {
                expression.dropLast(1)
            }
            if (lastChar.isDigit() && last4Digits.isNotEmpty()) {
                last4Digits = last4Digits.dropLast(1)
            }
        }
    }

    val handleEquals: () -> Unit = {
        // Secret unlock check
        if (last4Digits.length == 4 && last4Digits == "0000") {
            onEmergencyWipe()
        } else if (last4Digits.length == 4 && savedPin != null && last4Digits == savedPin) {
            onUnlock()
        } else {
            // If not matching PIN, compute and display normally
            val cleanExpr = expression.trim()
            if (cleanExpr.isNotEmpty()) {
                val res = CalculatorEngine.evaluate(cleanExpr)
                resultText = res
                justCalculated = true
            } else {
                resultText = "0"
                justCalculated = true
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .testTag("calculator_disguise_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display area showing current input and result
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Current input expression line
            Text(
                text = expression.ifEmpty { "0" },
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF9E9EA7),
                textAlign = TextAlign.End,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calc_input_display")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Result line
            Text(
                text = resultText,
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calc_result_display")
            )
        }

        // Keypad Grid: 5 Rows
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: C (weight 2), ⌫ (weight 1), ÷ (weight 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalcButton(
                    label = "C",
                    backgroundColor = Color(0xFF424451),
                    textColor = Color.White,
                    modifier = Modifier.weight(2f),
                    testTag = "calc_btn_C",
                    onClick = handleClear
                )
                CalcButton(
                    label = "⌫",
                    backgroundColor = Color(0xFF424451),
                    textColor = Color.White,
                    modifier = Modifier.weight(1f),
                    testTag = "calc_btn_backspace",
                    onClick = handleBackspace
                )
                CalcButton(
                    label = "÷",
                    backgroundColor = Color(0xFFFF9500),
                    textColor = Color.White,
                    modifier = Modifier.weight(1f),
                    testTag = "calc_btn_div",
                    onClick = { handleOperator("÷") }
                )
            }

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("7", "8", "9").forEach { num ->
                    CalcButton(
                        label = num,
                        backgroundColor = Color(0xFF282932),
                        textColor = Color.White,
                        modifier = Modifier.weight(1f),
                        testTag = "calc_btn_$num",
                        onClick = { handleDigit(num) }
                    )
                }
                CalcButton(
                    label = "×",
                    backgroundColor = Color(0xFFFF9500),
                    textColor = Color.White,
                    modifier = Modifier.weight(1f),
                    testTag = "calc_btn_mul",
                    onClick = { handleOperator("×") }
                )
            }

            // Row 3: 4, 5, 6, -
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("4", "5", "6").forEach { num ->
                    CalcButton(
                        label = num,
                        backgroundColor = Color(0xFF282932),
                        textColor = Color.White,
                        modifier = Modifier.weight(1f),
                        testTag = "calc_btn_$num",
                        onClick = { handleDigit(num) }
                    )
                }
                CalcButton(
                    label = "-",
                    backgroundColor = Color(0xFFFF9500),
                    textColor = Color.White,
                    modifier = Modifier.weight(1f),
                    testTag = "calc_btn_sub",
                    onClick = { handleOperator("-") }
                )
            }

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("1", "2", "3").forEach { num ->
                    CalcButton(
                        label = num,
                        backgroundColor = Color(0xFF282932),
                        textColor = Color.White,
                        modifier = Modifier.weight(1f),
                        testTag = "calc_btn_$num",
                        onClick = { handleDigit(num) }
                    )
                }
                CalcButton(
                    label = "+",
                    backgroundColor = Color(0xFFFF9500),
                    textColor = Color.White,
                    modifier = Modifier.weight(1f),
                    testTag = "calc_btn_add",
                    onClick = { handleOperator("+") }
                )
            }

            // Row 5: 0 (weight 2), . (weight 1), = (weight 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalcButton(
                    label = "0",
                    backgroundColor = Color(0xFF282932),
                    textColor = Color.White,
                    modifier = Modifier.weight(2f),
                    testTag = "calc_btn_0",
                    onClick = { handleDigit("0") }
                )
                CalcButton(
                    label = ".",
                    backgroundColor = Color(0xFF282932),
                    textColor = Color.White,
                    modifier = Modifier.weight(1f),
                    testTag = "calc_btn_dot",
                    onClick = {
                        justCalculated = false
                        if (expression.isEmpty()) {
                            expression = "0."
                        } else {
                            val lastToken = expression.trim().split(" ").lastOrNull() ?: ""
                            if (!lastToken.contains(".")) {
                                expression += "."
                            }
                        }
                    }
                )
                CalcButton(
                    label = "=",
                    backgroundColor = Color(0xFFFF9500),
                    textColor = Color.White,
                    modifier = Modifier.weight(1f),
                    testTag = "calc_btn_equals",
                    onClick = handleEquals
                )
            }
        }
    }
}

@Composable
fun CalcButton(
    label: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "calc_btn_$label",
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "calc_btn_scale"
    )

    Box(
        modifier = modifier
            .height(68.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isPressed) backgroundColor.copy(alpha = 0.8f) else backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
