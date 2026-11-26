package com.example.utils

import android.content.Context
import android.content.Intent
import com.example.constants.Translations

/**
 * Partner Share-a-Day phase message templates and built-in Android Share Intent helper.
 * Privacy guarantees:
 * - Only the user-reviewed phase message text ever leaves the app (no cycle dates, symptoms, PIN, or device IDs).
 * - Share sheet is only triggered via explicit user action on the 'Send' button after previewing/editing.
 * - Custom user messages are never written to Log/debug output.
 */
object ShareMessages {

    val shareTemplates: Map<String, String> = mapOf(
        "menstruation" to "Hey, I'm on my period today 🌸 I might need extra rest and comfort. Thanks for understanding 💕",
        "follicular" to "Feeling energetic today! 🌿 Good day to make plans together.",
        "ovulation" to "I'm at peak energy today 🌟 Feeling great!",
        "luteal" to "I'm in my PMS phase right now 🌙 Might be a bit more sensitive than usual — a little extra patience would mean a lot 💕"
    )

    fun normalizePhase(phase: String): String {
        return when (phase.lowercase().trim()) {
            "menstruation", "menstrual" -> "menstruation"
            "follicular" -> "follicular"
            "ovulation" -> "ovulation"
            "luteal" -> "luteal"
            else -> "follicular"
        }
    }

    /**
     * Returns the default template for a cycle phase localized via Translations.t().
     */
    fun getDefaultTemplate(phase: String, language: String = "English"): String {
        val normalized = normalizePhase(phase)
        val key = "share_template_$normalized"
        val translated = Translations.t(key, language)
        return if (translated == key) {
            shareTemplates[normalized] ?: shareTemplates.getValue("follicular")
        } else {
            translated
        }
    }

    /**
     * Returns the user's saved favorite message for the phase if present,
     * otherwise falls back to the localized default template.
     */
    fun getMessageForPhase(
        phase: String,
        language: String = "English",
        customMessages: Map<String, String> = emptyMap()
    ): String {
        val normalized = normalizePhase(phase)
        val custom = customMessages[normalized]?.trim()
        return if (!custom.isNullOrEmpty()) {
            custom
        } else {
            getDefaultTemplate(normalized, language)
        }
    }

    /**
     * Opens the built-in Android Share sheet with strictly the reviewed message string.
     * Returns true if the chooser launched, false on failure.
     */
    fun sharePhaseMessage(context: Context, message: String): Boolean {
        val sanitized = message.trim()
        if (sanitized.isEmpty()) return false

        return try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, sanitized)
            }
            val chooser = Intent.createChooser(sendIntent, null).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            false
        }
    }
}
