package com.example.screens

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.LocalAppLanguage
import com.example.ui.theme.LocalVelvetColors
import com.example.utils.StorageHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class PinMode {
    object Verify : PinMode()
    object Setup : PinMode()
    data class Confirm(val firstPin: String) : PinMode()
}

@Composable
fun PinScreen(
    storageHelper: StorageHelper,
    initialMode: PinMode = PinMode.Verify,
    onUnlocked: () -> Unit = {},
    onSetupComplete: () -> Unit = {},
    onCancelSetup: (() -> Unit)? = null
) {
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var currentMode by remember { mutableStateOf(initialMode) }
    var enteredDigits by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isShaking by remember { mutableStateOf(false) }
    var lockoutTimeLeft by remember { mutableStateOf(storageHelper.getLockoutTimeRemaining()) }

    // Lockout countdown loop
    LaunchedEffect(key1 = lockoutTimeLeft) {
        if (lockoutTimeLeft > 0) {
            while (lockoutTimeLeft > 0) {
                delay(1000L)
                lockoutTimeLeft = storageHelper.getLockoutTimeRemaining()
            }
            storageHelper.clearLockout()
        }
    }

    // Shake animation
    val shakeOffset by animateFloatAsState(
        targetValue = if (isShaking) 15f else 0f,
        animationSpec = keyframes {
            durationMillis = 300
            0f at 0
            -15f at 50
            15f at 100
            -15f at 150
            15f at 200
            -15f at 250
            0f at 300
        },
        finishedListener = { isShaking = false },
        label = "shake"
    )

    // Biometric prompt action
    val triggerBiometricAuth = {
        val activity = context as? FragmentActivity
        if (activity != null && storageHelper.biometricEnabled && currentMode == PinMode.Verify) {
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        storageHelper.clearLockout()
                        onUnlocked()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        val failedMsg = if (lang == "हिंदी") "सत्यापन विफल रहा" else if (lang == "తెలుగు") "ధృవీకరణ విఫలమైంది" else "Authentication failed"
                        Toast.makeText(context, failedMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            )

            val promptTitle = if (lang == "हिंदी") "VelvetCycle को अनलॉक करें" else if (lang == "తెలుగు") "VelvetCycle అన్‌లాక్ చేయండి" else "Unlock VelvetCycle"
            val promptSubtitle = if (lang == "हिंदी") "अनलॉक करने के लिए अपनी उंगली या चेहरे का उपयोग करें" else if (lang == "తెలుగు") "అన్‌లాక్ చేయడానికి వేలిముద్ర లేదా ఫేస్ లాక్ ఉపయోగించండి" else "Use your fingerprint or face to authenticate"
            val promptNegative = if (lang == "हिंदी") "पिन का उपयोग करें" else if (lang == "తెలుగు") "పిన్ ఉపయోగించండి" else "Use PIN"

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(promptTitle)
                .setSubtitle(promptSubtitle)
                .setNegativeButtonText(promptNegative)
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    // Auto-authenticate on verify start
    LaunchedEffect(key1 = currentMode) {
        if (currentMode == PinMode.Verify && storageHelper.biometricEnabled) {
            delay(350)
            triggerBiometricAuth()
        }
    }

    // Sensor capability check
    val isBiometricHardwareAvailable = remember {
        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }

    // Numpad key input
    val onDigitPressed: (String) -> Unit = { digit ->
        if (lockoutTimeLeft > 0) {
            val minutesLeft = Math.ceil(lockoutTimeLeft / 1000.0 / 60.0).toInt()
            errorMessage = if (lang == "हिंदी") "${minutesLeft} मिनट बाद पुनः प्रयास करें" else if (lang == "తెలుగు") "${minutesLeft} నిమిషాల తర్వాత ప్రయత్నించండి" else "Try again in $minutesLeft minutes"
        } else if (enteredDigits.length < 4) {
            enteredDigits += digit
            errorMessage = ""

            if (enteredDigits.length == 4) {
                scope.launch {
                    delay(150)
                    val pin = enteredDigits
                    enteredDigits = ""

                    when (val mode = currentMode) {
                        is PinMode.Verify -> {
                            val savedPin = storageHelper.userPin
                            if (savedPin == pin) {
                                storageHelper.clearLockout()
                                onUnlocked()
                            } else {
                                val attempts = storageHelper.wrongAttempts + 1
                                storageHelper.wrongAttempts = attempts
                                if (attempts >= 5) {
                                    storageHelper.setLockout()
                                    lockoutTimeLeft = storageHelper.getLockoutTimeRemaining()
                                    errorMessage = if (lang == "हिंदी") "अत्यधिक गलत प्रयास। 5 मिनट के लिए अवरुद्ध।" else if (lang == "తెలుగు") "ఎక్కువ సార్లు తప్పుగా టైప్ చేసారు. 5 నిమిషాలు లాక్ చేయబడింది." else "Too many wrong attempts. Locked for 5 minutes."
                                } else {
                                    val remaining = 5 - attempts
                                    errorMessage = if (lang == "हिंदी") "गलत पिन। ${remaining} प्रयास शेष।" else if (lang == "తెలుగు") "తప్పు పిన్. ఇంకా ${remaining} సార్లు మాత్రమే అవకాశం ఉంది." else "Incorrect PIN. $remaining attempts remaining."
                                }
                                isShaking = true
                            }
                        }
                        is PinMode.Setup -> {
                            currentMode = PinMode.Confirm(pin)
                            errorMessage = if (lang == "हिंदी") "अपने 4-अंकीय पिन की पुष्टि करें" else if (lang == "తెలుగు") "మీ 4-అంకెల పిన్‌ని నిర్ధారించండి" else "Confirm your 4-digit PIN"
                        }
                        is PinMode.Confirm -> {
                            if (mode.firstPin == pin) {
                                storageHelper.userPin = pin
                                val savedSuccessMsg = if (lang == "हिंदी") "पिन सफलतापूर्वक सहेजा गया!" else if (lang == "తెలుగు") "పిన్ విజయవంతంగా సేవ్ చేయబడింది!" else "PIN Saved Successfully!"
                                Toast.makeText(context, savedSuccessMsg, Toast.LENGTH_SHORT).show()
                                onSetupComplete()
                            } else {
                                errorMessage = if (lang == "हिंदी") "पिन मेल नहीं खाए। फिर से शुरू करें।" else if (lang == "తెలుగు") "పిన్ మ్యాచ్ కాలేదు. మళ్లీ మొదలు పెట్టండి." else "PINs did not match. Start over."
                                isShaking = true
                                delay(1000)
                                currentMode = PinMode.Setup
                            }
                        }
                    }
                }
            }
        }
    }

    val onBackspacePressed = {
        if (enteredDigits.isNotEmpty()) {
            enteredDigits = enteredDigits.dropLast(1)
            errorMessage = ""
        }
    }

    val subTitleLabel = if (lang == "हिंदी") "आपका सुरक्षित चक्र साथी" else if (lang == "తెలుగు") "మీ సురక్షిత సైకిల్ గైడ్" else "YOUR INTIMATE HEALTH PARTNER"

    val instructions = when (currentMode) {
        is PinMode.Verify -> if (lang == "हिंदी") "अनलॉक करने के लिए पिन दर्ज करें" else if (lang == "తెలుగు") "అన్‌లాక్ చేయడానికి పిన్ నమోదు చేయండి" else "Enter PIN to unlock"
        is PinMode.Setup -> if (lang == "हिंदी") "एक सुरक्षित 4-अंकीय पिन सेट करें" else if (lang == "తెలుగు") "సురक्षితమైన 4-అంకెల పిన్ సెట్ చేయండి" else "Set a secure 4-digit PIN"
        is PinMode.Confirm -> if (lang == "हिंदी") "पुष्टि करने के लिए फिर से पिन दर्ज करें" else if (lang == "తెలుగు") "ధృవీకరించడానికి పిన్‌ను మళ్లీ నమోదు చేయండి" else "Re-enter PIN to confirm"
    }

    // Screen Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo and Title Area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = "VelvetCycle",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colors.pinkAccent,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
            
            Text(
                text = subTitleLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            Text(
                text = instructions,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dynamic Dot Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .offset(x = shakeOffset.dp)
                    .padding(vertical = 16.dp)
                    .testTag("pin_dots_container")
            ) {
                for (i in 1..4) {
                    val isFilled = enteredDigits.length >= i
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) colors.pinkAccent else colors.textSecondary.copy(alpha = 0.3f))
                            .testTag("pin_dot_$i")
                    )
                }
            }

            // Error Display Label
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .testTag("pin_error_text")
                )
            }
        }

        // Numpad Column Layout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9")
            )

            keys.forEach { rowKeys ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    rowKeys.forEach { digit ->
                        NumpadButton(digit, onClick = { onDigitPressed(digit) })
                    }
                }
            }

            // Bottom row containing biometric trigger, 0, and backspace
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // Biometrics activation button
                if (currentMode == PinMode.Verify && storageHelper.biometricEnabled && isBiometricHardwareAvailable) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(colors.pinkAccent.copy(alpha = 0.1f))
                            .clickable { triggerBiometricAuth() }
                            .testTag("pin_btn_biometric"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint",
                            tint = colors.pinkAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(72.dp))
                }

                NumpadButton("0", onClick = { onDigitPressed("0") })

                // Backspace button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable { onBackspacePressed() }
                        .testTag("pin_btn_backspace"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Backspace",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Cancel trigger for security settings onboarding
            if (onCancelSetup != null && (currentMode is PinMode.Setup || currentMode is PinMode.Confirm)) {
                Spacer(modifier = Modifier.height(16.dp))
                val cancelSetupLabel = if (lang == "हिंदी") "सेटअप रद्द करें" else if (lang == "తెలుగు") "సెటప్ రద్దు చేయి" else "Cancel Setup"
                TextButton(
                    onClick = onCancelSetup,
                    modifier = Modifier.testTag("pin_cancel_setup_button")
                ) {
                    Text(
                        text = cancelSetupLabel,
                        color = colors.pinkAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun NumpadButton(
    digit: String,
    onClick: () -> Unit
) {
    val colors = LocalVelvetColors.current
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(colors.cardBackground)
            .clickable { onClick() }
            .testTag("pin_btn_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
    }
}
