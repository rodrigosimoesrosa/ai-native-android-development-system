package com.mirabilis.domain.profile.usecase

import com.mirabilis.domain.profile.model.UserPreferences
import com.mirabilis.domain.profile.repository.IPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** US3/FR-004: observe the current preferences, re-emitting on every change. */
class ObservePreferencesUseCase @Inject constructor(
    private val repository: IPreferencesRepository,
) {
    operator fun invoke(): Flow<UserPreferences> = repository.observe()
}
