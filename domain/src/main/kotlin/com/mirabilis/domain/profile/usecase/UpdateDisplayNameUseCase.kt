package com.mirabilis.domain.profile.usecase

import com.mirabilis.core.result.AppError
import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.profile.repository.IProfileRepository
import javax.inject.Inject

/**
 * US2/FR-002,003: update the display name. Rejects a blank value **before** any persistence (nothing
 * is written), otherwise delegates to [IProfileRepository]. The trimmed value is what gets saved.
 */
class UpdateDisplayNameUseCase @Inject constructor(
    private val repository: IProfileRepository,
) {
    suspend operator fun invoke(displayName: String): Result<User> {
        val trimmed = displayName.trim()
        if (trimmed.isBlank()) {
            return Result.Error(AppError.Validation("Display name can't be empty."))
        }
        return repository.updateDisplayName(trimmed)
    }
}
