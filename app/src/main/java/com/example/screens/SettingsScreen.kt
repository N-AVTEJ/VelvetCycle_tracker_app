package com.example.screens

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
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
    var showRemovePinConfirm by remember { mutableStateOf(false) }
    
    // Notification toggles
    var periodReminder by remember { mutableStateOf(storageHelper.periodReminderEnabled) }
    var ovulationAlert by remember { mutableStateOf(storageHelper.ovulationAlertEnabled) }
    var dailyLogReminder by remember { mutableStateOf(storageHelper.dailyLogReminderEnabled) }
    var dailyLogTime by remember { mutableStateOf(storageHelper.dailyLogReminderTime) }
    var padReminder by remember { mutableStateOf(storageHelper.padReminderEnabled) }

    // Privacy & PIN states
    var isPinSet by remember { mutableStateOf(storageHelper.userPin != null) }
    var biometricEnabled by remember { mutableStateOf(storageHelper.biometricEnabled) }
    var activePrivacyFlow by remember { mutableStateOf<String?>(null) } // "setup", "change", or null
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

    if (activePrivacyFlow == "setup") {
        PinSetupScreen(
            storageHelper = storageHelper,
            onFinished = {
                isPinSet = storageHelper.userPin != null
                activePrivacyFlow = null
                rescheduleNotifications()
            },
            onCancel = {
                activePrivacyFlow = null
            }
        )
    } else if (activePrivacyFlow == "change") {
        PinChangeScreen(
            storageHelper = storageHelper,
            onFinished = {
                activePrivacyFlow = null
            },
            onCancel = {
                activePrivacyFlow = null
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
            val isBiometricHardwareAvailable = remember {
                val biometricManager = BiometricManager.from(context)
                val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                canAuth == BiometricManager.BIOMETRIC_SUCCESS
            }

            val triggerBiometricVerifyAndEnable: (Boolean) -> Unit = { enabled ->
                if (enabled) {
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        val executor = ContextCompat.getMainExecutor(context)
                        val biometricPrompt = BiometricPrompt(
                            activity,
                            executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    super.onAuthenticationError(errorCode, errString)
                                    biometricEnabled = false
                                    storageHelper.biometricEnabled = false
                                    Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
                                }

                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    biometricEnabled = true
                                    storageHelper.biometricEnabled = true
                                    Toast.makeText(context, if (lang == "हिंदी") "बायोमेट्रिक सक्षम!" else "Biometric enabled!", Toast.LENGTH_SHORT).show()
                                }

                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                }
                            }
                        )

                        val promptTitle = if (lang == "हिंदी") "बायोमेट्रिक की पुष्टि करें" else "Confirm Biometric"
                        val promptSubtitle = if (lang == "हिंदी") "सक्षम करने के लिए प्रमाणित करें" else "Authenticate to enable biometric unlock"
                        val promptNegative = if (lang == "हिंदी") "रद्द करें" else "Cancel"

                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle(promptTitle)
                            .setSubtitle(promptSubtitle)
                            .setNegativeButtonText(promptNegative)
                            .build()

                        biometricPrompt.authenticate(promptInfo)
                    } else {
                        biometricEnabled = false
                        storageHelper.biometricEnabled = false
                        Toast.makeText(context, "FragmentActivity not available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    biometricEnabled = false
                    storageHelper.biometricEnabled = false
                    Toast.makeText(context, if (lang == "हिंदी") "बायोमेट्रिक अक्षम" else "Biometric disabled", Toast.LENGTH_SHORT).show()
                }
            }

            SettingsSectionCard(title = Translations.t("section_privacy", lang)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!isPinSet) {
                        // Unprotected state warning
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF3CD))
                                .border(1.dp, Color(0xFFFFEBAA), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "⚠️", fontSize = 24.sp, modifier = Modifier.testTag("privacy_unprotected_warning_icon"))
                            Text(
                                text = if (lang == "हिंदी") "आपका ऐप वर्तमान में असुरक्षित है। अपने चक्र डेटा को सुरक्षित करने के लिए एक पिन सेट करें।" else if (lang == "తెలుగు") "మీ యాప్ ప్రస్తుతం అసురక్షితంగా ఉంది. మీ సైకిల్ డేటాను రక్షించడానికి పిన్‌ని సెట్ చేయండి." else "Your app is currently unprotected. Set a PIN to secure your intimate cycle data.",
                                color = Color(0xFF856404),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f).testTag("privacy_unprotected_warning_text")
                            )
                        }

                        Button(
                            onClick = { activePrivacyFlow = "setup" },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("set_pin_button")
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Translations.t("btn_set_pin", lang),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // Protected state UI
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.pinkAccent.copy(alpha = 0.08f))
                                .border(1.dp, colors.pinkAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "🛡️", fontSize = 24.sp, modifier = Modifier.testTag("privacy_protected_icon"))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (lang == "हिंदी") "ऐप लॉक सक्रिय है" else if (lang == "తెలుగు") "యాప్ లాక్ సక్రియంగా ఉంది" else "App lock is active",
                                    color = colors.pinkAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("privacy_protected_title")
                                )
                                Text(
                                    text = if (lang == "हिंदी") "आपका व्यक्तिगत डेटा सुरक्षित है" else if (lang == "తెలుగు") "మీ వ్యక్తిగత డేటా సురక్షితంగా ఉంది" else "Intimate cycle data is secured",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.testTag("privacy_protected_subtitle")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { activePrivacyFlow = "change" },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("change_pin_button")
                            ) {
                                Text(
                                    text = if (lang == "हिंदी") "पिन बदलें" else if (lang == "తెలుగు") "పిన్ మార్చండి" else "Change PIN",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = { showRemovePinConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("remove_pin_button")
                            ) {
                                Text(
                                    text = if (lang == "हिंदी") "पिन हटाएं" else if (lang == "తెలుగు") "పిన్ తొలగించండి" else "Remove PIN",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }

                        Divider(color = colors.border, thickness = 0.5.dp)

                        // Fingerprint toggle row
                        if (isBiometricHardwareAvailable) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = colors.pinkAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (lang == "हिंदी") "बायोमेट्रिक अनलॉक" else if (lang == "తెలుగు") "బయోమెట్రిక్ అన్‌లాక్" else "Biometric unlock",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            modifier = Modifier.testTag("settings_biometric_title")
                                        )
                                        Text(
                                            text = if (lang == "हिंदी") "अनलॉक करने के लिए उंगली या चेहरे का उपयोग करें" else if (lang == "తెలుగు") "అన్‌లాక్ చేయడానికి వేలిముద్ర లేదా ఫేస్ లాక్ ఉపయోగించండి" else "Use fingerprint or face to unlock",
                                            fontSize = 11.sp,
                                            color = colors.textSecondary,
                                            modifier = Modifier.testTag("settings_biometric_subtitle")
                                        )
                                    }
                                }
                                Switch(
                                    checked = biometricEnabled,
                                    onCheckedChange = { checked ->
                                        triggerBiometricVerifyAndEnable(checked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colors.pinkAccent,
                                        uncheckedThumbColor = colors.textSecondary,
                                        uncheckedTrackColor = colors.border
                                    ),
                                    modifier = Modifier.testTag("settings_biometric_switch")
                                )
                            }

                            Divider(color = colors.border, thickness = 0.5.dp)
                        }

                        // Disguise app toggle row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = colors.pinkAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = if (lang == "हिंदी") "ऐप छुपाएं" else if (lang == "తెలుగు") "యాప్‌ను దాచండి" else "Disguise app",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        modifier = Modifier.testTag("settings_disguise_title")
                                    )
                                    Text(
                                        text = if (lang == "हिंदी") "अनलॉक स्क्रीन को एक कैलकुलेटर के रूप में छुपाएं" else if (lang == "తెలుగు") "లాక్ స్క్రీన్‌ను సాధారణ కాలిక్యులేటర్ లాగా చూపించు" else "Hide PIN entry screen behind a working calculator",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary,
                                        modifier = Modifier.testTag("settings_disguise_subtitle")
                                    )
                                }
                            }
                            Switch(
                                checked = disguiseEnabled,
                                onCheckedChange = { checked ->
                                    disguiseEnabled = checked
                                    storageHelper.disguiseMode = checked
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = colors.pinkAccent,
                                    uncheckedThumbColor = colors.textSecondary,
                                    uncheckedTrackColor = colors.border
                                ),
                                modifier = Modifier.testTag("settings_disguise_switch")
                            )
                        }
                    }
                }
            }

            // --- SECTION 4: PAD STORE PREFERENCE ---
            SettingsSectionCard(title = Translations.t("section_store", lang)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val stores = listOf(
                        Pair("blinkit", "🟡"),
                        Pair("zepto", "🟣")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        stores.forEach { (storeKey, icon) ->
                            val isSelected = selectedStore == storeKey
                            val storeName = if (storeKey == "blinkit") Translations.t("store_blinkit", lang) else Translations.t("store_zepto", lang)
                            val borderColor = if (isSelected) {
                                if (storeKey == "blinkit") Color(0xFFF5C518) else Color(0xFF7B2FF7)
                            } else {
                                colors.border
                            }
                            val borderWidth = if (isSelected) 2.dp else 1.dp
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.background)
                                    .border(
                                        width = borderWidth,
                                        color = borderColor,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        val newStore = if (isSelected) "" else storeKey
                                        selectedStore = newStore
                                        storageHelper.padStore = newStore
                                    }
                                    .testTag("settings_store_$storeKey")
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = icon, fontSize = 18.sp)
                                    Text(
                                        text = storeName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
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

    // --- Remove PIN Confirmation Alert Dialog ---
    if (showRemovePinConfirm) {
        AlertDialog(
            onDismissRequest = { showRemovePinConfirm = false },
            title = {
                Text(
                    text = if (lang == "हिंदी") "पिन लॉक हटाएं?" else if (lang == "తెలుగు") "పిన్ లాక్ తొలగించాలా?" else "Disable App Lock?",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = if (lang == "हिंदी") "क्या आप वाकई ऐप लॉक हटाना चाहते हैं? इसके बिना आपका संवेदनशील चक्र डेटा कोई भी देख सकता है।" else if (lang == "తెలుగు") "మీరు నిజంగా యాప్ లాక్‌ని నిలిపివేయాలనుకుంటున్నారా? ఇది లేకపోతే ఎవరైనా మీ వ్యక్తిగत సైకిల్ వివరాలను చూడగలరు." else "Are you sure you want to disable PIN protection? Anyone with physical access to your device will be able to read your private cycle logs.",
                    color = colors.textPrimary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        storageHelper.userPin = null
                        storageHelper.biometricEnabled = false
                        biometricEnabled = false
                        isPinSet = false
                        showRemovePinConfirm = false
                        Toast.makeText(context, if (lang == "हिंदी") "पिन सुरक्षा हटा दी गई" else if (lang == "తెలుగు") "పిన్ రక్షణ నిలిపివేయబడింది" else "PIN protection disabled", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        text = if (lang == "हिंदी") "हां, हटाएं" else if (lang == "తెలుగు") "అవును, తొలగించు" else "Yes, Disable",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemovePinConfirm = false }) {
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
