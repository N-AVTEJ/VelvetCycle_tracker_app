package com.example.screens

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LocalAppLanguage
import com.example.constants.Translations
import com.example.ui.theme.LocalVelvetColors
import com.example.utils.NotificationHelper
import com.example.utils.StorageHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    storageHelper: StorageHelper,
    onLanguageChanged: (String) -> Unit,
    onThemeChanged: (Boolean) -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

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
    var pinSetupMode by remember { mutableStateOf<PinMode?>(null) } 
    var disguiseEnabled by remember { mutableStateOf(storageHelper.disguiseMode) }

    // Store & Language states
    var selectedStore by remember { mutableStateOf(storageHelper.padStore) }
    var selectedLanguage by remember { mutableStateOf(storageHelper.appLanguage) }
    var isDarkModeTheme by remember { mutableStateOf(storageHelper.isDarkMode) }

    // Backup & Restore Simulation States
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var backupProgress by remember { mutableStateOf(0f) }
    var backupStatusText by remember { mutableStateOf("") }
    var restoreProgress by remember { mutableStateOf(0f) }
    var restoreStatusText by remember { mutableStateOf("") }

    // Reschedule push alerts helper
    val rescheduleNotifications = {
        NotificationHelper.scheduleAllNotifications(context, storageHelper)
    }

    if (pinSetupMode != null) {
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
        // --- Simulated Backup Dialog ---
        if (showBackupDialog) {
            AlertDialog(
                onDismissRequest = { if (backupProgress >= 1f) showBackupDialog = false },
                title = { Text(text = Translations.t("btn_backup", lang), fontWeight = FontWeight.Bold, color = colors.textPrimary) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = backupStatusText, color = colors.textPrimary, fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = backupProgress,
                            color = colors.pinkAccent,
                            trackColor = colors.pinkAccent.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                        )
                    }
                },
                confirmButton = {
                    if (backupProgress >= 1f) {
                        TextButton(onClick = { showBackupDialog = false }) {
                            Text(text = "OK", color = colors.pinkAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                containerColor = colors.cardBackground
            )
        }

        // --- Simulated Restore Dialog ---
        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { if (restoreProgress >= 1f) showRestoreDialog = false },
                title = { Text(text = Translations.t("btn_restore", lang), fontWeight = FontWeight.Bold, color = colors.textPrimary) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = restoreStatusText, color = colors.textPrimary, fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = restoreProgress,
                            color = colors.pinkAccent,
                            trackColor = colors.pinkAccent.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                        )
                    }
                },
                confirmButton = {
                    if (restoreProgress >= 1f) {
                        TextButton(onClick = { showRestoreDialog = false }) {
                            Text(text = "OK", color = colors.pinkAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                containerColor = colors.cardBackground
            )
        }

        // --- Main UI ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = if (lang == "हिंदी") "सेटिंग्स" else if (lang == "తెలుగు") "సెట్టింగ్స్" else "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Start
            )

            // --- SECTION 1: ACCOUNT ---
            SettingsSectionCard(title = Translations.t("section_account", lang)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Translations.t("lbl_display_name", lang),
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                            if (isEditingName) {
                                OutlinedTextField(
                                    value = displayName,
                                    onValueChange = { displayName = it },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.pinkAccent,
                                        unfocusedBorderColor = colors.border,
                                        focusedTextColor = colors.textPrimary,
                                        unfocusedTextColor = colors.textPrimary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .testTag("display_name_input"),
                                    singleLine = true
                                )
                            } else {
                                Text(
                                    text = displayName.ifEmpty { "User" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isEditingName) {
                                    storageHelper.userName = displayName.trim()
                                    Toast.makeText(context, if (lang == "हिंदी") "नाम अपडेट किया गया!" else if (lang == "తెలుగు") "పేరు మార్చబడింది!" else "Name updated!", Toast.LENGTH_SHORT).show()
                                }
                                isEditingName = !isEditingName
                            },
                            modifier = Modifier.testTag("edit_name_button")
                        ) {
                            Icon(
                                imageVector = if (isEditingName) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = null,
                                tint = colors.pinkAccent
                            )
                        }
                    }

                    Divider(color = colors.border, thickness = 0.5.dp)

                    // Edit cycle info
                    Button(
                        onClick = onNavigateToOnboarding,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_cycle_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = colors.pinkAccent,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = Translations.t("lbl_edit_cycle", lang),
                            color = colors.pinkAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // --- SECTION 2: NOTIFICATIONS ---
            SettingsSectionCard(title = Translations.t("section_notifications", lang)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    NotificationToggleItem(
                        title = Translations.t("lbl_period_reminder", lang),
                        subtitle = Translations.t("lbl_period_reminder_sub", lang),
                        checked = periodReminder,
                        onCheckedChange = {
                            periodReminder = it
                            storageHelper.periodReminderEnabled = it
                            rescheduleNotifications()
                        }
                    )
                    Divider(color = colors.border, thickness = 0.5.dp)
                    NotificationToggleItem(
                        title = Translations.t("lbl_ovulation_alert", lang),
                        subtitle = Translations.t("lbl_ovulation_alert_sub", lang),
                        checked = ovulationAlert,
                        onCheckedChange = {
                            ovulationAlert = it
                            storageHelper.ovulationAlertEnabled = it
                            rescheduleNotifications()
                        }
                    )
                    Divider(color = colors.border, thickness = 0.5.dp)
                    Column {
                        NotificationToggleItem(
                            title = Translations.t("lbl_daily_log", lang),
                            subtitle = Translations.t("lbl_daily_log_sub", lang),
                            checked = dailyLogReminder,
                            onCheckedChange = {
                                dailyLogReminder = it
                                storageHelper.dailyLogReminderEnabled = it
                                rescheduleNotifications()
                            }
                        )
                        if (dailyLogReminder) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.background)
                                    .clickable {
                                        val cal = Calendar.getInstance()
                                        val parts = dailyLogTime.split(":")
                                        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
                                        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                        
                                        TimePickerDialog(context, { _, h, m ->
                                            val newTime = String.format("%02d:%02d", h, m)
                                            dailyLogTime = newTime
                                            storageHelper.dailyLogReminderTime = newTime
                                            rescheduleNotifications()
                                        }, hour, minute, true).show()
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = Translations.t("lbl_reminder_time", lang),
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = dailyLogTime,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.pinkAccent
                                )
                            }
                        }
                    }
                    Divider(color = colors.border, thickness = 0.5.dp)
                    NotificationToggleItem(
                        title = Translations.t("lbl_pad_reminder", lang),
                        subtitle = Translations.t("lbl_pad_reminder_sub", lang),
                        checked = padReminder,
                        onCheckedChange = {
                            padReminder = it
                            storageHelper.padReminderEnabled = it
                            rescheduleNotifications()
                        }
                    )
                }
            }

            // --- SECTION 3: PRIVACY & SECURITY ---
            SettingsSectionCard(title = Translations.t("section_privacy", lang)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    NotificationToggleItem(
                        title = Translations.t("lbl_biometric", lang),
                        subtitle = Translations.t("lbl_biometric_sub", lang),
                        checked = biometricEnabled,
                        onCheckedChange = {
                            biometricEnabled = it
                            storageHelper.biometricEnabled = it
                        }
                    )
                    Divider(color = colors.border, thickness = 0.5.dp)

                    NotificationToggleItem(
                        title = if (lang == "हिंदी") "ऐप छुपाएं" else if (lang == "తెలుగు") "యాప్‌ను దాచండి" else "Disguise app",
                        subtitle = if (lang == "हिंदी") "अनलॉक स्क्रीन को एक कैलकुलेटर के रूप में छुपाएं" else if (lang == "తెలుగు") "లాక్ స్క్రీన్‌ను సాధారణ కాలిక్యులేటర్‌గా చూపించు" else "Disguise PIN screen as a plain calculator",
                        checked = disguiseEnabled,
                        onCheckedChange = {
                            disguiseEnabled = it
                            storageHelper.disguiseMode = it
                        }
                    )
                    Divider(color = colors.border, thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                pinSetupMode = PinMode.Setup
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set_pin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = colors.cardBackground,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = Translations.t("btn_set_pin", lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.cardBackground
                            )
                        }

                        if (isPinSet) {
                            Button(
                                onClick = {
                                    storageHelper.userPin = null
                                    isPinSet = false
                                    Toast.makeText(context, if (lang == "हिंदी") "पिन हटा दिया गया!" else if (lang == "తెలుగు") "పిన్ తొలగించబడింది!" else "PIN removed!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .testTag("remove_pin_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Translations.t("btn_remove_pin", lang),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 4: PAD STORE PREFERENCE ---
            SettingsSectionCard(title = Translations.t("section_store", lang)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val stores = listOf("Amazon", "Target", "Walmart", "Local Pharmacy")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        stores.forEach { store ->
                            val isSelected = selectedStore == store
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.pinkAccent else colors.background)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) colors.pinkAccent else colors.border,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedStore = store
                                        storageHelper.padStore = store
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = store,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) colors.cardBackground else colors.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 5: LANGUAGE ---
            SettingsSectionCard(title = Translations.t("section_language", lang)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val languages = listOf("English", "हिंदी", "తెలుగు")
                    languages.forEach { language ->
                        val isSelected = selectedLanguage == language
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.pinkAccent.copy(alpha = 0.08f) else colors.background)
                                .clickable {
                                    selectedLanguage = language
                                    storageHelper.appLanguage = language
                                    onLanguageChanged(language)
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = language,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = colors.textPrimary
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedLanguage = language
                                    storageHelper.appLanguage = language
                                    onLanguageChanged(language)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.pinkAccent)
                            )
                        }
                    }
                }
            }

            // --- SECTION 6: DISPLAY / THEME ---
            SettingsSectionCard(title = if (lang == "हिंदी") "थीम" else if (lang == "తెలుగు") "థీమ్" else "Display") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == "हिंदी") "डार्क मोड" else if (lang == "తెలుగు") "డార్క్ మోడ్" else "Dark Mode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (lang == "हिंदी") "आरामदायक रात के दृश्य के लिए" else if (lang == "తెలుగు") "రాత్రి వేళల్లో చదువుకోవడానికి అనుకూలమైనది" else "Enable dark mode for night viewing",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                        Switch(
                            checked = isDarkModeTheme,
                            onCheckedChange = {
                                isDarkModeTheme = it
                                storageHelper.isDarkMode = it
                                onThemeChanged(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.cardBackground,
                                checkedTrackColor = colors.pinkAccent,
                                uncheckedThumbColor = colors.textSecondary,
                                uncheckedTrackColor = colors.border
                            ),
                            modifier = Modifier.testTag("dark_mode_toggle")
                        )
                    }
                }
            }

            // --- SECTION 7: DATA & MAINTENANCE ---
            SettingsSectionCard(title = Translations.t("section_data", lang)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Backup Button
                    Button(
                        onClick = {
                            scope.launch {
                                showBackupDialog = true
                                backupProgress = 0f
                                backupStatusText = if (lang == "हिंदी") "गूगल ड्राइव से जुड़ रहा है..." else "Connecting to Google Drive..."
                                delay(800)
                                backupProgress = 0.3f
                                backupStatusText = if (lang == "हिंदी") "डेटा पैकेज तैयार हो रहा है..." else "Preparing local cycle backup package..."
                                delay(1000)
                                backupProgress = 0.7f
                                backupStatusText = if (lang == "हिंदी") "VelvetCycle_backup.json अपलोड हो रहा है..." else "Uploading VelvetCycle_backup.json..."
                                delay(800)
                                backupProgress = 1f
                                backupStatusText = if (lang == "हिंदी") "गूगल ड्राइव बैकअप सफल!" else "Google Drive backup successful!"
                                Toast.makeText(context, if (lang == "हिंदी") "बैकअप सफल!" else "Backup successful!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_drive_btn")
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = colors.pinkAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = Translations.t("btn_backup", lang), color = colors.pinkAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Restore Button
                    Button(
                        onClick = {
                            scope.launch {
                                showRestoreDialog = true
                                restoreProgress = 0f
                                restoreStatusText = if (lang == "हिंदी") "गूगल ड्राइव पर बैकअप खोज रहा है..." else "Scanning Google Drive for backup..."
                                delay(1000)
                                restoreProgress = 0.4f
                                restoreStatusText = if (lang == "हिंदी") "VelvetCycle_backup.json डाउनलोड हो रहा है..." else "Downloading VelvetCycle_backup.json..."
                                delay(1000)
                                restoreProgress = 0.8f
                                restoreStatusText = if (lang == "हिंदी") "डेटा स्थानीय रूप से पुनर्स्थापित हो रहा है..." else "Restoring logs locally..."
                                delay(600)
                                restoreProgress = 1f
                                restoreStatusText = if (lang == "हिंदी") "सफलतापूर्वक 14 दिनों के आंकड़े और 1 पुराना चक्र बहाल किया गया!" else "Successfully restored 14 symptoms logs and 1 previous cycle!"
                                Toast.makeText(context, if (lang == "हिंदी") "रीस्टोर सफल!" else "Restore complete!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("restore_drive_btn")
                    ) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = colors.pinkAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = Translations.t("btn_restore", lang), color = colors.pinkAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Delete Data
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_data_btn")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = colors.cardBackground, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = Translations.t("btn_delete_data", lang), color = colors.cardBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // --- SECTION 8: ABOUT ---
            SettingsSectionCard(title = Translations.t("section_about", lang)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VelvetCycle v1.0.0",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your private, secure, offline cycle companion",
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // --- Delete Confirmation Alert Dialog ---
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = if (lang == "हिंदी") "क्या आप निश्चित हैं?" else if (lang == "తెలుగు") "ఖచ్చితంగా ఉన్నారా?" else "Are you absolutely sure?",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = if (lang == "हिंदी") "यह आपके सभी पीरियड लॉग, पासवर्ड पिन और चक्र इतिहास को स्थायी रूप से हटा देगा। इसे पूर्ववत नहीं किया जा सकता है।" else if (lang == "తెలుగు") "ఇది మీ పీరియడ్ లాగ్‌లు, పిన్ పాస్‌వర్డ్‌లు మరియు సైకిల్ హిస్టరీ మొత్తాన్ని శాశ్వతంగా తొలగిస్తుంది. దీనిని తిరిగి పొందలేరు." else "This will permanently wipe all your period logs, PIN passwords, and cycle histories. This action cannot be undone.",
                    color = colors.textPrimary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        storageHelper.clear()
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "All data deleted! Restarting.", Toast.LENGTH_LONG).show()
                        onNavigateToOnboarding()
                    }
                ) {
                    Text(
                        text = if (lang == "हिंदी") "हां, हटाएं" else if (lang == "తెలుగు") "అవును, తొలగించు" else "Yes, Clear Everything",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(
                        text = if (lang == "हिंदी") "रद्द करें" else if (lang == "తెలుగు") "రద్దు చేయి" else "Cancel",
                        color = colors.textSecondary
                    )
                }
            },
            containerColor = colors.cardBackground
        )
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = LocalVelvetColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = colors.pinkAccent,
            modifier = Modifier.padding(start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun NotificationToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalVelvetColors.current
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
                color = colors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.cardBackground,
                checkedTrackColor = colors.pinkAccent,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.border
            )
        )
    }
}
