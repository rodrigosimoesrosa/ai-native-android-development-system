package com.mirabilis.domain.auth

import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.AuthSession
import com.mirabilis.domain.auth.model.PhoneVerificationChallenge
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.repository.IAuthRepository

/** Hand-written test double (ADR-0003: domain is testable with plain fakes, no framework). */
class FakeAuthRepository(
    var requestResult: Result<PhoneVerificationChallenge> =
        Result.Success(PhoneVerificationChallenge("+15551234567", "vt_1")),
    var verifyResult: Result<AuthSession> = Result.Success(AuthSession("a", "r", 900)),
    var refreshResult: Result<AuthSession> = Result.Success(AuthSession("a2", "r2", 900)),
    var currentUserResult: Result<User> = Result.Success(User("u_1", "+15551234567", "Ada")),
) : IAuthRepository {

    var requestedPhone: String? = null
    var verifyArgs: Pair<String, String>? = null
    var requestCallCount: Int = 0

    override suspend fun requestOtp(phone: String): Result<PhoneVerificationChallenge> {
        requestedPhone = phone
        requestCallCount++
        return requestResult
    }

    override suspend fun verifyOtp(verificationToken: String, code: String): Result<AuthSession> {
        verifyArgs = verificationToken to code
        return verifyResult
    }

    override suspend fun refresh(refreshToken: String): Result<AuthSession> = refreshResult

    override suspend fun currentUser(): Result<User> = currentUserResult
}
