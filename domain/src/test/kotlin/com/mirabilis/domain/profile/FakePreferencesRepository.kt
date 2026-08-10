package com.mirabilis.domain.profile

import com.mirabilis.core.result.Result
import com.mirabilis.domain.profile.model.Theme
import com.mirabilis.domain.profile.model.UserPreferences
import com.mirabilis.domain.profile.repository.IPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [IPreferencesRepository] test double; writes update the observable state. */
class FakePreferencesRepository(
    initial: UserPreferences = UserPreferences(),
) : IPreferencesRepository {

    val state = MutableStateFlow(initial)

    override fun observe(): Flow<UserPreferences> = state

    override suspend fun setTheme(theme: Theme): Result<Unit> {
        state.value = state.value.copy(theme = theme)
        return Result.Success(Unit)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> {
        state.value = state.value.copy(notificationsEnabled = enabled)
        return Result.Success(Unit)
    }
}
