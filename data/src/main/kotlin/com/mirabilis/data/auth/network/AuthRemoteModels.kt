package com.mirabilis.data.auth.network

import kotlinx.serialization.Serializable

// Remote models match the API contract (contracts/auth-api.md). Confined to :data; mapped to
// domain by the repository — a feature never sees these (ADR-0003).

@Serializable
data class OtpRequestRequest(val phone: String)

@Serializable
data class OtpRequestResponseRemote(val verificationToken: String)

@Serializable
data class VerifyRequest(val verificationToken: String, val code: String)

@Serializable
data class VerifyResponseRemote(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserRemote,
)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class RefreshResponseRemote(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long = 0,
)

@Serializable
data class UpdateProfileRequest(val displayName: String)

@Serializable
data class MeResponseRemote(val user: UserRemote)

@Serializable
data class UserRemote(
    val id: String,
    val phone: String,
    val displayName: String? = null,
)
