package com.mirabilis.feature.auth

import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.AuthSession
import com.mirabilis.domain.auth.model.PhoneVerificationChallenge
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.repository.IAuthRepository
import com.mirabilis.domain.auth.repository.ISessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Hand-written test double for feature (ViewModel) tests — real use cases run on top of it. */
class FakeAuthRepository(
    var requestResult: Result<PhoneVerificationChallenge> =
        Result.Success(PhoneVerificationChallenge("+15551234567", "vt_1")),
    var verifyResult: Result<AuthSession> = Result.Success(AuthSession("a", "r", 900)),
    var currentUserResult: Result<User> = Result.Success(User("u_1", "+15551234567", "Ada")),
) : IAuthRepository {

    var requestCallCount: Int = 0
    var verifyCallCount: Int = 0

    override suspend fun requestOtp(phone: String): Result<PhoneVerificationChallenge> {
        requestCallCount++
        return requestResult
    }

    override suspend fun verifyOtp(verificationToken: String, code: String): Result<AuthSession> {
        verifyCallCount++
        return verifyResult
    }

    override suspend fun refresh(refreshToken: String): Result<AuthSession> = verifyResult

    override suspend fun currentUser(): Result<User> = currentUserResult
}

class FakeSessionRepository(
    initialAuthenticated: Boolean = true,
    private val signOutResult: Result<Unit> = Result.Success(Unit),
) : ISessionRepository {

    val authState = MutableStateFlow(initialAuthenticated)
    var signOutCallCount: Int = 0

    override fun observeAuthState(): Flow<Boolean> = authState
    override fun observeUser(): Flow<User?> = MutableStateFlow(null)

    override suspend fun signOut(): Result<Unit> {
        signOutCallCount++
        when (signOutResult) {
            is Result.Success -> authState.value = false
            is Result.Error -> Unit // keep auth state — user is still signed in
        }
        return signOutResult
    }

    override suspend fun clearSession(): Result<Unit> {
        authState.value = false
        return Result.Success(Unit)
    }
}
