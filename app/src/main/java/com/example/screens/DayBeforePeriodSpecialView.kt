package com.example.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LocalAppLanguage
import com.example.constants.Translations
import com.example.ui.theme.LocalVelvetColors
import com.example.utils.SecureStore
import com.example.utils.StoreLinks
import java.time.LocalDate

/**
 * Day Before Period special home view replacing normal cards when isDayBeforePeriod is true.
 *
 * Consists of 5 special cards:
 * CARD 1: Alert banner (linear gradient pink #D4537E to #FF8C9E)
 * CARD 2: What to expect (5 symptom items with emojis)
 * CARD 3: Prepare today checklist (6 tappable rows, strikethrough + pink check, persisted in SecureStore 'prepChecklist_YYYY-MM-DD')
 * CARD 4: Pad reminder (Blinkit + Zepto buttons via openStore)
 * CARD 5: Self care tip (7 tips indexed by new Date().getDate() % 7)
 */
@Composable
fun DayBeforePeriodSpecialView(
    modifier: Modifier = Modifier
) {
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current
    val context = LocalContext.current

    val todayStr = remember { LocalDate.now().toString() }
    val checklistStorageKey = remember(todayStr) { "prepChecklist_$todayStr" }

    // CARD 3: Checklist state with SecureStore persistence
    // Indices 0..5
    var checklistState by remember {
        val initialTicks = BooleanArray(6) { false }
        val rawSaved = SecureStore.getSecure(context, checklistStorageKey)
        if (!rawSaved.isNullOrEmpty()) {
            val parts = rawSaved.split(",")
            for (i in 0 until minOf(parts.size, 6)) {
                initialTicks[i] = parts[i] == "1"
            }
        }
        mutableStateOf(initialTicks.toList())
    }

    val toggleItem: (Int) -> Unit = { index ->
        val updated = checklistState.toMutableList()
        updated[index] = !updated[index]
        checklistState = updated
        val encoded = updated.joinToString(",") { if (it) "1" else "0" }
        SecureStore.saveSecure(context, checklistStorageKey, encoded)
    }

    // CARD 5: Tip of the day (date % 7)
    val tipIndex = remember {
        (LocalDate.now().dayOfMonth % 7)
    }

    val selfCareTips = remember(lang) {
        listOf(
            Translations.t("tip_0", lang),
            Translations.t("tip_1", lang),
            Translations.t("tip_2", lang),
            Translations.t("tip_3", lang),
            Translations.t("tip_4", lang),
            Translations.t("tip_5", lang),
            Translations.t("tip_6", lang)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("day_before_period_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- CARD 1: Alert banner (Gradient Pink #D4537E to #FF8C9E) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFD4537E), Color(0xFFFF8C9E))
                    )
                )
                .padding(22.dp)
                .testTag("period_tomorrow_alert_card")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = Translations.t("period_tomorrow_title", lang),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = Translations.t("period_tomorrow_sub", lang),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.92f),
                    lineHeight = 20.sp
                )
            }
        }

        // --- CARD 2: What to expect ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("what_to_expect_card"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = Translations.t("what_to_expect", lang),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                val expectItems = listOf(
                    Translations.t("wte_1", lang),
                    Translations.t("wte_2", lang),
                    Translations.t("wte_3", lang),
                    Translations.t("wte_4", lang),
                    Translations.t("wte_5", lang)
                )

                expectItems.forEach { item ->
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        color = colors.textPrimary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // --- CARD 3: Prepare today checklist ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("prepare_today_card"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = Translations.t("prepare_today", lang),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                val checklistLabels = listOf(
                    Translations.t("prep_1", lang),
                    Translations.t("prep_2", lang),
                    Translations.t("prep_3", lang),
                    Translations.t("prep_4", lang),
                    Translations.t("prep_5", lang),
                    Translations.t("prep_6", lang)
                )

                checklistLabels.forEachIndexed { index, label ->
                    val isChecked = checklistState.getOrElse(index) { false }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { toggleItem(index) }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isChecked) "✓" else "☐",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isChecked) colors.pinkAccent else colors.textSecondary,
                            modifier = Modifier.width(28.dp)
                        )
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            color = if (isChecked) colors.textSecondary else colors.textPrimary,
                            textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // --- CARD 4: Pad reminder ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("day_before_pad_reminder_card"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = Translations.t("get_pads_now_sub", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    lineHeight = 20.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val blinkitLabel = if (lang == "हिंदी") "ब्लिंकिट" else if (lang == "తెలుగు") "బ్లింకిట్" else "Blinkit"
                    val zeptoLabel = if (lang == "हिंदी") "ज़ेप्टो" else if (lang == "తెలుగు") "జెప్టో" else "Zepto"

                    Button(
                        onClick = { StoreLinks.openStore(context, "blinkit") },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pad_order_blinkit_btn")
                    ) {
                        Text(
                            text = blinkitLabel,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { StoreLinks.openStore(context, "zepto") },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pad_order_zepto_btn")
                    ) {
                        Text(
                            text = zeptoLabel,
                            fontWeight = FontWeight.Bold,
                            color = colors.cardBackground,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // --- CARD 5: Self care tip ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tonight_tip_card"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = Translations.t("tonight_tip", lang),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.pinkAccent
                )

                Text(
                    text = selfCareTips.getOrElse(tipIndex) { selfCareTips[0] },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
