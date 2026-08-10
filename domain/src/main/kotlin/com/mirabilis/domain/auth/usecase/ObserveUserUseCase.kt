package com.mirabilis.domain.auth.usecase

import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.repository.ISessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * FR-001: observe the current authenticated user from the local session (reused by Profile). Reactive
 * so a persisted display-name change (US2) is reflected on the next emission without a reload.
 */
class ObserveUserUseCase @Inject constructor(
    private val repository: ISessionRepository,
) {
    operator fun invoke(): Flow<User?> = repository.observeUser()
}
