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
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightPinkBg
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.White
import com.example.utils.StorageHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    storageHelper: StorageHelper,
    onOnboardingFinished: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    
    // Onboarding form state
    var name by remember { mutableStateOf("") }
    var lastPeriodStart by remember { mutableStateOf(LocalDate.now()) }
    var periodDuration by remember { mutableStateOf(5) }
    var cycleLength by remember { mutableStateOf(28) }

    val context = LocalContext.current
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMMM d, yyyy") }

    Scaffold(
        containerColor = LightPinkBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Title & Step indicators
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "VelvetCycle",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPink,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your intimate cycle companion",
                    fontSize = 14.sp,
                    color = DarkText.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Dots
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
                                .background(if (step == i) PrimaryPink else PrimaryPink.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            // Main Content Area with transitions
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
                                    text = "What's your name?",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    placeholder = { Text("Enter your name") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Done
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryPink,
                                        unfocusedBorderColor = PrimaryPink.copy(alpha = 0.4f),
                                        focusedLabelColor = PrimaryPink,
                                        cursorColor = PrimaryPink
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
                                    text = "When did your last period start?",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText,
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
                                    colors = CardDefaults.cardColors(containerColor = White),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                                text = "First day of last period",
                                                fontSize = 12.sp,
                                                color = DarkText.copy(alpha = 0.5f)
                                            )
                                            Text(
                                                text = lastPeriodStart.format(dateFormatter),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = DarkText,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Pick date",
                                            tint = PrimaryPink,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                            3 -> {
                                Text(
                                    text = "How many days does your period usually last?",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText,
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
                                                .background(if (isSelected) PrimaryPink else White)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) PrimaryPink else PrimaryPink.copy(alpha = 0.2f),
                                                    shape = CircleShape
                                                )
                                                .clickable { periodDuration = num }
                                        ) {
                                            Text(
                                                text = num.toString(),
                                                fontSize = 16.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) White else DarkText
                                            )
                                        }
                                    }
                                }
                            }
                            4 -> {
                                Text(
                                    text = "How long is your usual cycle?",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText,
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
                                                .background(if (isSelected) PrimaryPink else White)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) PrimaryPink else PrimaryPink.copy(alpha = 0.2f),
                                                    shape = CircleShape
                                                )
                                                .clickable { cycleLength = num }
                                        ) {
                                            Text(
                                                text = num.toString(),
                                                fontSize = 16.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) White else DarkText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation: Back & Next Buttons
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
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryPink),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPink),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .padding(end = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Back")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // Next / Finish Button
                Button(
                    onClick = {
                        if (step < 4) {
                            step++
                        } else {
                            // Finish onboarding and save
                            storageHelper.userName = name.trim()
                            storageHelper.lastPeriodStart = lastPeriodStart
                            storageHelper.periodDuration = periodDuration
                            storageHelper.cycleLength = cycleLength
                            storageHelper.isOnboarded = true
                            
                            onOnboardingFinished()
                        }
                    },
                    enabled = step != 1 || name.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPink,
                        disabledContainerColor = PrimaryPink.copy(alpha = 0.4f),
                        contentColor = White,
                        disabledContentColor = White.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .padding(start = if (step > 1) 8.dp else 0.dp)
                        .testTag(if (step == 4) "submit_button" else "next_button")
                ) {
                    Text(
                        text = if (step == 4) "Finish" else "Next",
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
