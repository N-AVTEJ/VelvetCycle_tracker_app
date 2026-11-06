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
}
