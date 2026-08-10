package com.mirabilis.domain.auth.usecase

import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.repository.ISessionRepository
import javax.inject.Inject

/** FR-011: sign out — clears the session and user; auth-state then flips to false. */
class SignOutUseCase @Inject constructor(
    private val repository: ISessionRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.signOut()
}
