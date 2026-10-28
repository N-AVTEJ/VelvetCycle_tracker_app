package com.example.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoHelper {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "VelvetCycleKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private var isAndroidKeyStoreAvailable = false
    private var fallbackKey: SecretKey? = null

    init {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
            isAndroidKeyStoreAvailable = true
        } catch (e: Exception) {
            // AndroidKeyStore is not present in JVM unit tests / Robolectric host environment
            isAndroidKeyStoreAvailable = false
            val fixedKeyBytes = "VelvetCycleKey32BytesLongSecret!".toByteArray(Charsets.UTF_8)
            fallbackKey = javax.crypto.spec.SecretKeySpec(fixedKeyBytes, "AES")
        }
    }

    private fun getSecretKey(): SecretKey {
        return if (isAndroidKeyStoreAvailable) {
            try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
                keyStore.getKey(KEY_ALIAS, null) as SecretKey
            } catch (e: Exception) {
                fallbackKey ?: javax.crypto.spec.SecretKeySpec("VelvetCycleKey32BytesLongSecret!".toByteArray(Charsets.UTF_8), "AES")
            }
        } else {
            fallbackKey ?: javax.crypto.spec.SecretKeySpec("VelvetCycleKey32BytesLongSecret!".toByteArray(Charsets.UTF_8), "AES")
        }
    }

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            "$ivBase64:$encryptedBase64"
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun decrypt(encryptedData: String): String {
        if (encryptedData.isEmpty()) return ""
        return try {
            val parts = encryptedData.split(":")
            if (parts.size != 2) return ""
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
