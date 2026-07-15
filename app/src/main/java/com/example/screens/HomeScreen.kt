package com.example.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.utils.CycleEngine
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

    // Phase Descriptions and Titles
    val phaseTitle = Translations.t(currentPhase.lowercase(), lang)
    val phaseDescription = Translations.t("phase_desc_" + currentPhase.lowercase(), lang)

    // Localized Phase Guide Data
    val foodMorning = when (currentPhase.lowercase()) {
        "menstruation" -> if (lang == "हिंदी") "आयरन से भरपूर खाद्य पदार्थ — पालक, खजूर, अनार का रस, चुकंदर" else if (lang == "తెలుగు") "ఐరన్ ఎక్కువగా ఉండే ఆహారం — పాలకూర, ఖర్జూరాలు, దానిమ్మ జ్యూస్, బీట్‌రూట్" else "Iron-rich foods — spinach, dates, pomegranate juice, beetroot"
        "follicular" -> if (lang == "हिंदी") "ताजे फल, अंकुरित अनाज, दही, हरी सब्जियां" else if (lang == "తెలుగు") "తాజా పండ్లు, మొలకలు, పెరుగు, ఆకుకూరలు" else "Fresh fruits, sprouts, yogurt, green vegetables"
        "ovulation" -> if (lang == "हिंदी") "साबुत अनाज, फल, सलाद, बादाम, अखरोट" else if (lang == "తెలుగు") "ధాన్యాలు, పండ్లు, సలాడ్స్, బాదం, అక్రోట్" else "Whole grains, fruit salads, almonds, walnuts"
        else -> if (lang == "हिंदी") "फाइबर युक्त खाद्य पदार्थ — दलिया, शकरकंद, मैग्नीशियम" else if (lang == "తెలుగు") "పీచు పదార్థాలు — ఓట్స్, చిలగడదుంప, మెగ్నీషియం" else "Fiber-rich foods — oats, sweet potatoes, magnesium"
    }

    val foodAfternoon = when (currentPhase.lowercase()) {
        "menstruation" -> if (lang == "हिंदी") "डार्क चॉकलेट, अदरक की चाय, गर्म नींबू पानी" else if (lang == "తెలుగు") "డార్క్ చాక్లెట్, అల్లం టీ, గోరువెచ్చని నిమ్మరసం" else "Dark chocolate, ginger tea, warm lemon water"
        "follicular" -> if (lang == "हिंदी") "मिश्रित मेवे, कद्दू के बीज, नारियल पानी" else if (lang == "తెలుగు") "గింజలు, గుమ్మడికాయ విత్తనాలు, కొబ్బరి నీరు" else "Mixed nuts, pumpkin seeds, coconut water"
        "ovulation" -> if (lang == "हिंदी") "अंजीर, जामुन, अलसी के बीज का काढ़ा" else if (lang == "తెలుగు") "అంజీర పండ్లు, బెర్రీస్, అవిసె గింజలు" else "Figs, berries, flax seeds smoothie"
        else -> if (lang == "हिंदी") "कद्दू के बीज, कैमोमाइल हर्बल चाय" else if (lang == "తెలుగు") "గుమ్మడికాయ విత్తనాలు, చేమంతి టీ" else "Pumpkin seeds, chamomile herbal tea"
    }

    val foodEvening = when (currentPhase.lowercase()) {
        "menstruation" -> if (lang == "हिंदी") "गर्म सूप, खिचड़ी, हल्का दाल चावल। ठंडा या तीखा भोजन न करें।" else if (lang == "తెలుగు") "వేడి సూప్, కిచిడీ, పప్పు అన్నం. చల్లని లేదా కారంగా ఉండే ఆహారం వద్దు." else "Warm soup, khichdi, light dal rice. Avoid cold or spicy food."
        "follicular" -> if (lang == "हिंदी") "भुनी हुई सब्जियां, ग्रिल्ड प्रोटीन, ब्राउन राइस" else if (lang == "తెలుగు") "కూరగాయలు, గ్రిల్డ్ ప్రోటీన్, బ్రౌన్ రైస్" else "Roasted vegetables, grilled protein, brown rice"
        "ovulation" -> if (lang == "हिंदी") "हरी पत्तेदार सब्जियां, क्विनोआ सलाद, सूप" else if (lang == "తెలుగు") "ఆకుకూరలు, క్వినోవా సలాడ్, వేడి సూప్" else "Leafy greens, quinoa salad, clear soup"
        else -> if (lang == "हिंदी") "भुनी हुई शकरकंद, हर्बल सूप, मूंग दाल खिचड़ी" else if (lang == "తెలుగు") "చిలగడదుంప ఫ్రై, హెర్బల్ సూప్, పెసరపప్పు కిచిడీ" else "Baked sweet potato, herbal soup, mung dal khichdi"
    }

    val workoutAdvice = when (currentPhase.lowercase()) {
        "menstruation" -> if (lang == "हिंदी") "बालासन, बिल्ली-गाय खिंचाव, धीमी सैर। कठिन व्यायाम से बचें।" else if (lang == "తెలుగు") "బాలాసనం, పిల్లి-ఆవు స్ట్రెచ్, నెమ్మదిగా నడవడం. కఠినమైన వ్యాయామాలు వద్దు." else "Child's pose, cat-cow stretch, slow walk. Avoid heavy lifting."
        "follicular" -> if (lang == "हिंदी") "रनिंग, कार्डियो, सूर्य नमस्कार, लाइट वेट ट्रेनिंग" else if (lang == "తెలుగు") "రన్నింగ్, కార్డియో, సూర్య నమస్కారాలు, తేలికపాటి వెయిట్స్" else "Running, cardio, Surya Namaskar, light weight training"
        "ovulation" -> if (lang == "हिंदी") "हाई-इंटेंसिटी इंटरवल ट्रेनिंग (HIIT), स्ट्रेंथ ट्रेनिंग, ज़ुम्बा" else if (lang == "తెలుగు") "హై-ఇంటెన్సిటీ వ్యాయామాలు (HIIT), స్ట్రెంగ్త్ ట్రైనింగ్, జుంబా" else "High-intensity interval training (HIIT), strength training, Zumba"
        else -> if (lang == "हिंदी") "यांग योग, आसान पिलेट्स, तैरना, मध्यम स्ट्रेचिंग" else if (lang == "తెలుగు") "యోగ, తేలికపాటి పైలేట్స్, ఈత కొట్టడం, స్ట్రెచింగ్" else "Yin yoga, easy pilates, swimming, moderate stretching"
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
        // --- Header Section ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greetingLabel,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = dateText,
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            }
        }

        // --- Pad Reminder Banner (Phase 3 Requirement 4) ---
        AnimatedVisibility(
            visible = daysUntilNextPeriod == 2,
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
                        text = Translations.t("pad_reminder_banner_text", lang),
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
                        val shopBlinkitLabel = if (lang == "हिंदी") "ब्लिंकिट से मंगवाएं" else if (lang == "తెలుగు") "బ్లింకిట్ ప్యాడ్స్" else "Blinkit"
                        val shopAmazonLabel = if (lang == "हिंदी") "अमेज़न इंडिया" else if (lang == "తెలుగు") "అమెజాన్ ఇండియా" else "Amazon"

                        Button(
                            onClick = { uriHandler.openUri("https://blinkit.com") },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(shopBlinkitLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.cardBackground)
                        }

                        Button(
                            onClick = { uriHandler.openUri("https://amazon.in/s?k=sanitary+pads") },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(shopAmazonLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.cardBackground)
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
            }
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
                Divider(color = colors.border, thickness = 0.5.dp)
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
                Divider(color = colors.border, thickness = 0.5.dp)
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
            }
        }
    }
}
