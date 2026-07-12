package com.example.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.utils.CycleEngine
import com.example.utils.StorageHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.example.constants.PhaseGuide
import com.example.constants.PhaseGuideData
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Restaurant

@Composable
fun HomeScreen(
    storageHelper: StorageHelper
) {
    val uriHandler = LocalUriHandler.current
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    // Load data from storage helper
    val lastPeriodStart = storageHelper.lastPeriodStart
    val cycleLength = storageHelper.cycleLength
    val periodDuration = storageHelper.periodDuration
    val userName = storageHelper.userName

    val today = LocalDate.now()

    // Calculate values using CycleEngine
    val currentCycleDay = CycleEngine.getCurrentCycleDay(lastPeriodStart, cycleLength, today)
    val currentPhase = CycleEngine.getCurrentPhase(lastPeriodStart, cycleLength, periodDuration, today)
    val daysUntilNextPeriod = CycleEngine.getDaysUntilNextPeriod(lastPeriodStart, cycleLength, today)

    // Fertile Window Calculations
    val currentCycleStart = lastPeriodStart.plusDays(((ChronoUnitDaysBetween(lastPeriodStart, today) / cycleLength) * cycleLength))
    val ovulationDate = CycleEngine.getOvulationDay(currentCycleStart, cycleLength)
    val fertileWindowStart = ovulationDate.minusDays(5)
    val fertileWindowEnd = ovulationDate.plusDays(1)

    // Check if fertile window is active, upcoming, or passed
    val isFertileActive = !today.isBefore(fertileWindowStart) && !today.isAfter(fertileWindowEnd)
    val isFertileUpcoming = today.isBefore(fertileWindowStart)

    // Description text for different phases
    val phaseTitle = currentPhase.replaceFirstChar { it.uppercase() }
    val phaseDescription = when (currentPhase) {
        "menstruation" -> "Your body is shedding the uterine lining. Rest and nurture yourself."
        "follicular" -> "Estrogen levels are rising. You may feel more energetic and focused."
        "ovulation" -> "Estrogen peaks and an egg is released. Your fertility is at its highest."
        "luteal" -> "Progesterone dominates. It is time to wind down and practice self-care."
        else -> ""
    }

    // Get current Phase Guide data
    val guide = PhaseGuide.getGuide(currentPhase)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPinkBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, $userName",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                    fontSize = 14.sp,
                    color = DarkText.copy(alpha = 0.5f)
                )
            }
        }

        // Pad Reminder Banner (Phase 3 Requirement 4)
        if (daysUntilNextPeriod == 2) {
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            val padStore = storageHelper.padStore
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pad_reminder_banner"),
                colors = CardDefaults.cardColors(containerColor = PrimaryPink.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryPink.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🛍️", fontSize = 24.sp)
                        Column {
                            Text(
                                text = "Stock up on pads",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPink
                            )
                            Text(
                                text = "Your period is starting in 2 days. Be prepared!",
                                fontSize = 12.sp,
                                color = DarkText.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val showBlinkit = padStore.isEmpty() || padStore == "Blinkit"
                        val showAmazon = padStore.isEmpty() || padStore == "Amazon India"
                        
                        if (showBlinkit) {
                            Button(
                                onClick = { uriHandler.openUri("https://blinkit.com") },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("home_blinkit_btn")
                            ) {
                                Text("Blinkit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        
                        if (showAmazon) {
                            Button(
                                onClick = { uriHandler.openUri("https://www.amazon.in/s?k=sanitary+pads") },
                                colors = ButtonDefaults.buttonColors(containerColor = if (padStore.isEmpty()) DarkText.copy(alpha = 0.8f) else PrimaryPink),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("home_amazon_btn")
                            ) {
                                Text("Amazon India", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- Card 1 — Cycle status ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cycle_status_card"),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = "Cycle Status",
                        tint = PrimaryPink,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Cycle Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPink
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Day $currentCycleDay of $cycleLength",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryPink.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = phaseTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPink
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = phaseDescription,
                    fontSize = 14.sp,
                    color = DarkText.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        }

        // --- Card 2 — Next period prediction ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("next_period_card"),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Next Period",
                        tint = PrimaryPink,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Next Period",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPink
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val predictionText = when {
                    daysUntilNextPeriod > 0 -> "Period in $daysUntilNextPeriod days"
                    daysUntilNextPeriod == 0 -> "Period expected today"
                    else -> "Period is late by ${-daysUntilNextPeriod} days"
                }

                Text(
                    text = predictionText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                val nextPeriodDate = CycleEngine.getNextPeriodDate(lastPeriodStart, cycleLength)
                Text(
                    text = "Expected Start: ${nextPeriodDate.format(dateFormatter)}",
                    fontSize = 14.sp,
                    color = DarkText.copy(alpha = 0.6f)
                )
            }
        }

        // --- Card 3 — Fertile window ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fertile_window_card"),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Favorite,
                        contentDescription = "Fertile Window",
                        tint = PrimaryPink,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Fertility Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPink
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isFertileActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FertileGreen)
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Fertile Window is ACTIVE",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "High chance of conception today.",
                                fontSize = 13.sp,
                                color = DarkText.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else if (isFertileUpcoming) {
                    Column {
                        Text(
                            text = "Fertile window is upcoming",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Starts on ${fertileWindowStart.format(dateFormatter)}",
                            fontSize = 14.sp,
                            color = DarkText.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = "Fertile window has passed",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Active period of this cycle concluded.",
                            fontSize = 14.sp,
                            color = DarkText.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ovulation specific prediction
                Text(
                    text = "Predicted Ovulation: ${ovulationDate.format(dateFormatter)}",
                    fontSize = 12.sp,
                    color = Teal,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Card 4 — Pad reminder banner (ONLY show if period is 2 days away) ---
        AnimatedVisibility(
            visible = daysUntilNextPeriod == 2,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pad_reminder_banner"),
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBag,
                            contentDescription = "Reminders",
                            tint = PrimaryPink,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Smart Reminder",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPink
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Your period is expected in 2 days. Stock up on pads.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkText,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { uriHandler.openUri("https://blinkit.com") },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Blinkit", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { uriHandler.openUri("https://amazon.in/s?k=sanitary+pads") },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkText),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Amazon", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Card 5 — Today's Guide ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("todays_guide_card"),
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Today's Guide",
                        tint = PrimaryPink,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Today's Guide",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPink
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: Body info
                Text(
                    text = "BODY AWARENESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryPink.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = guide.body,
                    fontSize = 14.sp,
                    color = DarkText,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = LightPinkBg, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 2: Nutrition Guide
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Restaurant,
                        contentDescription = "Nutrition",
                        tint = PrimaryPink,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "NUTRITION PLAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPink.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Morning
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LightMode,
                        contentDescription = "Morning",
                        tint = Teal,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Morning", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                        Text(guide.foodMorning, fontSize = 13.sp, color = DarkText.copy(alpha = 0.7f), lineHeight = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Afternoon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WbSunny,
                        contentDescription = "Afternoon",
                        tint = Teal,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Afternoon", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                        Text(guide.foodAfternoon, fontSize = 13.sp, color = DarkText.copy(alpha = 0.7f), lineHeight = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Evening
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DarkMode,
                        contentDescription = "Evening",
                        tint = Teal,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Evening", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
                        Text(guide.foodEvening, fontSize = 13.sp, color = DarkText.copy(alpha = 0.7f), lineHeight = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = LightPinkBg, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 3: Exercise Guide
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = "Exercise",
                        tint = PrimaryPink,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "WORKOUT ADVICE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPink.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = guide.exercise,
                    fontSize = 13.sp,
                    color = DarkText.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// Utility function to calculate days between two dates
private fun ChronoUnitDaysBetween(start: LocalDate, end: LocalDate): Long {
    return java.time.temporal.ChronoUnit.DAYS.between(start, end)
}
