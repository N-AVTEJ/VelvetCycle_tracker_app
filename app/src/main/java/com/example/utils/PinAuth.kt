package com.example.utils

import android.content.Context

/**
 * Result data class for lockout status query.
 */
data class LockoutStatus(
    val isLocked: Boolean,
    val remainingMs: Long,
    val attempts: Int
)

/**
 * Tiered brute force PIN authentication manager for VelvetCycle.
 * Corresponds to utils/pinAuth.js.
 *
 * Persists in SecureStore:
 * - 'pin_attempts': number of consecutive wrong PIN attempts
 * - 'pin_lockout_until': timestamp (epoch ms) until which PIN entry is locked out
 *
 * Lockout tiers:
 * - 3 wrong attempts  -> lock 30 seconds (30,000 ms)
 * - 5 wrong attempts  -> lock 5 minutes (300,000 ms)
 * - 7 wrong attempts  -> lock 30 minutes (1,800,000 ms)
 * - 10 wrong attempts -> lock 24 hours (86,400,000 ms) + data protection warning
 */
object PinAuth {
    const val KEY_PIN_ATTEMPTS = "pin_attempts"
    const val KEY_PIN_LOCKOUT_UNTIL = "pin_lockout_until"

    const val TIER_3_ATTEMPTS = 3
    const val TIER_3_DURATION_MS = 30 * 1000L // 30 seconds

    const val TIER_5_ATTEMPTS = 5
    const val TIER_5_DURATION_MS = 5 * 60 * 1000L // 5 minutes

    const val TIER_7_ATTEMPTS = 7
    const val TIER_7_DURATION_MS = 30 * 60 * 1000L // 30 minutes

    const val TIER_10_ATTEMPTS = 10
    const val TIER_10_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours

    /**
     * Records a wrong PIN attempt:
     * - Increments 'pin_attempts' counter in SecureStore
     * - Applies the appropriate lockout tier if threshold reached
     * Returns the updated LockoutStatus.
     */
    fun recordWrongAttempt(context: Context): LockoutStatus {
        val currentAttempts = getAttempts(context) + 1
        SecureStore.saveSecure(context, KEY_PIN_ATTEMPTS, currentAttempts)

        val lockoutDurationMs: Long = when {
            currentAttempts >= TIER_10_ATTEMPTS -> TIER_10_DURATION_MS
            currentAttempts >= TIER_7_ATTEMPTS -> TIER_7_DURATION_MS
            currentAttempts >= TIER_5_ATTEMPTS -> TIER_5_DURATION_MS
            currentAttempts >= TIER_3_ATTEMPTS -> TIER_3_DURATION_MS
            else -> 0L
        }

        if (lockoutDurationMs > 0L) {
            val lockoutUntil = System.currentTimeMillis() + lockoutDurationMs
            SecureStore.saveSecure(context, KEY_PIN_LOCKOUT_UNTIL, lockoutUntil)
            // Also store total duration so UI progress bar can compute fraction
            SecureStore.saveSecure(context, "lockout_duration", lockoutDurationMs)
        }

        return getLockoutStatus(context)
    }

    /**
     * Resets wrong attempts and lockout state upon entering correct PIN.
     */
    fun resetAttempts(context: Context) {
        SecureStore.saveSecure(context, KEY_PIN_ATTEMPTS, 0)
        SecureStore.saveSecure(context, KEY_PIN_LOCKOUT_UNTIL, 0L)
        SecureStore.saveSecure(context, "lockout_duration", 0L)
        // Also clear legacy keys if present
        SecureStore.saveSecure(context, "lockout_expiration_time", 0L)
    }

    /**
     * Queries current lockout status:
     * returns { isLocked, remainingMs, attempts }
     */
    fun getLockoutStatus(context: Context): LockoutStatus {
        val attempts = getAttempts(context)
        val remainingMs = getRemainingMs(context)
        val isLocked = remainingMs > 0L
        return LockoutStatus(
            isLocked = isLocked,
            remainingMs = remainingMs,
            attempts = attempts
        )
    }

    /**
     * Returns true if currently locked out, false otherwise.
     */
    fun isLockedOut(context: Context): Boolean {
        return getRemainingMs(context) > 0L
    }

    /**
     * Returns number of recorded wrong attempts from SecureStore.
     */
    fun getAttempts(context: Context): Int {
        return SecureStore.getSecure(context, KEY_PIN_ATTEMPTS)?.toIntOrNull() ?: 0
    }

    /**
     * Returns remaining lockout duration in milliseconds.
     */
    fun getRemainingMs(context: Context): Long {
        val until = SecureStore.getSecure(context, KEY_PIN_LOCKOUT_UNTIL)?.toLongOrNull()
            ?: SecureStore.getSecure(context, "lockout_expiration_time")?.toLongOrNull()
            ?: 0L
        if (until <= 0L) return 0L
        val diff = until - System.currentTimeMillis()
        return if (diff > 0L) diff else 0L
    }

    /**
     * Returns total duration in ms of the current active lockout tier.
     */
    fun getLockoutTotalDuration(context: Context): Long {
        val duration = SecureStore.getSecure(context, "lockout_duration")?.toLongOrNull()
        if (duration != null && duration > 0L) return duration

        // Fallback calculation based on current attempts
        val attempts = getAttempts(context)
        return when {
            attempts >= TIER_10_ATTEMPTS -> TIER_10_DURATION_MS
            attempts >= TIER_7_ATTEMPTS -> TIER_7_DURATION_MS
            attempts >= TIER_5_ATTEMPTS -> TIER_5_DURATION_MS
            attempts >= TIER_3_ATTEMPTS -> TIER_3_DURATION_MS
            else -> 0L
        }
    }
}
