package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.StorageHelper
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun CalendarScreen(
    storageHelper: StorageHelper
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    val lastPeriodStart = storageHelper.lastPeriodStart
    val cycleLength = storageHelper.cycleLength
    val periodDuration = storageHelper.periodDuration

    val today = LocalDate.now()
    val monthYearFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    val detailsDateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy") }

    // Calendar grid calculations
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    
    // DayOfWeek: 1 = Monday, 7 = Sunday.
    // Shift is the number of empty slots needed at the start of the grid (using Monday-start).
    val startShift = firstDayOfMonth.dayOfWeek.value - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPinkBg)
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
                contentDescription = "Calendar Icon",
                tint = PrimaryPink,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Cycle Calendar",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }

        // Calendar Grid Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calendar_grid_card"),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Month Selector Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = PrimaryPink)
                    }
                    Text(
                        text = currentMonth.format(monthYearFormatter),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month", tint = PrimaryPink)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weekday Row
                val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekdays.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrayText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days Grid (Using standard Column + Row layout for maximum reliability and exact cell sizing)
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
                                val isToday = date == today
                                val isSelected = date == selectedDate

                                // Cell background color
                                val cellBgColor = when {
                                    isPeriod -> SecondaryPink // Pink background (#F4C0D1)
                                    isFertile -> FertileGreen // Green background (#C8EEDD)
                                    else -> White
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
                                            color = if (isToday) DarkText else if (isSelected) PrimaryPink.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent,
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
                                            color = if (isPeriod && isSelected) PrimaryPink else DarkText
                                        )
                                        
                                        // Ovulation dot
                                        if (isOvulation) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(Teal) // Teal dot (#4ECDC4)
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Empty spacer cell for offset padding
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }

        // Details Panel Card
        selectedDate?.let { date ->
            val isPeriod = isPeriodDay(date, lastPeriodStart, cycleLength, periodDuration)
            val isFertile = isFertileDay(date, lastPeriodStart, cycleLength)
            val isOvulation = isOvulationDay(date, lastPeriodStart, cycleLength)

            val statusTitle = when {
                isPeriod -> "Period Day"
                isOvulation -> "Ovulation Day"
                isFertile -> "Fertility Window"
                else -> "Regular Day"
            }

            val statusColor = when {
                isPeriod -> PrimaryPink
                isOvulation -> Teal
                isFertile -> DarkText
                else -> GrayText
            }

            val statusDescription = when {
                isPeriod -> "Expected menstrual phase. Rest and hydrate well."
                isOvulation -> "Egg release peak! Conception chances are very high."
                isFertile -> "Favorable time for conception. General wellness."
                else -> "Low chances of conception. Normal cycle day."
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("date_details_card"),
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
                        text = date.format(detailsDateFormatter),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GrayText
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
                                        isPeriod -> PrimaryPink
                                        isOvulation -> Teal
                                        isFertile -> FertileGreen
                                        else -> BorderColor
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
                        color = DarkText.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// Projection-safe functions to determine status for any date
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
