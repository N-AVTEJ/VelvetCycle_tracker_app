package com.example.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalVelvetColors

@Composable
fun NumberPad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: (() -> Unit)? = null,
    showBiometric: Boolean = false,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )

    Column(
        modifier = modifier
            .wrapContentSize()
            .testTag("number_pad"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                row.forEach { digit ->
                    NumberButton(
                        digit = digit,
                        letters = getLettersForDigit(digit),
                        onClick = { onDigitClick(digit) }
                    )
                }
            }
        }

        // Bottom row: Biometric / Space, 0, DEL
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.wrapContentSize()
        ) {
            // Biometric button (or spacer)
            if (showBiometric && onBiometricClick != null) {
                NumberButtonIcon(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    onClick = onBiometricClick,
                    testTag = "pin_btn_biometric"
                )
            } else {
                Spacer(modifier = Modifier.size(72.dp))
            }

            // '0' button
            NumberButton(
                digit = "0",
                letters = "",
                onClick = { onDigitClick("0") }
            )

            // Backspace button
            NumberButtonIcon(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Delete",
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = onDeleteClick,
                testTag = "pin_btn_backspace"
            )
        }
    }
}

@Composable
fun NumberButton(
    digit: String,
    letters: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val colors = LocalVelvetColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Background color: flashes to light pink (#FFF0F5) on press
    val backgroundColor = when {
        isPressed -> Color(0xFFFFF0F5)
        isDark -> Color(0xFF1A1A1A)
        else -> Color.White
    }

    // Border color
    val borderColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFF0F0F0)

    // Text color
    val textColor = when {
        isPressed -> Color(0xFFD4537E) // flash pink accent text on press
        isDark -> Color(0xFFF5F5F5)
        else -> Color(0xFF1A1A2E)
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(BorderStroke(1.dp, borderColor), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disable default gray ripple to let our pink flash shine
                onClick = onClick
            )
            .testTag("pin_btn_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = digit,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                lineHeight = 28.sp
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isPressed) Color(0xFFD4537E).copy(alpha = 0.8f) else colors.textSecondary,
                    lineHeight = 10.sp
                )
            }
        }
    }
}

@Composable
fun NumberButtonIcon(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    testTag: String
) {
    val isDark = isSystemInDarkTheme()
    val colors = LocalVelvetColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = when {
        isPressed -> Color(0xFFFFF0F5)
        isDark -> Color(0xFF1A1A1A)
        else -> Color.White
    }

    val borderColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFF0F0F0)
    val contentColor = when {
        isPressed -> Color(0xFFD4537E)
        isDark -> Color(0xFFF5F5F5)
        else -> Color(0xFF1A1A2E)
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(BorderStroke(1.dp, borderColor), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides contentColor
        ) {
            icon()
        }
    }
}

private fun getLettersForDigit(digit: String): String {
    return when (digit) {
        "2" -> "ABC"
        "3" -> "DEF"
        "4" -> "GHI"
        "5" -> "JKL"
        "6" -> "MNO"
        "7" -> "PQRS"
        "8" -> "TUV"
        "9" -> "WXYZ"
        else -> ""
    }
}
