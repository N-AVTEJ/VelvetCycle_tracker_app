package com.example.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object CycleEngine {

    /**
     * Returns what day of the cycle the user is on today (day 1 = first day of period)
     */
    fun getCurrentCycleDay(lastPeriodStart: LocalDate, cycleLength: Int, today: LocalDate = LocalDate.now()): Int {
        val daysSince = ChronoUnit.DAYS.between(lastPeriodStart, today)
        return if (daysSince >= 0) {
            ((daysSince % cycleLength).toInt()) + 1
        } else {
            1
        }
    }

    /**
     * Returns list of dates that are period days
     */
    fun getPeriodDays(lastPeriodStart: LocalDate, periodDuration: Int): List<LocalDate> {
        return (0 until periodDuration).map { lastPeriodStart.plusDays(it.toLong()) }
    }

    /**
     * Returns exact date of ovulation
     * Ovulation day cycle day = cycleLength - 14
     */
    fun getOvulationDay(lastPeriodStart: LocalDate, cycleLength: Int): LocalDate {
        val ovulationCycleDay = cycleLength - 14
        return lastPeriodStart.plusDays((ovulationCycleDay - 1).toLong())
    }

    /**
     * Fertile window = ovulation day -5 to ovulation day +1
     */
    fun getFertileWindow(lastPeriodStart: LocalDate, cycleLength: Int): List<LocalDate> {
        val ovulationDay = getOvulationDay(lastPeriodStart, cycleLength)
        return (-5..1).map { ovulationDay.plusDays(it.toLong()) }
    }

    /**
     * Returns predicted start date of next period
     */
    fun getNextPeriodDate(lastPeriodStart: LocalDate, cycleLength: Int): LocalDate {
        return lastPeriodStart.plusDays(cycleLength.toLong())
    }

    /**
     * Returns predicted start date of next period in projected cycle
     */
    fun getNextPeriodDateProjected(lastPeriodStart: LocalDate, cycleLength: Int, today: LocalDate = LocalDate.now()): LocalDate {
        val daysSince = ChronoUnit.DAYS.between(lastPeriodStart, today)
        if (daysSince < 0) {
            return lastPeriodStart.plusDays(cycleLength.toLong())
        }
        val currentCycleIndex = daysSince / cycleLength
        return lastPeriodStart.plusDays((currentCycleIndex + 1) * cycleLength.toLong())
    }

    /**
     * Returns number of days until next period (from today)
     */
    fun getDaysUntilNextPeriod(lastPeriodStart: LocalDate, cycleLength: Int, today: LocalDate = LocalDate.now()): Int {
        val nextPeriod = getNextPeriodDate(lastPeriodStart, cycleLength)
        return ChronoUnit.DAYS.between(today, nextPeriod).toInt()
    }

    /**
     * Returns one of: "menstruation" | "follicular" | "ovulation" | "luteal"
     */
    fun getCurrentPhase(lastPeriodStart: LocalDate, cycleLength: Int, periodDuration: Int, today: LocalDate = LocalDate.now()): String {
        val currentCycleDay = getCurrentCycleDay(lastPeriodStart, cycleLength, today)
        val ovulationDayNum = cycleLength - 14

        return when {
            currentCycleDay <= periodDuration -> "menstruation"
            currentCycleDay < ovulationDayNum -> "follicular"
            currentCycleDay == ovulationDayNum -> "ovulation"
            else -> "luteal"
        }
    }
}
