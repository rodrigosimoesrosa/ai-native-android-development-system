package com.mirabilis.domain.auth.usecase

import com.mirabilis.domain.auth.repository.ISessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** FR-006/FR-012: observe whether a valid session is stored — drives top-level auth routing. */
class ObserveAuthStateUseCase @Inject constructor(
    private val repository: ISessionRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeAuthState()
}
