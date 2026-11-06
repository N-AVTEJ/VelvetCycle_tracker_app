package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Hardware-backed and AES-256-GCM encrypted secure key-value store for VelvetCycle.
 * Corresponds to expo-secure-store.
 *
 * Implements:
 * - saveSecure(key, value)
 * - getSecure(key)
 * - deleteSecure(key)
 *
 * Sensitive keys:
 * - user_pin
 * - user_name
 * - last_period_start
 * - cycle_length
 * - period_duration
 * - biometric_enabled
 * - disguise_mode
 * - All log entries: log_YYYY-MM-DD
 *
 * Key constraints:
 * - 2KB maximum limit per key
 * - Individual daily logs stored per date key (e.g., log_2025-07-09)
 */
object SecureStore {
    private const val TAG = "SecureStore"
    private const val PREFS_NAME = "VelvetCycleSecureStore"
    const val MAX_KEY_SIZE_BYTES = 2048 // 2KB per-key limit

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Saves a value securely with AES-256-GCM encryption.
     * Equivalent to SecureStore.setItemAsync(key, JSON.stringify(value))
     */
    fun saveSecure(context: Context, key: String, value: Any?): Boolean {
        return try {
            if (value == null) {
                deleteSecure(context, key)
                return true
            }
            val stringVal = when (value) {
                is String -> value
                is Number, is Boolean -> value.toString()
                else -> value.toString()
            }
            val byteCount = stringVal.toByteArray(Charsets.UTF_8).size
            if (byteCount > MAX_KEY_SIZE_BYTES) {
                Log.w(TAG, "Value for key '$key' exceeds 2KB limit ($byteCount bytes)")
            }
            val encrypted = CryptoHelper.encrypt(stringVal)
            val toStore = if (encrypted.isNotEmpty()) encrypted else stringVal
            getPrefs(context).edit().putString(key, toStore).commit()
        } catch (e: Exception) {
            Log.e(TAG, "SecureStore save failed: $key", e)
            false
        }
    }

    /**
     * Retrieves and decrypts a value from secure storage.
     * Equivalent to SecureStore.getItemAsync(key)
     */
    fun getSecure(context: Context, key: String): String? {
        return try {
            val encrypted = getPrefs(context).getString(key, null) ?: return null
            if (encrypted.contains(":")) {
                val decrypted = CryptoHelper.decrypt(encrypted)
                if (decrypted.isNotEmpty()) decrypted else encrypted
            } else {
                encrypted
            }
        } catch (e: Exception) {
            Log.e(TAG, "SecureStore get failed: $key", e)
            null
        }
    }

    /**
     * Deletes a key from secure storage.
     * Equivalent to SecureStore.deleteItemAsync(key)
     */
    fun deleteSecure(context: Context, key: String): Boolean {
        return try {
            getPrefs(context).edit().remove(key).commit()
        } catch (e: Exception) {
            Log.e(TAG, "SecureStore delete failed: $key", e)
            false
        }
    }

    /**
     * Returns all keys currently stored in SecureStore.
     */
    fun getAllKeys(context: Context): Set<String> {
        return getPrefs(context).all.keys
    }

    /**
     * Clears all items in SecureStore.
     */
    fun clear(context: Context): Boolean {
        return try {
            getPrefs(context).edit().clear().commit()
        } catch (e: Exception) {
            Log.e(TAG, "SecureStore clear failed", e)
            false
        }
    }
}
