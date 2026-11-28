package com.example.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LocalAppLanguage
import com.example.constants.Translations
import com.example.ui.theme.LocalVelvetColors
import com.example.utils.ComfortModeHelper
import com.example.utils.LogData
import com.example.utils.StorageHelper
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * Toggle bar shown on HomeScreen whenever isDayBeforePeriod() OR currentPhase == "menstruation".
 * Accessible with minimum 56.dp height and clear TalkBack semantics.
 */
@Composable
fun ComfortModeToggleBar(
    isComfortModeOn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current
    val toggleLabel = Translations.t("comfort_mode_toggle", lang)
    val subtitle = Translations.t("comfort_mode_subtitle", lang)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("comfort_mode_toggle_card"),
        colors = CardDefaults.cardColors(
            containerColor = if (isComfortModeOn) colors.comfortCardBackground else colors.cardBackground
        ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (isComfortModeOn) 1.5.dp else 1.dp,
            color = if (isComfortModeOn) colors.pinkAccent.copy(alpha = 0.6f) else colors.border
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isComfortModeOn) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(
                    role = Role.Switch,
                    onClickLabel = toggleLabel
                ) { onToggle(!isComfortModeOn) }
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = toggleLabel,
                    fontSize = if (isComfortModeOn) 18.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = if (isComfortModeOn) 16.sp else 13.sp,
                    color = colors.textSecondary,
                    lineHeight = if (isComfortModeOn) 22.sp else 18.sp
                )
            }
            Switch(
                checked = isComfortModeOn,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.pinkAccent,
                    uncheckedThumbColor = colors.textSecondary,
                    uncheckedTrackColor = colors.border
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .semantics { contentDescription = toggleLabel }
                    .testTag("comfort_mode_switch")
            )
        }
    }
}

/**
 * Simplified Comfort Mode Home Layout for period days & day before period.
 * - Soft dimmed warm pink cards (adapts cleanly to dark mode #3A2430 palette via LocalVelvetColors)
 * - 24.dp vertical spacing, 22.sp headers, 16.sp body copy
 * - Only 3 essentials:
 *   1) Large mood check-in (72.dp emoji buttons)
 *   2) Quick flow log (full-width stacked pills, min 56.dp height)
 *   3) One rotating comfort tip card
 */
