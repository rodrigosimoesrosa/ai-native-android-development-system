package com.mirabilis.domain.auth.usecase

import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.AuthSession
import com.mirabilis.domain.auth.repository.IAuthRepository
import javax.inject.Inject

/** FR-003: verify the code for the pending challenge; on success the repository persists the session. */
class VerifyOtpUseCase @Inject constructor(
    private val repository: IAuthRepository,
) {
    suspend operator fun invoke(verificationToken: String, code: String): Result<AuthSession> =
        repository.verifyOtp(verificationToken, code)
}
