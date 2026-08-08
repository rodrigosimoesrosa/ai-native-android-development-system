package com.mirabilis.domain.auth.usecase

import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.repository.IAuthRepository
import javax.inject.Inject

/** FR-012: fetch the authenticated user's profile from the protected endpoint (exercises refresh). */
class GetCurrentUserUseCase @Inject constructor(
    private val repository: IAuthRepository,
) {
    suspend operator fun invoke(): Result<User> = repository.currentUser()
}
