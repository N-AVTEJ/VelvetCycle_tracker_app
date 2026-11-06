package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.utils.PinAuth
import com.example.utils.SecureStore
import com.example.utils.StorageHelper
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PinAuthTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        val storageHelper = StorageHelper(context)
        storageHelper.clear()
        PinAuth.resetAttempts(context)
    }

    @Test
    fun initialStatus_isNotLockedOut() {
        val status = PinAuth.getLockoutStatus(context)
        assertFalse(status.isLocked)
        assertEquals(0L, status.remainingMs)
        assertEquals(0, status.attempts)
        assertFalse(PinAuth.isLockedOut(context))
    }

    @Test
    fun tier1_threeAttempts_locks30Seconds() {
        // Attempt 1
        var status = PinAuth.recordWrongAttempt(context)
        assertEquals(1, status.attempts)
        assertFalse("1 wrong attempt should not lock", status.isLocked)

        // Attempt 2
        status = PinAuth.recordWrongAttempt(context)
        assertEquals(2, status.attempts)
        assertFalse("2 wrong attempts should not lock", status.isLocked)

        // Attempt 3 -> 30s lockout
        status = PinAuth.recordWrongAttempt(context)
        assertEquals(3, status.attempts)
        assertTrue("3 wrong attempts must trigger lockout", status.isLocked)
        assertTrue(PinAuth.isLockedOut(context))
        assertTrue("Remaining time should be ~30 seconds", status.remainingMs in 25000L..30000L)
        assertEquals(30 * 1000L, PinAuth.getLockoutTotalDuration(context))

        // Check SecureStore keys directly
        assertEquals("3", SecureStore.getSecure(context, PinAuth.KEY_PIN_ATTEMPTS))
        assertNotNull(SecureStore.getSecure(context, PinAuth.KEY_PIN_LOCKOUT_UNTIL))
    }

    @Test
    fun tier2_fiveAttempts_locks5Minutes() {
        // Simulate 4 attempts
        repeat(4) { PinAuth.recordWrongAttempt(context) }
        val status4 = PinAuth.getLockoutStatus(context)
        assertEquals(4, status4.attempts)

        // 5th attempt -> 5 minutes lockout (300,000 ms)
        val status5 = PinAuth.recordWrongAttempt(context)
        assertEquals(5, status5.attempts)
        assertTrue(status5.isLocked)
        assertTrue("Remaining time should be ~5 minutes", status5.remainingMs in 290000L..300000L)
        assertEquals(5 * 60 * 1000L, PinAuth.getLockoutTotalDuration(context))
    }

    @Test
    fun tier3_sevenAttempts_locks30Minutes() {
        repeat(6) { PinAuth.recordWrongAttempt(context) }
        val status7 = PinAuth.recordWrongAttempt(context)
        assertEquals(7, status7.attempts)
        assertTrue(status7.isLocked)
        assertTrue("Remaining time should be ~30 minutes", status7.remainingMs in 1790000L..1800000L)
        assertEquals(30 * 60 * 1000L, PinAuth.getLockoutTotalDuration(context))
    }

    @Test
    fun tier4_tenAttempts_locks24Hours() {
        repeat(9) { PinAuth.recordWrongAttempt(context) }
        val status10 = PinAuth.recordWrongAttempt(context)
        assertEquals(10, status10.attempts)
        assertTrue(status10.isLocked)
        assertTrue("Remaining time should be ~24 hours", status10.remainingMs in 86300000L..86400000L)
        assertEquals(24 * 60 * 60 * 1000L, PinAuth.getLockoutTotalDuration(context))
    }

    @Test
    fun resetAttempts_clearsLockoutAndCounter() {
        repeat(5) { PinAuth.recordWrongAttempt(context) }
        assertTrue(PinAuth.isLockedOut(context))

        PinAuth.resetAttempts(context)

        val status = PinAuth.getLockoutStatus(context)
        assertFalse(status.isLocked)
        assertEquals(0L, status.remainingMs)
        assertEquals(0, status.attempts)
        assertFalse(PinAuth.isLockedOut(context))
    }

    @Test
    fun lockoutPersistsAcrossAppRestarts() {
        // 1. Trigger 3 wrong attempts in session 1
        PinAuth.recordWrongAttempt(context)
        PinAuth.recordWrongAttempt(context)
        val status = PinAuth.recordWrongAttempt(context)
        assertTrue(status.isLocked)
        val remainingBeforeKill = status.remainingMs
        assertTrue(remainingBeforeKill > 0L)

        // 2. Simulate app kill & reopen (new query from persistent SecureStore)
        val freshStatus = PinAuth.getLockoutStatus(context)
        assertTrue("Timer must still be running after app restart", freshStatus.isLocked)
        assertEquals(3, freshStatus.attempts)
        assertTrue("Remaining time must be positive and close to original", freshStatus.remainingMs in 1000L..remainingBeforeKill)
    }
}
