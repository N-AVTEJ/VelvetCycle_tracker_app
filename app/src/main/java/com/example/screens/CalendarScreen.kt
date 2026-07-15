package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LocalAppLanguage
import com.example.constants.Translations
import com.example.ui.theme.FertileGreen
import com.example.ui.theme.LocalVelvetColors
import com.example.ui.theme.SecondaryPink
import com.example.ui.theme.Teal
import com.example.utils.StorageHelper
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    storageHelper: StorageHelper
) {
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current
    
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    val lastPeriodStart = storageHelper.lastPeriodStart
    val cycleLength = storageHelper.cycleLength
    val periodDuration = storageHelper.periodDuration

    val today = LocalDate.now()

    // Header values
    val calendarHeaderLabel = if (lang == "हिंदी") "चक्र कैलेंडर" else if (lang == "తెలుగు") "చక్ర క్యాలెండర్" else "Cycle Calendar"

    // Formatters
    val monthYearText = if (lang == "हिंदी" || lang == "తెలుగు") {
        val monthName = Translations.getMonthName(currentMonth.monthValue, lang)
        "$monthName ${currentMonth.year}"
    } else {
        currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    // Grid details
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val startShift = firstDayOfMonth.dayOfWeek.value - 1 // Monday-start shifts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = colors.pinkAccent,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = calendarHeaderLabel,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        // Calendar Grid Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calendar_grid_card"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = colors.pinkAccent)
                    }
                    Text(
                        text = monthYearText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month", tint = colors.pinkAccent)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weekday initials
                val weekdays = when (lang) {
                    "हिंदी" -> listOf("सोम", "मं", "बुध", "गु", "शु", "श", "र")
                    "తెలుగు" -> listOf("సోమ", "మం", "బుధ", "గురు", "శుక్ర", "శని", "ఆది")
                    else -> listOf("M", "T", "W", "T", "F", "S", "S")
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekdays.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days Grid
                val totalSlots = startShift + daysInMonth
                val rows = (totalSlots + 6) / 7

                for (rowIndex in 0 until rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (colIndex in 0 until 7) {
                            val slotIndex = rowIndex * 7 + colIndex
                            val dayOfMonth = slotIndex - startShift + 1

                            if (dayOfMonth in 1..daysInMonth) {
                                val date = currentMonth.atDay(dayOfMonth)
                                
                                val isPeriod = isPeriodDay(date, lastPeriodStart, cycleLength, periodDuration)
                                val isFertile = isFertileDay(date, lastPeriodStart, cycleLength)
                                val isOvulation = isOvulationDay(date, lastPeriodStart, cycleLength)
                                val isPrepare = isPrepareDay(date, lastPeriodStart, cycleLength)
                                val isToday = date == today
                                val isSelected = date == selectedDate

                                val cellBgColor = when {
                                    isPeriod -> SecondaryPink
                                    isFertile -> FertileGreen
                                    else -> colors.cardBackground
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(cellBgColor)
                                        .border(
                                            width = if (isToday) 2.dp else if (isSelected) 1.dp else 0.dp,
                                            color = if (isToday) colors.textPrimary else if (isSelected) colors.pinkAccent.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedDate = date },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayOfMonth.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isPeriod && isSelected) colors.pinkAccent else colors.textPrimary
                                        )
                                        
                                        if (isOvulation) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(Teal)
                                            )
                                        } else if (isPrepare) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFFA726))
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }

        // Selected Day Details Panel
        selectedDate?.let { date ->
            val isPeriod = isPeriodDay(date, lastPeriodStart, cycleLength, periodDuration)
            val isFertile = isFertileDay(date, lastPeriodStart, cycleLength)
            val isOvulation = isOvulationDay(date, lastPeriodStart, cycleLength)

            val statusTitle = when {
                isPeriod -> if (lang == "हिंदी") "मासिक धर्म का दिन" else if (lang == "తెలుగు") "పీరియడ్ రోజు" else "Period Day"
                isOvulation -> if (lang == "हिंदी") "डिंबोत्सर्जन दिवस" else if (lang == "తెలుగు") "అండవిడుదల రోజు" else "Ovulation Day"
                isFertile -> if (lang == "हिंदी") "उर्वर अवधि (Fertility)" else if (lang == "తెలుగు") "సంతానోత్పత్తి సమయం" else "Fertile Window"
                else -> if (lang == "हिंदी") "सामान्य दिन" else if (lang == "తెలుగు") "సాధారణ రోజు" else "Regular Day"
            }

            val statusColor = when {
                isPeriod -> colors.pinkAccent
                isOvulation -> Teal
                isFertile -> colors.textPrimary
                else -> colors.textSecondary
            }

            val statusDescription = when {
                isPeriod -> if (lang == "हिंदी") "मासिक धर्म चरण। पर्याप्त विश्राम और गर्म पानी पिएं।" else if (lang == "తెలుగు") "పీరియడ్ సమయం. తగినంత విశ్రాంతి తీసుకోండి." else "Expected menstrual phase. Rest and hydrate well."
                isOvulation -> if (lang == "हिंदी") "अंडा जारी होने का चरम! गर्भधारण की संभावना सबसे अधिक।" else if (lang == "తెలుగు") "అండవిడుదల సమయం! గర్భం దాల్చడానికి ఎక్కువ అవకాశాలు ఉన్నాయి." else "Egg release peak! Conception chances are very high."
                isFertile -> if (lang == "हिंदी") "गर्भधारण के लिए अनुकूल समय। स्वस्थ रहें।" else if (lang == "తెలుగు") "గర్భం దాల్చడానికి అనుకూల సమయం. ఆరోగ్యం పట్ల శ్రద్ధ వహించండి." else "Favorable time for conception. General wellness."
                else -> if (lang == "हिंदी") "गर्भधारण की कम संभावना। सामान्य चक्र दिवस।" else if (lang == "తెలుగు") "గర్భం దాల్చడానికి తక్కువ అవకాశాలు ఉన్నాయి. సాధారణ చక్రం రోజు." else "Low chances of conception. Normal cycle day."
            }

            val log = storageHelper.getLog(date.toString())

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("date_details_card"),
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
                        text = Translations.formatDate(date, lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isPeriod -> colors.pinkAccent
                                        isPrepare -> Color(0xFFFFA726)
                                        isOvulation -> Teal
                                        isFertile -> FertileGreen
                                        else -> colors.border
                                    }
                                )
                        )
                        Text(
                            text = statusTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = statusDescription,
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = colors.border, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (log != null) {
                        Text(
                            text = Translations.t("log_today", lang),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.pinkAccent,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (log.mood.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.background)
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(log.mood, fontSize = 28.sp)
                                }
                            }
                            
                            Column {
                                Text(
                                    text = Translations.t("flow_today", lang),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary
                                )
                                val flowLabel = when (log.flow.lowercase().trim()) {
                                    "spotting" -> Translations.t("flow_spotting", lang)
                                    "light" -> Translations.t("flow_light", lang)
                                    "medium" -> Translations.t("flow_medium", lang)
                                    "heavy" -> Translations.t("flow_heavy", lang)
                                    else -> Translations.t("flow_none", lang)
                                }
                                Text(
                                    text = flowLabel,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            }
                        }
                        
                        if (log.symptoms.isNotEmpty()) {
                            Text(
                                text = Translations.t("symptoms", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                log.symptoms.forEach { symptom ->
                                    val translatedSymptom = when (symptom.lowercase().trim()) {
                                        "cramps" -> Translations.t("sym_cramps", lang)
                                        "headache" -> Translations.t("sym_headache", lang)
                                        "bloating" -> Translations.t("sym_bloating", lang)
                                        "acne" -> Translations.t("sym_acne", lang)
                                        "fatigue" -> Translations.t("sym_fatigue", lang)
                                        "mood swings" -> Translations.t("sym_mood_swings", lang)
                                        "backache" -> Translations.t("sym_backache", lang)
                                        "breast tenderness" -> Translations.t("sym_breast_tenderness", lang)
                                        "cravings" -> Translations.t("sym_cravings", lang)
                                        "nausea" -> Translations.t("sym_nausea", lang)
                                        else -> symptom
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.pinkAccent.copy(alpha = 0.08f))
                                            .border(1.dp, colors.pinkAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(translatedSymptom, fontSize = 11.sp, color = colors.pinkAccent, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                        
                        if (log.notes.isNotEmpty()) {
                            Text(
                                text = Translations.t("notes", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = log.notes,
                                fontSize = 13.sp,
                                color = colors.textPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        Text(
                            text = if (lang == "हिंदी") "इस दिन के लिए कोई रिकॉर्ड नहीं है।" else if (lang == "తెలుగు") "ఈ రోజు కోసం లాగ్స్ లేవు." else "No log for this day.",
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.testTag("no_log_text")
                        )
                    }
                }
            }
        }
    }
}

private fun isPeriodDay(date: LocalDate, lastPeriodStart: LocalDate, cycleLength: Int, periodDuration: Int): Boolean {
    val daysBetween = ChronoUnit.DAYS.between(lastPeriodStart, date)
    val mod = if (daysBetween >= 0) {
        daysBetween % cycleLength
    } else {
        ((daysBetween % cycleLength) + cycleLength) % cycleLength
    }
    return mod < periodDuration
}

private fun isOvulationDay(date: LocalDate, lastPeriodStart: LocalDate, cycleLength: Int): Boolean {
    val daysBetween = ChronoUnit.DAYS.between(lastPeriodStart, date)
    val mod = if (daysBetween >= 0) {
        daysBetween % cycleLength
    } else {
        ((daysBetween % cycleLength) + cycleLength) % cycleLength
    }
    val ovulationDayNum = cycleLength - 14 - 1
    return mod == ovulationDayNum.toLong()
}

private fun isFertileDay(date: LocalDate, lastPeriodStart: LocalDate, cycleLength: Int): Boolean {
    val daysBetween = ChronoUnit.DAYS.between(lastPeriodStart, date)
    val mod = if (daysBetween >= 0) {
        daysBetween % cycleLength
    } else {
        ((daysBetween % cycleLength) + cycleLength) % cycleLength
    }
    val ovulationDayNum = cycleLength - 14 - 1
    val fertileStart = ovulationDayNum - 5
    val fertileEnd = ovulationDayNum + 1
    return mod in fertileStart..fertileEnd
}

private fun isPrepareDay(date: LocalDate, lastPeriodStart: LocalDate, cycleLength: Int): Boolean {
    val daysBetween = ChronoUnit.DAYS.between(lastPeriodStart, date)
    val mod = if (daysBetween >= 0) {
        daysBetween % cycleLength
    } else {
        ((daysBetween % cycleLength) + cycleLength) % cycleLength
    }
    return mod == (cycleLength - 1).toLong()
}

@Composable
private fun LegendItem(color: Color, label: String, isDot: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (isDot) 8.dp else 12.dp)
                .clip(if (isDot) CircleShape else RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = LocalVelvetColors.current.textSecondary
        )
    }
}
