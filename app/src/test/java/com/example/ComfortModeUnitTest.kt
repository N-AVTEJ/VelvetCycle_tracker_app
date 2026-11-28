package com.example

import com.example.constants.Translations
import com.example.ui.theme.DarkVelvetColors
import com.example.utils.ComfortModeHelper
import com.example.utils.InsightsEngine
import com.example.utils.LogData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ComfortModeUnitTest {

    @Test
    fun eligibility_autoDetectsDayBeforePeriodAndMenstruationOnly() {
        assertTrue(ComfortModeHelper.isEligibleForComfortMode(isDayBefore = true, currentPhase = "Luteal"))
        assertTrue(ComfortModeHelper.isEligibleForComfortMode(isDayBefore = false, currentPhase = "Menstruation"))
        assertTrue(ComfortModeHelper.isEligibleForComfortMode(isDayBefore = false, currentPhase = "menstruation"))
        assertTrue(ComfortModeHelper.isEligibleForComfortMode(isDayBefore = false, currentPhase = "menstrual"))

        assertFalse(ComfortModeHelper.isEligibleForComfortMode(isDayBefore = false, currentPhase = "Follicular"))
        assertFalse(ComfortModeHelper.isEligibleForComfortMode(isDayBefore = false, currentPhase = "Ovulation"))
        assertFalse(ComfortModeHelper.isEligibleForComfortMode(isDayBefore = false, currentPhase = "Luteal"))
    }

    @Test
    fun resolveActiveState_defaultsSuggestedOnWhenEligibleAndRespectsDailyOverride() {
        val day1 = LocalDate.of(2026, 11, 27)
        val day2 = LocalDate.of(2026, 11, 28)

        // Default suggested ON when eligible and no saved preference
        assertTrue(ComfortModeHelper.resolveActiveState(eligible = true, savedRaw = null, today = day1))

        // User turns Comfort Mode OFF on day1 -> stays OFF for day1
        val savedOffDay1 = ComfortModeHelper.encodePreference(active = false, date = day1)
        assertFalse(ComfortModeHelper.resolveActiveState(eligible = true, savedRaw = savedOffDay1, today = day1))

        // Next day (day2): daily reset returns to suggested ON
        assertNull(ComfortModeHelper.parsePreferenceForDate(savedOffDay1, today = day2))
        assertTrue(ComfortModeHelper.resolveActiveState(eligible = true, savedRaw = savedOffDay1, today = day2))
    }

    @Test
    fun resolveActiveState_autoTurnsOffWhenPeriodEnds() {
        val today = LocalDate.of(2026, 11, 27)
        val savedOnToday = ComfortModeHelper.encodePreference(active = true, date = today)

        // Even if preference was ON today, once period ends (eligible = false), Comfort Mode turns OFF
        assertFalse(ComfortModeHelper.resolveActiveState(eligible = false, savedRaw = savedOnToday, today = today))
    }

    @Test
    fun comfortTips_matchExactEnglishCopyAndRotateAcrossAllThreeTips() {
        val tips = ComfortModeHelper.getComfortTips("English")
        assertEquals(3, tips.size)
        assertEquals("Rest is productive too 🌸", tips[0])
        assertEquals("Be gentle with yourself today", tips[1])
        assertEquals("A warm drink and rest can help", tips[2])

        assertEquals(1, ComfortModeHelper.nextTipIndex(0))
        assertEquals(2, ComfortModeHelper.nextTipIndex(1))
        assertEquals(0, ComfortModeHelper.nextTipIndex(2))
    }

    @Test
    fun comfortStrings_localizedInHindiAndTelugu() {
        val keys = listOf(
            "comfort_mode_toggle",
            "comfort_mood_title",
            "comfort_flow_title",
            "comfort_tip_0",
            "comfort_tip_1",
            "comfort_tip_2"
        )
        for (lang in listOf("हिंदी", "తెలుగు")) {
            for (key in keys) {
                val translated = Translations.t(key, lang)
                assertTrue("$key should not be blank in $lang", translated.isNotBlank())
                assertFalse("$key should not return raw key in $lang", translated == key)
            }
        }
    }

    @Test
    fun darkModeTheme_usesWarmMutedPink3A2430ForComfortBackground() {
        assertEquals(0xFF3A2430.toInt(), DarkVelvetColors.comfortBgStart.value.shr(32).toInt())
    }

    @Test
    fun doctorSummaryExport_neverLeaksComfortModePreference() {
        val logs = mapOf(
            "2026-11-25" to LogData("😔", "Heavy", listOf("Heavy bleeding", "Severe cramps"), "")
        )
        val summary = InsightsEngine.buildDoctorSummaryText(
            avgCycleLength = 28,
            avgPeriodDuration = 5,
            topSymptoms = listOf("Heavy bleeding" to 3),
            lastThreePeriodDates = listOf(
                LocalDate.of(2026, 9, 30),
                LocalDate.of(2026, 10, 28),
                LocalDate.of(2026, 11, 25)
            )
        )
        assertFalse(summary.contains("comfort_mode_active", ignoreCase = true))
        assertFalse(summary.contains("comfort mode", ignoreCase = true))
        assertTrue(logs.isNotEmpty())
    }
}
