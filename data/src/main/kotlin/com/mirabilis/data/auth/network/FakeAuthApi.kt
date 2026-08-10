package com.mirabilis.data.auth.network

import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory fake backing the running app until a real backend is swapped in by build config
 * (ADR-0006 §6). MockWebServer backs network tests separately. The fake accepts the code
 * [ACCEPTED_CODE]; other codes yield a `400 invalid_code` (retryable, FR-004).
 *
 * Backend-owned policy (code format, expiry, cooldown, lockout) is simulated minimally here — the
 * app only reflects responses (FR-014), it does not define the thresholds.
 */
@Singleton
class FakeAuthApi @Inject constructor() : AuthApi {

    // Mutable so a display-name update (PATCH /me) is reflected by subsequent GET /me (FR-002/003).
    private var currentUser = demoUser

    override suspend fun requestOtp(body: OtpRequestRequest): OtpRequestResponseRemote {
        delay(NETWORK_LATENCY_MS)
        return OtpRequestResponseRemote(verificationToken = "vt_${UUID.randomUUID()}")
    }

    override suspend fun verifyOtp(body: VerifyRequest): VerifyResponseRemote {
        delay(NETWORK_LATENCY_MS)
        if (body.code != ACCEPTED_CODE) throw httpError(400, """{"error":"invalid_code"}""")
        return VerifyResponseRemote(
            accessToken = "access_${UUID.randomUUID()}",
            refreshToken = "refresh_${UUID.randomUUID()}",
            expiresIn = ACCESS_TTL_SECONDS,
            user = demoUser,
        )
    }

    override suspend fun refresh(body: RefreshRequest): RefreshResponseRemote {
        delay(NETWORK_LATENCY_MS)
        if (body.refreshToken.isBlank()) throw httpError(401, """{"error":"invalid_refresh"}""")
        return RefreshResponseRemote(
            accessToken = "access_${UUID.randomUUID()}",
            refreshToken = "refresh_${UUID.randomUUID()}",
            expiresIn = ACCESS_TTL_SECONDS,
        )
    }

    override suspend fun me(): MeResponseRemote {
        delay(NETWORK_LATENCY_MS)
        return MeResponseRemote(user = currentUser)
    }

    override suspend fun updateProfile(body: UpdateProfileRequest): MeResponseRemote {
        delay(NETWORK_LATENCY_MS)
        if (body.displayName.isBlank()) throw httpError(400, """{"error":"invalid_display_name"}""")
        currentUser = currentUser.copy(displayName = body.displayName)
        return MeResponseRemote(user = currentUser)
    }

    private fun httpError(code: Int, json: String): HttpException =
        HttpException(Response.error<Any>(code, json.toResponseBody("application/json".toMediaType())))

    private companion object {
        const val ACCEPTED_CODE = "123456"
        const val ACCESS_TTL_SECONDS = 900L
        const val NETWORK_LATENCY_MS = 300L
        val demoUser = UserRemote(id = "u_1", phone = "+15551234567", displayName = "Ada Lovelace")
    }
}
