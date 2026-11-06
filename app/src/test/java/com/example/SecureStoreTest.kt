package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.utils.LogData
import com.example.utils.SecureStore
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
class SecureStoreTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        val helper = StorageHelper(context)
        helper.clear()
    }

    @Test
    fun testSecureStore_saveGetDelete() {
        // Direct SecureStore operations
        assertTrue(SecureStore.saveSecure(context, "test_key", "secret_value_123"))
        assertEquals("secret_value_123", SecureStore.getSecure(context, "test_key"))

        assertTrue(SecureStore.deleteSecure(context, "test_key"))
        assertNull(SecureStore.getSecure(context, "test_key"))
    }

    @Test
    fun testPinPersistence_acrossAppRestarts() {
        // 1. App session 1: Set user PIN
        var helper1: StorageHelper? = StorageHelper(context)
        assertNull(helper1!!.userPin)
        helper1.userPin = "8742"
        assertEquals("8742", helper1.userPin)

        // Verify it is in SecureStore
        assertEquals("8742", SecureStore.getSecure(context, "user_pin"))

        // Verify it is NOT in unencrypted regularPrefs
        val regularPrefs = context.getSharedPreferences("VelvetCyclePrefs", Context.MODE_PRIVATE)
        assertFalse(regularPrefs.contains("user_pin"))

        // 2. Kill app / destroy session
        helper1 = null

        // 3. App session 2: Reopen app with a new StorageHelper instance
        val helper2 = StorageHelper(context)
        assertEquals("PIN should persist across app restarts", "8742", helper2.userPin)
    }

    @Test
    fun testSensitiveKeys_storedInSecureStore() {
        val helper = StorageHelper(context)
        val regularPrefs = context.getSharedPreferences("VelvetCyclePrefs", Context.MODE_PRIVATE)

        // user_name
        helper.userName = "Aria"
        assertEquals("Aria", helper.userName)
        assertEquals("Aria", SecureStore.getSecure(context, "user_name"))
        assertFalse(regularPrefs.contains("user_name"))

        // last_period_start
        val today = LocalDate.of(2025, 7, 9)
        helper.lastPeriodStart = today
        assertEquals(today, helper.lastPeriodStart)
        assertEquals("2025-07-09", SecureStore.getSecure(context, "last_period_start"))
        assertFalse(regularPrefs.contains("last_period_start"))

        // cycle_length
        helper.cycleLength = 30
        assertEquals(30, helper.cycleLength)
        assertEquals("30", SecureStore.getSecure(context, "cycle_length"))
        assertFalse(regularPrefs.contains("cycle_length"))

        // period_duration
        helper.periodDuration = 6
        assertEquals(6, helper.periodDuration)
        assertEquals("6", SecureStore.getSecure(context, "period_duration"))
        assertFalse(regularPrefs.contains("period_duration"))

        // biometric_enabled
        helper.biometricEnabled = true
        assertTrue(helper.biometricEnabled)
        assertEquals("true", SecureStore.getSecure(context, "biometric_enabled"))
        assertFalse(regularPrefs.contains("biometric_enabled"))

        // disguise_mode
        helper.disguiseMode = true
        assertTrue(helper.disguiseMode)
        assertEquals("true", SecureStore.getSecure(context, "disguise_mode"))
        assertFalse(regularPrefs.contains("disguise_mode"))
    }

    @Test
    fun testNonSensitiveKeys_keptInRegularPrefs() {
        val helper = StorageHelper(context)
        val regularPrefs = context.getSharedPreferences("VelvetCyclePrefs", Context.MODE_PRIVATE)

        // dark_mode
        helper.isDarkMode = true
        assertTrue(helper.isDarkMode)
        assertTrue(regularPrefs.getBoolean("dark_mode", false))
        assertNull(SecureStore.getSecure(context, "dark_mode"))

        // app_language
        helper.appLanguage = "es"
        assertEquals("es", helper.appLanguage)
        assertEquals("es", regularPrefs.getString("app_language", null))
        assertNull(SecureStore.getSecure(context, "app_language"))

        // pad_store
        helper.padStore = "12 pads remaining"
        assertEquals("12 pads remaining", helper.padStore)
        assertEquals("12 pads remaining", regularPrefs.getString("pad_store", null))
        assertNull(SecureStore.getSecure(context, "pad_store"))

        // permissions_asked
        helper.permissionsAsked = true
        assertTrue(helper.permissionsAsked)
        assertTrue(regularPrefs.getBoolean("permissions_asked", false))
        assertNull(SecureStore.getSecure(context, "permissions_asked"))

        // notification_settings (reminder toggles)
        helper.periodReminderEnabled = false
        assertFalse(helper.periodReminderEnabled)
        assertFalse(regularPrefs.getBoolean("notif_period_reminder", true))

        // notification_test_sent
        helper.notificationTestSent = true
        assertTrue(helper.notificationTestSent)
        assertTrue(regularPrefs.getBoolean("notification_test_sent", false))
        assertNull(SecureStore.getSecure(context, "notification_test_sent"))
    }

    @Test
    fun testIndividualDailyLogs_storedIndividuallyInSecureStore() {
        val helper = StorageHelper(context)

        val log1 = LogData(mood = "Happy", flow = "Medium", symptoms = listOf("Cramps", "Headache"), notes = "Day 1")
        val log2 = LogData(mood = "Calm", flow = "Light", symptoms = listOf("Fatigue"), notes = "Day 2")

        // Store individual logs: log_2025-07-09 and log_2025-07-10
        helper.saveLog("2025-07-09", log1)
        helper.saveLog("2025-07-10", log2)

        // Verify individual keys in SecureStore
        val rawLog1 = SecureStore.getSecure(context, "log_2025-07-09")
        val rawLog2 = SecureStore.getSecure(context, "log_2025-07-10")
        assertNotNull(rawLog1)
        assertNotNull(rawLog2)

        // Verify 2KB limit compliance
        assertTrue(rawLog1!!.toByteArray(Charsets.UTF_8).size < 2048)
        assertTrue(rawLog2!!.toByteArray(Charsets.UTF_8).size < 2048)

        // Verify retrieved logs match
        val retrieved1 = helper.getLog("2025-07-09")
        assertNotNull(retrieved1)
        assertEquals("Happy", retrieved1?.mood)
        assertEquals("Medium", retrieved1?.flow)
        assertEquals(listOf("Cramps", "Headache"), retrieved1?.symptoms)
        assertEquals("Day 1", retrieved1?.notes)

        val retrieved2 = helper.getLog("2025-07-10")
        assertNotNull(retrieved2)
        assertEquals("Calm", retrieved2?.mood)
        assertEquals("Light", retrieved2?.flow)

        // Verify getAllLogs returns both entries
        val allLogs = helper.getAllLogs()
        assertEquals(2, allLogs.size)
        assertTrue(allLogs.containsKey("2025-07-09"))
        assertTrue(allLogs.containsKey("2025-07-10"))

        // Delete single log
        helper.deleteLog("2025-07-09")
        assertNull(helper.getLog("2025-07-09"))
        assertNotNull(helper.getLog("2025-07-10"))
    }

    @Test
    fun testMigrationFromRegularPrefsToSecureStore() {
        // Place unencrypted legacy data in VelvetCyclePrefs directly
        val regularPrefs = context.getSharedPreferences("VelvetCyclePrefs", Context.MODE_PRIVATE)
        regularPrefs.edit()
            .putString("user_pin", "9999")
            .putString("user_name", "LegacyUser")
            .putString("cycle_length", "31")
            .putString("log_2025-01-01", """{"mood":"Tired","flow":"Heavy","symptoms":[],"notes":"Legacy Log"}""")
            .commit()

        // Create new StorageHelper -> triggers migrateSensitiveDataIfNeeded()
        val helper = StorageHelper(context)

        // Verify data was migrated to SecureStore
        assertEquals("9999", helper.userPin)
        assertEquals("LegacyUser", helper.userName)
        assertEquals(31, helper.cycleLength)
        val migratedLog = helper.getLog("2025-01-01")
        assertNotNull(migratedLog)
        assertEquals("Tired", migratedLog?.mood)

        // Verify sensitive keys were removed from regularPrefs
        assertFalse(regularPrefs.contains("user_pin"))
        assertFalse(regularPrefs.contains("user_name"))
        assertFalse(regularPrefs.contains("cycle_length"))
        assertFalse(regularPrefs.contains("log_2025-01-01"))
    }
}
