package com.example.utils

/**
 * AppLock handles the 2-minute auto-lock policy when VelvetCycle is backgrounded.
 *
 * Implements:
 * - shouldLock(backgroundedAt): returns true if elapsed time since backgrounding exceeds 2 minutes (120,000 ms)
 * - saveBackgroundTime(storageHelper): records the current timestamp
 * - clearBackgroundTime(storageHelper): clears the background timestamp upon unlock
 */
object AppLock {
    const val TIMEOUT_MS: Long = 2 * 60 * 1000L // 2 minutes

    fun shouldLock(backgroundedAt: Long?): Boolean {
        if (backgroundedAt == null || backgroundedAt <= 0L) return false
        val diff = System.currentTimeMillis() - backgroundedAt
        return diff > TIMEOUT_MS
    }

    fun shouldLock(backgroundedAtStr: String?): Boolean {
        if (backgroundedAtStr.isNullOrEmpty()) return false
        val time = backgroundedAtStr.toLongOrNull() ?: return false
        return shouldLock(time)
    }

    fun saveBackgroundTime(storageHelper: StorageHelper) {
        storageHelper.backgroundedAt = System.currentTimeMillis()
    }

    fun clearBackgroundTime(storageHelper: StorageHelper) {
        storageHelper.clearBackgroundTime()
    }
}
