package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.screens.CalendarScreen
import com.example.screens.HomeScreen
import com.example.screens.InsightsScreen
import com.example.screens.LogScreen
import com.example.screens.OnboardingScreen
import com.example.screens.SettingsScreen
import com.example.ui.theme.LocalVelvetColors
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.StorageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Provide a globally accessible composition local for translations
val LocalAppLanguage = staticCompositionLocalOf { "English" }

class MainActivity : androidx.fragment.app.FragmentActivity() {
    private lateinit var storageHelper: StorageHelper
    private var isUnlockedState by mutableStateOf(false)
    private var showPrivacyOverlay by mutableStateOf(false)
    private var inactivityJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        storageHelper = StorageHelper(this)
        
        // Screenshot prevention: prevents screenshots and screen recording.
        // Note: This only works on real device builds (APK/AAB).
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        
        if (storageHelper.isOnboarded) {
            com.example.utils.NotificationHelper.scheduleAllNotifications(this, storageHelper)
        }
        
        isUnlockedState = storageHelper.userPin == null

        // App lifecycle observer for auto-locking and privacy screen overlaying
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (storageHelper.userPin != null) {
                        storageHelper.backgroundedAt = System.currentTimeMillis()
                    }
                    // Instantly show the privacy background to protect content in App Switcher
                    showPrivacyOverlay = true
                }
                Lifecycle.Event.ON_START -> {
                    showPrivacyOverlay = false
                    if (storageHelper.userPin != null && isUnlockedState) {
                        val bgTime = storageHelper.backgroundedAt
                        if (bgTime > 0 && System.currentTimeMillis() - bgTime >= 2 * 60 * 1000L) {
                            // Lock automatically after 2 minutes in background
                            isUnlockedState = false
                        }
                    }
                    resetInactivityTimer()
                }
                else -> {}
            }
        })
        
        setContent {
            var isDarkTheme by remember { mutableStateOf(storageHelper.isDarkMode) }
            var appLanguage by remember { mutableStateOf(storageHelper.appLanguage) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (!isUnlockedState) {
                            com.example.screens.PinScreen(
                                storageHelper = storageHelper,
                                initialMode = com.example.screens.PinMode.Verify,
                                onUnlocked = { 
                                    isUnlockedState = true 
                                    resetInactivityTimer()
                                },
                                onEmergencyWipe = {
                                    storageHelper.clear()
                                    com.example.utils.NotificationHelper.cancelAllNotifications(this@MainActivity)
                                    isUnlockedState = storageHelper.userPin == null
                                }
                            )
                        } else {
                            MainAppLayout(
                                storageHelper = storageHelper,
                                onLanguageChanged = { newLang -> appLanguage = newLang },
                                onThemeChanged = { isDark -> isDarkTheme = isDark }
                            )
                        }

                        if (showPrivacyOverlay) {
                            PrivacyOverlay()
                        }
                    }
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        resetInactivityTimer()
        return super.dispatchTouchEvent(ev)
    }

    private fun resetInactivityTimer() {
        inactivityJob?.cancel()
        if (storageHelper.userPin == null || !isUnlockedState) return
        
        inactivityJob = CoroutineScope(Dispatchers.Main).launch {
            delay(10 * 60 * 1000L) // Auto-lock after 10 minutes of complete touch inactivity
            isUnlockedState = false
        }
    }
}

@Composable
fun PrivacyOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFFD4537E))
            .testTag("privacy_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "VelvetCycle",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color.White,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun MainAppLayout(
    storageHelper: StorageHelper,
    onLanguageChanged: (String) -> Unit,
    onThemeChanged: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val currentLanguage = LocalAppLanguage.current
    val colors = LocalVelvetColors.current
    
    val showBottomBar = currentRoute != "onboarding" && currentRoute != null
    val startDestination = if (storageHelper.isOnboarded) "home" else "onboarding"

    val translateLabel = { route: String, default: String ->
        when (route) {
            "home" -> if (currentLanguage == "हिंदी") "होम" else if (currentLanguage == "తెలుగు") "హోమ్" else "Home"
            "calendar" -> if (currentLanguage == "हिंदी") "कैलेंडर" else if (currentLanguage == "తెలుగు") "క్యాలెండర్" else "Calendar"
            "log" -> if (currentLanguage == "हिंदी") "लॉग" else if (currentLanguage == "తెలుగు") "లాగ్" else "Log"
            "insights" -> if (currentLanguage == "हिंदी") "आंकड़े" else if (currentLanguage == "తెలుగు") "అంతర్దృష్తులు" else "Insights"
            "settings" -> if (currentLanguage == "हिंदी") "सेटिंग्स" else if (currentLanguage == "తెలుగు") "సెట్టింగ్స్" else "Settings"
            else -> default
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = colors.tabBackground,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar")
                ) {
                    val tabs = listOf(
                        TabItem("home", translateLabel("home", "Home"), Icons.Filled.Home, Icons.Outlined.Home),
                        TabItem("calendar", translateLabel("calendar", "Calendar"), Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
                        TabItem("log", translateLabel("log", "Log"), Icons.Filled.Edit, Icons.Outlined.Edit),
                        TabItem("insights", translateLabel("insights", "Insights"), Icons.Filled.BarChart, Icons.Outlined.BarChart),
                        TabItem("settings", translateLabel("settings", "Settings"), Icons.Filled.Settings, Icons.Outlined.Settings)
                    )
                    
                    tabs.forEach { tab ->
                        val isSelected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.pinkAccent,
                                selectedTextColor = colors.pinkAccent,
                                indicatorColor = colors.pinkAccent.copy(alpha = 0.1f),
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("onboarding") {
                OnboardingScreen(
                    storageHelper = storageHelper,
                    onOnboardingFinished = {
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") {
                HomeScreen(storageHelper = storageHelper)
            }
            composable("calendar") {
                CalendarScreen(storageHelper = storageHelper)
            }
            composable("log") {
                LogScreen(storageHelper = storageHelper)
            }
            composable("insights") {
                InsightsScreen(
                    storageHelper = storageHelper,
                    onNavigateToLog = {
                        navController.navigate("log") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable("settings") {
                SettingsScreen(
                    storageHelper = storageHelper,
                    onLanguageChanged = onLanguageChanged,
                    onThemeChanged = onThemeChanged,
                    onNavigateToOnboarding = {
                        navController.navigate("onboarding") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}

data class TabItem(
    val route: String,
    val label: String,
    val activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector
)
