package com.example

import com.example.utils.ShareMessages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareMessagesUnitTest {

    @Test
    fun shareTemplates_matchExactEnglishTemplatesForAllFourPhases() {
        assertEquals(
            "Hey, I'm on my period today 🌸 I might need extra rest and comfort. Thanks for understanding 💕",
            ShareMessages.getDefaultTemplate("menstruation", "English")
        )
        assertEquals(
            "Feeling energetic today! 🌿 Good day to make plans together.",
            ShareMessages.getDefaultTemplate("follicular", "English")
        )
        assertEquals(
            "I'm at peak energy today 🌟 Feeling great!",
            ShareMessages.getDefaultTemplate("ovulation", "English")
        )
        assertEquals(
            "I'm in my PMS phase right now 🌙 Might be a bit more sensitive than usual — a little extra patience would mean a lot 💕",
            ShareMessages.getDefaultTemplate("luteal", "English")
        )
    }

    @Test
    fun normalizePhase_handlesAliasesCaseAndUnknownFallback() {
        assertEquals("menstruation", ShareMessages.normalizePhase("Menstrual"))
        assertEquals("menstruation", ShareMessages.normalizePhase("  MENSTRUATION "))
        assertEquals("follicular", ShareMessages.normalizePhase("Follicular"))
        assertEquals("ovulation", ShareMessages.normalizePhase("OVULATION"))
        assertEquals("luteal", ShareMessages.normalizePhase("Luteal"))
        assertEquals("follicular", ShareMessages.normalizePhase("unknown"))
    }

    @Test
    fun getMessageForPhase_prefersCustomFavoriteAndFallsBackWhenBlank() {
        val custom = mapOf("luteal" to "Cozy tea night please 💕", "ovulation" to "   ")

        assertEquals(
            "Cozy tea night please 💕",
            ShareMessages.getMessageForPhase("luteal", "English", custom)
        )
        assertEquals(
            ShareMessages.shareTemplates["ovulation"],
            ShareMessages.getMessageForPhase("ovulation", "English", custom)
        )
        assertEquals(
            ShareMessages.shareTemplates["menstruation"],
            ShareMessages.getMessageForPhase("menstrual", "English", custom)
        )
    }

    @Test
    fun shareTemplates_localizedInHindiAndTelugu() {
        for (phase in listOf("menstruation", "follicular", "ovulation", "luteal")) {
            val hi = ShareMessages.getDefaultTemplate(phase, "हिंदी")
            val te = ShareMessages.getDefaultTemplate(phase, "తెలుగు")
            assertTrue(hi.isNotBlank())
            assertTrue(te.isNotBlank())
            assertFalse(hi.startsWith("share_template_"))
            assertFalse(te.startsWith("share_template_"))
        }
    }

    @Test
    fun shareTemplates_neverIncludeSensitiveCycleDatesOrIdentifiers() {
        for (phase in listOf("menstruation", "follicular", "ovulation", "luteal")) {
            val msg = ShareMessages.getDefaultTemplate(phase, "English")
            assertFalse(Regex("\\d{4}-\\d{2}-\\d{2}").containsMatchIn(msg))
            assertFalse(Regex("\\b\\d{4}\\b").containsMatchIn(msg))
        }
    }
}