@Composable
fun ComfortModeView(
    storageHelper: StorageHelper,
    today: LocalDate = LocalDate.now(),
    modifier: Modifier = Modifier
) {
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current
    val todayStr = today.toString()

    // Load and persist directly into today's encrypted daily log (syncs seamlessly with LogScreen)
    val initialLog = remember(todayStr) { storageHelper.getLog(todayStr) }
    var selectedMood by remember(todayStr) { mutableStateOf(initialLog?.mood ?: "") }
    var selectedFlow by remember(todayStr) { mutableStateOf(initialLog?.flow ?: "") }
    var moodSaved by remember(todayStr) { mutableStateOf(false) }
    var flowSaved by remember(todayStr) { mutableStateOf(false) }

    val persistQuickLog: (String, String) -> Unit = { mood, flow ->
        val latest = storageHelper.getLog(todayStr)
        storageHelper.saveLog(
            todayStr,
            LogData(
                mood = mood,
                flow = flow,
                symptoms = latest?.symptoms ?: emptyList(),
                notes = latest?.notes ?: ""
            )
        )
    }

    // Rotating comfort tip card state
    val comfortTips = remember(lang) { ComfortModeHelper.getComfortTips(lang) }
    var tipIndex by remember(todayStr) { mutableIntStateOf(ComfortModeHelper.getInitialTipIndex(today)) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(8000L)
            tipIndex = ComfortModeHelper.nextTipIndex(tipIndex)
        }
    }

    val selectedStateDesc = Translations.t("comfort_selected_state", lang)
    val notSelectedStateDesc = Translations.t("comfort_not_selected_state", lang)
    val savedLabel = Translations.t("comfort_saved_today", lang)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("comfort_mode_container"),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- 1) Large Mood Check-In (72.dp emoji buttons) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("comfort_mood_card"),
            colors = CardDefaults.cardColors(containerColor = colors.comfortCardBackground),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, colors.comfortBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translations.t("comfort_mood_title", lang),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (moodSaved) {
                        Text(
                            text = "✓ $savedLabel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.pinkAccent,
                            modifier = Modifier.testTag("comfort_mood_saved_badge")
                        )
                    }
                }

                val moods = listOf(
                    "😔" to Translations.t("comfort_mood_sad", lang),
                    "😐" to Translations.t("comfort_mood_neutral", lang),
                    "🙂" to Translations.t("comfort_mood_happy", lang),
                    "😄" to Translations.t("comfort_mood_joyful", lang)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    moods.forEach { (emoji, moodLabel) ->
                        val isSelected = selectedMood == emoji
                        val accessibilityLabel = "$moodLabel, ${if (isSelected) selectedStateDesc else notSelectedStateDesc}"

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = {
                                    selectedMood = emoji
                                    persistQuickLog(emoji, selectedFlow)
                                    moodSaved = true
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) colors.pinkAccent.copy(alpha = 0.22f) else colors.comfortPillBackground,
                                border = BorderStroke(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) colors.pinkAccent else colors.comfortBorder
                                ),
                                modifier = Modifier
                                    .size(72.dp)
                                    .semantics {
                                        contentDescription = accessibilityLabel
                                        selected = isSelected
                                    }
                                    .testTag("comfort_mood_btn_$emoji")
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emoji,
                                        fontSize = 34.sp
                                    )
                                }
                            }
                            Text(
                                text = moodLabel,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colors.pinkAccent else colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // --- 2) Quick Flow Log (Full-width stacked pills, min 56.dp height) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("comfort_flow_card"),
            colors = CardDefaults.cardColors(containerColor = colors.comfortCardBackground),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, colors.comfortBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translations.t("comfort_flow_title", lang),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (flowSaved) {
                        Text(
                            text = "✓ $savedLabel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.pinkAccent,
                            modifier = Modifier.testTag("comfort_flow_saved_badge")
                        )
                    }
                }

                val flows = listOf(
                    "None" to Translations.t("flow_none", lang),
                    "Spotting" to Translations.t("flow_spotting", lang),
                    "Light" to Translations.t("flow_light", lang),
                    "Medium" to Translations.t("flow_medium", lang),
                    "Heavy" to Translations.t("flow_heavy", lang)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    flows.forEach { (flowKey, flowLabel) ->
                        val isSelected = selectedFlow == flowKey
                        val accessibilityLabel = "$flowLabel, ${if (isSelected) selectedStateDesc else notSelectedStateDesc}"

                        Surface(
                            onClick = {
                                selectedFlow = flowKey
                                persistQuickLog(selectedMood, flowKey)
                                flowSaved = true
                            },
                            shape = RoundedCornerShape(28.dp),
                            color = if (isSelected) colors.pinkAccent else colors.comfortPillBackground,
                            border = BorderStroke(
                                width = if (isSelected) 0.dp else 1.dp,
                                color = if (isSelected) Color.Transparent else colors.comfortBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .semantics {
                                    contentDescription = accessibilityLabel
                                    selected = isSelected
                                }
                                .testTag("comfort_flow_pill_$flowKey")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = flowLabel,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else colors.textPrimary
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3) Rotating Comfort Tip Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("comfort_tip_card"),
            colors = CardDefaults.cardColors(containerColor = colors.comfortCardBackground),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, colors.comfortBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Spa,
                        contentDescription = Translations.t("comfort_tip_title", lang),
                        tint = colors.pinkAccent,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = Translations.t("comfort_tip_title", lang),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.pinkAccent
                    )
                }

                AnimatedContent(
                    targetState = tipIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "comfort_tip_rotation"
                ) { idx ->
                    Text(
                        text = comfortTips.getOrElse(idx) { comfortTips.first() },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        lineHeight = 26.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("comfort_tip_text")
                    )
                }

                val nextTipLabel = Translations.t("comfort_tip_next_btn", lang)
                OutlinedButton(
                    onClick = { tipIndex = ComfortModeHelper.nextTipIndex(tipIndex) },
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, colors.pinkAccent.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = colors.comfortPillBackground,
                        contentColor = colors.pinkAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .semantics { contentDescription = nextTipLabel }
                        .testTag("comfort_tip_next_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = colors.pinkAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = nextTipLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.pinkAccent
                    )
                }
            }
        }
    }
}
