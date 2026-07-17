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

class StorageHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("VelvetCyclePrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_ONBOARDED = "is_onboarded"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_LAST_PERIOD_START = "last_period_start"
        private const val KEY_PERIOD_DURATION = "period_duration"
        private const val KEY_CYCLE_LENGTH = "cycle_length"
    }

    // --- SECURE STORAGE HELPERS ---
    private fun saveSecureString(key: String, value: String?) {
        if (value == null) {
            prefs.edit().remove(key).commit()
        } else {
            val encrypted = CryptoHelper.encrypt(value)
            prefs.edit().putString(key, encrypted).commit()
        }
    }

    private fun getSecureString(key: String, defaultValue: String = ""): String {
        val encrypted = prefs.getString(key, null) ?: return defaultValue
        if (encrypted.contains(":")) {
            val decrypted = CryptoHelper.decrypt(encrypted)
            return decrypted.ifEmpty { defaultValue }
        }
        return encrypted.ifEmpty { defaultValue }
    }

    private fun saveSecureInt(key: String, value: Int) {
        saveSecureString(key, value.toString())
    }

    private fun getSecureInt(key: String, defaultValue: Int): Int {
        val encrypted = prefs.getString(key, null)
        if (encrypted == null) {
            if (prefs.contains(key)) {
                try {
                    return prefs.getInt(key, defaultValue)
                } catch (e: Exception) {}
            }
            return defaultValue
        }
        val str = getSecureString(key, "")
        return str.toIntOrNull() ?: defaultValue
    }

    private fun saveSecureBoolean(key: String, value: Boolean) {
        saveSecureString(key, value.toString())
    }

    private fun getSecureBoolean(key: String, defaultValue: Boolean): Boolean {
        val encrypted = prefs.getString(key, null)
        if (encrypted == null) {
            if (prefs.contains(key)) {
                try {
                    return prefs.getBoolean(key, defaultValue)
                } catch (e: Exception) {}
            }
            return defaultValue
        }
        val str = getSecureString(key, "")
        return str.toBooleanStrictOrNull() ?: defaultValue
    }

    private fun saveSecureLong(key: String, value: Long) {
        saveSecureString(key, value.toString())
    }

    private fun getSecureLong(key: String, defaultValue: Long): Long {
        val str = getSecureString(key, "")
        return str.toLongOrNull() ?: defaultValue
    }

    // --- CONVENTIONAL & SECURED PROPERTIES ---
    var isOnboarded: Boolean
        get() = prefs.getBoolean(KEY_IS_ONBOARDED, false)
        set(value) { prefs.edit().putBoolean(KEY_IS_ONBOARDED, value).commit() }

    var userName: String
        get() = getSecureString(KEY_USER_NAME, "")
        set(value) = saveSecureString(KEY_USER_NAME, value)

    var lastPeriodStart: LocalDate
        get() {
            val dateStr = getSecureString(KEY_LAST_PERIOD_START, "")
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
        set(value) = saveSecureString(KEY_LAST_PERIOD_START, value.toString())

    var periodDuration: Int
        get() = getSecureInt(KEY_PERIOD_DURATION, 5)
        set(value) = saveSecureInt(KEY_PERIOD_DURATION, value)

    var cycleLength: Int
        get() = getSecureInt(KEY_CYCLE_LENGTH, 28)
        set(value) = saveSecureInt(KEY_CYCLE_LENGTH, value)

    var backgroundedAt: Long
        get() = prefs.getLong("backgrounded_at", 0L)
        set(value) { prefs.edit().putLong("backgrounded_at", value).commit() }

    // --- LOG STORAGE & PROCESSING (ENCRYPTED) ---
    fun saveLog(dateStr: String, logData: LogData) {
        val jsonObject = org.json.JSONObject()
        jsonObject.put("mood", logData.mood)
        jsonObject.put("flow", logData.flow)
        val symptomsArray = org.json.JSONArray()
        logData.symptoms.forEach { symptomsArray.put(it) }
        jsonObject.put("symptoms", symptomsArray)
        jsonObject.put("notes", logData.notes)
        
        saveSecureString("log_$dateStr", jsonObject.toString())
    }

    fun getLog(dateStr: String): LogData? {
        val jsonStr = getSecureString("log_$dateStr", "")
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

    fun clear() {
        prefs.edit().clear().commit()
    }

    fun getAllLogs(): Map<String, LogData> {
        val result = mutableMapOf<String, LogData>()
        val allPrefs = prefs.all
        for ((key, value) in allPrefs) {
            if (key.startsWith("log_") && value is String) {
                val dateStr = key.substring(4)
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

    // --- SECURE SECURITY PROPERTIES ---
    var userPin: String?
        get() {
            val pin = getSecureString("user_pin", "")
            return pin.ifEmpty { null }
        }
        set(value) = saveSecureString("user_pin", value)

    var biometricEnabled: Boolean
        get() = getSecureBoolean("biometric_enabled", false)
        set(value) = saveSecureBoolean("biometric_enabled", value)

    var disguiseMode: Boolean
        get() = getSecureBoolean("disguise_mode", false)
        set(value) = saveSecureBoolean("disguise_mode", value)

    var padStore: String
        get() = prefs.getString("pad_store", "") ?: ""
        set(value) { prefs.edit().putString("pad_store", value).commit() }

    var appLanguage: String
        get() = prefs.getString("app_language", "English") ?: "English"
        set(value) { prefs.edit().putString("app_language", value).commit() }

    var permissionsAsked: Boolean
        get() = prefs.getBoolean("permissions_asked", false)
        set(value) { prefs.edit().putBoolean("permissions_asked", value).commit() }

    var notificationTestSent: Boolean
        get() = prefs.getBoolean("notification_test_sent", false)
        set(value) { prefs.edit().putBoolean("notification_test_sent", value).commit() }

    var isDarkMode: Boolean
        get() = prefs.getBoolean("dark_mode", false)
        set(value) { prefs.edit().putBoolean("dark_mode", value).commit() }

    // --- SECURE NOTIFICATION PREFERENCES ---
    var periodReminderEnabled: Boolean
        get() = getSecureBoolean("notif_period_reminder", true)
        set(value) = saveSecureBoolean("notif_period_reminder", value)

    var ovulationAlertEnabled: Boolean
        get() = getSecureBoolean("notif_ovulation_alert", true)
        set(value) = saveSecureBoolean("notif_ovulation_alert", value)

    var dailyLogReminderEnabled: Boolean
        get() = getSecureBoolean("notif_daily_log", true)
        set(value) = saveSecureBoolean("notif_daily_log", value)

    var dailyLogReminderTime: String
        get() = getSecureString("notif_daily_log_time", "21:00")
        set(value) = saveSecureString("notif_daily_log_time", value)

    var padReminderEnabled: Boolean
        get() = getSecureBoolean("notif_pad_reminder", true)
        set(value) = saveSecureBoolean("notif_pad_reminder", value)

    // --- ADVANCED LOCKOUT & TIMEOUT MANAGEMENT ---
    var lockoutDuration: Long
        get() = getSecureLong("lockout_duration", 0L)
        set(value) = saveSecureLong("lockout_duration", value)

    fun getLockoutTimeRemaining(): Long {
        val expiration = getSecureLong("lockout_expiration_time", 0L)
        if (expiration == 0L) return 0L
        val diff = expiration - System.currentTimeMillis()
        return if (diff > 0) diff else 0L
    }

    fun setLockout(durationMs: Long) {
        lockoutDuration = durationMs
        saveSecureLong("lockout_expiration_time", System.currentTimeMillis() + durationMs)
    }

    fun clearLockout() {
        saveSecureLong("lockout_expiration_time", 0L)
        lockoutDuration = 0L
        saveSecureInt("pin_attempts", 0)
    }

    var wrongAttempts: Int
        get() = getSecureInt("pin_attempts", 0)
        set(value) = saveSecureInt("pin_attempts", value)
}
