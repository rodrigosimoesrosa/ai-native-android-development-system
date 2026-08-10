package com.mirabilis.domain.profile.usecase

import com.mirabilis.core.result.Result
import com.mirabilis.domain.profile.model.Theme
import com.mirabilis.domain.profile.repository.IPreferencesRepository
import javax.inject.Inject

/** US3/FR-004: persist the theme preference immediately. */
class SetThemeUseCase @Inject constructor(
    private val repository: IPreferencesRepository,
) {
    suspend operator fun invoke(theme: Theme): Result<Unit> = repository.setTheme(theme)
}
