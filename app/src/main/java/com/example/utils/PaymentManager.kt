package com.example.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.razorpay.Checkout
import org.json.JSONObject

/**
 * PaymentManager handles initializing the Razorpay Checkout instance and
 * orchestrates the openCheckout flow for VelvetCycle premium subscriptions.
 */
class PaymentManager(private val context: Context) {

    companion object {
        private const val TAG = "PaymentManager"

        // Default test Razorpay Key ID (Can be overridden via method parameter or StorageHelper)
        const val DEFAULT_KEY_ID = "rzp_test_VelvetCycle2026"

        // Default Subscription Plan Amounts (in paise)
        const val AMOUNT_MONTHLY_PAISE = 7000   // ₹70.00
        const val AMOUNT_YEARLY_PAISE = 39900   // ₹399.00
    }

    private val storageHelper = StorageHelper(context)

    init {
        // Preload Razorpay resources on initialization to ensure fast checkout launch
        try {
            Checkout.preload(context.applicationContext)
            Log.d(TAG, "Razorpay Checkout preloaded successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to preload Razorpay Checkout", e)
        }
    }

    /**
     * Retrieves the effective Razorpay Key ID.
     */
    fun getKeyId(): String {
        val savedKey = storageHelper.razorpayKeyId
        return if (savedKey.isNotBlank()) savedKey else DEFAULT_KEY_ID
    }

    /**
     * Initializes and launches the Razorpay Checkout flow for premium subscriptions.
     *
     * @param activity The host Activity launching the payment sheet
     * @param planType The subscription type, e.g. "monthly" or "yearly"
     * @param amountInPaise Billing amount in paise (e.g. 7000 = ₹70, 39900 = ₹399)
     * @param planId Optional Razorpay Plan ID (e.g., "plan_123456")
     * @param userEmail Optional user email for prefill
     * @param userContact Optional user contact number for prefill
     * @param customKeyId Optional override for the Razorpay Key ID
     */
    fun openCheckout(
        activity: Activity,
        planType: String = "monthly",
        amountInPaise: Int = if (planType.lowercase() == "yearly") AMOUNT_YEARLY_PAISE else AMOUNT_MONTHLY_PAISE,
        planId: String? = null,
        userEmail: String? = null,
        userContact: String? = null,
        customKeyId: String? = null
    ) {
        val checkout = Checkout()
        val effectiveKeyId = customKeyId ?: getKeyId()
        checkout.setKeyID(effectiveKeyId)

        try {
            val options = JSONObject().apply {
                put("name", "VelvetCycle")
                put("description", "VelvetCycle Premium (${planType.replaceFirstChar { it.uppercase() }})")
                put("image", "https://s2.gifyu.com/images/sample.png") // Placeholder app icon
                put("theme.color", "#D81B60")
                put("currency", "INR")
                put("amount", amountInPaise)

                // Attach Razorpay subscription_id if provided
                if (!planId.isNullOrBlank()) {
                    put("subscription_id", planId)
                }

                // Prefill user information if available
                val prefill = JSONObject().apply {
                    if (!userEmail.isNullOrBlank()) put("email", userEmail)
                    if (!userContact.isNullOrBlank()) put("contact", userContact)
                }
                if (prefill.length() > 0) {
                    put("prefill", prefill)
                }

                // Enable retry options
                val retryObj = JSONObject().apply {
                    put("enabled", true)
                    put("max_count", 2)
                }
                put("retry", retryObj)

                // Optional notes
                val notes = JSONObject().apply {
                    put("app", "VelvetCycle")
                    put("plan_type", planType)
                }
                put("notes", notes)
            }

            Log.d(TAG, "Opening Razorpay checkout: KeyID=$effectiveKeyId, Plan=$planType, Amount=$amountInPaise")
            checkout.open(activity, options)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Razorpay Checkout", e)
        }
    }
}
