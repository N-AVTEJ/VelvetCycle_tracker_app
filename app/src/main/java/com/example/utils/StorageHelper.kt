package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

data class LogData(
    val mood: String,
    val flow: String,
    val symptoms: List<String>,
    val notes: String
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

    var isOnboarded: Boolean
        get() = prefs.getBoolean(KEY_IS_ONBOARDED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_ONBOARDED, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var lastPeriodStart: LocalDate
        get() {
            val dateStr = prefs.getString(KEY_LAST_PERIOD_START, null)
            return if (dateStr != null) {
                try {
                    LocalDate.parse(dateStr)
                } catch (e: Exception) {
                    LocalDate.now()
                }
            } else {
                LocalDate.now()
            }
        }
        set(value) = prefs.edit().putString(KEY_LAST_PERIOD_START, value.toString()).apply()

    var periodDuration: Int
        get() = prefs.getInt(KEY_PERIOD_DURATION, 5)
        set(value) = prefs.edit().putInt(KEY_PERIOD_DURATION, value).apply()

    var cycleLength: Int
        get() = prefs.getInt(KEY_CYCLE_LENGTH, 28)
        set(value) = prefs.edit().putInt(KEY_CYCLE_LENGTH, value).apply()

    fun saveLog(dateStr: String, logData: LogData) {
        val jsonObject = org.json.JSONObject()
        jsonObject.put("mood", logData.mood)
        jsonObject.put("flow", logData.flow)
        val symptomsArray = org.json.JSONArray()
        logData.symptoms.forEach { symptomsArray.put(it) }
        jsonObject.put("symptoms", symptomsArray)
        jsonObject.put("notes", logData.notes)
        
        prefs.edit().putString("log_$dateStr", jsonObject.toString()).apply()
    }

    fun getLog(dateStr: String): LogData? {
        val jsonStr = prefs.getString("log_$dateStr", null) ?: return null
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
        prefs.edit().clear().apply()
    }

    // Phase 3 Properties
    var userPin: String?
        get() = prefs.getString("user_pin", null)
        set(value) = prefs.edit().putString("user_pin", value).apply()

    var biometricEnabled: Boolean
        get() = prefs.getBoolean("biometric_enabled", false)
        set(value) = prefs.edit().putBoolean("biometric_enabled", value).apply()

    var padStore: String
        get() = prefs.getString("pad_store", "") ?: ""
        set(value) = prefs.edit().putString("pad_store", value).apply()

    var appLanguage: String
        get() = prefs.getString("app_language", "English") ?: "English"
        set(value) = prefs.edit().putString("app_language", value).apply()

    var periodReminderEnabled: Boolean
        get() = prefs.getBoolean("notif_period_reminder", true)
        set(value) = prefs.edit().putBoolean("notif_period_reminder", value).apply()

    var ovulationAlertEnabled: Boolean
        get() = prefs.getBoolean("notif_ovulation_alert", true)
        set(value) = prefs.edit().putBoolean("notif_ovulation_alert", value).apply()

    var dailyLogReminderEnabled: Boolean
        get() = prefs.getBoolean("notif_daily_log", true)
        set(value) = prefs.edit().putBoolean("notif_daily_log", value).apply()

    var dailyLogReminderTime: String
        get() = prefs.getString("notif_daily_log_time", "21:00") ?: "21:00"
        set(value) = prefs.edit().putString("notif_daily_log_time", value).apply()

    var padReminderEnabled: Boolean
        get() = prefs.getBoolean("notif_pad_reminder", true)
        set(value) = prefs.edit().putBoolean("notif_pad_reminder", value).apply()

    fun getLockoutTimeRemaining(): Long {
        val lockoutTime = prefs.getLong("lockout_time", 0L)
        if (lockoutTime == 0L) return 0L
        val diff = (lockoutTime + 5 * 60 * 1000) - System.currentTimeMillis()
        return if (diff > 0) diff else 0L
    }

    fun setLockout() {
        prefs.edit().putLong("lockout_time", System.currentTimeMillis()).apply()
    }

    fun clearLockout() {
        prefs.edit().putLong("lockout_time", 0L).apply()
        prefs.edit().putInt("wrong_attempts", 0).apply()
    }

    var wrongAttempts: Int
        get() = prefs.getInt("wrong_attempts", 0)
        set(value) = prefs.edit().putInt("wrong_attempts", value).apply()
}

