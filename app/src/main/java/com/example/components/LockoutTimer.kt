package com.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LocalAppLanguage
import com.example.ui.theme.LocalVelvetColors

@Composable
fun LockoutTimer(
    timeLeftMs: Long,
    totalDurationMs: Long,
    modifier: Modifier = Modifier,
    isDataProtectionWarning: Boolean = false
) {
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current

    val titleText = if (lang == "हिंदी") "अत्यधिक प्रयास" else if (lang == "తెలుగు") "చాలా సార్లు ప్రయత్నించారు" else "Too many attempts"
    val warningText = if (lang == "हिंदी") "डेटा सुरक्षा मोड सक्रिय है।" else if (lang == "తెలుగు") "డేటా రక్షణ మోడ్ యాక్టివ్ చేయబడింది." else "Data protection mode active."

    val formattedTime = rememberFormattedTime(timeLeftMs)
    val progress = if (totalDurationMs > 0) {
        (timeLeftMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag("lockout_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large Lock Icon / Emoji
        Text(
            text = "🔒",
            fontSize = 64.sp,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .testTag("lockout_icon")
        )

        // Title
        Text(
            text = titleText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .testTag("lockout_title")
        )

        // MM:SS Countdown
        val labelText = if (lang == "हिंदी") "पुनः प्रयास करें " else if (lang == "తెలుగు") "మళ్లీ ప్రయత్నించండి " else "Try again in "
        Text(
            text = "$labelText$formattedTime",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.pinkAccent,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .testTag("lockout_countdown")
        )

        // Progress Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .testTag("lockout_progress"),
            color = colors.pinkAccent,
            trackColor = colors.pinkAccent.copy(alpha = 0.2f),
        )

        if (isDataProtectionWarning) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = warningText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .testTag("lockout_warning")
            )
        }
    }
}

@Composable
fun rememberFormattedTime(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
