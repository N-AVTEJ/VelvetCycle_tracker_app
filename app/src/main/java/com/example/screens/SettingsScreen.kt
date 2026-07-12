package com.example.screens

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightPinkBg
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.White
import com.example.utils.NotificationHelper
import com.example.utils.StorageHelper
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    storageHelper: StorageHelper,
    onNavigateToOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Screen states
    var displayName by remember { mutableStateOf(storageHelper.userName) }
    var isEditingName by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    // Notification toggles
    var periodReminder by remember { mutableStateOf(storageHelper.periodReminderEnabled) }
    var ovulationAlert by remember { mutableStateOf(storageHelper.ovulationAlertEnabled) }
    var dailyLogReminder by remember { mutableStateOf(storageHelper.dailyLogReminderEnabled) }
    var dailyLogTime by remember { mutableStateOf(storageHelper.dailyLogReminderTime) }
    var padReminder by remember { mutableStateOf(storageHelper.padReminderEnabled) }

    // Privacy & PIN states
    var isPinSet by remember { mutableStateOf(storageHelper.userPin != null) }
    var biometricEnabled by remember { mutableStateOf(storageHelper.biometricEnabled) }
    var pinSetupMode by remember { mutableStateOf<PinMode?>(null) } // null means normal settings, non-null means showing PIN setup

    // Store & Language states
    var selectedStore by remember { mutableStateOf(storageHelper.padStore) }
    var selectedLanguage by remember { mutableStateOf(storageHelper.appLanguage) }

    // Trigger notification rescheduling whenever settings change
    val rescheduleNotifications = {
        NotificationHelper.scheduleAllNotifications(context, storageHelper)
    }

    if (pinSetupMode != null) {
        // Overlay PIN Setup Screen
        PinScreen(
            storageHelper = storageHelper,
            initialMode = pinSetupMode!!,
            onSetupComplete = {
                isPinSet = storageHelper.userPin != null
                pinSetupMode = null
                rescheduleNotifications()
            },
            onCancelSetup = {
                pinSetupMode = null
            }
        )
    } else {
        // Main Settings UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightPinkBg)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Start
            )

            // SECTION — ACCOUNT
            SettingsCard(title = "Account") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Display Name Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Display Name",
                                fontSize = 12.sp,
                                color = DarkText.copy(alpha = 0.5f)
                            )
                            if (isEditingName) {
                                OutlinedTextField(
                                    value = displayName,
                                    onValueChange = { displayName = it },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryPink,
                                        unfocusedBorderColor = PrimaryPink.copy(alpha = 0.2f),
                                        focusedTextColor = DarkText,
                                        unfocusedTextColor = DarkText
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .testTag("display_name_input"),
                                    singleLine = true
                                )
                            } else {
                                Text(
                                    text = displayName.ifEmpty { "Not set" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkText,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isEditingName) {
                                    storageHelper.userName = displayName.trim()
                                    Toast.makeText(context, "Name updated!", Toast.LENGTH_SHORT).show()
                                }
                                isEditingName = !isEditingName
                            },
                            modifier = Modifier.testTag("edit_name_button")
                        ) {
                            Icon(
                                imageVector = if (isEditingName) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditingName) "Save Name" else "Edit Name",
                                tint = PrimaryPink
                            )
                        }
                    }

                    Divider(color = LightPinkBg, thickness = 1.dp)

                    // Edit Cycle Info Button
                    Button(
                        onClick = { onNavigateToOnboarding() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_cycle_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = PrimaryPink,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Edit Cycle & Period Info",
                            color = PrimaryPink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // SECTION — NOTIFICATIONS
            SettingsCard(title = "Notifications") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Period Reminder Toggle
                    NotificationToggleRow(
                        title = "Period reminder",
                        subtitle = "Alert 2 days before predicted period",
                        checked = periodReminder,
                        onCheckedChange = {
                            periodReminder = it
                            storageHelper.periodReminderEnabled = it
                            rescheduleNotifications()
                        },
                        tag = "toggle_period_reminder"
                    )

                    Divider(color = LightPinkBg, thickness = 1.dp)

                    // Ovulation Alert Toggle
                    NotificationToggleRow(
                        title = "Ovulation alert",
                        subtitle = "Notify on ovulation day at 9:00 AM",
                        checked = ovulationAlert,
                        onCheckedChange = {
                            ovulationAlert = it
                            storageHelper.ovulationAlertEnabled = it
                            rescheduleNotifications()
                        },
                        tag = "toggle_ovulation_alert"
                    )

                    Divider(color = LightPinkBg, thickness = 1.dp)

                    // Daily Log Reminder Toggle + Time picker
                    Column {
                        NotificationToggleRow(
                            title = "Daily log reminder",
                            subtitle = "Daily prompt to log your symptoms",
                            checked = dailyLogReminder,
                            onCheckedChange = {
                                dailyLogReminder = it
                                storageHelper.dailyLogReminderEnabled = it
                                rescheduleNotifications()
                            },
                            tag = "toggle_daily_log"
                        )

                        if (dailyLogReminder) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LightPinkBg.copy(alpha = 0.5f))
                                    .clickable {
                                        val initialHour = dailyLogTime.split(":")[0].toIntOrNull() ?: 21
                                        val initialMinute = dailyLogTime.split(":")[1].toIntOrNull() ?: 0
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                val formatted = String.format("%02d:%02d", hour, minute)
                                                dailyLogTime = formatted
                                                storageHelper.dailyLogReminderTime = formatted
                                                rescheduleNotifications()
                                                Toast.makeText(context, "Reminder set to $formatted", Toast.LENGTH_SHORT).show()
                                            },
                                            initialHour,
                                            initialMinute,
                                            true
                                        ).show()
                                    }
                                    .padding(12.dp)
                                    .testTag("reminder_time_picker"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reminder Time",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DarkText
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = dailyLogTime,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryPink,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Time",
                                        tint = PrimaryPink
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = LightPinkBg, thickness = 1.dp)

                    // Pad Reminder Toggle
                    NotificationToggleRow(
                        title = "Pad reminder",
                        subtitle = "Alert 2 days before to stock up on pads",
                        checked = padReminder,
                        onCheckedChange = {
                            padReminder = it
                            storageHelper.padReminderEnabled = it
                            rescheduleNotifications()
                        },
                        tag = "toggle_pad_reminder"
                    )
                }
            }

            // SECTION — PRIVACY & SECURITY
            SettingsCard(title = "Privacy & Security") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isPinSet) "PIN Lock is Active" else "PIN Lock is Disabled",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText
                            )
                            Text(
                                text = "Require PIN entry upon launching the app",
                                fontSize = 11.sp,
                                color = DarkText.copy(alpha = 0.5f)
                            )
                        }

                        if (!isPinSet) {
                            Button(
                                onClick = { pinSetupMode = PinMode.Setup },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("set_pin_button")
                            ) {
                                Text("Set PIN", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    storageHelper.userPin = null
                                    isPinSet = false
                                    biometricEnabled = false
                                    storageHelper.biometricEnabled = false
                                    Toast.makeText(context, "PIN Security Removed", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("remove_pin_button")
                            ) {
                                Text("Remove PIN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (isPinSet) {
                        Divider(color = LightPinkBg, thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Fingerprint / Face Unlock",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                                Text(
                                    text = "Enable biometric unlock when possible",
                                    fontSize = 11.sp,
                                    color = DarkText.copy(alpha = 0.5f)
                                )
                            }

                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = {
                                    biometricEnabled = it
                                    storageHelper.biometricEnabled = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = White,
                                    checkedTrackColor = PrimaryPink,
                                    uncheckedThumbColor = DarkText.copy(alpha = 0.4f),
                                    uncheckedTrackColor = LightPinkBg
                                ),
                                modifier = Modifier.testTag("toggle_biometric")
                            )
                        }
                    }
                }
            }

            // SECTION — PAD STORE PREFERENCE
            SettingsCard(title = "Pad Store Preference") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Preferred store for stocking up sanitaries:",
                        fontSize = 12.sp,
                        color = DarkText.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StorePreferenceButton(
                            name = "Blinkit",
                            isSelected = selectedStore == "Blinkit",
                            onClick = {
                                selectedStore = "Blinkit"
                                storageHelper.padStore = "Blinkit"
                                Toast.makeText(context, "Store preference: Blinkit", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("store_blinkit_btn")
                        )

                        StorePreferenceButton(
                            name = "Amazon India",
                            isSelected = selectedStore == "Amazon India",
                            onClick = {
                                selectedStore = "Amazon India"
                                storageHelper.padStore = "Amazon India"
                                Toast.makeText(context, "Store preference: Amazon India", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("store_amazon_btn")
                        )
                    }
                }
            }

            // SECTION — LANGUAGE
            SettingsCard(title = "Language") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "App Display Language",
                        fontSize = 12.sp,
                        color = DarkText.copy(alpha = 0.5f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("English", "हिंदी", "తెలుగు").forEach { lang ->
                            val isSel = selectedLanguage == lang
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) PrimaryPink else White)
                                    .border(1.dp, if (isSel) PrimaryPink else PrimaryPink.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedLanguage = lang
                                        storageHelper.appLanguage = lang
                                        Toast.makeText(context, "Language support coming in Phase 4", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 12.dp)
                                    .testTag("lang_btn_${lang.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lang,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) White else DarkText
                                )
                            }
                        }
                    }
                }
            }

            // SECTION — DATA & MAINTENANCE
            SettingsCard(title = "Data & Maintenance") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Backup to Google Drive
                    Button(
                        onClick = {
                            Toast.makeText(context, "Backup option coming in Phase 4", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_data_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = PrimaryPink,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Backup to Google Drive", color = PrimaryPink, fontWeight = FontWeight.Bold)
                    }

                    // Delete All My Data (Red alert button)
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_data_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Delete all my data",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // SECTION — ABOUT
            SettingsCard(title = "About") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "VelvetCycle",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPink
                    )
                    Text(
                        text = "Version 1.0.0",
                        fontSize = 12.sp,
                        color = DarkText.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Made with ❤️ for women",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Delete Data Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete all data?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you absolutely sure you want to delete all logs, PIN, and cycle details? This operation cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        NotificationHelper.cancelAllNotifications(context)
                        storageHelper.clear()
                        Toast.makeText(context, "All data wiped!", Toast.LENGTH_SHORT).show()
                        onNavigateToOnboarding()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_btn")
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    modifier = Modifier.testTag("dismiss_delete_btn")
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPink,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = DarkText.copy(alpha = 0.5f)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = PrimaryPink,
                uncheckedThumbColor = DarkText.copy(alpha = 0.4f),
                uncheckedTrackColor = LightPinkBg
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}

@Composable
fun StorePreferenceButton(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) PrimaryPink else White)
            .border(1.dp, if (isSelected) PrimaryPink else PrimaryPink.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) White else DarkText
        )
    }
}
