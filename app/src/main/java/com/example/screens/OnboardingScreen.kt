package com.example.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LocalAppLanguage
import com.example.constants.Translations
import com.example.ui.theme.LocalVelvetColors
import com.example.utils.StorageHelper
import java.time.LocalDate
import java.util.Calendar

@Composable
fun OnboardingScreen(
    storageHelper: StorageHelper,
    onOnboardingFinished: () -> Unit
) {
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current

    var step by remember { mutableStateOf(1) }
    
    // Onboarding form state
    var name by remember { mutableStateOf(storageHelper.userName) }
    var lastPeriodStart by remember { mutableStateOf(storageHelper.lastPeriodStart) }
    var periodDuration by remember { mutableStateOf(storageHelper.periodDuration.coerceIn(1, 9)) }
    var cycleLength by remember { mutableStateOf(storageHelper.cycleLength.coerceIn(21, 35)) }

    val context = LocalContext.current

    // Localized Strings
    val backButtonLabel = if (lang == "हिंदी") "पीछे" else if (lang == "తెలుగు") "వెనుకకు" else "Back"
    val nextButtonLabel = if (lang == "हिंदी") "आगे" else if (lang == "తెలుగు") "తరువాత" else "Next"
    val finishButtonLabel = if (lang == "हिंदी") "समाप्त" else if (lang == "తెలుగు") "ముగించు" else "Finish"

    val step1Title = if (lang == "हिंदी") "आपका नाम क्या है?" else if (lang == "తెలుగు") "మీ పేరు ఏమిటి?" else "What's your name?"
    val step1Placeholder = if (lang == "हिंदी") "अपना नाम दर्ज करें" else if (lang == "తెలుగు") "మీ పేరు నమోదు చేయండి" else "Enter your name"

    val step2Title = if (lang == "हिंदी") "आपका पिछला मासिक धर्म कब शुरू हुआ था?" else if (lang == "తెలుగు") "మీ చివరి పీరియడ్ ఎప్పుడు ప్రారంభమైంది?" else "When did your last period start?"
    val step2Subtitle = if (lang == "हिंदी") "पिछले मासिक धर्म का पहला दिन" else if (lang == "తెలుగు") "చివరి పీరియడ్ మొదటి రోజు" else "First day of last period"

    val step3Title = if (lang == "हिंदी") "आपका मासिक धर्म आमतौर पर कितने दिनों तक रहता है?" else if (lang == "తెలుగు") "సాధారణంగా మీ పీరియడ్ ఎన్ని రోజులు ఉంటుంది?" else "How many days does your period usually last?"
    val step4Title = if (lang == "हिंदी") "आपका सामान्य चक्र कितना लंबा होता है?" else if (lang == "తెలుగు") "మీ సాధారణ సైకిల్ ఎన్ని రోజులు ఉంటుంది?" else "How long is your usual cycle?"

    Scaffold(
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "VelvetCycle",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.pinkAccent,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (lang == "हिंदी") "आपका सुरक्षित चक्र साथी" else if (lang == "తెలుగు") "మీ సురక్షిత సైకిల్ గైడ్" else "Your intimate cycle companion",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 1..4) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = if (step == i) 24.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(if (step == i) colors.pinkAccent else colors.pinkAccent.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            // Main steps transition content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    },
                    label = "step_transition"
                ) { targetStep ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (targetStep) {
                            1 -> {
                                Text(
                                    text = step1Title,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    placeholder = { Text(step1Placeholder, color = colors.textSecondary.copy(alpha = 0.6f)) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Done
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.pinkAccent,
                                        unfocusedBorderColor = colors.border,
                                        focusedTextColor = colors.textPrimary,
                                        unfocusedTextColor = colors.textPrimary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("username_input")
                                        .padding(horizontal = 8.dp),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                            2 -> {
                                Text(
                                    text = step2Title,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                        .clickable {
                                            val calendar = Calendar.getInstance()
                                            DatePickerDialog(
                                                context,
                                                { _, year, month, dayOfMonth ->
                                                    lastPeriodStart = LocalDate.of(year, month + 1, dayOfMonth)
                                                },
                                                lastPeriodStart.year,
                                                lastPeriodStart.monthValue - 1,
                                                lastPeriodStart.dayOfMonth
                                            ).show()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = step2Subtitle,
                                                fontSize = 12.sp,
                                                color = colors.textSecondary
                                            )
                                            Text(
                                                text = Translations.formatDate(lastPeriodStart, lang),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textPrimary,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Pick date",
                                            tint = colors.pinkAccent,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                            3 -> {
                                Text(
                                    text = step3Title,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    (1..9).forEach { num ->
                                        val isSelected = periodDuration == num
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) colors.pinkAccent else colors.cardBackground)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) colors.pinkAccent else colors.border,
                                                    shape = CircleShape
                                                )
                                                .clickable { periodDuration = num }
                                        ) {
                                            Text(
                                                text = num.toString(),
                                                fontSize = 16.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) colors.cardBackground else colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                            4 -> {
                                Text(
                                    text = step4Title,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(5),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    items(15) { index ->
                                        val num = 21 + index
                                        val isSelected = cycleLength == num
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .clip(CircleShape)
                                                .background(if (isSelected) colors.pinkAccent else colors.cardBackground)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) colors.pinkAccent else colors.border,
                                                    shape = CircleShape
                                                )
                                                .clickable { cycleLength = num }
                                        ) {
                                            Text(
                                                text = num.toString(),
                                                fontSize = 16.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) colors.cardBackground else colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom controls bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.pinkAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.pinkAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .padding(end = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Back")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(backButtonLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // Next or Finish Button
                Button(
                    onClick = {
                        if (step < 4) {
                            step++
                        } else {
                            // Save user cycle data
                            storageHelper.userName = name.trim()
                            storageHelper.lastPeriodStart = lastPeriodStart
                            storageHelper.periodDuration = periodDuration
                            storageHelper.cycleLength = cycleLength
                            storageHelper.isOnboarded = true
                            
                            // Reschedule alerts
                            com.example.utils.NotificationHelper.scheduleAllNotifications(context, storageHelper)
                            
                            onOnboardingFinished()
                        }
                    },
                    enabled = step != 1 || name.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.pinkAccent,
                        disabledContainerColor = colors.pinkAccent.copy(alpha = 0.4f),
                        contentColor = colors.cardBackground,
                        disabledContentColor = colors.cardBackground.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .padding(start = if (step > 1) 8.dp else 0.dp)
                        .testTag(if (step == 4) "submit_button" else "next_button")
                ) {
                    Text(
                        text = if (step == 4) finishButtonLabel else nextButtonLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (step < 4) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next")
                    }
                }
            }
        }
    }
}
