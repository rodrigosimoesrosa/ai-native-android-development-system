package com.mirabilis.domain.auth

import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.repository.ISessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Test double for session state; `signOut`/`clearSession` flip the observable auth-state to false. */
class FakeSessionRepository(
    initialAuthenticated: Boolean = true,
) : ISessionRepository {

    val authState = MutableStateFlow(initialAuthenticated)
    private val userFlow = MutableStateFlow<User?>(null)

    var signOutCallCount: Int = 0

    override fun observeAuthState(): Flow<Boolean> = authState
    override fun observeUser(): Flow<User?> = userFlow

    override suspend fun signOut(): Result<Unit> {
        signOutCallCount++
        authState.value = false
        return Result.Success(Unit)
    }

    override suspend fun clearSession(): Result<Unit> {
        authState.value = false
        return Result.Success(Unit)
    }
}
