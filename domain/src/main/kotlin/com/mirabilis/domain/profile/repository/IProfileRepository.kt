package com.mirabilis.domain.profile.repository

import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.User

/**
 * Profile mutations against the backend. Declared in `:domain`, implemented in `:data` (DIP,
 * ADR-0003). Updating the display name persists the returned [User] locally (contracts/profile-api).
 */
interface IProfileRepository {

    /** US2/FR-002,003: update the display name (pre-validated non-blank) → persisted [User]. */
    suspend fun updateDisplayName(displayName: String): Result<User>
}
