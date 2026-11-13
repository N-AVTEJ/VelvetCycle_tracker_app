package com.example

import com.example.utils.CycleEngine
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CycleEngineTest {

    @Test
    fun isDayBeforePeriod_returnsTrue_whenPeriodIsTomorrow() {
        val today = LocalDate.of(2025, 6, 15)

        // If today is day before period (i.e. period starts tomorrow June 16):
        // Period is day 1 of next cycle.
        // With cycleLength = 28, lastPeriodStart was 27 days ago.
        // daysSinceLast = 27
        // currentCycleDay = (27 % 28) + 1 = 28
        // daysUntilNext = 28 - 28 = 0
        // Wait, under the prompt formula:
        // const daysSinceLast = Math.floor((today - last) / (1000 * 60 * 60 * 24))
        // const currentCycleDay = (daysSinceLast % cycleLength) + 1
        // const daysUntilNext = cycleLength - currentCycleDay
        // return daysUntilNext === 1
        // For daysUntilNext === 1:
        // currentCycleDay must be cycleLength - 1.
        // (daysSinceLast % cycleLength) + 1 = cycleLength - 1
        // daysSinceLast = cycleLength - 2.
        // Let's verify:
        // If lastPeriodStart = May 19, cycleLength = 28.
        // May 19 + 28 days = June 16 (next period).
        // If today is June 14:
        // daysSinceLast = 26.
        // currentCycleDay = (26 % 28) + 1 = 27.
        // daysUntilNext = 28 - 27 = 1. -> returns true!
        // And tomorrow is June 15. Next period is June 16.
        // So daysUntilNext === 1 means exactly 1 full day remaining before period day.
        val lastPeriodStart28 = today.minusDays(26)
        assertTrue(
            "Period is tomorrow when daysUntilNext is 1",
            CycleEngine.isDayBeforePeriod(lastPeriodStart28, 28, today)
        )
    }

    @Test
    fun isDayBeforePeriod_returnsFalse_forAllOtherDays() {
        val today = LocalDate.of(2025, 6, 15)
        val cycleLength = 28

        // Day 1
        assertFalse(CycleEngine.isDayBeforePeriod(today, cycleLength, today))

        // Day 5
        assertFalse(CycleEngine.isDayBeforePeriod(today.minusDays(4), cycleLength, today))

        // Day 14
        assertFalse(CycleEngine.isDayBeforePeriod(today.minusDays(13), cycleLength, today))

        // Day 26 (daysUntilNext = 2)
        assertFalse(CycleEngine.isDayBeforePeriod(today.minusDays(25), cycleLength, today))

        // Day 27 (daysUntilNext = 1 -> TRUE)
        assertTrue(CycleEngine.isDayBeforePeriod(today.minusDays(26), cycleLength, today))

        // Day 28 (daysUntilNext = 0)
        assertFalse(CycleEngine.isDayBeforePeriod(today.minusDays(27), cycleLength, today))
    }

    @Test
    fun isDayBeforePeriod_worksWithAnyCycleLength_21to35() {
        val today = LocalDate.of(2025, 6, 15)

        for (cycleLength in 21..35) {
            // Under prompt formula, daysSinceLast = cycleLength - 2 gives daysUntilNext === 1
            val lastPeriodStart = today.minusDays((cycleLength - 2).toLong())

            assertTrue(
                "Cycle length $cycleLength should return true when daysUntilNext is 1",
                CycleEngine.isDayBeforePeriod(lastPeriodStart, cycleLength, today)
            )

            // Other days must return false
            val twoDaysBefore = today.minusDays((cycleLength - 3).toLong())
            assertFalse(
                "Cycle length $cycleLength should return false two days before",
                CycleEngine.isDayBeforePeriod(twoDaysBefore, cycleLength, today)
            )

            val dayOfPeriod = today.minusDays(cycleLength.toLong())
            assertFalse(
                "Cycle length $cycleLength should return false on day of period",
                CycleEngine.isDayBeforePeriod(dayOfPeriod, cycleLength, today)
            )
        }
    }

    @Test
    fun isDayBeforePeriod_stringOverload_worksCorrectly() {
        val today = LocalDate.of(2025, 6, 15)
        val lastPeriodStartStr = today.minusDays(26).toString()

        assertTrue(CycleEngine.isDayBeforePeriod(lastPeriodStartStr, 28, today))
    }

    @Test
    fun getPrepDay_returnsExactDayBeforeNextPeriod() {
        val lastPeriodStart = LocalDate.of(2025, 6, 1)
        val cycleLength = 28
        // Next period is June 1 + 28 = June 29
        // Prep day is June 29 - 1 = June 28
        val expectedPrepDay = LocalDate.of(2025, 6, 28)

        assertEquals(expectedPrepDay, CycleEngine.getPrepDay(lastPeriodStart, cycleLength))
        assertEquals(expectedPrepDay, CycleEngine.getPrepDay(lastPeriodStart.toString(), cycleLength))
    }

    @Test
    fun notifications_prepDayCalculationMatchesTomorrow() {
        // Set lastPeriodStart so day before next period is tomorrow
        val today = LocalDate.now()
        val cycleLength = 28
        // Next period is today + 2 days, so prepDay is tomorrow (today + 1 day)
        // lastPeriodStart = nextPeriod - 28 = (today + 2) - 28 = today - 26
        val lastPeriodStart = today.minusDays(26)
        val prepDay = CycleEngine.getPrepDay(lastPeriodStart, cycleLength)
        assertEquals(today.plusDays(1), prepDay)
    }

    @Test
    fun calendar_prepDayToday_dotColorAndNonOverlap() {
        // When day before period is today:
        val today = LocalDate.now()
        val cycleLength = 28
        val periodDuration = 5

        // Period is tomorrow (today + 1), so last period was 27 days ago
        val lastPeriodStart = today.minusDays(27)

        val prepDay = CycleEngine.getPrepDay(lastPeriodStart, cycleLength)
        assertEquals("prepDay formatted as YYYY-MM-DD matches today", today, prepDay)

        // Verify non-overlap:
        // Day before period is cycle day 28
        // Period days are 1..5
        val daysSince = java.time.temporal.ChronoUnit.DAYS.between(lastPeriodStart, prepDay)
        val mod = daysSince % cycleLength
        val isPeriod = mod < periodDuration
        assertFalse("Prepare day must not overlap with period days", isPeriod)

        // Ovulation is cycle day 14 (mod 13)
        val isOvulation = mod == (cycleLength - 14 - 1).toLong()
        assertFalse("Prepare day must not overlap with ovulation day", isOvulation)

        // Fertile window is cycle days 9..15
        val ovulationDayNum = cycleLength - 14 - 1
        val isFertile = mod in (ovulationDayNum - 5)..(ovulationDayNum + 1)
        assertFalse("Prepare day must not overlap with fertile window", isFertile)

        // Confirm it is prepare day (cycleLength - 1)
        val isPrepare = mod == (cycleLength - 1).toLong()
        assertTrue("Prepare day is exactly day before period", isPrepare)
    }

    @Test
    fun detectAnomalies_detectsCycleLengthDeviationOver7Days() {
        // Average of history: (28 + 28 + 28) / 3 = 28
        // Current cycle length: 36 (deviation = 8 > 7)
        val history = listOf(28, 28, 28)
        val anomalies = CycleEngine.detectAnomalies(history, currentCycleLength = 36, currentPeriodDuration = 5)

        assertEquals(1, anomalies.size)
        val anomaly = anomalies[0]
        assertEquals("cycle_length", anomaly.type)
        assertEquals("notice", anomaly.severity)
        assertEquals(8, anomaly.deviationDays)
        assertTrue(anomaly.message.contains("8 days"))
    }

    @Test
    fun detectAnomalies_noCycleLengthAnomaly_whenDeviation7OrLess() {
        // Average = 28, current = 35 (deviation = 7, not > 7)
        val history = listOf(28, 28, 28)
        val anomalies = CycleEngine.detectAnomalies(history, currentCycleLength = 35, currentPeriodDuration = 5)

        assertTrue("No anomaly when deviation is 7 or less", anomalies.isEmpty())
    }

    @Test
    fun detectAnomalies_ignoresNegativeOrZeroValuesSafely() {
        // Invalid input validation test
        val history = listOf(28, -5, 0, 28, 28)
        val anomalies = CycleEngine.detectAnomalies(history, currentCycleLength = 40, currentPeriodDuration = 5)

        assertEquals(1, anomalies.size)
        assertEquals("cycle_length", anomalies[0].type)
    }

    @Test
    fun detectAnomalies_detectsLongPeriodDurationOver9Days() {
        val history = listOf(28, 28, 28)
        val anomalies = CycleEngine.detectAnomalies(history, currentCycleLength = 28, currentPeriodDuration = 10)

        assertEquals(1, anomalies.size)
        val anomaly = anomalies[0]
        assertEquals("long_period", anomaly.type)
        assertEquals("warning", anomaly.severity)
        assertTrue(anomaly.message.contains("9 days"))
    }

    @Test
    fun isMissedPeriod_returnsTrue_whenMoreThan7DaysPastCycleLength() {
        val today = LocalDate.of(2025, 6, 15)
        val cycleLength = 28

        // Exactly cycleLength + 7 days ago (35 days) -> not missed (> 35 required)
        val lastStart35 = today.minusDays(35)
        assertFalse(CycleEngine.isMissedPeriod(lastStart35, cycleLength, today))

        // 36 days ago (cycleLength + 8) -> missed!
        val lastStart36 = today.minusDays(36)
        assertTrue(CycleEngine.isMissedPeriod(lastStart36, cycleLength, today))
    }
}
