package com.example.screens

import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import com.example.ui.theme.FertileGreen
import com.example.ui.theme.LocalVelvetColors
import com.example.ui.theme.SecondaryPink
import com.example.ui.theme.Teal
import com.example.utils.StorageHelper
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    storageHelper: StorageHelper
) {
    val context = LocalContext.current
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current
    
    // Biometric Security state
    var isUnlocked by remember { mutableStateOf(false) }
    var authErrorMsg by mutableStateOf<String?>(null)
    var showPinFallbackDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by mutableStateOf(false)

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    val lastPeriodStart = storageHelper.lastPeriodStart
    val cycleLength = storageHelper.cycleLength
    val periodDuration = storageHelper.periodDuration

    val today = LocalDate.now()

    // Header values
    val calendarHeaderLabel = if (lang == "हिंदी") "चक्र कैलेंडर" else if (lang == "తెలుగు") "చక్ర క్యాలెండర్" else "Cycle Calendar"

    // Formatters
    val monthYearText = if (lang == "हिंदी" || lang == "తెలుగు") {
        val monthName = Translations.getMonthName(currentMonth.monthValue, lang)
        "$monthName ${currentMonth.year}"
    } else {
        currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    // Grid details
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val startShift = firstDayOfMonth.dayOfWeek.value - 1 // Monday-start shifts

    val triggerBiometricAuth = {
        val activity = context as? FragmentActivity
        if (activity != null) {
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.d("CalendarScreen", "Biometric error ($errorCode): $errString")
                        authErrorMsg = errString.toString()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        isUnlocked = true
                        authErrorMsg = null
                        Toast.makeText(context, if (lang == "हिंदी") "कैलेंडर अनलॉक हुआ! 🔓" else "Calendar unlocked! 🔓", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        authErrorMsg = if (lang == "हिंदी") "बायोमेट्रिक सत्यापन विफल रहा" else if (lang == "తెలుగు") "ధృవీకరణ విఫలమైంది" else "Authentication failed. Try again."
                    }
                }
            )

            val promptTitle = if (lang == "हिंदी") "स्वास्थ्य डेटा अनलॉक करें" else if (lang == "తెలుగు") "ఆరోగ్య డేటాను అన్‌లాక్ చేయండి" else "Unlock Health Data"
            val promptSubtitle = if (lang == "हिंदी") "चक्र कैलेंडर देखने के लिए अपनी उंगली या चेहरे का उपयोग करें" else if (lang == "తెలుగు") "క్యాలెండర్ చూడటానికి వేలిముద్ర లేదా ఫేస్ ఉపయోగించండి" else "Use fingerprint or face unlock to view cycle calendar"
            val promptNegative = if (lang == "हिंदी") "पिन / रद्द करें" else if (lang == "తెలుగు") "పిన్ / రద్దు" else "Cancel / Use PIN"

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(promptTitle)
                .setSubtitle(promptSubtitle)
                .setNegativeButtonText(promptNegative)
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    // Automatically trigger biometric unlock prompt when calendar is opened
    LaunchedEffect(Unit) {
        if (!isUnlocked) {
            delay(250)
            triggerBiometricAuth()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Header & Lock Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = colors.pinkAccent,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = calendarHeaderLabel,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            if (isUnlocked) {
                IconButton(
                    onClick = { isUnlocked = false },
                    modifier = Modifier.testTag("btn_lock_calendar")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Calendar",
                        tint = colors.pinkAccent
                    )
                }
            }
        }

        if (!isUnlocked) {
            // BIOMETRIC SECURITY LOCK SCREEN
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_biometric_lock_card"),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(colors.pinkAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = colors.pinkAccent,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Text(
                        text = if (lang == "हिंदी") "स्वास्थ्य डेटा सुरक्षित है 🔒" else if (lang == "తెలుగు") "ఆరోగ్య డేటా సురక్షితం 🔒" else "Protected Health Data 🔒",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (lang == "हिंदी") "कैलेंडर स्वास्थ्य डेटा देखने के लिए androidx.biometric का उपयोग करके फ़िंगरप्रिंट या चेहरे का सत्यापन आवश्यक है।"
                        else if (lang == "తెలుగు") "క్యాలెండర్ వివరాలను చూడటానికి androidx.biometric ద్వారా వేలిముద్ర లేదా ఫేస్ అన్‌లాక్ అవసరం."
                        else "Fingerprint or face unlock is required via androidx.biometric before displaying your sensitive menstrual calendar.",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    if (authErrorMsg != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = authErrorMsg!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { triggerBiometricAuth() },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_unlock_biometrics")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == "हिंदी") "बायोमेट्रिक (फ़िंगरप्रिंट / फ़ेस) से अनलॉक करें" else if (lang == "తెలుగు") "బయోమెట్రిక్ తో అన్‌లాక్ చేయండి" else "Unlock with Fingerprint / Face",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (storageHelper.userPin != null) {
                        OutlinedButton(
                            onClick = { showPinFallbackDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_pin_fallback")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = colors.pinkAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "हिंदी") "सुरक्षा पिन दर्ज करें" else "Enter Security PIN",
                                color = colors.pinkAccent,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // PIN Fallback Dialog if requested
            if (showPinFallbackDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showPinFallbackDialog = false
                        enteredPin = ""
                        pinError = false
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = colors.pinkAccent,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = if (lang == "हिंदी") "सुरक्षा पिन दर्ज करें" else "Enter Security PIN",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (lang == "हिंदी") "कैलेंडर देखने के लिए अपना 4-अंकीय पिन दर्ज करें:" else "Enter your 4-digit PIN to access the calendar:",
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                            OutlinedTextField(
                                value = enteredPin,
                                onValueChange = {
                                    if (it.length <= 4) {
                                        enteredPin = it
                                        pinError = false
                                    }
                                },
                                singleLine = true,
                                isError = pinError,
                                placeholder = { Text("****") },
                                modifier = Modifier.testTag("input_fallback_pin")
                            )
                            if (pinError) {
                                Text(
                                    text = if (lang == "हिंदी") "गलत पिन!" else "Incorrect PIN!",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (enteredPin == storageHelper.userPin) {
                                    isUnlocked = true
                                    showPinFallbackDialog = false
                                    enteredPin = ""
                                    pinError = false
                                } else {
                                    pinError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                            modifier = Modifier.testTag("btn_confirm_fallback_pin")
                        ) {
                            Text("Unlock", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showPinFallbackDialog = false
                            enteredPin = ""
                            pinError = false
                        }) {
                            Text("Cancel", color = colors.textSecondary)
                        }
                    }
                )
            }
        } else {
            // UNLOCKED STATE: Show Security Banner and Calendar grid
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("unlocked_health_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (lang == "हिंदी") "स्वास्थ्य डेटा सुरक्षित रूप से अनलॉक किया गया (androidx.biometric)" else "Health Data Authenticated via androidx.biometric",
                        fontSize = 12.sp,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Calendar Grid Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_grid_card"),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = colors.pinkAccent)
                    }
                    Text(
                        text = monthYearText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month", tint = colors.pinkAccent)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weekday initials
                val weekdays = when (lang) {
                    "हिंदी" -> listOf("सोम", "मं", "बुध", "गु", "शु", "श", "र")
                    "తెలుగు" -> listOf("సోమ", "మం", "బుధ", "గురు", "శుక్ర", "శని", "ఆది")
                    else -> listOf("M", "T", "W", "T", "F", "S", "S")
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekdays.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days Grid
                val totalSlots = startShift + daysInMonth
                val rows = (totalSlots + 6) / 7

                for (rowIndex in 0 until rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (colIndex in 0 until 7) {
                            val slotIndex = rowIndex * 7 + colIndex
                            val dayOfMonth = slotIndex - startShift + 1

                            if (dayOfMonth in 1..daysInMonth) {
                                val date = currentMonth.atDay(dayOfMonth)
                                
                                val isPeriod = isPeriodDay(date, lastPeriodStart, cycleLength, periodDuration)
                                val isFertile = isFertileDay(date, lastPeriodStart, cycleLength)
                                val isOvulation = isOvulationDay(date, lastPeriodStart, cycleLength)
                                val isPrepare = isPrepareDay(date, lastPeriodStart, cycleLength)
                                val isToday = date == today
                                val isSelected = date == selectedDate

                                val cellBgColor = when {
                                    isPeriod -> SecondaryPink
                                    isFertile -> FertileGreen
                                    else -> colors.cardBackground
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(cellBgColor)
                                        .border(
                                            width = if (isToday) 2.dp else if (isSelected) 1.dp else 0.dp,
                                            color = if (isToday) colors.textPrimary else if (isSelected) colors.pinkAccent.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedDate = date },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayOfMonth.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isPeriod && isSelected) colors.pinkAccent else colors.textPrimary
                                        )
                                        
                                        if (isOvulation) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(Teal)
                                            )
                                        } else if (isPrepare) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFF8C42))
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Legend
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val labelPeriod = if (lang == "हिंदी") "मासिक धर्म का दिन" else if (lang == "తెలుగు") "పీరియడ్ రోజు" else "Period day"
                    val labelFertile = if (lang == "हिंदी") "उर्वर अवधि" else if (lang == "తెలుగు") "సంతానోత్పత్తి సమయం" else "Fertile window"
                    val labelOvulation = if (lang == "हिंदी") "डिंबोत्सर्जन दिवस" else if (lang == "తెలుగు") "అండవిడుదల రోజు" else "Ovulation day"
                    val labelPrepare = Translations.t("prepare_day", lang)

                    LegendItem(color = SecondaryPink, label = labelPeriod, isDot = false)
                    LegendItem(color = FertileGreen, label = labelFertile, isDot = false)
                    LegendItem(color = Teal, label = labelOvulation, isDot = true)
                    LegendItem(color = Color(0xFFFF8C42), label = labelPrepare, isDot = true)
                }
            }
        }

        // Selected Day Details Panel
        selectedDate?.let { date ->
            val isPeriod = isPeriodDay(date, lastPeriodStart, cycleLength, periodDuration)
            val isFertile = isFertileDay(date, lastPeriodStart, cycleLength)
            val isOvulation = isOvulationDay(date, lastPeriodStart, cycleLength)
            val isPrepare = isPrepareDay(date, lastPeriodStart, cycleLength)

            val statusTitle = when {
                isPeriod -> if (lang == "हिंदी") "मासिक धर्म का दिन" else if (lang == "తెలుగు") "పీరియడ్ రోజు" else "Period Day"
                isPrepare -> Translations.t("prepare_day", lang)
                isOvulation -> if (lang == "हिंदी") "डिंबोत्सर्जन दिवस" else if (lang == "తెలుగు") "అండవిడుదల రోజు" else "Ovulation Day"
                isFertile -> if (lang == "हिंदी") "उर्वर अवधि (Fertility)" else if (lang == "తెలుగు") "సంతానోత్పత్తి సమయం" else "Fertile Window"
                else -> if (lang == "हिंदी") "सामान्य दिन" else if (lang == "తెలుగు") "సాధారణ రోజు" else "Regular Day"
            }

            val statusColor = when {
                isPeriod -> colors.pinkAccent
                isPrepare -> Color(0xFFFF8C42)
                isOvulation -> Teal
                isFertile -> colors.textPrimary
                else -> colors.textSecondary
            }

            val statusDescription = when {
                isPeriod -> if (lang == "हिंदी") "मासिक धर्म चरण। पर्याप्त विश्राम और गर्म पानी पिएं।" else if (lang == "తెలుగు") "పీరియడ్ సమయం. తగినంత విశ్రాంతి తీసుకోండి." else "Expected menstrual phase. Rest and hydrate well."
                isPrepare -> if (lang == "हिंदी") "कल मासिक धर्म होने की उम्मीद है। अपना पैड स्टॉक जांचें और एक आसान दिन की योजना बनाएं।" else if (lang == "తెలుగు") "మీ పీరియడ్ రేపు రావచ్చు. మీ ప్యాడ్ స్టాక్ సరిచూసుకోండి మరియు తేలికపాటి రోజును ప్లాన్ చేయండి." else "Period expected tomorrow. Check your pad stock and plan a gentle day."
                isOvulation -> if (lang == "हिंदी") "अंडा जारी होने का चरम! गर्भधारण की संभावना सबसे अधिक।" else if (lang == "తెలుగు") "అండవిడుదల సమయం! గర్భం దాల్చడానికి ఎక్కువ అవకాశాలు ఉన్నాయి." else "Egg release peak! Conception chances are very high."
                isFertile -> if (lang == "हिंदी") "गर्भधारण के लिए अनुकूल समय। स्वस्थ रहें।" else if (lang == "తెలుగు") "గర్భం దాల్చడానికి అనుకూల సమయం. ఆరోగ్యం పట్ల శ్రద్ధ వహించండి." else "Favorable time for conception. General wellness."
                else -> if (lang == "हिंदी") "गर्भधारण की कम संभावना। सामान्य चक्र दिवस।" else if (lang == "తెలుగు") "గర్భం దాల్చడానికి तక్కువ అవకాశాలు ఉన్నాయి. సాధారణ చక్రం రోజు." else "Low chances of conception. Normal cycle day."
            }

            val log = storageHelper.getLog(date.toString())

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("date_details_card"),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = Translations.formatDate(date, lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isPeriod -> colors.pinkAccent
                                        isPrepare -> Color(0xFFFF8C42)
                                        isOvulation -> Teal
                                        isFertile -> FertileGreen
                                        else -> colors.border
                                    }
                                )
                        )
                        Text(
                            text = statusTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = statusDescription,
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (log != null) {
                        Text(
                            text = Translations.t("log_today", lang),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.pinkAccent,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (log.mood.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.background)
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(log.mood, fontSize = 28.sp)
                                }
                            }
                            
                            Column {
                                Text(
                                    text = Translations.t("flow_today", lang),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary
                                )
                                val flowLabel = when (log.flow.lowercase().trim()) {
                                    "spotting" -> Translations.t("flow_spotting", lang)
                                    "light" -> Translations.t("flow_light", lang)
                                    "medium" -> Translations.t("flow_medium", lang)
                                    "heavy" -> Translations.t("flow_heavy", lang)
                                    else -> Translations.t("flow_none", lang)
                                }
                                Text(
                                    text = flowLabel,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            }
                        }
                        
                        if (log.symptoms.isNotEmpty()) {
                            Text(
                                text = Translations.t("symptoms", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                log.symptoms.forEach { symptom ->
                                    val translatedSymptom = when (symptom.lowercase().trim()) {
                                        "cramps" -> Translations.t("sym_cramps", lang)
                                        "headache" -> Translations.t("sym_headache", lang)
                                        "bloating" -> Translations.t("sym_bloating", lang)
                                        "acne" -> Translations.t("sym_acne", lang)
                                        "fatigue" -> Translations.t("sym_fatigue", lang)
                                        "mood swings" -> Translations.t("sym_mood_swings", lang)
                                        "backache" -> Translations.t("sym_backache", lang)
                                        "breast tenderness" -> Translations.t("sym_breast_tenderness", lang)
                                        "cravings" -> Translations.t("sym_cravings", lang)
                                        "nausea" -> Translations.t("sym_nausea", lang)
                                        else -> symptom
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.pinkAccent.copy(alpha = 0.08f))
                                            .border(1.dp, colors.pinkAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(translatedSymptom, fontSize = 11.sp, color = colors.pinkAccent, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                        
                        if (log.notes.isNotEmpty()) {
                            Text(
                                text = Translations.t("notes", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = log.notes,
                                fontSize = 13.sp,
                                color = colors.textPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        Text(
                            text = if (lang == "हिंदी") "इस दिन के लिए कोई रिकॉर्ड नहीं है।" else if (lang == "తెలుగు") "ఈ రోజు కోసం లాగ్స్ లేవు." else "No log for this day.",
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.testTag("no_log_text")
                        )
                    }
                }
            }
        }
    }
}
}

private fun isPeriodDay(date: LocalDate, lastPeriodStart: LocalDate, cycleLength: Int, periodDuration: Int): Boolean {
    val daysBetween = ChronoUnit.DAYS.between(lastPeriodStart, date)
    val mod = if (daysBetween >= 0) {
        daysBetween % cycleLength
    } else {
        ((daysBetween % cycleLength) + cycleLength) % cycleLength
    }
    return mod < periodDuration
}

private fun isOvulationDay(date: LocalDate, lastPeriodStart: LocalDate, cycleLength: Int): Boolean {
    val daysBetween = ChronoUnit.DAYS.between(lastPeriodStart, date)
    val mod = if (daysBetween >= 0) {
        daysBetween % cycleLength
    } else {
        ((daysBetween % cycleLength) + cycleLength) % cycleLength
    }
    val ovulationDayNum = cycleLength - 14 - 1
    return mod == ovulationDayNum.toLong()
}

private fun isFertileDay(date: LocalDate, lastPeriodStart: LocalDate, cycleLength: Int): Boolean {
    val daysBetween = ChronoUnit.DAYS.between(lastPeriodStart, date)
    val mod = if (daysBetween >= 0) {
        daysBetween % cycleLength
    } else {
        ((daysBetween % cycleLength) + cycleLength) % cycleLength
    }
    val ovulationDayNum = cycleLength - 14 - 1
    val fertileStart = ovulationDayNum - 5
    val fertileEnd = ovulationDayNum + 1
    return mod in fertileStart..fertileEnd
}

private fun isPrepareDay(date: LocalDate, lastPeriodStart: LocalDate, cycleLength: Int): Boolean {
    val daysBetween = ChronoUnit.DAYS.between(lastPeriodStart, date)
    val mod = if (daysBetween >= 0) {
        daysBetween % cycleLength
    } else {
        ((daysBetween % cycleLength) + cycleLength) % cycleLength
    }
    return mod == (cycleLength - 1).toLong()
}

@Composable
private fun LegendItem(color: Color, label: String, isDot: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (isDot) 8.dp else 12.dp)
                .clip(if (isDot) CircleShape else RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = LocalVelvetColors.current.textSecondary
        )
    }
}
