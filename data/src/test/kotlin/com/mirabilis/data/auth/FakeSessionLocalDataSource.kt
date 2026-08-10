package com.mirabilis.data.auth

import com.mirabilis.core.result.Result
import com.mirabilis.data.auth.datastore.ISessionLocalDataSource
import com.mirabilis.data.auth.session.SessionHolder
import com.mirabilis.domain.auth.model.AuthSession
import com.mirabilis.domain.auth.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory ISessionLocalDataSource for tests; keeps a real [SessionHolder] in sync like the impl. */
class FakeSessionLocalDataSource(
    private val holder: SessionHolder = SessionHolder(),
) : ISessionLocalDataSource {

    private val sessionFlow = MutableStateFlow<AuthSession?>(null)
    private val userFlow = MutableStateFlow<User?>(null)

    override fun session(): Flow<AuthSession?> = sessionFlow
    override fun user(): Flow<User?> = userFlow

    override suspend fun save(session: AuthSession, user: User): Result<Unit> {
        sessionFlow.value = session
        userFlow.value = user
        holder.update(session.accessToken, session.refreshToken)
        return Result.Success(Unit)
    }

    override suspend fun updateTokens(session: AuthSession): Result<Unit> {
        sessionFlow.value = session
        holder.update(session.accessToken, session.refreshToken)
        return Result.Success(Unit)
    }

    override suspend fun clear(): Result<Unit> {
        sessionFlow.value = null
        userFlow.value = null
        holder.clear()
        return Result.Success(Unit)
    }

    override suspend fun prime(): Result<Unit> = Result.Success(Unit)
}
