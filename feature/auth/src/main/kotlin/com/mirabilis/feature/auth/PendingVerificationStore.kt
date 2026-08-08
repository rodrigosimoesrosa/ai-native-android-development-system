package com.mirabilis.feature.auth

import com.mirabilis.domain.auth.model.PhoneVerificationChallenge
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the active phone-verification challenge **in memory only** while the sign-in flow is open
 * (FR-013). Shared between SendPhone (writes) and VerifyPhone (reads). Never persisted, so a process
 * death loses it — VerifyPhone then routes back to SendPhone (the required edge-case behavior).
 */
@Singleton
class PendingVerificationStore @Inject constructor() {

    @Volatile
    var challenge: PhoneVerificationChallenge? = null

    fun clear() {
        challenge = null
    }
}
