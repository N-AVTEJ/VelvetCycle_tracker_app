package com.example.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun PinSetupScreen(
    storageHelper: StorageHelper,
    onFinished: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableStateOf(1) }
    var firstPin by remember { mutableStateOf("") }
    var enteredPin by remember { mutableStateOf("") }
    var isShaking by remember { mutableStateOf(false) }
    var flashSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var enableBiometrics by remember { mutableStateOf(storageHelper.biometricEnabled) }

    val handleDigitClick: (String) -> Unit = { digit ->
        if (enteredPin.length < 4) {
            enteredPin += digit
            errorMessage = ""

            if (enteredPin.length == 4) {
                scope.launch {
                    delay(150)
                    val pin = enteredPin
                    enteredPin = ""

                    if (step == 2) {
                        firstPin = pin
                        step = 3
                    } else if (step == 3) {
                        if (firstPin == pin) {
                            flashSuccess = true
                            delay(400)
                            storageHelper.userPin = pin
                            flashSuccess = false
                            step = 4
                        } else {
                            errorMessage = if (lang == "हिंदी") "पिन मेल नहीं खाए। फिर से प्रयास करें।" else if (lang == "తెలుగు") "పిన్ మ్యాచ్ కాలేదు. మళ్లీ ప్రయత్నించండి." else "PINs do not match. Try again."
                            isShaking = true
                            delay(500)
                            isShaking = false
                            enteredPin = ""
                            step = 2 // Go back to step 2 to choose a PIN again
                        }
                    }
                }
            }
        }
    }

    val handleDeleteClick = {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
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
            when (step) {
                1 -> {
                    // STEP 1: Privacy Pitch Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .testTag("setup_step_1"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 40.dp)
                        ) {
                            Text(
                                text = "VelvetCycle",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.pinkAccent,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = if (lang == "हिंदी") "अपनी गोपनीयता की रक्षा करें" else if (lang == "తెలుగు") "మీ గోప్యతను రక్షించుకోండి" else "Protect your privacy",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (lang == "हिंदी") "आपका चक्र डेटा व्यक्तिगत है। यह सुनिश्चित करने के लिए कि केवल आप ही VelvetCycle का उपयोग कर सकें, एक पिन सेट करें।" else if (lang == "తెలుగు") "మీ సైకిల్ డేటా వ్యక్తిగతమైనది. మీరు మాత్రమే VelvetCycle యాక్సెస్ చేయగలరని నిర్ధారించుకోవడానికి పిన్‌ని సెట్ చేయండి." else "Your cycle data is personal. Set a PIN to make sure only you can access VelvetCycle.",
                                fontSize = 15.sp,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // Bullets
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            BulletItem(
                                icon = "🔒",
                                text = if (lang == "हिंदी") "हर बार खोलने पर ऐप लॉक करें" else if (lang == "తెలుగు") "యాప్‌ను ప్రతిసారీ తెరిచినప్పుడు లాక్ చేయండి" else "Lock app on every open"
                            )
                            BulletItem(
                                icon = "📵",
                                text = if (lang == "हिंदी") "पृष्ठभूमि में 2 मिनट के बाद ऑटो-लॉक" else if (lang == "తెలుగు") "నేపథ్యంలో 2 నిమిషాల తర్వాత ఆటో-లాక్ చేయబడుతుంది" else "Auto-lock after 2 minutes in background"
                            )
                            BulletItem(
                                icon = "🧮",
                                text = if (lang == "हिंदी") "कैलेंडर के रूप में वैकल्पिक भेस" else if (lang == "తెలుగు") "కాలిక్యులేటర్గా దాచే ఐచ్ఛికం" else "Optional disguise as calculator"
                            )
                        }

                        // Action Buttons
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { step = 2 },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("setup_set_pin_button")
                            ) {
                                Text(
                                    text = if (lang == "हिंदी") "पिन सेट करें" else if (lang == "తెలుగు") "పిన్ సెట్ చేయండి" else "Set PIN",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = if (lang == "हिंदी") "अभी छोड़ें" else if (lang == "తెలుగు") "ఇప్పుడే దాటవేయి" else "Skip for now",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.pinkAccent,
                                modifier = Modifier
                                    .clickable { onCancel() }
                                    .padding(8.dp)
                                    .testTag("setup_skip_button")
                            )
                        }
                    }
                }
                2, 3 -> {
                    // STEP 2 & 3: Create and Confirm PIN
                    val headerText = if (step == 2) {
                        if (lang == "हिंदी") "पिन बनाएं" else if (lang == "తెలుగు") "పిన్ సృష్టించండి" else "Create a PIN"
                    } else {
                        if (lang == "हिंदी") "अपने पिन की पुष्टि करें" else if (lang == "తెలుగు") "మీ పిన్‌ని నిర్ధారించండి" else "Confirm your PIN"
                    }

                    val subtext = if (step == 2) {
                        if (lang == "हिंदी") "4 अंक चुनें जिन्हें आप याद रखेंगे" else if (lang == "తెలుగు") "మీరు గుర్తుంచుకోగల 4 అంకెలను ఎంచుకోండి" else "Choose 4 digits you will remember"
                    } else {
                        if (lang == "हिंदी") "वही 4 अंक दोबारा दर्ज करें" else if (lang == "తెలుగు") "అదే 4 అంకెలను మళ్లీ నమోదు చేయండి" else "Enter the same 4 digits again"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .testTag("setup_step_$step"),
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
                                enteredLength = enteredPin.length,
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

                        NumberPad(
                            onDigitClick = handleDigitClick,
                            onDeleteClick = handleDeleteClick,
                            showBiometric = false,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
                4 -> {
                    // STEP 4: Success Screen
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
                            .testTag("setup_step_4"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Green check circle
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9))
                                    .testTag("success_checkmark_container"),
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
                                text = if (lang == "हिंदी") "पिन सफलतापूर्वक सेट किया गया" else if (lang == "తెలుగు") "పిన్ విజయవంతంగా సెట్ చేయబడింది" else "PIN set successfully",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (lang == "हिंदी") "VelvetCycle अब सुरक्षित है।" else if (lang == "తెలుగు") "VelvetCycle ఇప్పుడు రక్షించబడింది." else "VelvetCycle is now protected.",
                                fontSize = 15.sp,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            // Biometrics Option Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.cardBackground)
                                    .padding(16.dp)
                                    .testTag("biometric_setup_option"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (lang == "हिंदी") "फिंगरप्रिंट भी सक्षम करें?" else if (lang == "తెలుగు") "వేలిముద్రను కూడా ప్రారంభించాలా?" else "Enable fingerprint too?",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (lang == "हिंदी") "अनलॉक करने का तेज़ और सुरक्षित तरीका" else if (lang == "తెలుగు") "అన్‌లాక్ చేయడానికి వేగవంతమైన మరియు సురక్షితమైన మార్గం" else "Faster, more convenient way to unlock",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Switch(
                                    checked = enableBiometrics,
                                    onCheckedChange = { checked ->
                                        enableBiometrics = checked
                                        storageHelper.biometricEnabled = checked
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colors.pinkAccent,
                                        uncheckedThumbColor = colors.textSecondary,
                                        uncheckedTrackColor = colors.border
                                    ),
                                    modifier = Modifier.testTag("biometric_setup_switch")
                                )
                            }
                        }

                        // Done Button
                        Button(
                            onClick = {
                                storageHelper.isOnboarded = true
                                onFinished()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("setup_success_done_button")
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
}

@Composable
fun BulletItem(icon: String, text: String) {
    val colors = LocalVelvetColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.pinkAccent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
        }
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}
