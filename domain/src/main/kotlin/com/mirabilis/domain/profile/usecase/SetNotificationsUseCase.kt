package com.mirabilis.domain.profile.usecase

import com.mirabilis.core.result.Result
import com.mirabilis.domain.profile.repository.IPreferencesRepository
import javax.inject.Inject

/** US3/FR-004: persist the notifications preference immediately. */
class SetNotificationsUseCase @Inject constructor(
    private val repository: IPreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> = repository.setNotificationsEnabled(enabled)
}
