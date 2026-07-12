package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.screens.CalendarScreen
import com.example.screens.HomeScreen
import com.example.screens.SettingsScreen
import com.example.screens.LogScreen
import com.example.screens.OnboardingScreen
import com.example.ui.theme.DarkText
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryPink
import com.example.ui.theme.White
import com.example.utils.StorageHelper

class MainActivity : androidx.fragment.app.FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val storageHelper = StorageHelper(this)
        if (storageHelper.isOnboarded) {
            com.example.utils.NotificationHelper.scheduleAllNotifications(this, storageHelper)
        }
        
        setContent {
            MyApplicationTheme {
                var isUnlocked by remember { 
                    mutableStateOf(storageHelper.userPin == null) 
                }
                
                if (!isUnlocked) {
                    com.example.screens.PinScreen(
                        storageHelper = storageHelper,
                        initialMode = com.example.screens.PinMode.Verify,
                        onUnlocked = { isUnlocked = true }
                    )
                } else {
                    MainAppLayout(storageHelper)
                }
            }
        }
    }
}

@Composable
fun MainAppLayout(storageHelper: StorageHelper) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showBottomBar = currentRoute != "onboarding" && currentRoute != null
    val startDestination = if (storageHelper.isOnboarded) "home" else "onboarding"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = White,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar")
                ) {
                    val tabs = listOf(
                        TabItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
                        TabItem("calendar", "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
                        TabItem("log", "Log", Icons.Filled.Edit, Icons.Outlined.Edit),
                        TabItem("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
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
                                selectedIconColor = PrimaryPink,
                                selectedTextColor = PrimaryPink,
                                indicatorColor = PrimaryPink.copy(alpha = 0.1f),
                                unselectedIconColor = DarkText.copy(alpha = 0.6f),
                                unselectedTextColor = DarkText.copy(alpha = 0.6f)
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
                OnboardingScreen(storageHelper = storageHelper) {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
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
            composable("settings") {
                SettingsScreen(
                    storageHelper = storageHelper,
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
