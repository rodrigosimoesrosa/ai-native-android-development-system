package com.mirabilis.data.profile.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.mirabilis.data.auth.datastore.CryptoManager
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * Proto DataStore serializer for preferences, encrypting via [CryptoManager] (ADR-0005/0006) —
 * reuses the same Keystore-backed AES/GCM path as the session/user stores.
 */
class PreferencesSerializer @Inject constructor(
    private val crypto: CryptoManager,
) : Serializer<PreferencesProto> {

    override val defaultValue: PreferencesProto = PreferencesProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): PreferencesProto {
        val encrypted = input.readBytes()
        if (encrypted.isEmpty()) return defaultValue
        return try {
            PreferencesProto.parseFrom(crypto.decrypt(encrypted))
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read PreferencesProto", e)
        }
    }

    override suspend fun writeTo(t: PreferencesProto, output: OutputStream) {
        output.write(crypto.encrypt(t.toByteArray()))
    }
}
