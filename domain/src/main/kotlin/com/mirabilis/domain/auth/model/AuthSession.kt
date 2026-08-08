package com.mirabilis.domain.auth.model

/**
 * The signed-in state: a short-lived access credential plus a longer-lived renewal credential
 * (ADR-0006 concretizes these as JWT access/refresh tokens). Persisted securely (FR-006/FR-007),
 * renewable (FR-008), clearable (FR-010/FR-011).
 */
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
) {
    val isValid: Boolean get() = accessToken.isNotBlank() && refreshToken.isNotBlank()
}
