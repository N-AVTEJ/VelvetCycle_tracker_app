package com.example.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun PinDots(
    enteredLength: Int,
    isShaking: Boolean,
    flashSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    val pinkAccent = Color(0xFFD4537E)
    val successColor = Color(0xFF4CAF50)

    // Shake animation offset
    val shakeOffset by animateFloatAsState(
        targetValue = if (isShaking) 15f else 0f,
        animationSpec = keyframes {
            durationMillis = 300
            0f at 0
            -15f at 50
            15f at 100
            -15f at 150
            15f at 200
            -15f at 250
            0f at 300
        },
        label = "shake_dots"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .offset(x = shakeOffset.dp)
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
    val scale = remember { Animatable(1.0f) }

    LaunchedEffect(isFilled) {
        if (isFilled) {
            scale.animateTo(1.2f, animationSpec = tween(75))
            scale.animateTo(1.0f, animationSpec = tween(75))
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
                        .background(Color.Transparent)
                        .border(2.dp, borderColor, CircleShape)
                }
            )
            .testTag(testTag)
    )
}
