package com.mirabilis.data.auth.repository

import com.mirabilis.core.result.Result
import com.mirabilis.data.auth.datastore.ISessionLocalDataSource
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.repository.ISessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Local session state (ADR-0005/0006). Auth-state is derived from a validly stored session. */
class SessionRepository @Inject constructor(
    private val local: ISessionLocalDataSource,
) : ISessionRepository {

    override fun observeAuthState(): Flow<Boolean> =
        local.session().map { it?.isValid == true }

    override fun observeUser(): Flow<User?> = local.user()

    override suspend fun signOut(): Result<Unit> = local.clear()

    override suspend fun clearSession(): Result<Unit> = local.clear()
}
