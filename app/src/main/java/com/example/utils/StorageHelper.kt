package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class LogData(
    val mood: String,
    val flow: String,
    val symptoms: List<String>,
    val notes: String
)

data class CycleRecord(
    val cycleNo: Int,
    val startDate: LocalDate,
    val duration: Int,
    val notes: String = ""
)

/**
 * Main persistent storage manager for VelvetCycle.
 *
 * Architecture:
 * - Sensitive data (PIN, user profile, cycle history, biometrics, disguise mode, individual logs)
 *   is encrypted using SecureStore (AES-256-GCM hardware-backed keystore).
 * - Non-sensitive data (theme, language, pad inventory, notification permissions)
 *   is stored in regular SharedPreferences (AsyncStorage equivalent).
 * - Transparent migration guarantees existing unencrypted preferences transition automatically.
 */
class StorageHelper(private val context: Context) {
    // Non-sensitive regular preferences (AsyncStorage)
    private val regularPrefs: SharedPreferences = context.getSharedPreferences("VelvetCyclePrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_ONBOARDED = "is_onboarded"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_LAST_PERIOD_START = "last_period_start"
        private const val KEY_PERIOD_DURATION = "period_duration"
        private const val KEY_CYCLE_LENGTH = "cycle_length"
        private const val KEY_USER_PIN = "user_pin"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_DISGUISE_MODE = "disguise_mode"
    }

    init {
        migrateSensitiveDataIfNeeded()
    }

    // --- SECURE STORE CORE API (Corresponds to expo-secure-store) ---

    fun saveSecure(key: String, value: Any?): Boolean {
        return SecureStore.saveSecure(context, key, value)
    }

    fun getSecure(key: String): String? {
        return SecureStore.getSecure(context, key)
    }

    fun deleteSecure(key: String): Boolean {
        return SecureStore.deleteSecure(context, key)
    }

    /**
     * Auto-migrates any legacy sensitive keys found in regularPrefs to SecureStore.
     */
    private fun migrateSensitiveDataIfNeeded() {
        val sensitiveKeys = listOf(
            KEY_USER_NAME,
            KEY_LAST_PERIOD_START,
            KEY_PERIOD_DURATION,
            KEY_CYCLE_LENGTH,
            KEY_USER_PIN,
            KEY_BIOMETRIC_ENABLED,
            KEY_DISGUISE_MODE,
            "lockout_duration",
            "lockout_expiration_time",
            "pin_attempts",
            "razorpay_key_id",
            "monthly_plan_id",
            "yearly_plan_id",
            "is_premium_user",
            "subscription_plan"
        )

        val editor = regularPrefs.edit()
        var changed = false

        for (key in sensitiveKeys) {
            if (regularPrefs.contains(key) && SecureStore.getSecure(context, key) == null) {
                val rawVal = regularPrefs.all[key]
                if (rawVal != null) {
                    val stringVal = if (rawVal is String && rawVal.contains(":")) {
                        CryptoHelper.decrypt(rawVal).ifEmpty { rawVal }
                    } else {
                        rawVal.toString()
                    }
                    SecureStore.saveSecure(context, key, stringVal)
                    editor.remove(key)
                    changed = true
                }
            }
        }

        // Migrate any existing daily logs (log_YYYY-MM-DD)
        for ((key, value) in regularPrefs.all) {
            if (key.startsWith("log_")) {
                if (SecureStore.getSecure(context, key) == null) {
                    val rawStr = value as? String ?: value.toString()
                    val decrypted = if (rawStr.contains(":")) {
                        CryptoHelper.decrypt(rawStr).ifEmpty { rawStr }
                    } else {
                        rawStr
                    }
                    SecureStore.saveSecure(context, key, decrypted)
                }
                editor.remove(key)
                changed = true
            }
        }

        if (changed) {
            editor.commit()
        }
    }

    // --- CONVENTIONAL NON-SENSITIVE PROPERTIES (AsyncStorage) ---

    var isOnboarded: Boolean
        get() = regularPrefs.getBoolean(KEY_IS_ONBOARDED, false)
        set(value) { regularPrefs.edit().putBoolean(KEY_IS_ONBOARDED, value).commit() }

    var isDarkMode: Boolean
        get() = regularPrefs.getBoolean("dark_mode", false)
        set(value) { regularPrefs.edit().putBoolean("dark_mode", value).commit() }

    var appLanguage: String
        get() = regularPrefs.getString("app_language", "English") ?: "English"
        set(value) { regularPrefs.edit().putString("app_language", value).commit() }

    var padStore: String
        get() = regularPrefs.getString("pad_store", "") ?: ""
        set(value) { regularPrefs.edit().putString("pad_store", value).commit() }

    var permissionsAsked: Boolean
        get() = regularPrefs.getBoolean("permissions_asked", false)
        set(value) { regularPrefs.edit().putBoolean("permissions_asked", value).commit() }

