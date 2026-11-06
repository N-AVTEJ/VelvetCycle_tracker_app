package com.example.utils

import android.content.Context

/**
 * Emergency wipe utility for VelvetCycle.
 * Corresponds to utils/wipeData.js.
 *
 * Implements wipeAllData():
 * 1. Cancels all notifications.
 * 2. Clears all SecureStore sensitive keys:
 *    - user_pin
 *    - user_name
 *    - last_period_start
 *    - cycle_length
 *    - period_duration
 *    - biometric_enabled
 *    - disguise_mode
 *    - pin_attempts
 *    - pin_lockout_until
 *    - individual logs log_YYYY-MM-DD
 * 3. Clears all non-sensitive regular preferences (AsyncStorage equivalent).
 */
object WipeData {

    fun wipeAllData(context: Context, storageHelper: StorageHelper? = null) {
        // 1. Cancel all scheduled notifications
        try {
            NotificationHelper.cancelAllNotifications(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Clear SecureStore sensitive keys
        val secureKeys = listOf(
            "user_pin",
            "user_name",
            "last_period_start",
            "cycle_length",
            "period_duration",
            "biometric_enabled",
            "disguise_mode",
            "pin_attempts",
            "pin_lockout_until",
            "lockout_duration",
            "lockout_expiration_time",
            "razorpay_key_id",
            "monthly_plan_id",
            "yearly_plan_id",
            "is_premium_user",
            "subscription_plan"
        )

        for (key in secureKeys) {
            try {
                SecureStore.deleteSecure(context, key)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Clear all SecureStore data (including daily log keys log_YYYY-MM-DD)
        try {
            SecureStore.clear(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Clear all regular preferences (AsyncStorage equivalent)
        try {
            val regularPrefs = context.getSharedPreferences("VelvetCyclePrefs", Context.MODE_PRIVATE)
            regularPrefs.edit().clear().commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If storageHelper reference provided, ensure its internal states are also cleared
        storageHelper?.clear()
    }
}
