package com.example.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LocalAppLanguage
import com.example.components.NumberPad
import com.example.components.PinDots
import com.example.ui.theme.LocalVelvetColors
import com.example.utils.StorageHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PinChangeScreen(
    storageHelper: StorageHelper,
    onFinished: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableStateOf(1) } // 1: Verify Old, 2: Enter New, 3: Confirm New, 4: Success
    var oldPinEntered by remember { mutableStateOf("") }
    var newPin1 by remember { mutableStateOf("") }
    var currentPinInput by remember { mutableStateOf("") }

    var localWrongTries by remember { mutableStateOf(0) }
    var isShaking by remember { mutableStateOf(false) }
    var flashSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val handleDigitClick: (String) -> Unit = { digit ->
        if (localWrongTries >= 3) {
            errorMessage = if (lang == "हिंदी") "बहुत सारे प्रयास। बाद में पुनः प्रयास करें।" else if (lang == "తెలుగు") "చాలా సార్లు తప్పుగా టైప్ చేసారు. తర్వాత ప్రయత్నించండి." else "Too many attempts. Try again later."
        } else if (currentPinInput.length < 4) {
            currentPinInput += digit
            errorMessage = ""

            if (currentPinInput.length == 4) {
                scope.launch {
                    delay(150)
                    val input = currentPinInput
                    currentPinInput = ""

                    when (step) {
                        1 -> {
                            // Verify current PIN
                            val savedPin = storageHelper.userPin
                            if (savedPin == input) {
                                flashSuccess = true
                                delay(300)
                                flashSuccess = false
                                step = 2
                            } else {
                                localWrongTries++
                                isShaking = true
                                if (localWrongTries >= 3) {
                                    errorMessage = if (lang == "हिंदी") "बहुत सारे प्रयास। बाद में पुनः प्रयास करें।" else if (lang == "తెలుగు") "చాలా సార్లు తప్పుగా టైప్ చేసారు. తర్వాత ప్రయత్నించండి." else "Too many attempts. Try again later."
                                } else {
                                    val remaining = 3 - localWrongTries
                                    errorMessage = if (lang == "हिंदी") "गलत पिन। $remaining प्रयास शेष।" else if (lang == "తెలుగు") "తప్పు పిన్. ఇంకా $remaining సార్లు మాత్రమే అవకాశం ఉంది." else "Incorrect PIN. $remaining attempts remaining."
                                }
                                delay(500)
                                isShaking = false
                            }
                        }
                        2 -> {
                            newPin1 = input
                            step = 3
                        }
                        3 -> {
                            if (newPin1 == input) {
                                flashSuccess = true
                                delay(400)
                                storageHelper.userPin = input
                                flashSuccess = false
                                step = 4
                            } else {
                                errorMessage = if (lang == "हिंदी") "पिन मेल नहीं खाए। फिर से शुरू करें।" else if (lang == "తెలుగు") "పిన్ మ్యాచ్ కాలేదు. మళ్లీ మొదలు పెట్టండి." else "PINs do not match. Start over."
                                isShaking = true
                                delay(500)
                                isShaking = false
                                step = 2 // back to enter new
                            }
                        }
                    }
                }
            }
        }
    }

    val handleDeleteClick = {
        if (localWrongTries < 3 && currentPinInput.isNotEmpty()) {
            currentPinInput = currentPinInput.dropLast(1)
            errorMessage = ""
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (step < 4) {
                val headerText = when (step) {
                    1 -> if (lang == "हिंदी") "वर्तमान पिन दर्ज करें" else if (lang == "తెలుగు") "ప్రస్తుత పిన్ నమోదు చేయండి" else "Enter current PIN"
                    2 -> if (lang == "हिंदी") "नया पिन दर्ज करें" else if (lang == "తెలుగు") "కొత్త పిన్ నమోదు చేయండి" else "Enter new PIN"
                    else -> if (lang == "हिंदी") "नए पिन की पुष्टि करें" else if (lang == "తెలుగు") "కొత్త పిన్‌ను నిర్ధారించండి" else "Confirm new PIN"
                }

                val subtext = when (step) {
                    1 -> if (lang == "हिंदी") "अपनी पहचान सत्यापित करें" else if (lang == "తెలుగు") "మీ గుర్తింపును ధృవీకరించండి" else "Verify your identity"
                    2 -> if (lang == "हिंदी") "एक नया 4-अंकीय पिन चुनें" else if (lang == "తెలుగు") "కొత్త 4-అంకెల పిన్‌ను ఎంచుకోండి" else "Choose a new 4-digit PIN"
                    else -> if (lang == "हिंदी") "अपना नया पिन पुनः दर्ज करें" else if (lang == "తెలుగు") "మీ కొత్త పిన్‌ను మళ్లీ నమోదు చేయండి" else "Re-enter your new PIN"
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .testTag("change_step_$step"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        Text(
                            text = headerText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = subtext,
                            fontSize = 15.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(40.dp))

                        PinDots(
                            enteredLength = currentPinInput.length,
                            isShaking = isShaking,
                            flashSuccess = flashSuccess,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        NumberPad(
                            onDigitClick = handleDigitClick,
                            onDeleteClick = handleDeleteClick,
                            showBiometric = false,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        TextButton(
                            onClick = onCancel,
                            modifier = Modifier.testTag("change_cancel_button")
                        ) {
                            Text(
                                text = if (lang == "हिंदी") "रद्द करें" else if (lang == "తెలుగు") "రద్దు చేయి" else "Cancel",
                                color = colors.pinkAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            } else {
                // Success step
                val checkmarkScale = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    checkmarkScale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .testTag("change_step_4"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9))
                                .testTag("change_success_checkmark_container"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .size(48.dp)
                                    .graphicsLayer {
                                        scaleX = checkmarkScale.value
                                        scaleY = checkmarkScale.value
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = if (lang == "हिंदी") "पिन सफलतापूर्वक बदला गया" else if (lang == "తెలుగు") "పిన్ విజయవంతంగా మార్చబడింది" else "PIN changed successfully",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (lang == "हिंदी") "VelvetCycle अब नए पिन से सुरक्षित है।" else if (lang == "తెలుగు") "VelvetCycle ఇప్పుడు కొత్త పిన్‌తో రక్షించబడింది." else "VelvetCycle is now secured with your new PIN.",
                            fontSize = 15.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = onFinished,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("change_success_done_button")
                    ) {
                        Text(
                            text = if (lang == "हिंदी") "हो गया" else if (lang == "తెలుగు") "పూర్తయింది" else "Done",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
