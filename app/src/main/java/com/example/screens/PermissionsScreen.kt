package com.example.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
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
import com.example.LocalAppLanguage
import com.example.ui.theme.LocalVelvetColors
import com.example.utils.NotificationHelper
import com.example.utils.StorageHelper

@Composable
fun PermissionsScreen(
    storageHelper: StorageHelper,
    onContinueClicked: () -> Unit
) {
    val colors = LocalVelvetColors.current
    val lang = LocalAppLanguage.current
    val context = LocalContext.current

    // Localized Strings
    val screenTitle = when (lang) {
        "हिंदी" -> "वेलवेटसाइकिल को अपनी बेहतर सहायता करने की अनुमति दें"
        "తెలుగు" -> "మీకు మరింత మెరుగ్గా సహాయం చేయడానికి వెల్వెట్‌సైకిల్‌ని అనుమతించండి"
        else -> "Allow VelvetCycle to help you better"
    }

    val notifTitle = when (lang) {
        "हिंदी" -> "मासिक धर्म और स्वास्थ्य अनुस्मारक"
        "తెలుగు" -> "పీరియడ్ & ఆరోగ్య రిమైండర్లు"
        else -> "Period & health reminders"
    }

    val notifSubtitle = when (lang) {
        "हिंदी" -> "अपने मासिक धर्म से पहले, ओव्यूलेशन के दिन और पैड रिमाइंडर्स की सूचनाएं प्राप्त करें"
        "తెలుగు" -> "మీ పీరియడ్‌కు ముందు, అండవిడుదల రోజున మరియు ప్యాడ్ రిమైండర్‌ల నోటిఫికేషన్‌లను పొందండి"
        else -> "Get notified before your period, on ovulation day, and pad reminders"
    }

    val btnAllow = when (lang) {
        "हिंदी" -> "अनुमति दें"
        "తెలుగు" -> "అనుమతించు"
        else -> "Allow"
    }

    val btnSkip = when (lang) {
        "हिंदी" -> "छोड़ें"
        "తెలుగు" -> "వదిలేయి"
        else -> "Skip"
    }

    val btnAllowedText = when (lang) {
        "हिंदी" -> "✓ अनुमति दी गई"
        "తెలుగు" -> "✓ అనుమతించబడింది"
        else -> "✓ Allowed"
    }

    val locTitle = when (lang) {
        "हिंदी" -> "स्थान (जल्द आ रहा है)"
        "తెలుగు" -> "స్థానం (త్వరలో రాబోతోంది)"
        else -> "Location (coming soon)"
    }

    val locSubtitle = when (lang) {
        "हिंदी" -> "निकटतम फार्मेसियों को ढूंढें — अगले अपडेट में आ रहा है"
        "తెలుగు" -> "దగ్గరలోని ఫార్మసీలను కనుగొనండి — తదుపరి అప్‌డేట్‌లో రాబోతోంది"
        else -> "Find nearby pharmacies — coming in next update"
    }

    val btnNotAvailable = when (lang) {
        "हिंदी" -> "अभी उपलब्ध नहीं है"
        "తెలుగు" -> "ఇంకా అందుబాటులో లేదు"
        else -> "Not available yet"
    }

    val btnContinue = when (lang) {
        "हिंदी" -> "ऐप पर जाएं"
        "తెలుగు" -> "యాప్‌కి కొనసాగండి"
        else -> "Continue to app"
    }

    // Permission state handling
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    var isRequestingPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isRequestingPermission = false
        hasNotificationPermission = isGranted
        // Ensure state is committed instantly so that if the OS kills the process, the app state is preserved
        storageHelper.permissionsAsked = true
        if (isGranted) {
            // First time permission granted -> create notification channel, schedule alarms & send test notification after 10s
            NotificationHelper.createNotificationChannel(context)
            NotificationHelper.scheduleAllNotifications(context, storageHelper)
            NotificationHelper.scheduleTestNotificationIfNeeded(context, storageHelper)
            Toast.makeText(context, "Notification permission granted! 💕", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notifications disabled. You can enable them in Settings.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section (Header)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VelvetCycle",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.pinkAccent,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = screenTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Middle Section (Cards)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1 — Notifications
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("permission_row_notifications"),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.border),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.pinkAccent.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Notification Icon",
                                    tint = colors.pinkAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notifTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notifSubtitle,
                                    fontSize = 13.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasNotificationPermission) {
                                Text(
                                    text = btnAllowedText,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp, horizontal = 12.dp)
                                        .testTag("btn_allowed_indicator")
                                )
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        // Mark permissions asked as true synchronously and show Toast
                                        storageHelper.permissionsAsked = true
                                        Toast.makeText(context, "Notification reminders skipped.", Toast.LENGTH_SHORT).show()
                                    },
                                    border = BorderStroke(1.dp, colors.border),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !isRequestingPermission,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .testTag("btn_skip_notification")
                                ) {
                                    Text(btnSkip, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }

                                if (isRequestingPermission) {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContainerColor = colors.border.copy(alpha = 0.5f),
                                            disabledContentColor = colors.textSecondary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("btn_requesting_notification")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = colors.textSecondary,
                                                strokeWidth = 2.dp
                                            )
                                            Text(
                                                text = if (lang == "हिंदी") "अनुरोध कर रहा है..." else if (lang == "తెలుగు") "అభ్యర్థిస్తోంది..." else "Requesting...",
                                                color = colors.textSecondary,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            isRequestingPermission = true
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            } else {
                                                isRequestingPermission = false
                                                hasNotificationPermission = true
                                                storageHelper.permissionsAsked = true
                                                NotificationHelper.createNotificationChannel(context)
                                                NotificationHelper.scheduleAllNotifications(context, storageHelper)
                                                NotificationHelper.scheduleTestNotificationIfNeeded(context, storageHelper)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("btn_allow_notification")
                                    ) {
                                        Text(btnAllow, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Row 2 — Location (Disabled Placeholder)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("permission_row_location"),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.border.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.textSecondary.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "Location Icon",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = locTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = locSubtitle,
                                    fontSize = 13.sp,
                                    color = colors.textSecondary.copy(alpha = 0.6f),
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { },
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.border,
                                    disabledContainerColor = colors.border.copy(alpha = 0.5f),
                                    disabledContentColor = colors.textSecondary.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_disabled_location")
                            ) {
                                Text(
                                    text = btnNotAvailable,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Bottom Section (Continue Button)
            Button(
                onClick = {
                    storageHelper.permissionsAsked = true
                    onContinueClicked()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_continue_to_app")
            ) {
                Text(
                    text = btnContinue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
