package com.example.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalVelvetColors

@Composable
fun PinDots(
    enteredLength: Int,
    isShaking: Boolean,
    flashSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    val pinkAccent = Color(0xFFD4537E)
    val successColor = Color(0xFF4CAF50)

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isShaking) {
        if (isShaking) {
            // Shake left-right 3 times
            repeat(3) {
                shakeOffset.animateTo(-16f, tween(50, easing = LinearEasing))
                shakeOffset.animateTo(16f, tween(50, easing = LinearEasing))
            }
            shakeOffset.animateTo(0f, tween(50, easing = LinearEasing))
        } else {
            shakeOffset.snapTo(0f)
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .offset(x = shakeOffset.value.dp)
            .testTag("pin_dots_container")
    ) {
        for (i in 0 until 4) {
            val isFilled = enteredLength > i
            SinglePinDot(
                isFilled = isFilled,
                fillColor = if (flashSuccess) successColor else pinkAccent,
                borderColor = if (flashSuccess) successColor else pinkAccent,
                testTag = "pin_dot_${i + 1}"
            )
        }
    }
}

@Composable
fun SinglePinDot(
    isFilled: Boolean,
    fillColor: Color,
    borderColor: Color,
    testTag: String
) {
    val isDark = isSystemInDarkTheme()
    val colors = LocalVelvetColors.current
    val emptyBg = if (isDark) colors.cardBackground else Color.White
    val scale = remember { Animatable(1.0f) }

    LaunchedEffect(isFilled) {
        if (isFilled) {
            scale.animateTo(1.2f, animationSpec = tween(75))
            scale.animateTo(1.0f, animationSpec = tween(75))
        } else {
            scale.snapTo(1.0f)
        }
    }

    Box(
        modifier = Modifier
            .size(20.dp)
            .graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value
            )
            .clip(CircleShape)
            .then(
                if (isFilled) {
                    Modifier.background(fillColor)
                } else {
                    Modifier
                        .background(emptyBg)
                        .border(2.dp, borderColor, CircleShape)
                }
            )
            .testTag(testTag)
    )
}
