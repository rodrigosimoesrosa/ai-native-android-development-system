package com.mirabilis.domain.auth.model

/**
 * A pending request to verify a specific phone number. **Strictly transient** — never persisted
 * (FR-013); it lives only in the VerifyPhone flow's in-memory state.
 *
 * @param verificationToken opaque handle returned by the OTP request step.
 * @param expiresAtEpochMs optional client hint; the backend is authoritative on expiry.
 */
data class PhoneVerificationChallenge(
    val phone: String,
    val verificationToken: String,
    val expiresAtEpochMs: Long? = null,
)
