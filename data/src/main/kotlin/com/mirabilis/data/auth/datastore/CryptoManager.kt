package com.mirabilis.data.auth.datastore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AES/GCM encryption backed by a non-exportable Android Keystore key (ADR-0006 §5, FR-007).
 * Session/user bytes are never written to disk in plaintext. The 12-byte IV is prepended to the
 * ciphertext on write and split off on read.
 */
@Singleton
class CryptoManager @Inject constructor() {

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun secretKey(): SecretKey {
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
        }.generateKey()
    }

    fun encrypt(bytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey()) }
        return cipher.iv + cipher.doFinal(bytes)
    }

    fun decrypt(bytes: ByteArray): ByteArray {
        if (bytes.size <= IV_SIZE) return ByteArray(0)
        val iv = bytes.copyOfRange(0, IV_SIZE)
        val payload = bytes.copyOfRange(IV_SIZE, bytes.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
            .apply { init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, iv)) }
        return cipher.doFinal(payload)
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "mirabilis_session_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE = 12
        const val TAG_BITS = 128
    }
}
