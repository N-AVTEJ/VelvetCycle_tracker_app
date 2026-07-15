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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LocalAppLanguage
import com.example.constants.Translations
import com.example.ui.theme.LocalVelvetColors
import com.example.utils.LogData
import com.example.utils.StorageHelper
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogScreen(storageHelper: StorageHelper) {
    val context = LocalContext.current
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current

    val today = LocalDate.now()
    val todayStr = today.toString()
    val formattedDate = Translations.formatDate(today, lang)

    // Load existing log
    val existingLog = remember(todayStr) { storageHelper.getLog(todayStr) }

    var selectedMood by remember { mutableStateOf(existingLog?.mood ?: "") }
    var selectedFlow by remember { mutableStateOf(existingLog?.flow ?: "") }
    var selectedSymptoms by remember { mutableStateOf(existingLog?.symptoms?.toSet() ?: emptySet()) }
    var notesText by remember { mutableStateOf(existingLog?.notes ?: "") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(
                text = Translations.t("log_today", lang),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = formattedDate,
                fontSize = 14.sp,
                color = colors.pinkAccent,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Section 1: How are you feeling?
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mood_section_card"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = Translations.t("how_feeling", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
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
                                .background(if (isSelected) colors.pinkAccent.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable { selectedMood = emoji }
                                .padding(4.dp)
                                .testTag("mood_btn_$emoji"),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = androidx.compose.ui.graphics.Color.Transparent,
                                border = if (isSelected) BorderStroke(2.dp, colors.pinkAccent) else null,
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

        // Section 2: Flow intensity today
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("flow_section_card"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = Translations.t("flow_today", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val flows = listOf("None", "Spotting", "Light", "Medium", "Heavy")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    flows.forEach { flow ->
                        val isSelected = selectedFlow == flow
                        val translatedLabel = when (flow.lowercase().trim()) {
                            "spotting" -> Translations.t("flow_spotting", lang)
                            "light" -> Translations.t("flow_light", lang)
                            "medium" -> Translations.t("flow_medium", lang)
                            "heavy" -> Translations.t("flow_heavy", lang)
                            else -> Translations.t("flow_none", lang)
                        }

                        Button(
                            onClick = { selectedFlow = flow },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) colors.pinkAccent else colors.background,
                                contentColor = if (isSelected) colors.cardBackground else colors.textPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("flow_btn_$flow")
                        ) {
                            Text(
                                text = translatedLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Symptoms
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("symptoms_section_card"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = Translations.t("symptoms", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
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
                        val translatedSymptom = when (symptom.lowercase().trim()) {
                            "cramps" -> Translations.t("sym_cramps", lang)
                            "headache" -> Translations.t("sym_headache", lang)
                            "bloating" -> Translations.t("sym_bloating", lang)
                            "acne" -> Translations.t("sym_acne", lang)
                            "fatigue" -> Translations.t("sym_fatigue", lang)
                            "mood swings" -> Translations.t("sym_mood_swings", lang)
                            "back pain" -> Translations.t("sym_backache", lang)
                            "breast tenderness" -> Translations.t("sym_breast_tenderness", lang)
                            "insomnia" -> if (lang == "हिंदी") "अनिद्रा" else if (lang == "తెలుగు") "నిద్రలేమి" else "Insomnia"
                            "nausea" -> Translations.t("sym_nausea", lang)
                            else -> symptom
                        }

                        Surface(
                            onClick = {
                                selectedSymptoms = if (isSelected) {
                                    selectedSymptoms - symptom
                                } else {
                                    selectedSymptoms + symptom
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) colors.pinkAccent.copy(alpha = 0.08f) else colors.background,
                            border = if (isSelected) BorderStroke(1.5.dp, colors.pinkAccent) else null,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("symptom_chip_$symptom")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = translatedSymptom,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) colors.pinkAccent else colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Notes
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notes_section_card"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = Translations.t("notes", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val notesPlaceholder = if (lang == "हिंदी") "आप आज कैसा महसूस कर रहे हैं? कुछ लिखने के लिए..." else if (lang == "తెలుగు") "ఈ రోజు మీ అనుభూతి ఎలా ఉంది? ఏదైనా రాయండి..." else "How are you feeling today? Anything to note..."

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = {
                        Text(
                            text = notesPlaceholder,
                            fontSize = 13.sp,
                            color = colors.textSecondary.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("notes_text_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.pinkAccent,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
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
                Toast.makeText(context, if (lang == "हिंदी") "रिकॉर्ड सहेजा गया!" else if (lang == "తెలుగు") "లాగ్ సేవ్ చేయబడింది!" else "Log saved!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_log_button")
        ) {
            Text(
                text = Translations.t("save_log", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.cardBackground
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
