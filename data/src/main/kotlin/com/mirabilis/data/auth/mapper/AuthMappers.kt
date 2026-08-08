package com.mirabilis.data.auth.mapper

import com.mirabilis.data.auth.datastore.SessionProto
import com.mirabilis.data.auth.datastore.UserProto
import com.mirabilis.data.auth.network.OtpRequestResponseRemote
import com.mirabilis.data.auth.network.RefreshResponseRemote
import com.mirabilis.data.auth.network.UserRemote
import com.mirabilis.data.auth.network.VerifyResponseRemote
import com.mirabilis.domain.auth.model.AuthSession
import com.mirabilis.domain.auth.model.PhoneVerificationChallenge
import com.mirabilis.domain.auth.model.User

// Boundary mappers (ADR-0003). Colocated in :data, never domain-visible. Read path: Remote→domain;
// persistence path: domain↔Proto (single hop, ADR-0005).

fun OtpRequestResponseRemote.toDomain(phone: String): PhoneVerificationChallenge =
    PhoneVerificationChallenge(phone = phone, verificationToken = verificationToken)

fun VerifyResponseRemote.toDomain(): AuthSession =
    AuthSession(accessToken = accessToken, refreshToken = refreshToken, expiresInSeconds = expiresIn)

fun RefreshResponseRemote.toDomain(): AuthSession =
    AuthSession(accessToken = accessToken, refreshToken = refreshToken, expiresInSeconds = expiresIn)

fun UserRemote.toDomain(): User = User(id = id, phone = phone, displayName = displayName)

fun AuthSession.toProto(): SessionProto =
    SessionProto.newBuilder()
        .setAccessToken(accessToken)
        .setRefreshToken(refreshToken)
        .setExpiresInSeconds(expiresInSeconds)
        .build()

fun SessionProto.toDomain(): AuthSession? =
    if (accessToken.isNullOrBlank()) null
    else AuthSession(accessToken = accessToken, refreshToken = refreshToken, expiresInSeconds = expiresInSeconds)

fun User.toProto(): UserProto =
    UserProto.newBuilder()
        .setId(id)
        .setPhone(phone)
        .setDisplayName(displayName.orEmpty())
        .build()

fun UserProto.toDomain(): User? =
    if (id.isNullOrBlank()) null
    else User(id = id, phone = phone, displayName = displayName.ifBlank { null })
