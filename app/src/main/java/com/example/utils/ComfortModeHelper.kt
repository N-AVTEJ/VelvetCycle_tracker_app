package com.example.utils

import com.example.constants.Translations
import java.time.LocalDate

/**
 * Pure logic for Phase 23 Comfort Mode UI:
 * - Auto-detects eligibility on day before period OR menstruation phase.
 * - Suggests ON by default each day while eligible, while allowing user override.
 * - Resets daily (stored preference is date-stamped as YYYY-MM-DD).
 * - Automatically turns OFF when period ends so it resets cleanly each cycle.
 *
 * Security note:
 * comfort_mode_active is strictly a non-sensitive local UI preference and is never included
 * in any health summary export or partner share output.
 */
object ComfortModeHelper {

    const val PREF_KEY_COMFORT_MODE_ACTIVE = "comfort_mode_active"

    val comfortTipKeys: List<String> = listOf(
        "comfort_tip_0",
        "comfort_tip_1",
        "comfort_tip_2"
    )

    fun isEligibleForComfortMode(isDayBefore: Boolean, currentPhase: String): Boolean {
        val normalized = currentPhase.trim().lowercase()
        return isDayBefore || normalized == "menstruation" || normalized == "menstrual"
    }

    /**
     * Serializes today's comfort mode override into a compact date-scoped string.
     */
    fun encodePreference(active: Boolean, date: LocalDate = LocalDate.now()): String {
        return "${date}|$active"
    }

    /**
     * Parses a stored preference string ("YYYY-MM-DD|true" or "YYYY-MM-DD|false").
     * Returns null if missing, malformed, or from a previous day (daily reset).
     */
    fun parsePreferenceForDate(raw: String?, today: LocalDate = LocalDate.now()): Boolean? {
        if (raw.isNullOrBlank()) return null
        val parts = raw.split("|")
        if (parts.size != 2) return null
        if (parts[0] != today.toString()) return null
        return parts[1].toBooleanStrictOrNull()
    }

    /**
     * Computes whether Comfort Mode should be active right now:
     * - If period has ended (!eligible), ALWAYS returns false.
     * - If eligible and user toggled today, returns user's override for today.
     * - If eligible and no override exists for today (first open today / daily reset), returns true.
     */
    fun resolveActiveState(
        eligible: Boolean,
        savedRaw: String?,
        today: LocalDate = LocalDate.now()
    ): Boolean {
        if (!eligible) return false
        return parsePreferenceForDate(savedRaw, today) ?: true
    }

    fun getComfortTips(language: String): List<String> {
        return comfortTipKeys.map { Translations.t(it, language) }
    }

    fun getInitialTipIndex(today: LocalDate = LocalDate.now()): Int {
        return (today.dayOfYear % comfortTipKeys.size).coerceIn(0, comfortTipKeys.lastIndex)
    }

    fun nextTipIndex(currentIndex: Int): Int {
        return (currentIndex + 1) % comfortTipKeys.size
    }
}
