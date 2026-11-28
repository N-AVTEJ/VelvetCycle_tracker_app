package com.example.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LocalAppLanguage
import com.example.constants.Translations
import com.example.ui.theme.LocalVelvetColors
import com.example.ui.theme.Teal
import com.example.utils.ComfortModeHelper
import com.example.utils.CycleEngine
import com.example.utils.ShareMessages
import com.example.utils.StorageHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    storageHelper: StorageHelper
) {
    val uriHandler = LocalUriHandler.current
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current
    
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    // Load user cycle profile
    val lastPeriodStart = storageHelper.lastPeriodStart
    val cycleLength = storageHelper.cycleLength
    val periodDuration = storageHelper.periodDuration
    val userName = storageHelper.userName
    val padStore = storageHelper.padStore

    val today = LocalDate.now()

    // Calculations using CycleEngine
    val currentCycleDay = CycleEngine.getCurrentCycleDay(lastPeriodStart, cycleLength, today)
    val currentPhase = CycleEngine.getCurrentPhase(lastPeriodStart, cycleLength, periodDuration, today)
    val daysUntilNextPeriod = CycleEngine.getDaysUntilNextPeriod(lastPeriodStart, cycleLength, today)
    val isDayBefore = CycleEngine.isDayBeforePeriod(lastPeriodStart, cycleLength, today)

    // Phase 19 & 21: Anomaly detection, Missed Period, and Cycle Confidence Score (purely local, encrypted SecureStore)
    val cycleRecords = remember(lastPeriodStart, cycleLength) { storageHelper.getCycleHistory() }
    val cycleHistory = remember(cycleRecords) {
        cycleRecords.take(6).map { it.duration }.filter { it > 0 }
    }
    val anomalies = remember(cycleHistory, cycleLength, periodDuration) {
        CycleEngine.detectAnomalies(cycleHistory, cycleLength, periodDuration)
    }
    val isMissed = remember(lastPeriodStart, cycleLength, today) {
        CycleEngine.isMissedPeriod(lastPeriodStart, cycleLength, today)
    }
    val confidence = remember(cycleHistory) {
        CycleEngine.getConfidenceScore(cycleHistory)
    }
    var showConfidenceDialog by remember { mutableStateOf(false) }

    // Phase 22: Partner Share-a-Day state
    val context = LocalContext.current
    var showShareButton by remember { mutableStateOf(storageHelper.showShareButtonOnHome) }
    var customShareMessages by remember { mutableStateOf(storageHelper.getCustomShareMessages()) }
    LaunchedEffect(Unit) {
        showShareButton = storageHelper.showShareButtonOnHome
        customShareMessages = storageHelper.getCustomShareMessages()
    }
    var showSharePreviewDialog by remember { mutableStateOf(false) }
    var shareDraftText by remember { mutableStateOf("") }
    var showShareErrorDialog by remember { mutableStateOf(false) }
    var showFavoriteMessageDialog by remember { mutableStateOf(false) }
    var favoriteDraftText by remember { mutableStateOf("") }

    // Phase 23: Comfort Mode auto-detection (day before period OR menstruation phase)
    // Persists daily preference under 'comfort_mode_active' and auto-resets when period ends
    val isComfortEligible = ComfortModeHelper.isEligibleForComfortMode(isDayBefore, currentPhase)
    var isComfortModeOn by remember(isComfortEligible, today) {
        mutableStateOf(storageHelper.getComfortModeActive(isComfortEligible, today))
    }
    LaunchedEffect(isComfortEligible, today) {
        isComfortModeOn = storageHelper.getComfortModeActive(isComfortEligible, today)
    }

    // Fertile Window Calculations
    val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastPeriodStart, today)
    val currentCycleStart = lastPeriodStart.plusDays((daysBetween / cycleLength) * cycleLength)
    val ovulationDate = CycleEngine.getOvulationDay(currentCycleStart, cycleLength)
    val fertileWindowStart = ovulationDate.minusDays(5)
    val fertileWindowEnd = ovulationDate.plusDays(1)

    val isFertileActive = !today.isBefore(fertileWindowStart) && !today.isAfter(fertileWindowEnd)
    val isFertileUpcoming = today.isBefore(fertileWindowStart)

    // Translations helper
    val greetingLabel = if (lang == "हिंदी") "नमस्ते, $userName!" else if (lang == "తెలుగు") "హలో, $userName!" else "Hello, $userName!"
    
    val dateText = when (lang) {
        "हिंदी" -> today.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        "తెలుగు" -> today.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        else -> today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    }

    val cycleStatusTitle = if (lang == "हिंदी") "चक्र की स्थिति" else if (lang == "తెలుగు") "చక్రం స్థితి" else "Cycle Status"
    val nextPeriodTitle = if (lang == "हिंदी") "अगला मासिक धर्म" else if (lang == "తెలుగు") "తదుపరి పీరియడ్" else "Next Period"
    val fertilityTitle = if (lang == "हिंदी") "प्रजनन क्षमता" else if (lang == "తెలుగు") "సంతానోత్పత్తి స్థితి" else "Fertility Status"
    val smartReminderTitle = if (lang == "हिंदी") "स्मार्ट अनुस्मारक" else if (lang == "తెలుగు") "స్మార్ట్ రిమైండర్" else "Smart Reminder"
    val todaysGuideTitle = if (lang == "हिंदी") "आज का गाइड" else if (lang == "తెలుగు") "ఈ రోజు గైడ్" else "Today's Guide"

    val bodyAwarenessLabel = if (lang == "हिंदी") "शरीर के प्रति जागरूकता" else if (lang == "తెలుగు") "శరీర అవగాహన" else "BODY AWARENESS"
    val nutritionLabel = if (lang == "हिंदी") "पोषण योजना" else if (lang == "తెలుగు") "ఆహార ప్రణాళిక" else "NUTRITION PLAN"
    val exerciseLabelText = if (lang == "हिंदी") "व्यायाम सलाह" else if (lang == "తెలుగు") "వ్యాయామ సలహా" else "WORKOUT ADVICE"

    // Phase Descriptions and Titles (with Day Before Period Override)
    val phaseTitle = if (isDayBefore) {
        if (lang == "हिंदी") "तैयारी का दिन" else if (lang == "తెలుగు") "సిద్ధమవ్వాల్సిన రోజు" else "Day Before Period"
    } else {
        Translations.t(currentPhase.lowercase(), lang)
    }

    val phaseDescription = if (isDayBefore) {
        Translations.t("day_before_body", lang)
    } else {
        Translations.t("phase_desc_" + currentPhase.lowercase(), lang)
    }

    // Localized Phase Guide Data (with Day Before Period Override)
    val foodMorning = if (isDayBefore) {
        Translations.t("day_before_food_morning", lang)
    } else when (currentPhase.lowercase()) {
        "menstruation" -> if (lang == "हिंदी") "आयरन से भरपूर खाद्य पदार्थ — पालक, खजूर, अनार का रस, चुकंदर" else if (lang == "తెలుగు") "ఐరన్ ఎక్కువగా ఉండే ఆహారం — పాలకూర, ఖర్జూరాలు, దానిమ్మ జ్యూస్, బీట్‌రూట్" else "Iron-rich foods — spinach, dates, pomegranate juice, beetroot"
        "follicular" -> if (lang == "हिंदी") "ताजे फल, अंकुरित अनाज, दही, हरी सब्जियां" else if (lang == "తెలుగు") "తాజా పండ్లు, మొలకలు, పెరుగు, ఆకుకూరలు" else "Fresh fruits, sprouts, yogurt, green vegetables"
        "ovulation" -> if (lang == "हिंदी") "साबुत अनाज, फल, सलाद, बादाम, अखरोट" else if (lang == "తెలుగు") "ధాన్యాలు, పండ్లు, సలాడ్స్, బాదం, అక్రోట్" else "Whole grains, fruit salads, almonds, walnuts"
        else -> if (lang == "हिंदी") "फाइबर युक्त खाद्य पदार्थ — दलिया, शकरकंद, मैग्नीशियम" else if (lang == "తెలుగు") "పీచు పదార్థాలు — ఓట్స్, చిలగడదుంప, మెగ్నీషియం" else "Fiber-rich foods — oats, sweet potatoes, magnesium"
    }

    val foodAfternoon = if (isDayBefore) {
        Translations.t("day_before_food_afternoon", lang)
    } else when (currentPhase.lowercase()) {
        "menstruation" -> if (lang == "हिंदी") "डार्क चॉकलेट, अदरक की चाय, गर्म नींबू पानी" else if (lang == "తెలుగు") "డార్క్ చాక్లెట్, అల్లం టీ, గోరువెచ్చని నిమ్మరసం" else "Dark chocolate, ginger tea, warm lemon water"
        "follicular" -> if (lang == "हिंदी") "मिश्रित मेवे, कद्दू के बीज, नारियल पानी" else if (lang == "తెలుగు") "గింజలు, గుమ్మడికాయ విత్తనాలు, కొబ్బరి నీరు" else "Mixed nuts, pumpkin seeds, coconut water"
        "ovulation" -> if (lang == "हिंदी") "अंजीर, जामुन, अलसी के बीज का काढ़ा" else if (lang == "తెలుగు") "అంజీర పండ్లు, బెర్రీస్, అవిసె గింజలు" else "Figs, berries, flax seeds smoothie"
        else -> if (lang == "हिंदी") "कद्दू के बीज, कैमोमाइल हर्बल चाय" else if (lang == "తెలుగు") "గుమ్మడికాయ విత్తనాలు, చేమంతి టీ" else "Pumpkin seeds, chamomile herbal tea"
    }

    val foodEvening = if (isDayBefore) {
        Translations.t("day_before_food_evening", lang)
    } else when (currentPhase.lowercase()) {
        "menstruation" -> if (lang == "हिंदी") "गर्म सूप, खिचड़ी, हल्का दाल चावल। ठंडा या तीखा भोजन न करें।" else if (lang == "తెలుగు") "వేడి సూప్, కిచిడీ, పప్పు అన్నం. చల్లని లేదా కారంగా ఉండే ఆహారం వద్దు." else "Warm soup, khichdi, light dal rice. Avoid cold or spicy food."
        "follicular" -> if (lang == "हिंदी") "भुनी हुई सब्जियां, ग्रिल्ड प्रोटीन, ब्राउन राइस" else if (lang == "తెలుగు") "కూరగాయలు, గ్రిల్డ్ ప్రోటీన్, బ్రౌన్ రైస్" else "Roasted vegetables, grilled protein, brown rice"
        "ovulation" -> if (lang == "हिंदी") "हरी पत्तेदार सब्जियां, क्विनोआ सलाद, सूप" else if (lang == "తెలుగు") "ఆకుకూరలు, క్వినోవా సలాడ్, వేడి సూప్" else "Leafy greens, quinoa salad, clear soup"
        else -> if (lang == "हिंदी") "भुनी हुई शकरकंद, हर्बल सूप, मूंग दाल खिचड़ी" else if (lang == "తెలుగు") "చిలగడదుంప ఫ్రై, హెర్బల్ సూప్, పెసరపప్పు కిచిడీ" else "Baked sweet potato, herbal soup, mung dal khichdi"
    }

    val workoutAdvice = if (isDayBefore) {
        Translations.t("day_before_exercise", lang)
    } else when (currentPhase.lowercase()) {
        "menstruation" -> if (lang == "हिंदी") "बालासन, बिल्ली-गाय खिंचाव, धीमी सैर। कठिन व्यायाम से बचें।" else if (lang == "తెలుగు") "బాలాసనం, పిల్లి-ఆవు స్ట్రెచ్, నెమ్మదిగా నడవడం. కఠినమైన వ్యాయామాలు వద్దు." else "Child's pose, cat-cow stretch, slow walk. Avoid heavy lifting."
        "follicular" -> if (lang == "हिंदी") "रनिंग, कार्डियो, सूर्य नमस्कार, लाइट वेट ट्रेनिंग" else if (lang == "తెలుగు") "రన్నింగ్, కార్డియో, సూర్య నమస్కారాలు, తేలికపాటి వెయిట్స్" else "Running, cardio, Surya Namaskar, light weight training"
        "ovulation" -> if (lang == "हिंदी") "हाई-इंटेंसिटी इंटरवल ट्रेनिंग (HIIT), स्ट्रेंथ ट्रेनिंग, ज़ुम्बा" else if (lang == "తెలుగు") "హై-ఇంటెన్సిటీ వ్యాయామాలు (HIIT), స్ట్రెంగ్త్ ట్రైనింగ్, జుంబా" else "High-intensity interval training (HIIT), strength training, Zumba"
        else -> if (lang == "हिंदी") "यांग योग, आसान पिलेट्स, तैरना, मध्यम स्ट्रेचिंग" else if (lang == "తెలుగు") "యోగ, తేలికపాటి పైలేట్స్, ఈత కొట్టడం, స్ట్రెచింగ్" else "Yin yoga, easy pilates, swimming, moderate stretching"
    }

    val screenModifier = if (isComfortModeOn) {
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colors.comfortBgStart, colors.comfortBgEnd)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    } else {
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    }

    Column(
        modifier = screenModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (isComfortModeOn) 24.dp else 16.dp)
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isComfortModeOn) 8.dp else 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greetingLabel,
                    fontSize = if (isComfortModeOn) 26.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = dateText,
                    fontSize = if (isComfortModeOn) 16.sp else 14.sp,
                    color = colors.textSecondary
                )
            }
        }

        // Phase 23: Comfort Mode Toggle (auto-shown on day before period or menstruation days)
        if (isComfortEligible) {
            ComfortModeToggleBar(
                isComfortModeOn = isComfortModeOn,
                onToggle = { enabled ->
                    isComfortModeOn = enabled
                    storageHelper.setComfortModeActive(enabled, today)
                }
            )
        }

        if (isComfortModeOn) {
            // Simplified cozy Comfort Mode layout — hides secondary cards
            ComfortModeView(
                storageHelper = storageHelper,
                today = today
            )
        } else if (isDayBefore) {
            // Special Day Before Period state: Replaces all normal cards with the 5 special cards
            DayBeforePeriodSpecialView()
            if (showShareButton) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(modifier = Modifier.padding(20.dp)) {
                        PartnerShareButton(
                            label = Translations.t("share_with_partner_btn", lang),
                            hint = Translations.t("share_with_partner_hint", lang),
                            onClick = {
                                shareDraftText = ShareMessages.getMessageForPhase(
                                    phase = currentPhase,
                                    language = lang,
                                    customMessages = customShareMessages
                                )
                                showSharePreviewDialog = true
                            },
                            onLongClick = {
                                favoriteDraftText = ShareMessages.getMessageForPhase(
                                    phase = currentPhase,
                                    language = lang,
                                    customMessages = customShareMessages
                                )
                                showFavoriteMessageDialog = true
                            }
                        )
                    }
                }
            }
        } else {
            // --- Normal cards displayed on all other days ---

            // --- Phase 19: Anomaly Card (Soft light yellow, never alarming) ---
            if (anomalies.isNotEmpty()) {
                val anomaly = anomalies.first()
                val anomalyMsg = when (anomaly.type) {
                    "cycle_length" -> Translations.t("anomaly_cycle_length", lang, anomaly.deviationDays)
                    "long_period" -> Translations.t("anomaly_long_period", lang)
                    else -> anomaly.message
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("anomaly_alert_card"),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFFF8E1)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFFFE082))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "ℹ️",
                            fontSize = 20.sp
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = anomalyMsg,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = androidx.compose.ui.graphics.Color(0xFF5D4037),
                                lineHeight = 19.sp
                            )
                            Text(
                                text = Translations.t("anomaly_subtext", lang),
                                fontSize = 12.sp,
                                color = androidx.compose.ui.graphics.Color(0xFF8D6E63),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            // --- Phase 19: Missed Period Card (Soft light orange) ---
            if (isMissed) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("missed_period_card"),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFFFCC80))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "🌸",
                            fontSize = 20.sp
                        )
                        Text(
                            text = Translations.t("missed_period_message", lang),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = androidx.compose.ui.graphics.Color(0xFFE65100),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // --- Pad Reminder Banner (Phase 3 Requirement 4) ---
            AnimatedVisibility(
                visible = daysUntilNextPeriod == 1 || daysUntilNextPeriod == 2,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pad_reminder_banner"),
                colors = CardDefaults.cardColors(containerColor = colors.pinkAccent.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, colors.pinkAccent.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBag,
                            contentDescription = smartReminderTitle,
                            tint = colors.pinkAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = smartReminderTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.pinkAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (daysUntilNextPeriod == 1) {
                            if (lang == "हिंदी") "आपका मासिक धर्म कल होने की उम्मीद है। पैड का स्टॉक कर लें।" 
                            else if (lang == "తెలుగు") "మీ పీరియడ్ రేపు ప్రారంభమౌతుంది. ప్యాడ్‌లు సిద్ధం చేసుకోండి." 
                            else "Your period is expected tomorrow. Stock up on pads."
                        } else {
                            Translations.t("pad_reminder_banner_text", lang)
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val shopBlinkitLabel = if (lang == "हिंदी") "ब्लिंकिट से मंगवाएं" else if (lang == "తెలుగు") "బ్లింకిట్ ప్యాడ్స్" else "Order on Blinkit"
                        val shopZeptoLabel = if (lang == "हिंदी") "जेप्टो से मंगवाएं" else if (lang == "తెలుగు") "జెప్టో ప్యాడ్స్" else "Order on Zepto"

                        Button(
                            onClick = { com.example.utils.StoreLinks.openStore(context, "blinkit") },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                            modifier = Modifier.weight(1f).testTag("shop_blinkit_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(shopBlinkitLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.cardBackground)
                        }

                        Button(
                            onClick = { com.example.utils.StoreLinks.openStore(context, "zepto") },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary),
                            modifier = Modifier.weight(1f).testTag("shop_zepto_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(shopZeptoLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.cardBackground)
                        }
                    }
                }
            }
        }

        // --- Card 1: Cycle status ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cycle_status_card"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WaterDrop,
                            contentDescription = cycleStatusTitle,
                            tint = colors.pinkAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = cycleStatusTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.pinkAccent
                        )
                    }

                    // Phase 21: Confidence Score Pill Badge (shown only when >= 2 cycles logged)
                    confidence.score?.let { scoreValue ->
                        Surface(
                            onClick = { showConfidenceDialog = true },
                            shape = RoundedCornerShape(50),
                            color = colors.pinkAccent.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.pinkAccent),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("confidence_score_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = Translations.t("confidence_accurate_badge", lang, scoreValue),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.pinkAccent
                                )
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = Translations.t("confidence_tooltip_title", lang),
                                    tint = colors.pinkAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = Translations.t("day_x_of_cycle", lang, currentCycleDay),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.pinkAccent.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = phaseTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.pinkAccent
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = phaseDescription,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    lineHeight = 18.sp
                )

                // Phase 21: Building pattern note when fewer than 2 cycles are logged
                if (confidence.score == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.pinkAccent.copy(alpha = 0.06f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.pinkAccent.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confidence_building_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = Translations.t("confidence_building_label", lang),
                                tint = colors.pinkAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = Translations.t("confidence_building_sub", lang, confidence.cyclesNeeded),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        if (showConfidenceDialog && confidence.score != null) {
            AlertDialog(
                onDismissRequest = { showConfidenceDialog = false },
                containerColor = colors.cardBackground,
                titleContentColor = colors.textPrimary,
                textContentColor = colors.textSecondary,
                modifier = Modifier.testTag("confidence_tooltip_dialog"),
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = Translations.t("confidence_tooltip_title", lang),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = colors.pinkAccent.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.pinkAccent.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${Translations.t("confidence_accurate_badge", lang, confidence.score)} · ${Translations.t(confidence.labelKey, lang)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.pinkAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                text = {
                    Text(
                        text = Translations.t("confidence_tooltip_body", lang, confidence.cycleCount),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = colors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { showConfidenceDialog = false },
                        modifier = Modifier.testTag("confidence_tooltip_close_btn")
                    ) {
                        Text(
                            text = Translations.t("btn_got_it", lang),
                            fontWeight = FontWeight.Bold,
                            color = colors.pinkAccent
                        )
                    }
                }
            )
        }

        // --- Card 2: Next period prediction ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("next_period_card"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = nextPeriodTitle,
                        tint = colors.pinkAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = nextPeriodTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.pinkAccent
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val predictionText = when {
                    daysUntilNextPeriod > 0 -> Translations.t("period_in_x_days", lang, daysUntilNextPeriod)
                    daysUntilNextPeriod == 0 -> Translations.t("period_expected_today", lang)
                    else -> Translations.t("period_is_late_by_x_days", lang, -daysUntilNextPeriod)
                }

                Text(
                    text = predictionText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                val nextPeriodDate = CycleEngine.getNextPeriodDate(lastPeriodStart, cycleLength)
                val expectedStartLabel = if (lang == "हिंदी") "अपेक्षित शुरुआत: " else if (lang == "తెలుగు") "అంచనా తేదీ: " else "Expected Start: "
                Text(
                    text = expectedStartLabel + Translations.formatDate(nextPeriodDate, lang),
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }
        }

        // --- Card 3: Fertility window ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fertile_window_card"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Favorite,
                        contentDescription = fertilityTitle,
                        tint = colors.pinkAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = fertilityTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.pinkAccent
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isFertileActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.pinkAccent.copy(alpha = 0.1f))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = Translations.t("fertile_window_active", lang),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.pinkAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val conceptionAdvice = if (lang == "हिंदी") "गर्भाधान की उच्च संभावना।" else if (lang == "తెలుగు") "గర్భం దాల్చడానికి అనుకూల సమయం." else "High chance of conception today."
                            Text(
                                text = conceptionAdvice,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                        }
                    }
                } else if (isFertileUpcoming) {
                    Column {
                        val upcomingFertilityLabel = if (lang == "हिंदी") "उर्वर अवधि आ रही है" else if (lang == "తెలుగు") "సంతానోత్పత్తి సమయం రాబోతోంది" else "Fertile window is upcoming"
                        Text(
                            text = upcomingFertilityLabel,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val startsOnLabel = if (lang == "हिंदी") "शुरुआत तिथि: " else if (lang == "తెలుగు") "ప్రారంభ తేదీ: " else "Starts on "
                        Text(
                            text = startsOnLabel + Translations.formatDate(fertileWindowStart, lang),
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    }
                } else {
                    Column {
                        val passedFertilityLabel = if (lang == "हिंदी") "उर्वर अवधि समाप्त हो चुकी है" else if (lang == "తెలుగు") "సంతానోత్పత్తి సమయం ముగిసింది" else "Fertile window has passed"
                        Text(
                            text = passedFertilityLabel,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val cycleConcludedLabel = if (lang == "हिंदी") "इस चक्र का सक्रिय उर्वर चरण समाप्त हो गया।" else if (lang == "తెలుగు") "ఈ చక్రం యొక్క సంతానోత్పత్తి సమయం ముగిసింది." else "Active fertile period of this cycle concluded."
                        Text(
                            text = cycleConcludedLabel,
                            fontSize = 13.sp,
                            color = colors.textSecondary.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Predicted Ovulation
                val predictedOvulationLabel = if (lang == "हिंदी") "अनुमानित डिंबोत्सर्जन: " else if (lang == "తెలుగు") "అండవిడుదల అంచనా తేదీ: " else "Predicted Ovulation: "
                Text(
                    text = predictedOvulationLabel + Translations.formatDate(ovulationDate, lang),
                    fontSize = 12.sp,
                    color = Teal,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Card 4: Today's Guide ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("todays_guide_card"),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = todaysGuideTitle,
                        tint = colors.pinkAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = todaysGuideTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.pinkAccent
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: Body info
                Text(
                    text = bodyAwarenessLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.pinkAccent.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = phaseDescription,
                    fontSize = 14.sp,
                    color = colors.textPrimary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 2: Nutrition Guide
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Restaurant,
                        contentDescription = "Nutrition",
                        tint = colors.pinkAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = nutritionLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.pinkAccent.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Morning
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LightMode,
                        contentDescription = "Morning",
                        tint = Teal,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(Translations.t("food_morning", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(foodMorning, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Afternoon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WbSunny,
                        contentDescription = "Afternoon",
                        tint = Teal,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(Translations.t("food_afternoon", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(foodAfternoon, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Evening
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DarkMode,
                        contentDescription = "Evening",
                        tint = Teal,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(Translations.t("food_evening", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(foodEvening, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 3: Exercise Guide
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = "Exercise",
                        tint = colors.pinkAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = exerciseLabelText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.pinkAccent.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = workoutAdvice,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    lineHeight = 18.sp
                )

                // Phase 22: Partner Share-a-Day button — completely removed from UI tree when disabled in Settings
                if (showShareButton) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    PartnerShareButton(
                        label = Translations.t("share_with_partner_btn", lang),
                        hint = Translations.t("share_with_partner_hint", lang),
                        onClick = {
                            shareDraftText = ShareMessages.getMessageForPhase(
                                phase = currentPhase,
                                language = lang,
                                customMessages = customShareMessages
                            )
                            showSharePreviewDialog = true
                        },
                        onLongClick = {
                            favoriteDraftText = ShareMessages.getMessageForPhase(
                                phase = currentPhase,
                                language = lang,
                                customMessages = customShareMessages
                            )
                            showFavoriteMessageDialog = true
                        }
                    )
                }
            }
        }
        }

        // Phase 22: Editable Share Preview Modal (explicit 'Send' tap required)
        if (showSharePreviewDialog) {
            AlertDialog(
                onDismissRequest = { showSharePreviewDialog = false },
                containerColor = colors.cardBackground,
                titleContentColor = colors.textPrimary,
                textContentColor = colors.textSecondary,
                modifier = Modifier.testTag("share_partner_preview_dialog"),
                title = {
                    Text(
                        text = Translations.t("share_preview_title", lang),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = Translations.t("share_preview_subtitle", lang),
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            lineHeight = 17.sp
                        )
                        OutlinedTextField(
                            value = shareDraftText,
                            onValueChange = { shareDraftText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 110.dp)
                                .testTag("share_partner_message_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.pinkAccent,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = shareDraftText.trim()
                            showSharePreviewDialog = false
                            val shared = ShareMessages.sharePhaseMessage(context, trimmed)
                            if (!shared) {
                                showShareErrorDialog = true
                            }
                        },
                        enabled = shareDraftText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("share_partner_send_btn")
                    ) {
                        Text(
                            text = Translations.t("share_send_btn", lang),
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showSharePreviewDialog = false },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("share_partner_cancel_btn")
                    ) {
                        Text(
                            text = Translations.t("share_cancel_btn", lang),
                            color = colors.textSecondary
                        )
                    }
                }
            )
        }

        // Phase 22: Long-Press Favorite Partner Message Modal
        if (showFavoriteMessageDialog) {
            AlertDialog(
                onDismissRequest = { showFavoriteMessageDialog = false },
                containerColor = colors.cardBackground,
                titleContentColor = colors.textPrimary,
                textContentColor = colors.textSecondary,
                modifier = Modifier.testTag("favorite_share_dialog"),
                title = {
                    Text(
                        text = Translations.t("favorite_message_title", lang),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = Translations.t("favorite_message_subtitle", lang),
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            lineHeight = 17.sp
                        )
                        OutlinedTextField(
                            value = favoriteDraftText,
                            onValueChange = { favoriteDraftText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 110.dp)
                                .testTag("favorite_share_message_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.pinkAccent,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )
                        TextButton(
                            onClick = {
                                storageHelper.saveCustomShareMessage(currentPhase, "")
                                customShareMessages = storageHelper.getCustomShareMessages()
                                favoriteDraftText = ShareMessages.getDefaultTemplate(currentPhase, lang)
                                Toast.makeText(
                                    context,
                                    Translations.t("favorite_message_reset_toast", lang),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.testTag("favorite_share_reset_btn")
                        ) {
                            Text(
                                text = Translations.t("favorite_message_reset_btn", lang),
                                fontSize = 12.sp,
                                color = colors.pinkAccent
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            storageHelper.saveCustomShareMessage(currentPhase, favoriteDraftText)
                            customShareMessages = storageHelper.getCustomShareMessages()
                            showFavoriteMessageDialog = false
                            Toast.makeText(
                                context,
                                Translations.t("favorite_message_saved_toast", lang),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        enabled = favoriteDraftText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("favorite_share_save_btn")
                    ) {
                        Text(
                            text = Translations.t("favorite_message_save_btn", lang),
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showFavoriteMessageDialog = false },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = Translations.t("share_cancel_btn", lang),
                            color = colors.textSecondary
                        )
                    }
                }
            )
        }

        // Phase 22: Share Error Dialog
        if (showShareErrorDialog) {
            AlertDialog(
                onDismissRequest = { showShareErrorDialog = false },
                containerColor = colors.cardBackground,
                title = {
                    Text(
                        text = Translations.t("share_error_title", lang),
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = Translations.t("share_error_body", lang),
                        color = colors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showShareErrorDialog = false }) {
                        Text(
                            text = Translations.t("btn_got_it", lang),
                            fontWeight = FontWeight.Bold,
                            color = colors.pinkAccent
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PartnerShareButton(
    label: String,
    hint: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colors = LocalVelvetColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.pinkAccent.copy(alpha = 0.08f))
                .border(1.dp, colors.pinkAccent, RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("share_with_partner_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = label,
                    tint = colors.pinkAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.pinkAccent
                )
            }
        }
        Text(
            text = hint,
            fontSize = 11.sp,
            color = colors.textSecondary
        )
    }
}
