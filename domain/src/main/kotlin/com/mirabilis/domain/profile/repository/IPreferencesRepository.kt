package com.mirabilis.domain.profile.repository

import com.mirabilis.core.result.Result
import com.mirabilis.domain.profile.model.Theme
import com.mirabilis.domain.profile.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Local preferences store (US3/FR-004). Declared in `:domain`, implemented in `:data` over an
 * encrypted Proto DataStore (ADR-0005). Writes persist immediately and survive restarts.
 */
interface IPreferencesRepository {

    /** The current preferences, re-emitting on every change. */
    fun observe(): Flow<UserPreferences>

    /** Persist the theme preference. */
    suspend fun setTheme(theme: Theme): Result<Unit>

    /** Persist the notifications preference. */
    suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit>
}
