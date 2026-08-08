package com.mirabilis.domain.auth.repository

import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.AuthSession
import com.mirabilis.domain.auth.model.PhoneVerificationChallenge
import com.mirabilis.domain.auth.model.User

/**
 * Auth operations against the backend. **Declared in `:domain`; implemented in `:data`** — this is
 * the DIP arrow that keeps the domain the pure center (ADR-0003). Every call returns a typed
 * [Result]; no raw exceptions cross the boundary.
 */
interface IAuthRepository {

    /** FR-001: request a one-time code for [phone] (already format-validated by the use case). */
    suspend fun requestOtp(phone: String): Result<PhoneVerificationChallenge>

    /** FR-003: verify [code] for the pending challenge; on success the impl persists the session. */
    suspend fun verifyOtp(verificationToken: String, code: String): Result<AuthSession>

    /** FR-008: exchange the refresh token for a fresh session (called by the token refresher). */
    suspend fun refresh(refreshToken: String): Result<AuthSession>

    /** FR-012: fetch the authenticated user's profile from a protected endpoint. */
    suspend fun currentUser(): Result<User>
}