    var notificationTestSent: Boolean
        get() = regularPrefs.getBoolean("notification_test_sent", false)
        set(value) { regularPrefs.edit().putBoolean("notification_test_sent", value).commit() }

    var backgroundedAt: Long
        get() = regularPrefs.getLong("backgrounded_at", 0L)
        set(value) { regularPrefs.edit().putLong("backgrounded_at", value).commit() }

    fun clearBackgroundTime() {
        regularPrefs.edit().remove("backgrounded_at").commit()
    }

    // --- NON-SENSITIVE NOTIFICATION SETTINGS ---

    var periodReminderEnabled: Boolean
        get() = regularPrefs.getBoolean("notif_period_reminder", true)
        set(value) { regularPrefs.edit().putBoolean("notif_period_reminder", value).commit() }

    var periodReminderDaysBefore: Int
        get() = regularPrefs.getInt("notif_period_days_before", 1)
        set(value) { regularPrefs.edit().putInt("notif_period_days_before", value).commit() }

    var periodReminderTime: String
        get() = regularPrefs.getString("notif_period_time", "08:00") ?: "08:00"
        set(value) { regularPrefs.edit().putString("notif_period_time", value).commit() }

    var ovulationAlertEnabled: Boolean
        get() = regularPrefs.getBoolean("notif_ovulation_alert", true)
        set(value) { regularPrefs.edit().putBoolean("notif_ovulation_alert", value).commit() }

    var dailyLogReminderEnabled: Boolean
        get() = regularPrefs.getBoolean("notif_daily_log", true)
        set(value) { regularPrefs.edit().putBoolean("notif_daily_log", value).commit() }

    var dailyLogReminderTime: String
        get() = regularPrefs.getString("notif_daily_log_time", "21:00") ?: "21:00"
        set(value) { regularPrefs.edit().putString("notif_daily_log_time", value).commit() }

    var padReminderEnabled: Boolean
        get() = regularPrefs.getBoolean("notif_pad_reminder", true)
        set(value) { regularPrefs.edit().putBoolean("notif_pad_reminder", value).commit() }

    // --- SENSITIVE DATA STORED IN SECURESTORE ---

    var userPin: String?
        get() {
            val pin = getSecure(KEY_USER_PIN)
            return if (pin.isNullOrEmpty()) null else pin
        }
        set(value) {
            if (value == null) {
                deleteSecure(KEY_USER_PIN)
            } else {
                saveSecure(KEY_USER_PIN, value)
            }
        }

    var userName: String
        get() = getSecure(KEY_USER_NAME) ?: ""
        set(value) { saveSecure(KEY_USER_NAME, value) }

    var lastPeriodStart: LocalDate
        get() {
            val dateStr = getSecure(KEY_LAST_PERIOD_START) ?: ""
            return if (dateStr.isNotEmpty()) {
                try {
                    LocalDate.parse(dateStr)
                } catch (e: Exception) {
                    LocalDate.now()
                }
            } else {
                LocalDate.now()
            }
        }
        set(value) { saveSecure(KEY_LAST_PERIOD_START, value.toString()) }

    var periodDuration: Int
        get() = getSecure(KEY_PERIOD_DURATION)?.toIntOrNull() ?: 5
        set(value) { saveSecure(KEY_PERIOD_DURATION, value) }

    var cycleLength: Int
        get() = getSecure(KEY_CYCLE_LENGTH)?.toIntOrNull() ?: 28
        set(value) { saveSecure(KEY_CYCLE_LENGTH, value) }

    var biometricEnabled: Boolean
        get() = getSecure(KEY_BIOMETRIC_ENABLED)?.toBooleanStrictOrNull() ?: false
        set(value) { saveSecure(KEY_BIOMETRIC_ENABLED, value) }

    var disguiseMode: Boolean
        get() = getSecure(KEY_DISGUISE_MODE)?.toBooleanStrictOrNull() ?: false
        set(value) { saveSecure(KEY_DISGUISE_MODE, value) }

    // --- INDIVIDUAL DAILY LOG ENTRIES IN SECURESTORE (log_YYYY-MM-DD) ---

    /**
     * Stores each daily log as an individual encrypted key in SecureStore: log_YYYY-MM-DD
     * Adheres to SecureStore's 2KB maximum per-key size limit.
     */
    fun saveLog(dateStr: String, logData: LogData) {
        val jsonObject = org.json.JSONObject()
        jsonObject.put("mood", logData.mood)
        jsonObject.put("flow", logData.flow)
        val symptomsArray = org.json.JSONArray()
        logData.symptoms.forEach { symptomsArray.put(it) }
        jsonObject.put("symptoms", symptomsArray)
        jsonObject.put("notes", logData.notes)

        saveSecure("log_$dateStr", jsonObject.toString())
    }

