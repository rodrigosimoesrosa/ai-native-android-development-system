package com.mirabilis.data.auth.repository

import com.mirabilis.core.result.Result
import com.mirabilis.core.result.map
import com.mirabilis.data.auth.datastore.ISessionLocalDataSource
import com.mirabilis.data.auth.mapper.toDomain
import com.mirabilis.data.auth.network.IAuthRemoteDataSource
import com.mirabilis.domain.auth.model.AuthSession
import com.mirabilis.domain.auth.model.PhoneVerificationChallenge
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.repository.IAuthRepository
import javax.inject.Inject

/**
 * Coordinates remote auth IO + local persistence, mapping remote models to domain (ADR-0003).
 * On successful verify/refresh it persists the session (and user) via the local data source.
 */
class AuthRepository @Inject constructor(
    private val remote: IAuthRemoteDataSource,
    private val local: ISessionLocalDataSource,
) : IAuthRepository {

    override suspend fun requestOtp(phone: String): Result<PhoneVerificationChallenge> =
        remote.requestOtp(phone).map { it.toDomain(phone) }

    override suspend fun verifyOtp(verificationToken: String, code: String): Result<AuthSession> =
        when (val response = remote.verifyOtp(verificationToken, code)) {
            is Result.Success -> {
                val session = response.data.toDomain()
                val user = response.data.user.toDomain()
                when (val saved = local.save(session, user)) {
                    is Result.Success -> Result.Success(session)
                    is Result.Error -> saved
                }
            }
            is Result.Error -> response
        }

    override suspend fun refresh(refreshToken: String): Result<AuthSession> =
        when (val response = remote.refresh(refreshToken)) {
            is Result.Success -> {
                val session = response.data.toDomain()
                when (val saved = local.updateTokens(session)) {
                    is Result.Success -> Result.Success(session)
                    is Result.Error -> saved
                }
            }
            is Result.Error -> response
        }

    override suspend fun currentUser(): Result<User> =
        remote.me().map { it.user.toDomain() }
}
