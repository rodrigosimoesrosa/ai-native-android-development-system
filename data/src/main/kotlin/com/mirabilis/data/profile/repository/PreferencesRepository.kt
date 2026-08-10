package com.mirabilis.data.profile.repository

import com.mirabilis.core.result.Result
import com.mirabilis.data.profile.mapper.toDomain
import com.mirabilis.data.profile.mapper.toProto
import com.mirabilis.data.profile.preferences.IPreferencesLocalDataSource
import com.mirabilis.domain.profile.model.Theme
import com.mirabilis.domain.profile.model.UserPreferences
import com.mirabilis.domain.profile.repository.IPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Local preferences over an encrypted Proto DataStore (ADR-0005). Writes persist immediately. */
class PreferencesRepository @Inject constructor(
    private val local: IPreferencesLocalDataSource,
) : IPreferencesRepository {

    override fun observe(): Flow<UserPreferences> = local.preferences().map { it.toDomain() }

    override suspend fun setTheme(theme: Theme): Result<Unit> =
        local.update { it.toBuilder().setTheme(theme.toProto()).build() }

    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> =
        local.update { it.toBuilder().setNotificationsDisabled(!enabled).build() }
}
