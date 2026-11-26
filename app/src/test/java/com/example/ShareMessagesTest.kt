package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.utils.ShareMessages
import com.example.utils.StorageHelper
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ShareMessagesTest {

    private lateinit var context: Context
    private lateinit var storageHelper: StorageHelper

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        storageHelper = StorageHelper(context)
        storageHelper.clear()
    }

    @Test
    fun shareTemplates_matchExactEnglishCopyForAllFourPhases() {
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
    fun shareTemplates_supportHindiAndTeluguTranslations() {
        for (phase in listOf("menstruation", "follicular", "ovulation", "luteal")) {
            val hi = ShareMessages.getDefaultTemplate(phase, "हिंदी")
            val te = ShareMessages.getDefaultTemplate(phase, "తెలుగు")
            assertTrue("Hindi template for $phase should not be blank", hi.isNotBlank())
            assertTrue("Telugu template for $phase should not be blank", te.isNotBlank())
            assertFalse("Hindi template should not return raw key", hi.startsWith("share_template_"))
            assertFalse("Telugu template should not return raw key", te.startsWith("share_template_"))
        }
    }

    @Test
    fun showShareButtonOnHome_defaultsToTrue_andPersistsToggle() {
        assertTrue("Share button on home screen must default to ON", storageHelper.showShareButtonOnHome)

        storageHelper.showShareButtonOnHome = false
        assertFalse(storageHelper.showShareButtonOnHome)

        storageHelper.showShareButtonOnHome = true
        assertTrue(storageHelper.showShareButtonOnHome)
    }

    @Test
    fun customShareMessages_persistsPerPhaseInAsyncStorageKey_andFallsBackWhenCleared() {
        val customLuteal = "Need cozy movie night and tea tonight 💕"
        storageHelper.saveCustomShareMessage("luteal", customLuteal)

        // Verify stored under exact 'custom_share_messages' key in regularPrefs
        val plainPrefs = context.getSharedPreferences("VelvetCyclePrefs", Context.MODE_PRIVATE)
        assertTrue(plainPrefs.contains("custom_share_messages"))

        val map = storageHelper.getCustomShareMessages()
        assertEquals(customLuteal, ShareMessages.getMessageForPhase("luteal", "English", map))
        // Other phases remain default
        assertEquals(
            ShareMessages.shareTemplates["follicular"],
            ShareMessages.getMessageForPhase("follicular", "English", map)
        )

        // Resetting restores default
        storageHelper.saveCustomShareMessage("luteal", "")
        val afterReset = storageHelper.getCustomShareMessages()
        assertEquals(
            ShareMessages.shareTemplates["luteal"],
            ShareMessages.getMessageForPhase("luteal", "English", afterReset)
        )
    }

    @Test
    fun shareTemplates_containNoSensitiveDataOrIdentifiers() {
        storageHelper.userPin = "9182"
        storageHelper.userName = "SecretName"

        val allTemplates = listOf("menstruation", "follicular", "ovulation", "luteal")
            .map { ShareMessages.getMessageForPhase(it, "English", emptyMap()) }

        allTemplates.forEach { msg ->
            assertFalse("Message must not contain PIN", msg.contains("9182"))
            assertFalse("Message must not contain userName", msg.contains("SecretName"))
            assertFalse("Message must not contain ISO cycle dates", Regex("\\d{4}-\\d{2}-\\d{2}").containsMatchIn(msg))
        }
    }
}
