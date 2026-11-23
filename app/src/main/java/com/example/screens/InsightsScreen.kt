package com.example.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
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
import com.example.LocalAppLanguage
import com.example.constants.Translations
import com.example.ui.theme.LocalVelvetColors
import com.example.ui.theme.PrimaryPink
import com.example.utils.InsightsEngine
import com.example.utils.PdfExportHelper
import com.example.utils.StorageHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun InsightsScreen(
    storageHelper: StorageHelper,
    onNavigateToLog: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current
    val scrollState = rememberScrollState()

    // Retrieve all logs and cycle history
    val logs = storageHelper.getAllLogs()
    val cycleHistory = storageHelper.getCycleHistory()

    // Determine if we have enough data (at least 1 logged day with symptoms or flow)
    val hasData = logs.isNotEmpty()

    if (!hasData) {
        // --- EMPTY STATE ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🌸",
                fontSize = 80.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = Translations.t("no_insights_title", lang),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = Translations.t("no_insights_subtitle", lang),
                fontSize = 14.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(
                onClick = onNavigateToLog,
                colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
                    .testTag("navigate_to_log_btn")
            ) {
                Text(
                    text = Translations.t("log_today", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    } else {
        // --- DATA STATE ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = Translations.t("most_common_symptoms", lang).replace("Most common symptoms", "Insights").replace("सबसे आम लक्षण", "आंकड़े").replace("సాధారణంగా కనిపించే లక్షణాలు", "అంతర్దృష్టులు"),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Calculate Metrics
            val avgCycle = if (cycleHistory.isNotEmpty()) {
                cycleHistory.map { it.duration }.average().toInt()
            } else {
                storageHelper.cycleLength
            }
            val avgPeriod = storageHelper.periodDuration
            val totalTracked = cycleHistory.size
            val lastPeriodFormatted = Translations.formatDate(storageHelper.lastPeriodStart, lang)

            // Aggregate symptoms once for both Doctor Flag checklist and Frequency chart
            val symptomsMap = remember(logs) {
                val map = mutableMapOf<String, Int>()
                logs.values.forEach { log ->
                    log.symptoms.forEach { s ->
                        map[s] = (map[s] ?: 0) + 1
                    }
                }
                map
            }
            val topSymptoms = remember(symptomsMap) {
                symptomsMap.entries.sortedByDescending { it.value }.take(5)
            }
            val lastThreePeriodDates = remember(cycleHistory) {
                cycleHistory.take(3).map { it.startDate }
            }

            // Phase 20: Symptom-to-Doctor Flag (strictly local, >=3 cycles required, 30-day SecureStore dismissal)
            val doctorFlag = remember(logs, totalTracked) {
                InsightsEngine.checkDoctorFlag(logs, totalTracked)
            }
            var isDoctorCardDismissed by remember {
                mutableStateOf(
                    InsightsEngine.isDoctorFlagDismissed(storageHelper.doctorFlagDismissedUntil)
                )
            }

            AnimatedVisibility(
                visible = doctorFlag?.shouldFlag == true && !isDoctorCardDismissed,
                exit = shrinkVertically() + fadeOut()
            ) {
                DoctorFlagSupportCard(
                    avgCycle = avgCycle,
                    avgPeriod = avgPeriod,
                    topSymptoms = topSymptoms.map { it.key to it.value },
                    lastThreePeriodDates = lastThreePeriodDates,
                    lang = lang,
                    onExportSummary = {
                        val summaryText = InsightsEngine.buildDoctorSummaryText(
                            avgCycleLength = avgCycle,
                            avgPeriodDuration = avgPeriod,
                            topSymptoms = topSymptoms.map { it.key to it.value },
                            lastThreePeriodDates = lastThreePeriodDates
                        )
                        InsightsEngine.shareDoctorSummary(context, summaryText)
                    },
                    onDismiss = {
                        storageHelper.dismissDoctorFlagFor30Days()
                        isDoctorCardDismissed = true
                    }
                )
            }

            // Section 1: Stats Grid (2x2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        title = Translations.t("avg_cycle_length", lang),
                        value = "$avgCycle",
                        unit = "days",
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = Translations.t("avg_period_duration", lang),
                        value = "$avgPeriod",
                        unit = "days",
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        title = Translations.t("cycles_tracked", lang),
                        value = "$totalTracked",
                        unit = "cycles",
                        icon = Icons.Default.BarChart,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = Translations.t("last_period_date", lang),
                        value = lastPeriodFormatted,
                        unit = "",
                        icon = Icons.Default.CalendarToday,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section 2: Symptom Frequency Horizontal Bar Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Translations.t("most_common_symptoms", lang),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.pinkAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val totalLogs = logs.size.coerceAtLeast(1)

                    if (topSymptoms.isEmpty()) {
                        Text(
                            text = "No symptoms logged yet.",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        val maxCount = topSymptoms.first().value.coerceAtLeast(1)
                        topSymptoms.forEach { entry ->
                            val translatedSymptom = getTranslatedSymptom(entry.key, lang)
                            val percentage = (entry.value * 100) / totalLogs
                            
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = translatedSymptom,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "$percentage%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.pinkAccent
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // Custom Horizontal Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(colors.pinkAccent.copy(alpha = 0.1f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = (entry.value.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f))
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.pinkAccent)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Cycle History List
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val cycleLabel = if (lang == "हिंदी") "चक्र इतिहास" else if (lang == "తెలుగు") "చక్రాల చరిత్ర" else "Cycle History"
                    Text(
                        text = cycleLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.pinkAccent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    cycleHistory.forEachIndexed { index, record ->
                        val startFormatted = Translations.formatDate(record.startDate, lang)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val cycleNoLabel = if (lang == "हिंदी") "चक्र ${record.cycleNo}" else if (lang == "తెలుగు") "చక్రం ${record.cycleNo}" else "Cycle ${record.cycleNo}"
                                Text(
                                    text = cycleNoLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = startFormatted,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Text(
                                text = "${record.duration} " + (if (lang == "हिंदी") "दिन" else if (lang == "తెలుగు") "రోజులు" else "days"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.pinkAccent
                            )
                        }
                        if (index < cycleHistory.size - 1) {
                            HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                        }
                    }
                }
            }

            // Section 4: Export PDF Button
            Button(
                onClick = {
                    PdfExportHelper.exportReport(context, storageHelper)
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("export_pdf_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = colors.cardBackground,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = Translations.t("export_pdf_report", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.cardBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = LocalVelvetColors.current
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.pinkAccent.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DoctorFlagSupportCard(
    avgCycle: Int,
    avgPeriod: Int,
    topSymptoms: List<Pair<String, Int>>,
    lastThreePeriodDates: List<LocalDate>,
    lang: String,
    onExportSummary: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalVelvetColors.current
    val isDark = colors.background == Color(0xFF0D0D0D)

    val cardContainer = if (isDark) Color(0xFF231D20) else Color(0xFFFFF8F5)
    val cardBorder = colors.pinkAccent.copy(alpha = if (isDark) 0.45f else 0.3f)
    val checklistContainer = if (isDark) Color(0xFF171416) else Color(0xFFFFFFFF)
    val daysUnit = if (lang == "हिंदी") "दिन" else if (lang == "తెలుగు") "రోజులు" else "days"

    val formattedSymptoms = if (topSymptoms.isEmpty()) {
        Translations.t("doctor_flag_none_logged", lang)
    } else {
        topSymptoms.take(4).joinToString(", ") { (sym, _) -> getTranslatedSymptom(sym, lang) }
    }

    val formattedPeriodDates = if (lastThreePeriodDates.isEmpty()) {
        Translations.t("doctor_flag_none_logged", lang)
    } else {
        lastThreePeriodDates.take(3).joinToString(", ") { Translations.formatDate(it, lang) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doctor_flag_card"),
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Supportive Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.pinkAccent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = Translations.t("doctor_flag_title", lang),
                        tint = colors.pinkAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = Translations.t("doctor_flag_title", lang),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Supportive, non-alarming body text
            Text(
                text = Translations.t("doctor_flag_body", lang),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = colors.textSecondary
            )

            // Auto-generated Appointment Checklist
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("doctor_flag_checklist"),
                shape = RoundedCornerShape(12.dp),
                color = checklistContainer,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = Translations.t("doctor_flag_checklist_title", lang),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.pinkAccent
                    )

                    DoctorChecklistRow(
                        label = Translations.t("avg_cycle_length", lang),
                        value = "$avgCycle $daysUnit"
                    )
                    DoctorChecklistRow(
                        label = Translations.t("avg_period_duration", lang),
                        value = "$avgPeriod $daysUnit"
                    )
                    DoctorChecklistRow(
                        label = Translations.t("doctor_flag_top_symptoms", lang),
                        value = formattedSymptoms
                    )
                    DoctorChecklistRow(
                        label = Translations.t("doctor_flag_last_3_periods", lang),
                        value = formattedPeriodDates
                    )
                }
            }

            // Actions: Export Summary + Not Now (dismiss for 30 days)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onExportSummary,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.pinkAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("doctor_flag_export_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Translations.t("doctor_flag_export_btn", lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("doctor_flag_dismiss_btn")
                ) {
                    Text(
                        text = Translations.t("doctor_flag_dismiss_btn", lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DoctorChecklistRow(label: String, value: String) {
    val colors = LocalVelvetColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = colors.pinkAccent.copy(alpha = 0.75f),
            modifier = Modifier
                .padding(top = 2.dp)
                .size(15.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                lineHeight = 18.sp
            )
        }
    }
}

private fun getTranslatedSymptom(symptom: String, language: String): String {
    return when (symptom.lowercase().trim()) {
        "cramps" -> Translations.t("sym_cramps", language)
        "headache" -> Translations.t("sym_headache", language)
        "bloating" -> Translations.t("sym_bloating", language)
        "acne" -> Translations.t("sym_acne", language)
        "fatigue" -> Translations.t("sym_fatigue", language)
        "mood swings" -> Translations.t("sym_mood_swings", language)
        "backache", "back pain" -> Translations.t("sym_backache", language)
        "breast tenderness" -> Translations.t("sym_breast_tenderness", language)
        "cravings" -> Translations.t("sym_cravings", language)
        "nausea" -> Translations.t("sym_nausea", language)
        "heavy bleeding" -> Translations.t("sym_heavy_bleeding", language)
        "severe cramps" -> Translations.t("sym_severe_cramps", language)
        "severe pain" -> Translations.t("sym_severe_pain", language)
        else -> symptom
    }
}