    /**
     * Retrieves an individual daily log from SecureStore by date.
     */
    fun getLog(dateStr: String): LogData? {
        val jsonStr = getSecure("log_$dateStr") ?: return null
        if (jsonStr.isEmpty()) return null
        return try {
            val jsonObject = org.json.JSONObject(jsonStr)
            val mood = jsonObject.optString("mood", "")
            val flow = jsonObject.optString("flow", "")
            val symptomsArray = jsonObject.optJSONArray("symptoms")
            val symptoms = mutableListOf<String>()
            if (symptomsArray != null) {
                for (i in 0 until symptomsArray.length()) {
                    symptoms.add(symptomsArray.getString(i))
                }
            }
            val notes = jsonObject.optString("notes", "")
            LogData(mood, flow, symptoms, notes)
        } catch (e: Exception) {
            null
        }
    }

    fun deleteLog(dateStr: String): Boolean {
        return deleteSecure("log_$dateStr")
    }

    /**
     * Returns all individual daily logs stored in SecureStore.
     */
    fun getAllLogs(): Map<String, LogData> {
        val result = mutableMapOf<String, LogData>()
        val allSecureKeys = SecureStore.getAllKeys(context)
        for (key in allSecureKeys) {
            if (key.startsWith("log_")) {
                val dateStr = key.removePrefix("log_")
                val logData = getLog(dateStr)
                if (logData != null) {
                    result[dateStr] = logData
                }
            }
        }
        return result
    }

    fun getCycleHistory(): List<CycleRecord> {
        val logs = getAllLogs()
        val flowDates = logs.filter { 
            it.value.flow == "Light" || it.value.flow == "Medium" || it.value.flow == "Heavy" 
        }.keys.mapNotNull { 
            try { LocalDate.parse(it) } catch (e: Exception) { null }
        }.sorted()

        val detectedPeriods = mutableListOf<LocalDate>()
        var lastDate: LocalDate? = null
        for (date in flowDates) {
            if (lastDate == null || ChronoUnit.DAYS.between(lastDate, date) > 14) {
                detectedPeriods.add(date)
            }
            lastDate = date
        }

        val onboardingStart = lastPeriodStart
        if (!detectedPeriods.contains(onboardingStart)) {
            detectedPeriods.add(onboardingStart)
        }
        detectedPeriods.sort()

        val records = mutableListOf<CycleRecord>()
        for (i in 0 until detectedPeriods.size) {
            val start = detectedPeriods[i]
            val duration = if (i < detectedPeriods.size - 1) {
                ChronoUnit.DAYS.between(start, detectedPeriods[i + 1]).toInt()
            } else {
                cycleLength
            }
            val notesStr = logs[start.toString()]?.notes ?: ""
            records.add(CycleRecord(
                cycleNo = i + 1,
                startDate = start,
                duration = duration,
                notes = notesStr.ifEmpty { "Regular Cycle" }
            ))
        }

        return records.sortedByDescending { it.startDate }
    }

    // --- SENSITIVE LOCKOUT & PIN ATTEMPTS (DELEGATED TO PINAUTH) ---

    var lockoutDuration: Long
        get() = PinAuth.getLockoutTotalDuration(context)
        set(value) { saveSecure("lockout_duration", value) }

    fun getLockoutTimeRemaining(): Long {
        return PinAuth.getRemainingMs(context)
    }

    fun setLockout(durationMs: Long) {
        saveSecure("lockout_duration", durationMs)
        saveSecure(PinAuth.KEY_PIN_LOCKOUT_UNTIL, System.currentTimeMillis() + durationMs)
        saveSecure("lockout_expiration_time", System.currentTimeMillis() + durationMs)
    }

    fun clearLockout() {
        PinAuth.resetAttempts(context)
    }

    var wrongAttempts: Int
        get() = PinAuth.getAttempts(context)
        set(value) { saveSecure(PinAuth.KEY_PIN_ATTEMPTS, value) }

    // --- SENSITIVE SUBSCRIPTION & RAZORPAY PREFERENCES ---

    var razorpayKeyId: String
        get() = getSecure("razorpay_key_id") ?: ""
        set(value) { saveSecure("razorpay_key_id", value) }

    var monthlyPlanId: String
        get() = getSecure("monthly_plan_id") ?: ""
        set(value) { saveSecure("monthly_plan_id", value) }

    var yearlyPlanId: String
        get() = getSecure("yearly_plan_id") ?: ""
        set(value) { saveSecure("yearly_plan_id", value) }

    var isPremiumUser: Boolean
        get() = getSecure("is_premium_user")?.toBooleanStrictOrNull() ?: false
        set(value) { saveSecure("is_premium_user", value) }

    var subscriptionPlan: String
        get() = getSecure("subscription_plan") ?: "none"
        set(value) { saveSecure("subscription_plan", value) }

    // --- CLEAR EVERYTHING ---

    fun clear() {
        regularPrefs.edit().clear().commit()
        SecureStore.clear(context)
    }
}
