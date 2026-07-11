package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

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

    fun clear() {
        prefs.edit().clear().apply()
    }
}
