package com.example.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightPinkBg
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.White
import com.example.utils.LogData
import com.example.utils.StorageHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogScreen(storageHelper: StorageHelper) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val todayStr = today.toString()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

    // Load existing log if available
    val existingLog = remember(todayStr) { storageHelper.getLog(todayStr) }

    var selectedMood by remember { mutableStateOf(existingLog?.mood ?: "") }
    var selectedFlow by remember { mutableStateOf(existingLog?.flow ?: "") }
    var selectedSymptoms by remember { mutableStateOf(existingLog?.symptoms?.toSet() ?: emptySet()) }
    var notesText by remember { mutableStateOf(existingLog?.notes ?: "") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPinkBg)
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(
                text = "Log today",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Text(
                text = formattedDate,
                fontSize = 14.sp,
                color = PrimaryPink,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Section 1 — How are you feeling?
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mood_section_card"),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "How are you feeling?",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val moods = listOf(
                        "😔" to "Sad",
                        "😐" to "Neutral",
                        "🙂" to "Happy",
                        "😄" to "Joyful"
                    )

                    moods.forEach { (emoji, label) ->
                        val isSelected = selectedMood == emoji
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) PrimaryPink.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { selectedMood = emoji }
                                .padding(4.dp)
                                .testTag("mood_btn_$emoji"),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                border = if (isSelected) BorderStroke(2.dp, PrimaryPink) else null,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = emoji,
                                        fontSize = 28.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2 — Flow today
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("flow_section_card"),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Flow today",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val flows = listOf("None", "Spotting", "Light", "Medium", "Heavy")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    flows.forEach { flow ->
                        val isSelected = selectedFlow == flow
                        Button(
                            onClick = { selectedFlow = flow },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) PrimaryPink else LightPinkBg,
                                contentColor = if (isSelected) White else DarkText
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("flow_btn_$flow")
                        ) {
                            Text(
                                text = flow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Section 3 — Symptoms
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("symptoms_section_card"),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Symptoms",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val symptoms = listOf(
                    "Cramps", "Headache", "Bloating", "Fatigue", "Acne",
                    "Back pain", "Nausea", "Breast tenderness", "Mood swings", "Insomnia"
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    symptoms.forEach { symptom ->
                        val isSelected = selectedSymptoms.contains(symptom)
                        Surface(
                            onClick = {
                                selectedSymptoms = if (isSelected) {
                                    selectedSymptoms - symptom
                                } else {
                                    selectedSymptoms + symptom
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFFFFF0F5) else LightPinkBg,
                            border = if (isSelected) BorderStroke(1.5.dp, PrimaryPink) else null,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("symptom_chip_$symptom")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = symptom,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) PrimaryPink else DarkText.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4 — Notes
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notes_section_card"),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Notes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = {
                        Text(
                            text = "How are you feeling today? Anything to note...",
                            fontSize = 13.sp,
                            color = DarkText.copy(alpha = 0.4f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("notes_text_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPink,
                        unfocusedBorderColor = LightPinkBg,
                        focusedContainerColor = LightPinkBg.copy(alpha = 0.5f),
                        unfocusedContainerColor = LightPinkBg.copy(alpha = 0.5f),
                        focusedTextColor = DarkText,
                        unfocusedTextColor = DarkText
                    ),
                    textStyle = TextStyle(fontSize = 13.sp)
                )
            }
        }

        // Save Button
        Button(
            onClick = {
                val logData = LogData(
                    mood = selectedMood,
                    flow = selectedFlow,
                    symptoms = selectedSymptoms.toList(),
                    notes = notesText
                )
                storageHelper.saveLog(todayStr, logData)
                Toast.makeText(context, "Log saved!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_log_button")
        ) {
            Text(
                text = "Save log",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
