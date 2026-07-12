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
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightPinkBg
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.White
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var currentMode by remember { mutableStateOf(initialMode) }
    var enteredDigits by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isShaking by remember { mutableStateOf(false) }
    var lockoutTimeLeft by remember { mutableStateOf(storageHelper.getLockoutTimeRemaining()) }

    // Lockout timer polling
    LaunchedEffect(key1 = lockoutTimeLeft) {
        if (lockoutTimeLeft > 0) {
            while (lockoutTimeLeft > 0) {
                delay(1000L)
                lockoutTimeLeft = storageHelper.getLockoutTimeRemaining()
            }
            storageHelper.clearLockout()
        }
    }

    // Shake animation offset
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

    // Trigger Android System Biometric Prompt
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
                        // Ignore general cancels, but log others
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        storageHelper.clearLockout()
                        onUnlocked()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock VelvetCycle")
                .setSubtitle("Use your fingerprint or face to authenticate")
                .setNegativeButtonText("Use PIN")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    // Auto-launch biometric prompt on start if Verify mode and enabled
    LaunchedEffect(key1 = currentMode) {
        if (currentMode == PinMode.Verify && storageHelper.biometricEnabled) {
            delay(300) // slight delay to let Compose render
            triggerBiometricAuth()
        }
    }

    // Check if biometric sensor is available on device
    val isBiometricHardwareAvailable = remember {
        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }

    // Handles digit key taps
    val onDigitPressed: (String) -> Unit = { digit ->
        if (lockoutTimeLeft > 0) {
            errorMessage = "Try again in ${Math.ceil(lockoutTimeLeft / 1000.0 / 60.0).toInt()} minutes"
        } else if (enteredDigits.length < 4) {
            enteredDigits += digit
            errorMessage = ""

            if (enteredDigits.length == 4) {
                scope.launch {
                    delay(150) // let the last dot fill before validating
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
                                    errorMessage = "Too many wrong attempts. Locked for 5 minutes."
                                } else {
                                    errorMessage = "Incorrect PIN. ${5 - attempts} attempts remaining."
                                }
                                isShaking = true
                            }
                        }
                        is PinMode.Setup -> {
                            currentMode = PinMode.Confirm(pin)
                            errorMessage = "Confirm your 4-digit PIN"
                        }
                        is PinMode.Confirm -> {
                            if (mode.firstPin == pin) {
                                storageHelper.userPin = pin
                                Toast.makeText(context, "PIN Saved Successfully!", Toast.LENGTH_SHORT).show()
                                onSetupComplete()
                            } else {
                                errorMessage = "PINs did not match. Start over."
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

    // Screen layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPinkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section with logo & instructions
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = "VelvetCycle",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPink,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
            
            Text(
                text = "YOUR INTIMATE HEALTH PARTNER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText.copy(alpha = 0.5f),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            val instructions = when (currentMode) {
                is PinMode.Verify -> "Enter PIN to unlock"
                is PinMode.Setup -> "Set a secure 4-digit PIN"
                is PinMode.Confirm -> "Re-enter PIN to confirm"
            }
            
            Text(
                text = instructions,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 4 Dot Indicators
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
                            .background(if (isFilled) PrimaryPink else DarkText.copy(alpha = 0.2f))
                            .testTag("pin_dot_$i")
                    )
                }
            }

            // Error Message
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

        // Numpad Keys section
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

            // Last row: Biometrics, 0, Backspace
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // Biometrics Button
                if (currentMode == PinMode.Verify && storageHelper.biometricEnabled && isBiometricHardwareAvailable) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PrimaryPink.copy(alpha = 0.1f))
                            .clickable { triggerBiometricAuth() }
                            .testTag("pin_btn_biometric"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint",
                            tint = PrimaryPink,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    // Blank spacer to maintain grid alignment
                    Spacer(modifier = Modifier.size(72.dp))
                }

                NumpadButton("0", onClick = { onDigitPressed("0") })

                // Backspace Button
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
                        tint = DarkText.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Cancel Button for Setup mode
            if (onCancelSetup != null && (currentMode is PinMode.Setup || currentMode is PinMode.Confirm)) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onCancelSetup,
                    modifier = Modifier.testTag("pin_cancel_setup_button")
                ) {
                    Text(
                        text = "Cancel Setup",
                        color = PrimaryPink,
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
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(White)
            .clickable { onClick() }
            .testTag("pin_btn_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
    }
}
