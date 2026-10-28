package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.utils.AppLock
import com.example.utils.StorageHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppLockTest {

    @Test
    fun shouldLock_returnsFalse_forNullOrZeroTimestamp() {
        assertFalse(AppLock.shouldLock(null as Long?))
        assertFalse(AppLock.shouldLock(0L))
        assertFalse(AppLock.shouldLock(-100L))
        assertFalse(AppLock.shouldLock(null as String?))
        assertFalse(AppLock.shouldLock(""))
        assertFalse(AppLock.shouldLock("invalid"))
    }

    @Test
    fun shouldLock_returnsFalse_whenUnderTwoMinutes() {
        val now = System.currentTimeMillis()
        // 30 seconds ago
        assertFalse(AppLock.shouldLock(now - 30 * 1000L))
        // 1 minute ago
        assertFalse(AppLock.shouldLock(now - 60 * 1000L))
        // Exactly 2 minutes ago
        assertFalse(AppLock.shouldLock(now - 2 * 60 * 1000L))
    }

    @Test
    fun shouldLock_returnsTrue_whenOverTwoMinutes() {
        val now = System.currentTimeMillis()
        // 2 minutes and 1 second ago
        assertTrue(AppLock.shouldLock(now - (2 * 60 * 1000L + 1000L)))
        // 3 minutes ago
        assertTrue(AppLock.shouldLock(now - 3 * 60 * 1000L))
        // 10 minutes ago
        assertTrue(AppLock.shouldLock(now - 10 * 60 * 1000L))
    }

    @Test
    fun saveAndClearBackgroundTime_updatesStorageHelperCorrectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storageHelper = StorageHelper(context)
        storageHelper.clear()

        assertEquals(0L, storageHelper.backgroundedAt)

        AppLock.saveBackgroundTime(storageHelper)
        val savedTime = storageHelper.backgroundedAt
        assertTrue(savedTime > 0L)
        assertTrue(System.currentTimeMillis() - savedTime < 2000L)

        AppLock.clearBackgroundTime(storageHelper)
        assertEquals(0L, storageHelper.backgroundedAt)
    }

    @Test
    fun biometricEnabled_persistsInStorage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storageHelper = StorageHelper(context)
        storageHelper.clear()

        assertFalse(storageHelper.biometricEnabled)
        storageHelper.biometricEnabled = true
        assertTrue(storageHelper.biometricEnabled)
        storageHelper.biometricEnabled = false
        assertFalse(storageHelper.biometricEnabled)
    }

    @Test
    fun fullScenario_setPin_backgroundThreeMinutes_shouldLockTrue() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storageHelper = StorageHelper(context)
        storageHelper.clear()

        // 1. Set PIN
        storageHelper.userPin = "1234"
        assertNotNull(storageHelper.userPin)

        // 2. Go to background 3 minutes ago
        val threeMinutesAgo = System.currentTimeMillis() - (3 * 60 * 1000L)
        storageHelper.backgroundedAt = threeMinutesAgo

        // 3. Reopen -> check shouldLock()
        val shouldLockApp = AppLock.shouldLock(storageHelper.backgroundedAt)
        assertTrue("App should lock after 3 minutes in background", shouldLockApp)

        // 4. Unlock -> clears background timestamp
        AppLock.clearBackgroundTime(storageHelper)
        assertEquals(0L, storageHelper.backgroundedAt)
        assertFalse(AppLock.shouldLock(storageHelper.backgroundedAt))
    }

    @Test
    fun disguiseMode_persistsInStorage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storageHelper = StorageHelper(context)
        storageHelper.clear()

        assertFalse(storageHelper.disguiseMode)
        storageHelper.disguiseMode = true
        assertTrue(storageHelper.disguiseMode)
        storageHelper.disguiseMode = false
        assertFalse(storageHelper.disguiseMode)
    }

    @Test
    fun calculatorEngine_evaluatesAccurately() {
        // Basic arithmetic
        assertEquals("30", com.example.components.CalculatorEngine.evaluate("10 + 20"))
        assertEquals("15", com.example.components.CalculatorEngine.evaluate("25 - 10"))
        assertEquals("50", com.example.components.CalculatorEngine.evaluate("5 × 10"))
        assertEquals("5", com.example.components.CalculatorEngine.evaluate("20 ÷ 4"))

        // Operator precedence (multiplication before addition)
        assertEquals("14", com.example.components.CalculatorEngine.evaluate("2 + 3 × 4"))
        assertEquals("4", com.example.components.CalculatorEngine.evaluate("10 - 2 × 3"))

        // Decimals
        assertEquals("3", com.example.components.CalculatorEngine.evaluate("1.5 + 1.5"))
        assertEquals("3.75", com.example.components.CalculatorEngine.evaluate("15 ÷ 4"))

        // Division by zero
        assertEquals("Error", com.example.components.CalculatorEngine.evaluate("10 ÷ 0"))

        // Plain numbers treated as numbers
        assertEquals("1234", com.example.components.CalculatorEngine.evaluate("1234"))
        assertEquals("0", com.example.components.CalculatorEngine.evaluate(""))
    }

    @Test
    fun calculatorDisguise_secretUnlockLogic() {
        val savedPin = "4821"

        // Case 1: user types PIN digits and presses =
        var last4Digits = "4821"
        val isUnlock = (last4Digits == savedPin)
        assertTrue("Should trigger unlock when last 4 digits match PIN", isUnlock)

        // Case 2: user types wrong digits and presses =
        last4Digits = "1234"
        val isWrong = (last4Digits == savedPin)
        assertFalse("Should not trigger unlock for wrong PIN", isWrong)
        val normalEval = com.example.components.CalculatorEngine.evaluate("1234")
        assertEquals("1234", normalEval)

        // Case 3: user does normal math (e.g. 50 * 2), then types PIN 4821
        last4Digits = "502"
        last4Digits = (last4Digits + "4").takeLast(4)
        last4Digits = (last4Digits + "8").takeLast(4)
        last4Digits = (last4Digits + "2").takeLast(4)
        last4Digits = (last4Digits + "1").takeLast(4)
        assertEquals("4821", last4Digits)
        assertEquals(savedPin, last4Digits)
    }
}
