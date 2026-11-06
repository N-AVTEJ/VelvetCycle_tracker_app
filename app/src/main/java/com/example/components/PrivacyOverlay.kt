package com.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * PrivacyOverlay component for VelvetCycle.
 * Shows instantly when the app transitions to background or inactive state
 * to prevent sensitive cycle data from being exposed in the Android app switcher / recents screen.
 *
 * Requirements:
 * - Full screen view, background #D4537E (pink)
 * - 'VelvetCycle' text centered in white, large
 * - Small lotus/flower emoji below (🌸 / 🪷)
 * - Supports disguise mode if enabled
 */
@Composable
fun PrivacyOverlay(
    isDisguised: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (isDisguised) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF121214))
                .testTag("privacy_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Calculator",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9E9EA7)
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFD4537E))
                .testTag("privacy_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "VelvetCycle",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🌸",
                    fontSize = 28.sp
                )
            }
        }
    }
}
