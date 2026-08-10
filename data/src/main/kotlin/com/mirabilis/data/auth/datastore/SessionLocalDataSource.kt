package com.mirabilis.data.auth.datastore

import androidx.datastore.core.DataStore
import com.mirabilis.core.dispatcher.Dispatcher
import com.mirabilis.core.dispatcher.DispatcherType
import com.mirabilis.core.result.Result
import com.mirabilis.data.auth.mapper.toDomain
import com.mirabilis.data.auth.mapper.toProto
import com.mirabilis.data.auth.session.SessionHolder
import com.mirabilis.domain.auth.model.AuthSession
import com.mirabilis.domain.auth.model.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Local, encrypted session/user store (ADR-0005/0006). Keeps [SessionHolder] in sync on writes. */
interface ISessionLocalDataSource {
    fun session(): Flow<AuthSession?>
    fun user(): Flow<User?>
    suspend fun save(session: AuthSession, user: User): Result<Unit>
    suspend fun updateTokens(session: AuthSession): Result<Unit>
    suspend fun clear(): Result<Unit>

    /** Load the persisted tokens into the in-memory cache at startup (ADR-0006 §4). */
    suspend fun prime(): Result<Unit>
}

class SessionLocalDataSource @Inject constructor(
    private val sessionStore: DataStore<SessionProto>,
    private val userStore: DataStore<UserProto>,
    private val sessionHolder: SessionHolder,
    @Dispatcher(DispatcherType.IO) io: CoroutineDispatcher,
) : SafeLocalDataSource(io), ISessionLocalDataSource {

    override fun session(): Flow<AuthSession?> = sessionStore.data.map { it.toDomain() }

    override fun user(): Flow<User?> = userStore.data.map { it.toDomain() }

    override suspend fun save(session: AuthSession, user: User): Result<Unit> = safeCall {
        sessionStore.updateData { session.toProto() }
        userStore.updateData { user.toProto() }
        sessionHolder.update(session.accessToken, session.refreshToken)
    }

    override suspend fun updateTokens(session: AuthSession): Result<Unit> = safeCall {
        sessionStore.updateData { session.toProto() }
        sessionHolder.update(session.accessToken, session.refreshToken)
    }

    override suspend fun clear(): Result<Unit> = safeCall {
        sessionStore.updateData { SessionProto.getDefaultInstance() }
        userStore.updateData { UserProto.getDefaultInstance() }
        sessionHolder.clear()
    }

    override suspend fun prime(): Result<Unit> = safeCall {
        val session = sessionStore.data.first().toDomain()
        sessionHolder.update(session?.accessToken, session?.refreshToken)
    }
}
