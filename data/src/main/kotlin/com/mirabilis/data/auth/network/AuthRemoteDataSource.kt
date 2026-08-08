package com.mirabilis.data.auth.network

import com.mirabilis.core.dispatcher.Dispatcher
import com.mirabilis.core.dispatcher.DispatcherType
import com.mirabilis.core.result.Result
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/** Remote auth IO, wrapped in typed [Result] by [SafeRemoteDataSource]. */
interface IAuthRemoteDataSource {
    suspend fun requestOtp(phone: String): Result<OtpRequestResponseRemote>
    suspend fun verifyOtp(verificationToken: String, code: String): Result<VerifyResponseRemote>
    suspend fun refresh(refreshToken: String): Result<RefreshResponseRemote>
    suspend fun me(): Result<MeResponseRemote>
}

class AuthRemoteDataSource @Inject constructor(
    private val api: AuthApi,
    @Dispatcher(DispatcherType.IO) io: CoroutineDispatcher,
) : SafeRemoteDataSource(io), IAuthRemoteDataSource {

    override suspend fun requestOtp(phone: String) =
        safeCall { api.requestOtp(OtpRequestRequest(phone)) }

    override suspend fun verifyOtp(verificationToken: String, code: String) =
        safeCall { api.verifyOtp(VerifyRequest(verificationToken, code)) }

    override suspend fun refresh(refreshToken: String) =
        safeCall { api.refresh(RefreshRequest(refreshToken)) }

    override suspend fun me() = safeCall { api.me() }
}
