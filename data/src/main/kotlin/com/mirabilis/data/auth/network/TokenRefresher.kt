package com.mirabilis.data.auth.network

import com.mirabilis.core.dispatcher.Dispatcher
import com.mirabilis.core.dispatcher.DispatcherType
import com.mirabilis.core.result.Result
import com.mirabilis.core.result.map
import com.mirabilis.data.auth.mapper.toDomain
import com.mirabilis.domain.auth.model.AuthSession
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/** Exchanges a refresh token for a fresh session over the anti-loop client. */
interface TokenRefresher {
    suspend fun refresh(refreshToken: String): Result<AuthSession>
}

class RetrofitTokenRefresher @Inject constructor(
    @RefreshClient private val api: AuthApi,
    @Dispatcher(DispatcherType.IO) io: CoroutineDispatcher,
) : SafeRemoteDataSource(io), TokenRefresher {

    override suspend fun refresh(refreshToken: String): Result<AuthSession> =
        safeCall { api.refresh(RefreshRequest(refreshToken)) }.map { it.toDomain() }
}
