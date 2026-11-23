package com.example.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDate

data class DoctorFlagResult(
    val shouldFlag: Boolean,
    val reason: String,
    val message: String
)

/**
 * Purely local, on-device insights and symptom pattern helper.
 * Performs no network requests, no analytics events, and no off-device health data logging.
 */
object InsightsEngine {
    const val THIRTY_DAYS_MS: Long = 30L * 24L * 60L * 60L * 1000L

    val SEVERE_SYMPTOMS: Set<String> = setOf(
        "Heavy bleeding",
        "Severe cramps",
        "Severe pain"
    )

    /**
     * Checks whether severe symptoms have been logged repeatedly across recent cycles.
     * Never fires for users with fewer than 3 tracked cycles.
     */
    fun checkDoctorFlag(allLogs: List<LogData>, cycleCount: Int): DoctorFlagResult? {
        if (cycleCount < 3) return null

        val recentLogs = allLogs.takeLast(90)
        val severeDays = recentLogs.filter { log ->
            log.symptoms.any { symptom -> symptom in SEVERE_SYMPTOMS }
        }

        if (severeDays.size >= 6) {
            return DoctorFlagResult(
                shouldFlag = true,
                reason = "severe_symptoms",
                message = "You have logged severe symptoms repeatedly over your last few cycles."
            )
        }

        return null
    }

    /**
     * Date-keyed overload: sorts logs chronologically by ISO date key before evaluating
     * the most recent 90 logged entries.
     */
    fun checkDoctorFlag(allLogsByDate: Map<String, LogData>, cycleCount: Int): DoctorFlagResult? {
        if (cycleCount < 3) return null
        val orderedLogs = allLogsByDate.entries
            .sortedBy { it.key }
            .map { it.value }
        return checkDoctorFlag(orderedLogs, cycleCount)
    }

    /**
     * Returns true if the doctor flag card is currently dismissed (within the 30-day window).
     */
    fun isDoctorFlagDismissed(
        dismissedUntilMs: Long,
        nowMs: Long = System.currentTimeMillis()
    ): Boolean {
        return dismissedUntilMs > nowMs
    }

    /**
     * Generates a clean, on-device plain-text appointment checklist summary.
     * Contains ONLY cycle stats, top symptoms, and last 3 period dates —
     * never includes PIN, device ID, or personal account identifiers.
     */
    fun buildDoctorSummaryText(
        avgCycleLength: Int,
        avgPeriodDuration: Int,
        topSymptoms: List<Pair<String, Int>>,
        lastThreePeriodDates: List<LocalDate>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("VelvetCycle — Appointment Summary Checklist")
        sb.appendLine("--------------------------------------------")
        sb.appendLine()
        sb.appendLine("• Average cycle length: $avgCycleLength days")
        sb.appendLine("• Average period duration: $avgPeriodDuration days")
        sb.appendLine()
        sb.appendLine("• Top symptoms logged:")
        if (topSymptoms.isEmpty()) {
            sb.appendLine("  - None logged")
        } else {
            topSymptoms.forEach { (symptom, count) ->
                sb.appendLine("  - $symptom ($count days)")
            }
        }
        sb.appendLine()
        sb.appendLine("• Dates of last 3 periods:")
        if (lastThreePeriodDates.isEmpty()) {
            sb.appendLine("  - Not enough period dates logged")
        } else {
            lastThreePeriodDates.take(3).forEach { date ->
                sb.appendLine("  - $date")
            }
        }
        sb.appendLine()
        sb.appendLine("Note: This summary is generated locally on-device from personal cycle logs to help guide a conversation with a healthcare professional.")
        return sb.toString()
    }

    /**
     * Shares the appointment summary text strictly on-device via the Android system share sheet
     * (explicit user action only; no background upload or external server transmission).
     */
    fun shareDoctorSummary(context: Context, summaryText: String): Boolean {
        return try {
            val summaryFile = File(context.cacheDir, "VelvetCycle_Doctor_Summary.txt")
            summaryFile.writeText(summaryText, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                summaryFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "VelvetCycle Appointment Summary")
                putExtra(Intent.EXTRA_TEXT, summaryText)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Export this summary").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to export summary. Please try again.", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
