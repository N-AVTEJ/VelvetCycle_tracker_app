package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.constants.Translations
import com.example.utils.InsightsEngine
import com.example.utils.LogData
import com.example.utils.StorageHelper
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class InsightsEngineTest {

    private lateinit var context: Context
    private lateinit var storageHelper: StorageHelper

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        storageHelper = StorageHelper(context)
        storageHelper.clear()
    }

    @Test
    fun checkDoctorFlag_neverFiresWithFewerThan3Cycles() {
        val severeLogs = (1..10).map {
            LogData(mood = "😔", flow = "Heavy", symptoms = listOf("Heavy bleeding", "Severe cramps"), notes = "")
        }

        assertNull("Must not fire when cycleCount is 0", InsightsEngine.checkDoctorFlag(severeLogs, 0))
        assertNull("Must not fire when cycleCount is 1", InsightsEngine.checkDoctorFlag(severeLogs, 1))
        assertNull("Must not fire when cycleCount is 2", InsightsEngine.checkDoctorFlag(severeLogs, 2))
    }

    @Test
    fun checkDoctorFlag_returnsNullWhenFewerThan6SevereDays() {
        val logs = listOf(
            LogData("", "Heavy", listOf("Heavy bleeding"), ""),
            LogData("", "Heavy", listOf("Severe cramps"), ""),
            LogData("", "Medium", listOf("Severe pain"), ""),
            LogData("", "Light", listOf("Heavy bleeding"), ""),
            LogData("", "Light", listOf("Severe cramps"), ""),
            LogData("", "None", listOf("Cramps", "Headache"), "")
        )

        val result = InsightsEngine.checkDoctorFlag(logs, cycleCount = 3)
        assertNull("Should not flag with only 5 severe days", result)
    }

    @Test
    fun checkDoctorFlag_firesAt6OrMoreSevereDaysInLast90LogsWithAtLeast3Cycles() {
        val logs = listOf(
            LogData("", "Heavy", listOf("Heavy bleeding"), ""),
            LogData("", "Heavy", listOf("Severe cramps"), ""),
            LogData("", "Medium", listOf("Severe pain"), ""),
            LogData("", "Heavy", listOf("Heavy bleeding"), ""),
            LogData("", "Heavy", listOf("Severe cramps"), ""),
            LogData("", "Heavy", listOf("Severe pain"), "")
        )

        val result = InsightsEngine.checkDoctorFlag(logs, cycleCount = 3)
        assertNotNull("Should flag with 6 severe days and 3 cycles", result)
        assertTrue(result!!.shouldFlag)
        assertEquals("severe_symptoms", result.reason)
        assertEquals(
            "You have logged severe symptoms repeatedly over your last few cycles.",
            result.message
        )
    }

    @Test
    fun checkDoctorFlag_onlyEvaluatesLast90Logs() {
        // 10 old severe logs followed by 90 mild logs -> should NOT flag
        val oldSevere = (1..10).map {
            LogData("", "Heavy", listOf("Severe pain"), "")
        }
        val recentMild = (1..90).map {
            LogData("", "None", listOf("Headache"), "")
        }

        val result = InsightsEngine.checkDoctorFlag(oldSevere + recentMild, cycleCount = 5)
        assertNull("Severe logs older than last 90 entries should not trigger flag", result)
    }

    @Test
    fun dismissalTimestamp_storedInSecureStoreNotPlainPrefs_andLasts30Days() {
        val now = 1_700_000_000_000L
        storageHelper.dismissDoctorFlagFor30Days(now)

        val savedUntil = storageHelper.doctorFlagDismissedUntil
        assertEquals(now + InsightsEngine.THIRTY_DAYS_MS, savedUntil)

        // Must be dismissed during the 30-day window
        assertTrue(InsightsEngine.isDoctorFlagDismissed(savedUntil, now + 1000L))
        assertTrue(InsightsEngine.isDoctorFlagDismissed(savedUntil, now + InsightsEngine.THIRTY_DAYS_MS - 1L))

        // Must reappear after 30 days
        assertFalse(InsightsEngine.isDoctorFlagDismissed(savedUntil, now + InsightsEngine.THIRTY_DAYS_MS + 1L))

        // Security check: must NOT be stored in plain SharedPreferences (VelvetCyclePrefs)
        val plainPrefs = context.getSharedPreferences("VelvetCyclePrefs", Context.MODE_PRIVATE)
        assertFalse(
            "Dismissal timestamp must not exist in plain SharedPreferences",
            plainPrefs.contains("doctor_flag_dismissed_until")
        )
    }

    @Test
    fun buildDoctorSummaryText_containsChecklistOnly_noPinNoDeviceIdNoDiagnosis() {
        storageHelper.userPin = "4829"
        storageHelper.userName = "PrivateUser"

        val summary = InsightsEngine.buildDoctorSummaryText(
            avgCycleLength = 29,
            avgPeriodDuration = 6,
            topSymptoms = listOf("Severe cramps" to 5, "Heavy bleeding" to 4),
            lastThreePeriodDates = listOf(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 9, 2),
                LocalDate.of(2026, 8, 4)
            )
        )

        assertTrue(summary.contains("Average cycle length: 29 days"))
        assertTrue(summary.contains("Average period duration: 6 days"))
        assertTrue(summary.contains("Severe cramps (5 days)"))
        assertTrue(summary.contains("Heavy bleeding (4 days)"))
        assertTrue(summary.contains("2026-10-01"))
        assertTrue(summary.contains("2026-09-02"))
        assertTrue(summary.contains("2026-08-04"))

        // Privacy & medical disclaimer checks
        assertFalse("Summary must never include user PIN", summary.contains("4829"))
        assertFalse("Summary must never include personal account name", summary.contains("PrivateUser"))
        assertFalse("Summary must never use the word diagnosis", summary.lowercase().contains("diagnosis"))

        // Verify UI strings also never use the word diagnosis
        assertFalse(Translations.t("doctor_flag_title", "English").lowercase().contains("diagnosis"))
        assertFalse(Translations.t("doctor_flag_body", "English").lowercase().contains("diagnosis"))
    }
}
