package com.example.constants

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Translations {
    val languages = listOf("English", "हिंदी", "తెలుగు")

    private val translations = mapOf(
        "English" to mapOf(
            "app_name" to "VelvetCycle",
            "day_x_of_cycle" to "Day %d of your cycle",
            "period_in_x_days" to "Period in %d days",
            "period_expected_today" to "Period expected today",
            "period_is_late_by_x_days" to "Period is late by %d days",
            "fertile_window_active" to "Fertile window active",
            "ovulation_today" to "Ovulation today",
            "pad_reminder_banner_text" to "Your period is in 2 days. Stock up on pads.",
            "stock_up_pads" to "Stock up on pads",
            "menstruation" to "Menstruation",
            "follicular" to "Follicular",
            "ovulation" to "Ovulation",
            "luteal" to "Luteal",
            
            "phase_desc_menstruation" to "Your period has started. Focus on warm foods, gentle stretching, and plenty of rest. Stay hydrated.",
            "phase_desc_follicular" to "Estrogen is rising. You'll feel more energetic. Great time for high-intensity workouts and fresh, raw vegetables.",
            "phase_desc_ovulation" to "Your fertility peaks today. You might feel highly social and vibrant. Ideal time for strength training and light meals.",
            "phase_desc_luteal" to "Progesterone rises, bringing nesting vibes. Focus on fiber-rich foods, magnesium, and restorative yoga.",
            
            "food_morning" to "Morning",
            "food_afternoon" to "Afternoon",
            "food_evening" to "Evening",
            "exercise" to "Exercise",
            
            "log_today" to "Log Today",
            "how_are_you_feeling" to "How are you feeling?",
            "flow_today" to "Flow today",
            "flow_none" to "None",
            "flow_spotting" to "Spotting",
            "flow_light" to "Light",
            "flow_medium" to "Medium",
            "flow_heavy" to "Heavy",
            "symptoms" to "Symptoms",
            
            // Symptoms
            "sym_cramps" to "Cramps",
            "sym_headache" to "Headache",
            "sym_bloating" to "Bloating",
            "sym_acne" to "Acne",
            "sym_fatigue" to "Fatigue",
            "sym_mood_swings" to "Mood Swings",
            "sym_backache" to "Backache",
            "sym_breast_tenderness" to "Breast Tenderness",
            "sym_cravings" to "Cravings",
            "sym_nausea" to "Nausea",
            
            "notes" to "Notes",
            "save_log" to "Save Log",
            "update_log" to "Update Log",
            "log_saved" to "Log saved!",
            
            "period_days" to "Period Days",
            "fertile_window" to "Fertile Window",
            
            // Months
            "month_1" to "January",
            "month_2" to "February",
            "month_3" to "March",
            "month_4" to "April",
            "month_5" to "May",
            "month_6" to "June",
            "month_7" to "July",
            "month_8" to "August",
            "month_9" to "September",
            "month_10" to "October",
            "month_11" to "November",
            "month_12" to "December",
            
            // Insights
            "avg_cycle_length" to "Average cycle length",
            "avg_period_duration" to "Average period duration",
            "most_common_symptoms" to "Most common symptoms",
            "export_pdf_report" to "Export PDF report",
            "cycles_tracked" to "Cycles tracked",
            "last_period_date" to "Last period date",
            "no_insights_title" to "Track at least one full cycle to see your insights.",
            "no_insights_subtitle" to "Log daily to build your health picture.",
            "no_calendar_logs" to "Your cycle journey starts here. Log your first period to see predictions.",
            "log_period_start" to "Log period start",
            
            // Settings
            "section_account" to "Account",
            "section_notifications" to "Notifications",
            "section_privacy" to "Privacy & Security",
            "section_store" to "Pad Store Preference",
            "section_language" to "Language",
            "section_data" to "Data & Maintenance",
            "section_about" to "About",
            
            "lbl_display_name" to "Display Name",
            "lbl_edit_cycle" to "Edit Cycle & Period Info",
            "lbl_period_reminder" to "Period reminder",
            "lbl_period_reminder_sub" to "Alert 2 days before predicted period",
            "lbl_ovulation_alert" to "Ovulation alert",
            "lbl_ovulation_alert_sub" to "Notify on ovulation day at 9:00 AM",
            "lbl_daily_log" to "Daily log reminder",
            "lbl_daily_log_sub" to "Daily prompt to log your symptoms",
            "lbl_pad_reminder" to "Pad reminder",
            "lbl_pad_reminder_sub" to "Alert 2 days before to stock up on pads",
            "lbl_biometric" to "Fingerprint / Face Unlock",
            "lbl_biometric_sub" to "Enable biometric unlock when possible",
            "lbl_reminder_time" to "Reminder Time",
            
            "btn_set_pin" to "Set PIN",
            "btn_remove_pin" to "Remove PIN",
            "btn_backup" to "Backup to Google Drive",
            "btn_restore" to "Restore from Google Drive",
            "btn_delete_data" to "Delete all my data",
            
            "onboarding_q1" to "What is your name?",
            "onboarding_q2" to "When did your last period start?",
            "onboarding_q3" to "How many days does your period usually last?",
            "onboarding_q4" to "How long is your typical cycle?",
            "btn_next" to "Next",
            "btn_back" to "Back",
            "btn_get_started" to "Get Started",
            "btn_submit" to "Submit"
        ),
        "हिंदी" to mapOf(
            "app_name" to "वेलवेट साइकिल",
            "day_x_of_cycle" to "आपके चक्र का दिन %d",
            "period_in_x_days" to "%d दिनों में मासिक धर्म",
            "period_expected_today" to "मासिक धर्म आज अपेक्षित है",
            "period_is_late_by_x_days" to "मासिक धर्म %d दिन की देरी से है",
            "fertile_window_active" to "उर्वर अवधि सक्रिय",
            "ovulation_today" to "आज डिंबोत्सर्जन (Ovulation) है",
            "pad_reminder_banner_text" to "आपका मासिक धर्म 2 दिनों में है। पैड का स्टॉक कर लें।",
            "stock_up_pads" to "पैड का स्टॉक करें",
            "menstruation" to "मासिक धर्म (Menstruation)",
            "follicular" to "फॉलिकुलर (Follicular)",
            "ovulation" to "डिंबोत्सर्जन (Ovulation)",
            "luteal" to "ल्यूटियल (Luteal)",
            
            "phase_desc_menstruation" to "आपका मासिक धर्म शुरू हो गया है। गर्म भोजन, हल्की स्ट्रेचिंग और पर्याप्त आराम पर ध्यान दें। हाइड्रेटेड रहें।",
            "phase_desc_follicular" to "एस्ट्रोजन बढ़ रहा है। आप अधिक ऊर्जावान महसूस करेंगी। उच्च तीव्रता वाले वर्कआउट और ताजी, कच्ची सब्जियों के लिए बहुत अच्छा समय है।",
            "phase_desc_ovulation" to "आपकी प्रजनन क्षमता आज चरम पर है। आप अत्यधिक सामाजिक और जीवंत महसूस कर सकती हैं। शक्ति प्रशिक्षण और हल्के भोजन के लिए आदर्श समय।",
            "phase_desc_luteal" to "प्रोजेस्टेरोन बढ़ता है, जिससे आराम करने की इच्छा होती है। फाइबर युक्त खाद्य पदार्थों, मैग्नीशियम और उपचारात्मक योग पर ध्यान दें।",
            
            "food_morning" to "सुबह",
            "food_afternoon" to "दोपहर",
            "food_evening" to "शाम",
            "exercise" to "व्यायाम",
            
            "log_today" to "आज का लॉग",
            "how_are_you_feeling" to "आप कैसा महसूस कर रही हैं?",
            "flow_today" to "आज का प्रवाह",
            "flow_none" to "कोई नहीं",
            "flow_spotting" to "धब्बे आना",
            "flow_light" to "हल्का",
            "flow_medium" to "मध्यम",
            "flow_heavy" to "भारी",
            "symptoms" to "लक्षण",
            
            // Symptoms
            "sym_cramps" to "ऐंठन (Cramps)",
            "sym_headache" to "सिरदर्द",
            "sym_bloating" to "पेट फूलना",
            "sym_acne" to "मुँहासे",
            "sym_fatigue" to "थकान",
            "sym_mood_swings" to "मूड में बदलाव",
            "sym_backache" to "पीठ दर्द",
            "sym_breast_tenderness" to "स्तनों में कोमलता",
            "sym_cravings" to "तीव्र इच्छा (Cravings)",
            "sym_nausea" to "जी मिचलाना",
            
            "notes" to "टिप्पणियाँ",
            "save_log" to "लॉग सहेजें",
            "update_log" to "लॉग अपडेट करें",
            "log_saved" to "लॉग सहेजा गया!",
            
            "period_days" to "मासिक धर्म के दिन",
            "fertile_window" to "उर्वर अवधि",
            
            // Months
            "month_1" to "जनवरी",
            "month_2" to "फ़रवरी",
            "month_3" to "मार्च",
            "month_4" to "अप्रैल",
            "month_5" to "मई",
            "month_6" to "जून",
            "month_7" to "जुलाई",
            "month_8" to "अगस्त",
            "month_9" to "सितंबर",
            "month_10" to "अक्टूबर",
            "month_11" to "नवंबर",
            "month_12" to "दिसंबर",
            
            // Insights
            "avg_cycle_length" to "औसत चक्र अवधि",
            "avg_period_duration" to "औसत मासिक धर्म अवधि",
            "most_common_symptoms" to "सबसे आम लक्षण",
            "export_pdf_report" to "पीडीएफ रिपोर्ट निर्यात करें",
            "cycles_tracked" to "ट्रैक किए गए चक्र",
            "last_period_date" to "अंतिम मासिक धर्म की तिथि",
            "no_insights_title" to "अपने आंकड़े देखने के लिए कम से कम एक पूरा चक्र ट्रैक करें।",
            "no_insights_subtitle" to "अपनी स्वास्थ्य स्थिति को समझने के लिए रोजाना लॉग दर्ज करें।",
            "no_calendar_logs" to "आपकी चक्र यात्रा यहाँ से शुरू होती है। भविष्यवाणियाँ देखने के लिए अपना पहला मासिक धर्म लॉग करें।",
            "log_period_start" to "मासिक धर्म की शुरुआत लॉग करें",
            
            // Settings
            "section_account" to "खाता",
            "section_notifications" to "सूचनाएं",
            "section_privacy" to "गोपनीयता और सुरक्षा",
            "section_store" to "पैड स्टोर प्राथमिकता",
            "section_language" to "भाषा",
            "section_data" to "डेटा और रखरखाव",
            "section_about" to "ऐप के बारे में",
            
            "lbl_display_name" to "प्रदर्शित नाम",
            "lbl_edit_cycle" to "चक्र और मासिक धर्म की जानकारी बदलें",
            "lbl_period_reminder" to "मासिक धर्म अनुस्मारक",
            "lbl_period_reminder_sub" to "अनुमानित मासिक धर्म से 2 दिन पहले सचेत करें",
            "lbl_ovulation_alert" to "डिंबोत्सर्जन अलर्ट",
            "lbl_ovulation_alert_sub" to "डिंबोत्सर्जन के दिन सुबह 9:00 बजे सूचित करें",
            "lbl_daily_log" to "दैनिक लॉग अनुस्मारक",
            "lbl_daily_log_sub" to "अपने लक्षणों को दर्ज करने के लिए दैनिक अनुस्मारक",
            "lbl_pad_reminder" to "पैड अनुस्मारक",
            "lbl_pad_reminder_sub" to "पैड का स्टॉक करने के लिए 2 दिन पहले सचेत करें",
            "lbl_biometric" to "फिंगरप्रिंट / फेस अनलॉक",
            "lbl_biometric_sub" to "संभव होने पर बायोमेट्रिक अनलॉक सक्षम करें",
            "lbl_reminder_time" to "अनुस्मारक समय",
            
            "btn_set_pin" to "पिन सेट करें",
            "btn_remove_pin" to "पिन हटाएं",
            "btn_backup" to "गूगल ड्राइव में बैकअप लें",
            "btn_restore" to "गूगल ड्राइव से पुनर्स्थापित करें",
            "btn_delete_data" to "मेरा सारा डेटा हटाएं",
            
            "onboarding_q1" to "आपका नाम क्या है?",
            "onboarding_q2" to "आपका पिछला मासिक धर्म कब शुरू हुआ था?",
            "onboarding_q3" to "आपका मासिक धर्म आमतौर पर कितने दिनों तक चलता है?",
            "onboarding_q4" to "आपका सामान्य चक्र कितने दिनों का होता है?",
            "btn_next" to "अगला",
            "btn_back" to "पीछे",
            "btn_get_started" to "शुरू करें",
            "btn_submit" to "जमा करें"
        ),
        "తెలుగు" to mapOf(
            "app_name" to "వెల్వెట్ సైకిల్",
            "day_x_of_cycle" to "మీ చక్రంలో %d వ రోజు",
            "period_in_x_days" to "%d రోజుల్లో పీరియడ్",
            "period_expected_today" to "ఈ రోజు పీరియడ్ రావచ్చు",
            "period_is_late_by_x_days" to "పీరియడ్ %d రోజులు ఆలస్యమైంది",
            "fertile_window_active" to "ఫెర్టైల్ విండో యాక్టివ్",
            "ovulation_today" to "ఈ రోజు అండవిడుదల (Ovulation) రోజు",
            "pad_reminder_banner_text" to "మీ పీరియడ్ ఇంకా 2 రోజుల్లో ప్రారంభమౌతుంది. ప్యాడ్‌లు సిద్ధం చేసుకోండి.",
            "stock_up_pads" to "ప్యాడ్‌లను సిద్ధం చేసుకోండి",
            "menstruation" to "ఋతుస్రావం (Menstruation)",
            "follicular" to "ఫాలిక్యులర్ (Follicular)",
            "ovulation" to "అండవిడుదల (Ovulation)",
            "luteal" to "లూటియల్ (Luteal)",
            
            "phase_desc_menstruation" to "మీ పీరియడ్ ప్రారంభమైంది. వేడి ఆహారం, తేలికపాటి వ్యాయామాలు మరియు తగినంత విశ్రాంతిపై దృష్టి పెట్టండి. నీరు ఎక్కువగా తాగండి.",
            "phase_desc_follicular" to "ఈస్ట్రోజెన్ పెరుగుతోంది. మీరు మరింత ఉత్సాహంగా ఉంటారు. కఠినమైన వ్యాయామాలు మరియు తాజా కూరగాయలు తినడానికి ఇది మంచి సమయం.",
            "phase_desc_ovulation" to "ఈ రోజు మీ సంతానోత్పత్తి సామర్థ్యం చాలా ఎక్కువగా ఉంటుంది. చాలా చురుకుగా ఉంటారు. బరువులు ఎత్తడం మరియు తేలికపాటి భోజనం చేయడానికి అనుకూల సమయం.",
            "phase_desc_luteal" to "ప్రొజెస్టెరాన్ పెరుగుతుంది, విశ్రాంతి తీసుకోవాలనిపిస్తుంది. పీచు పదార్థాలు ఉన్న ఆహారం, మెగ్నీషియం మరియు యోగాపై దృష్టి పెట్టండి.",
            
            "food_morning" to "ఉదయం",
            "food_afternoon" to "మధ్యాహ్నం",
            "food_evening" to "సాయంత్రం",
            "exercise" to "వ్యాయామం",
            
            "log_today" to "ఈ రోజు లాగ్ చేయండి",
            "how_are_you_feeling" to "మీరు ఎలా ఉన్నారు?",
            "flow_today" to "ఈ రోజు ప్రవాహం",
            "flow_none" to "ఏమీ లేదు",
            "flow_spotting" to "స్పాటింగ్ (Spotting)",
            "flow_light" to "తక్కువ",
            "flow_medium" to "మధ్యస్థం",
            "flow_heavy" to "ఎక్కువ",
            "symptoms" to "లక్షణాలు",
            
            // Symptoms
            "sym_cramps" to "నొప్పి/కడుపునొప్పి (Cramps)",
            "sym_headache" to "తలనొప్పి",
            "sym_bloating" to "కడుపు ఉబ్బరం (Bloating)",
            "sym_acne" to "మొటిమలు",
            "sym_fatigue" to "అలసట",
            "sym_mood_swings" to "మూడ్ స్వింగ్స్",
            "sym_backache" to "నడుము నొప్పి",
            "sym_breast_tenderness" to "రొమ్ముల నొప్పి",
            "sym_cravings" to "ఆహార కోరికలు (Cravings)",
            "sym_nausea" to "కడుపులో తిప్పడం (Nausea)",
            
            "notes" to "గమనికలు",
            "save_log" to "లాగ్ సేవ్ చేయి",
            "update_log" to "లాగ్ అప్‌డేట్ చేయి",
            "log_saved" to "లాగ్ సేవ్ చేయబడింది!",
            
            "period_days" to "పీరియడ్ రోజులు",
            "fertile_window" to "ఫెర్టైల్ విండో",
            
            // Months
            "month_1" to "జనవరి",
            "month_2" to "ఫిబ్రవరి",
            "month_3" to "మార్చి",
            "month_4" to "ఏప్రిల్",
            "month_5" to "మే",
            "month_6" to "జూన్",
            "month_7" to "జూలై",
            "month_8" to "ఆగస్టు",
            "month_9" to "సెప్టెంబర్",
            "month_10" to "అక్టోబర్",
            "month_11" to "నవంబర్",
            "month_12" to "డిసెంబర్",
            
            // Insights
            "avg_cycle_length" to "సగటు చక్రం వ్యవధి",
            "avg_period_duration" to "సగటు పీరియడ్ వ్యవధి",
            "most_common_symptoms" to "సాధారణంగా కనిపించే లక్షణాలు",
            "export_pdf_report" to "పిడిఎఫ్ రిపోర్ట్ డౌన్‌లోడ్ చేయి",
            "cycles_tracked" to "ట్రాక్ చేసిన చక్రాలు",
            "last_period_date" to "చివరి పీరియడ్ తేదీ",
            "no_insights_title" to "మీ అంతర్దృష్టులను చూడటానికి కనీసం ఒక పూర్తి చక్రాన్ని ట్రాక్ చేయండి.",
            "no_insights_subtitle" to "మీ ఆరోగ్య స్థితిని నిర్మించుకోవడానికి ప్రతిరోజూ లాగ్ చేయండి.",
            "no_calendar_logs" to "మీ సైకిల్ ప్రయాణం ఇక్కడే ప్రారంభమవుతుంది. అంచనాలను చూడటానికి మీ మొదటి పీరియడ్‌ను లాగ్ చేయండి.",
            "log_period_start" to "పీరియడ్ ప్రారంభాన్ని లాగ్ చేయి",
            
            // Settings
            "section_account" to "ఖాతా",
            "section_notifications" to "నోటిఫికేషన్లు",
            "section_privacy" to "గోప్యత & భద్రత",
            "section_store" to "ప్యాడ్ స్టోర్ ప్రాధాన్యత",
            "section_language" to "భాష",
            "section_data" to "డేటా & నిర్వహణ",
            "section_about" to "యాప్ గురించి",
            
            "lbl_display_name" to "ప్రదర్శన పేరు",
            "lbl_edit_cycle" to "చక్రం & పీరియడ్ వివరాలను మార్చు",
            "lbl_period_reminder" to "పీరియడ్ రిమైండర్",
            "lbl_period_reminder_sub" to "పీరియడ్ అంచనా వేసిన తేదీకి 2 రోజుల ముందు తెలియజేయుట",
            "lbl_ovulation_alert" to "అండవిడుదల అలర్ట్",
            "lbl_ovulation_alert_sub" to "అండవిడుదల రోజున ఉదయం 9:00 గంటలకు తెలియజేయుట",
            "lbl_daily_log" to "రోజువారీ లాగ్ రిమైండర్",
            "lbl_daily_log_sub" to "లక్షణాలను నమోదు చేయడానికి రోజువారీ రిమైండర్",
            "lbl_pad_reminder" to "ప్యాడ్ రిమైండర్",
            "lbl_pad_reminder_sub" to "ప్యాడ్‌లను సిద్ధం చేసుకోవడానికి 2 రోజుల ముందు తెలియజేయుట",
            "lbl_biometric" to "ఫింగర్‌ప్రింట్ / ఫేస్ అన్‌లాక్",
            "lbl_biometric_sub" to "అనుకూలమైనప్పుడు బయోమెట్రిక్ అన్‌లాక్‌ను ప్రారంభించు",
            "lbl_reminder_time" to "రిమైండర్ సమయం",
            
            "btn_set_pin" to "పిన్ సెట్ చేయి",
            "btn_remove_pin" to "పిన్ తొలగించు",
            "btn_backup" to "గూగుల్ డ్రైవ్‌కు బ్యాకప్ చేయి",
            "btn_restore" to "గూగుల్ డ్రైవ్ నుండి రీస్టోర్ చేయి",
            "btn_delete_data" to "నా డేటా మొత్తం తొలగించు",
            
            "onboarding_q1" to "మీ పేరు ఏమిటి?",
            "onboarding_q2" to "మీ చివరి పీరియడ్ ఎప్పుడు ప్రారంభమైంది?",
            "onboarding_q3" to "మీ పీరియడ్ సాధారణంగా ఎన్ని రోజులు ఉంటుంది?",
            "onboarding_q4" to "మీ సాధారణ సైకిల్ వ్యవధి ఎంత?",
            "btn_next" to "తరువాత",
            "btn_back" to "వెనుకకు",
            "btn_get_started" to "ప్రారంభించు",
            "btn_submit" to "సమర్పించు"
        )
    )

    fun t(key: String, language: String, vararg args: Any): String {
        val langMap = translations[language] ?: translations["English"]!!
        val formatString = langMap[key] ?: translations["English"]?.get(key) ?: key
        return try {
            if (args.isEmpty()) formatString else String.format(formatString, *args)
        } catch (e: Exception) {
            formatString
        }
    }

    fun getMonthName(monthValue: Int, language: String): String {
        return t("month_$monthValue", language)
    }

    fun formatDate(date: LocalDate, language: String): String {
        return when (language) {
            "English" -> date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            "हिंदी", "తెలుగు" -> {
                val day = date.dayOfMonth
                val month = getMonthName(date.monthValue, language)
                val year = date.year
                "$day $month $year"
            }
            else -> date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        }
    }
}
