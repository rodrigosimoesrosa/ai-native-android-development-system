package com.mirabilis.data.auth.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/** Proto DataStore serializer for the session, encrypting via [CryptoManager] (ADR-0005/0006). */
class SessionSerializer @Inject constructor(
    private val crypto: CryptoManager,
) : Serializer<SessionProto> {

    override val defaultValue: SessionProto = SessionProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): SessionProto {
        val encrypted = input.readBytes()
        if (encrypted.isEmpty()) return defaultValue
        return try {
            SessionProto.parseFrom(crypto.decrypt(encrypted))
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read SessionProto", e)
        }
    }

    override suspend fun writeTo(t: SessionProto, output: OutputStream) {
        output.write(crypto.encrypt(t.toByteArray()))
    }
}

/** Proto DataStore serializer for the current user, encrypted at rest. */
class UserSerializer @Inject constructor(
    private val crypto: CryptoManager,
) : Serializer<UserProto> {

    override val defaultValue: UserProto = UserProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserProto {
        val encrypted = input.readBytes()
        if (encrypted.isEmpty()) return defaultValue
        return try {
            UserProto.parseFrom(crypto.decrypt(encrypted))
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read UserProto", e)
        }
    }

    override suspend fun writeTo(t: UserProto, output: OutputStream) {
        output.write(crypto.encrypt(t.toByteArray()))
    }
}
