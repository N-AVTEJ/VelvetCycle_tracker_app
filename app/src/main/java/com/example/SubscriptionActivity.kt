package com.example

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalVelvetColors
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VelvetColors
import com.example.utils.PaymentManager
import com.example.utils.StorageHelper
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class SubscriptionActivity : ComponentActivity(), PaymentResultWithDataListener {

    companion object {
        private const val TAG = "SubscriptionActivity"
    }

    private lateinit var storageHelper: StorageHelper
    private lateinit var paymentManager: PaymentManager

    private var paymentStatusMessage by mutableStateOf<String?>(null)
    private var isSuccessDialogVisible by mutableStateOf(false)
    private var isErrorDialogVisible by mutableStateOf(false)
    private var lastPaymentId by mutableStateOf("")

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        storageHelper = StorageHelper(this)
        paymentManager = PaymentManager(this)

        setContent {
            MyApplicationTheme {
                val colors = LocalVelvetColors.current
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("subscription_screen"),
                    containerColor = colors.background,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "VelvetCycle Premium",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { finish() },
                                    modifier = Modifier.testTag("btn_back")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = colors.textPrimary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = colors.cardBackground
                            )
                        )
                    }
                ) { innerPadding ->
                    SubscriptionContent(
                        modifier = Modifier.padding(innerPadding),
                        storageHelper = storageHelper,
                        onSubscribeClicked = { planType, customKey, customPlanId ->
                            launchCheckout(planType, customKey, customPlanId)
                        }
                    )

                    // Success Dialog
                    if (isSuccessDialogVisible) {
                        AlertDialog(
                            onDismissRequest = { isSuccessDialogVisible = false },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(48.dp)
                                )
                            },
                            title = {
                                Text(
                                    text = "Subscription Active! 🌸",
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            },
                            text = {
                                Text(
                                    text = "Thank you for subscribing to VelvetCycle Premium.\n\nPayment ID: $lastPaymentId\nPlan: ${storageHelper.subscriptionPlan.replaceFirstChar { it.uppercase() }}",
                                    textAlign = TextAlign.Center
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        isSuccessDialogVisible = false
                                        finish()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                                    modifier = Modifier.testTag("btn_close_success_dialog")
                                ) {
                                    Text("Done", color = Color.White)
                                }
                            }
                        )
                    }

                    // Failure Dialog
                    if (isErrorDialogVisible) {
                        AlertDialog(
                            onDismissRequest = { isErrorDialogVisible = false },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(48.dp)
                                )
                            },
                            title = {
                                Text(
                                    text = "Payment Canceled or Failed",
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            },
                            text = {
                                Text(
                                    text = paymentStatusMessage ?: "The transaction could not be completed. Please try again.",
                                    textAlign = TextAlign.Center
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = { isErrorDialogVisible = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                                    modifier = Modifier.testTag("btn_close_error_dialog")
                                ) {
                                    Text("Try Again", color = Color.White)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun launchCheckout(planType: String, customKey: String?, customPlanId: String?) {
        val amount = if (planType == "yearly") PaymentManager.AMOUNT_YEARLY_PAISE else PaymentManager.AMOUNT_MONTHLY_PAISE
        val effectivePlanId = customPlanId ?: if (planType == "yearly") storageHelper.yearlyPlanId else storageHelper.monthlyPlanId
        val effectiveKey = customKey?.ifBlank { null } ?: storageHelper.razorpayKeyId.ifBlank { null }

        Log.d(TAG, "Initiating payment for $planType plan ($amount paise)...")
        paymentManager.openCheckout(
            activity = this,
            planType = planType,
            amountInPaise = amount,
            planId = effectivePlanId.ifBlank { null },
            customKeyId = effectiveKey
        )
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        val pId = razorpayPaymentId ?: paymentData?.paymentId ?: "N/A"
        Log.d(TAG, "Payment Successful: $pId")

        storageHelper.isPremiumUser = true
        lastPaymentId = pId
        isSuccessDialogVisible = true
        Toast.makeText(this, "Payment Successful! Premium Unlocked.", Toast.LENGTH_LONG).show()
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        Log.e(TAG, "Payment Error ($code): $response")
        paymentStatusMessage = response ?: "Payment processing was not completed."
        isErrorDialogVisible = true
    }
}

@Composable
private fun SubscriptionContent(
    modifier: Modifier = Modifier,
    storageHelper: StorageHelper,
    onSubscribeClicked: (planType: String, customKey: String?, customPlanId: String?) -> Unit
) {
    val colors = LocalVelvetColors.current
    var selectedPlan by remember { mutableStateOf("yearly") } // "monthly" or "yearly"
    
    var showConfigSection by remember { mutableStateOf(false) }
    var razorpayKeyInput by remember { mutableStateOf(storageHelper.razorpayKeyId) }
    var monthlyPlanIdInput by remember { mutableStateOf(storageHelper.monthlyPlanId) }
    var yearlyPlanIdInput by remember { mutableStateOf(storageHelper.yearlyPlanId) }

    val isPremium = storageHelper.isPremiumUser

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Hero Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isPremium) Color(0xFFE8F5E9) else colors.cardBackground
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isPremium) Color(0xFF4CAF50).copy(alpha = 0.15f) else colors.pinkAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = if (isPremium) Color(0xFF2E7D32) else colors.pinkAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isPremium) "VelvetCycle Premium Active" else "Unlock Full VelvetCycle Experience",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPremium) Color(0xFF1B5E20) else colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isPremium)
                        "Current Plan: ${storageHelper.subscriptionPlan.replaceFirstChar { it.uppercase() }}\nYou have full access to all premium insights and tools."
                    else
                        "Enjoy accurate cycle forecasts, doctor export reports, custom notification schedules, and ad-free tracking.",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Features Section
        Text(
            text = "PREMIUM FEATURES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureRow(
                    icon = Icons.Default.Psychology,
                    title = "AI Cycle & Ovulation Forecasts",
                    subtitle = "Enhanced multi-cycle prediction algorithm"
                )
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                FeatureRow(
                    icon = Icons.Default.Star,
                    title = "Medical PDF Cycle Reports",
                    subtitle = "One-click health reports formatted for your doctor"
                )
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                FeatureRow(
                    icon = Icons.Default.Lock,
                    title = "Private PIN & Biometric Guard",
                    subtitle = "Bank-grade encryption for sensitive symptom logs"
                )
                HorizontalDivider(color = colors.border, thickness = 0.5.dp)
                FeatureRow(
                    icon = Icons.Default.Notifications,
                    title = "Smart Period & Pill Reminders",
                    subtitle = "Customizable notification times & days before"
                )
            }
        }

        // Pricing Tiers
        Text(
            text = "SELECT YOUR PLAN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )

        // Plan 1: Yearly (Recommended)
        PlanCard(
            title = "Yearly Pass",
            priceText = "₹399 / year",
            subtext = "Equivalent to ₹33.25 / month",
            badgeText = "SAVE 52% — BEST VALUE",
            isSelected = selectedPlan == "yearly",
            colors = colors,
            onClick = { selectedPlan = "yearly" },
            testTag = "plan_yearly_card"
        )

        // Plan 2: Monthly
        PlanCard(
            title = "Monthly Pass",
            priceText = "₹70 / month",
            subtext = "Flexible month-to-month subscription",
            badgeText = null,
            isSelected = selectedPlan == "monthly",
            colors = colors,
            onClick = { selectedPlan = "monthly" },
            testTag = "plan_monthly_card"
        )

        // Razorpay API Credentials Config Accordion
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showConfigSection = !showConfigSection }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = colors.pinkAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Razorpay API Keys & Plan IDs",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                    Text(
                        text = if (showConfigSection) "Hide" else "Edit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.pinkAccent
                    )
                }

                if (showConfigSection) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = razorpayKeyInput,
                        onValueChange = {
                            razorpayKeyInput = it
                            storageHelper.razorpayKeyId = it
                        },
                        label = { Text("Razorpay Key ID (e.g. rzp_test_...)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_razorpay_key"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = monthlyPlanIdInput,
                        onValueChange = {
                            monthlyPlanIdInput = it
                            storageHelper.monthlyPlanId = it
                        },
                        label = { Text("Monthly Plan ID (e.g. plan_xxx)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_monthly_plan_id"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = yearlyPlanIdInput,
                        onValueChange = {
                            yearlyPlanIdInput = it
                            storageHelper.yearlyPlanId = it
                        },
                        label = { Text("Yearly Plan ID (e.g. plan_yyy)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_yearly_plan_id"),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // CTA Button
        Button(
            onClick = {
                val effectivePlanId = if (selectedPlan == "yearly") yearlyPlanIdInput else monthlyPlanIdInput
                onSubscribeClicked(selectedPlan, razorpayKeyInput, effectivePlanId)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_subscribe_now"),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.pinkAccent
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (selectedPlan == "yearly") "Subscribe Yearly — ₹399" else "Subscribe Monthly — ₹70",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Text(
            text = "Payments are safely processed by Razorpay. Cancel anytime in account settings.",
            fontSize = 11.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val colors = LocalVelvetColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.pinkAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.pinkAccent,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    priceText: String,
    subtext: String,
    badgeText: String?,
    isSelected: Boolean,
    colors: VelvetColors,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colors.pinkAccent else colors.border,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.pinkAccent.copy(alpha = 0.08f) else colors.cardBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (badgeText != null) {
                Surface(
                    color = colors.pinkAccent,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = subtext,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = colors.pinkAccent
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = priceText,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.pinkAccent
            )
        }
    }
}
