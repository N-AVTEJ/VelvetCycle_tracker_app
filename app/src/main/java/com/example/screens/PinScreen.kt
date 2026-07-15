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
    onCancelSetup: (() -> Unit)? = null,
    onEmergencyWipe: () -> Unit = {}
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
    var showEmergencyConfirm by remember { mutableStateOf(false) }

    // Calculator state
    var calcExpression by remember { mutableStateOf("") }
    var calcResult by remember { mutableStateOf("") }

    // Lockout countdown loop
    LaunchedEffect(key1 = lockoutTimeLeft) {
        if (lockoutTimeLeft > 0) {
            while (lockoutTimeLeft > 0) {
                delay(1000L)
                lockoutTimeLeft = storageHelper.getLockoutTimeRemaining()
            }
            storageHelper.clearLockout()
            errorMessage = ""
            if (calcResult.startsWith("LOCKED") || calcResult.startsWith("Locked")) {
                calcResult = ""
            }
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
        if (activity != null && storageHelper.biometricEnabled && currentMode == PinMode.Verify && lockoutTimeLeft <= 0) {
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

    // Auto-authenticate on verify start (unless disguised or locked out)
    LaunchedEffect(key1 = currentMode) {
        if (currentMode == PinMode.Verify && storageHelper.biometricEnabled && !storageHelper.disguiseMode && lockoutTimeLeft <= 0) {
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

    // Mathematical Evaluator for Calculator
    val evaluateMathExpression = { expr: String ->
        if (expr.isEmpty()) ""
        else {
            try {
                val cleanExpr = expr.replace("×", "*").replace("÷", "/")
                val tokens = mutableListOf<String>()
                var currentNumber = StringBuilder()
                
                for (char in cleanExpr) {
                    if (char in '0'..'9' || char == '.') {
                        currentNumber.append(char)
                    } else if (char in listOf('+', '-', '*', '/')) {
                        if (currentNumber.isNotEmpty()) {
                            tokens.add(currentNumber.toString())
                            currentNumber = StringBuilder()
                        }
                        tokens.add(char.toString())
                    }
                }
                if (currentNumber.isNotEmpty()) {
                    tokens.add(currentNumber.toString())
                }
                
                if (tokens.isEmpty()) ""
                else {
                    var i = 0
                    while (i < tokens.size) {
                        if (tokens[i] == "*" || tokens[i] == "/") {
                            val op = tokens[i]
                            val prev = tokens.getOrNull(i - 1)?.toDoubleOrNull()
                            val next = tokens.getOrNull(i + 1)?.toDoubleOrNull()
                            if (prev != null && next != null) {
                                val result = if (op == "*") prev * next else prev / next
                                tokens[i - 1] = result.toString()
                                tokens.removeAt(i)
                                tokens.removeAt(i)
                                i--
                            } else {
                                "Error"
                            }
                        } else {
                            i++
                        }
                    }
                    
                    var total = tokens.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                    i = 1
                    while (i < tokens.size) {
                        val op = tokens[i]
                        val next = tokens.getOrNull(i + 1)?.toDoubleOrNull()
                        if (next != null) {
                            total = if (op == "+") total + next else total - next
                            i += 2
                        } else {
                            break
                        }
                    }
                    
                    val intValue = total.toLong()
                    if (total == intValue.toDouble()) {
                        intValue.toString()
                    } else {
                        String.format("%.4f", total).trimEnd('0').trimEnd('.')
                    }
                }
            } catch (e: Exception) {
                "Error"
            }
        }
    }

    // Handles Wrong Attempts with Tiers
    val handleWrongAttempt = {
        val attempts = storageHelper.wrongAttempts + 1
        storageHelper.wrongAttempts = attempts
        
        if (attempts >= 10) {
            storageHelper.setLockout(24 * 60 * 60 * 1000L) // 24 hours
            lockoutTimeLeft = storageHelper.getLockoutTimeRemaining()
            errorMessage = if (lang == "हिंदी") "अत्यधिक गलत प्रयास। डेटा सुरक्षा मोड सक्रिय है।" else if (lang == "తెలుగు") "చాలా సార్లు తప్పుగా టైప్ చేసారు. డేటా ప్రొటెక్షన్ మోడ్ యాక్టివ్ చేయబడింది." else "Too many failed attempts. Data protection mode active."
            calcResult = "LOCKED (24h)"
        } else if (attempts >= 7) {
            storageHelper.setLockout(30 * 60 * 1000L) // 30 minutes
            lockoutTimeLeft = storageHelper.getLockoutTimeRemaining()
            errorMessage = if (lang == "हिंदी") "अत्यधिक गलत प्रयास। 30 मिनट के लिए अवरुद्ध।" else if (lang == "తెలుగు") "ఎక్కువ సార్లు తప్పుగా టైప్ చేసారు. 30 నిమిషాలు లాక్ చేయబడింది." else "Too many wrong attempts. Locked for 30 minutes."
            calcResult = "LOCKED (30m)"
        } else if (attempts >= 5) {
            storageHelper.setLockout(5 * 60 * 1000L) // 5 minutes
            lockoutTimeLeft = storageHelper.getLockoutTimeRemaining()
            errorMessage = if (lang == "हिंदी") "अत्यधिक गलत प्रयास। 5 मिनट के लिए अवरुद्ध।" else if (lang == "తెలుగు") "ఎక్కువ సార్లు తప్పుగా టైప్ చేసారు. 5 నిమిషాలు లాక్ చేయబడింది." else "Too many wrong attempts. Locked for 5 minutes."
            calcResult = "LOCKED (5m)"
        } else if (attempts >= 3) {
            storageHelper.setLockout(30 * 1000L) // 30 seconds
            lockoutTimeLeft = storageHelper.getLockoutTimeRemaining()
            errorMessage = if (lang == "हिंदी") "अत्यधिक गलत प्रयास। 30 सेकंड के लिए अवरुद्ध।" else if (lang == "తెలుగు") "ఎక్కువ सార్లు తప్పుగా టైప్ చేసారు. 30 సెకన్లు లాక్ చేయబడింది." else "Too many wrong attempts. Locked for 30 seconds."
            calcResult = "LOCKED (30s)"
        } else {
            val remaining = 3 - attempts // attempts before 1st lockout
            val rem = if (remaining > 0) remaining else 1
            errorMessage = if (lang == "हिंदी") "गलत पिन। ${rem} प्रयास शेष।" else if (lang == "తెలుగు") "తప్పు పిన్. ఇంకా ${rem} సార్లు మాత్రమే అవకాశం ఉంది." else "Incorrect PIN. $rem attempts remaining."
            calcResult = "Wrong PIN"
            isShaking = true
        }
    }

    // Normal PIN input
    val onDigitPressed: (String) -> Unit = { digit ->
        if (lockoutTimeLeft > 0) {
            val secondsLeft = (lockoutTimeLeft / 1000L)
            errorMessage = if (lang == "हिंदी") "कृपया $secondsLeft सेकंड बाद पुनः प्रयास करें" else if (lang == "తెలుగు") "దయచేసి $secondsLeft సెకన్ల తర్వాత ప్రయత్నించండి" else "Try again in $secondsLeft seconds"
        } else if (enteredDigits.length < 4) {
            enteredDigits += digit
            errorMessage = ""

            if (enteredDigits.length == 4) {
                scope.launch {
                    delay(150)
                    val pin = enteredDigits
                    enteredDigits = ""

                    if (pin == "0000" && currentMode == PinMode.Verify) {
                        showEmergencyConfirm = true
                    } else {
                        when (val mode = currentMode) {
                            is PinMode.Verify -> {
                                val savedPin = storageHelper.userPin
                                if (savedPin == pin) {
                                    storageHelper.clearLockout()
                                    onUnlocked()
                                } else {
                                    handleWrongAttempt()
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
    }

    val onBackspacePressed = {
        if (enteredDigits.isNotEmpty()) {
            enteredDigits = enteredDigits.dropLast(1)
            errorMessage = ""
        }
    }

    // Emergency Wipe confirmation Dialog
    if (showEmergencyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmergencyConfirm = false },
            title = {
                Text(
                    text = if (lang == "हिंदी") "आपातकालीन डेटा मिटाएं" else if (lang == "తెలుగు") "అత్యవసర డేటా తుడిచివేయి" else "Emergency Data Wipe",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = if (lang == "हिंदी") "क्या आप वाकई सारा डेटा स्थायी रूप से मिटाना चाहते हैं और ऐप को रीसेट करना चाहते हैं? इसे वापस नहीं लिया जा सकता।" else if (lang == "తెలుగు") "మీరు నిజంగా మొత్తం డేటాను శాశ్వతంగా తుడిచివేసి, యాప్‌ని రీసెట్ చేయాలనుకుంటున్నారా? దీనిని తిరిగి పొందలేరు." else "Are you sure you want to permanently erase all data and reset the app? This cannot be undone.",
                    color = colors.textPrimary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEmergencyConfirm = false
                        onEmergencyWipe()
                    }
                ) {
                    Text(
                        text = if (lang == "हिंदी") "डेटा मिटाएं" else if (lang == "తెలుగు") "డేటాను తుడిచివేయి" else "Wipe All Data",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyConfirm = false }) {
                    Text(
                        text = if (lang == "हिंदी") "रद्द करें" else if (lang == "తెలుగు") "రద్దు చేయి" else "Cancel",
                        color = colors.textSecondary
                    )
                }
            },
            containerColor = colors.cardBackground
        )
    }

    val isDisguiseActive = storageHelper.disguiseMode && currentMode == PinMode.Verify

    if (isDisguiseActive) {
        // --- 100% DISGUISED CALCULATOR MODE LAYOUT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F0F)) // Neutral black background
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Calculator Display Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Expression line
                Text(
                    text = calcExpression.ifEmpty { "0" },
                    fontSize = 32.sp,
                    color = Color(0xFFA5A5A5),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth().testTag("calc_expression")
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Result line (showing countdown timer if locked)
                val displayResultText = if (lockoutTimeLeft > 0) {
                    val totalSecs = (lockoutTimeLeft / 1000L).toInt()
                    val mins = totalSecs / 60
                    val secs = totalSecs % 60
                    String.format("Locked: %02d:%02d", mins, secs)
                } else {
                    calcResult
                }

                Text(
                    text = displayResultText,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth().testTag("calc_result")
                )
            }

            // Calculator Button Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: C, Backspace, ÷
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CalculatorButton(
                        label = "C",
                        backgroundColor = Color(0xFFA5A5A5),
                        textColor = Color.Black,
                        modifier = Modifier.weight(1f)
                    ) {
                        calcExpression = ""
                        calcResult = ""
                    }
                    CalculatorButton(
                        label = "⌫",
                        backgroundColor = Color(0xFFA5A5A5),
                        textColor = Color.Black,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (calcExpression.isNotEmpty()) {
                            calcExpression = calcExpression.dropLast(1)
                        }
                    }
                    CalculatorButton(
                        label = "÷",
                        backgroundColor = Color(0xFFFF9F0A),
                        textColor = Color.White,
                        modifier = Modifier.weight(1f)
                    ) {
                        calcExpression += "÷"
                    }
                }

                // Row 2: 7, 8, 9, ×
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("7", "8", "9").forEach { num ->
                        CalculatorButton(
                            label = num,
                            backgroundColor = Color(0xFF333333),
                            textColor = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            calcExpression += num
                        }
                    }
                    CalculatorButton(
                        label = "×",
                        backgroundColor = Color(0xFFFF9F0A),
                        textColor = Color.White,
                        modifier = Modifier.weight(1f)
                    ) {
                        calcExpression += "×"
                    }
                }

                // Row 3: 4, 5, 6, -
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("4", "5", "6").forEach { num ->
                        CalculatorButton(
                            label = num,
                            backgroundColor = Color(0xFF333333),
                            textColor = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            calcExpression += num
                        }
                    }
                    CalculatorButton(
                        label = "-",
                        backgroundColor = Color(0xFFFF9F0A),
                        textColor = Color.White,
                        modifier = Modifier.weight(1f)
                    ) {
                        calcExpression += "-"
                    }
                }

                // Row 4: 1, 2, 3, +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("1", "2", "3").forEach { num ->
                        CalculatorButton(
                            label = num,
                            backgroundColor = Color(0xFF333333),
                            textColor = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            calcExpression += num
                        }
                    }
                    CalculatorButton(
                        label = "+",
                        backgroundColor = Color(0xFFFF9F0A),
                        textColor = Color.White,
                        modifier = Modifier.weight(1f)
                    ) {
                        calcExpression += "+"
                    }
                }

                // Row 5: 0, ., =
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CalculatorButton(
                        label = "0",
                        backgroundColor = Color(0xFF333333),
                        textColor = Color.White,
                        modifier = Modifier.weight(1.5f)
                    ) {
                        calcExpression += "0"
                    }
                    CalculatorButton(
                        label = ".",
                        backgroundColor = Color(0xFF333333),
                        textColor = Color.White,
                        modifier = Modifier.weight(0.75f)
                    ) {
                        calcExpression += "."
                    }
                    CalculatorButton(
                        label = "=",
                        backgroundColor = Color(0xFFFF9F0A),
                        textColor = Color.White,
                        modifier = Modifier.weight(0.75f)
                    ) {
                        val cleanExpr = calcExpression.trim()
                        if (cleanExpr == "0000") {
                            showEmergencyConfirm = true
                        } else if (storageHelper.userPin != null && cleanExpr == storageHelper.userPin) {
                            storageHelper.clearLockout()
                            onUnlocked()
                        } else {
                            if (lockoutTimeLeft > 0) {
                                val secondsRemaining = (lockoutTimeLeft / 1000L)
                                calcResult = "Locked: ${secondsRemaining}s"
                            } else {
                                // If 4 digits only and mismatch -> register wrong attempt
                                if (cleanExpr.length == 4 && cleanExpr.all { it.isDigit() }) {
                                    handleWrongAttempt()
                                } else {
                                    val ev = evaluateMathExpression(cleanExpr)
                                    calcResult = ev
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- BEAUTIFUL STANDARD VELVETCYCLE PIN DESIGN ---
        val subTitleLabel = if (lang == "हिंदी") "आपका सुरक्षित चक्र साथी" else if (lang == "తెలుగు") "మీ సురక్షిత సైకిల్ గైడ్" else "YOUR INTIMATE HEALTH PARTNER"

        val instructions = when (currentMode) {
            is PinMode.Verify -> if (lang == "हिंदी") "अनलॉक करने के लिए पिन दर्ज करें" else if (lang == "తెలుగు") "అన్‌లాక్ చేయడానికి పిన్ నమోదు చేయండి" else "Enter PIN to unlock"
            is PinMode.Setup -> if (lang == "हिंदी") "एक सुरक्षित 4-अंकीय पिन सेट करें" else if (lang == "తెలుగు") "సురక్షితమైన 4-అంకెల పిన్ సెట్ చేయండి" else "Set a secure 4-digit PIN"
            is PinMode.Confirm -> if (lang == "हिंदी") "पुष्टि करने के लिए फिर से पिन दर्ज करें" else if (lang == "తెలుగు") "ధృవీకరించడానికి పిన్‌ను మళ్లీ నమోదు చేయండి" else "Re-enter PIN to confirm"
        }

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
}

@Composable
fun CalculatorButton(
    label: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() }
            .testTag("calc_btn_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
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
