package com.mirabilis.feature.profile.profile

import androidx.lifecycle.viewModelScope
import com.mirabilis.core.result.FlowResult
import com.mirabilis.core.result.Result
import com.mirabilis.core.result.asResult
import com.mirabilis.core.ui.mvi.MVIViewModel
import com.mirabilis.core.ui.mvi.UiEffect
import com.mirabilis.core.ui.mvi.UiEvent
import com.mirabilis.core.ui.mvi.UiIntent
import com.mirabilis.core.ui.mvi.UiState
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.usecase.ObserveUserUseCase
import com.mirabilis.domain.profile.model.Theme
import com.mirabilis.domain.profile.model.UserPreferences
import com.mirabilis.domain.profile.usecase.ObservePreferencesUseCase
import com.mirabilis.domain.profile.usecase.SetNotificationsUseCase
import com.mirabilis.domain.profile.usecase.SetThemeUseCase
import com.mirabilis.domain.profile.usecase.UpdateDisplayNameUseCase
import com.mirabilis.feature.profile.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
    // US2 — display-name edit
    val nameDraft: String = "",
    val isSaving: Boolean = false,
    val saveError: String? = null,
    // US3 — preferences
    val preferences: UserPreferences = UserPreferences(),
) : UiState

sealed interface ProfileIntent : UiIntent {
    data object Retry : ProfileIntent
    data class NameChanged(val value: String) : ProfileIntent
    data object Save : ProfileIntent
    data class SetTheme(val theme: Theme) : ProfileIntent
    data class SetNotifications(val enabled: Boolean) : ProfileIntent
}

sealed interface ProfileEvent : UiEvent {
    data object Loading : ProfileEvent
    data class UserLoaded(val user: User) : ProfileEvent
    data class Failed(val message: String) : ProfileEvent
    data class DraftChanged(val value: String) : ProfileEvent
    data object Saving : ProfileEvent
    data object Saved : ProfileEvent
    data class SaveFailed(val message: String) : ProfileEvent
    data class PreferencesLoaded(val preferences: UserPreferences) : ProfileEvent
}

/** Profile has no one-shot effects; the route guard is handled at the navigation layer (FR-005). */
sealed interface ProfileEffect : UiEffect

/** US1: view the user (FR-001/006). US2: edit the display name (FR-002/003/006). */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeUser: ObserveUserUseCase,
    private val updateDisplayName: UpdateDisplayNameUseCase,
    observePreferences: ObservePreferencesUseCase,
    private val setTheme: SetThemeUseCase,
    private val setNotifications: SetNotificationsUseCase,
) : MVIViewModel<ProfileUiState, ProfileEvent, ProfileEffect, ProfileIntent>() {

    private var userJob: Job? = null

    init {
        loadUser()
        observePreferences()
            .onEach { prefs -> setEvent { ProfileEvent.PreferencesLoaded(prefs) } }
            .launchIn(viewModelScope)
    }

    override fun getInitial() = ProfileUiState()

    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Retry -> loadUser()
            is ProfileIntent.NameChanged -> setEvent { ProfileEvent.DraftChanged(intent.value) }
            ProfileIntent.Save -> save()
            is ProfileIntent.SetTheme -> viewModelScope.launch { setTheme(intent.theme) }
            is ProfileIntent.SetNotifications -> viewModelScope.launch { setNotifications(intent.enabled) }
        }
    }

    private fun loadUser() {
        userJob?.cancel()
        setEvent { ProfileEvent.Loading }
        userJob = observeUser().asResult()
            .onEach { result ->
                when (result) {
                    FlowResult.Loading -> setEvent { ProfileEvent.Loading }
                    is FlowResult.Success -> result.data
                        ?.let { user -> setEvent { ProfileEvent.UserLoaded(user) } }
                        ?: setEvent { ProfileEvent.Failed("Couldn't load your profile.") }
                    is FlowResult.Error -> setEvent { ProfileEvent.Failed(result.error.toUserMessage()) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun save() {
        val name = state.value.nameDraft
        setEvent { ProfileEvent.Saving }
        viewModelScope.launch {
            when (val result = updateDisplayName(name)) {
                is Result.Success -> setEvent { ProfileEvent.Saved }
                is Result.Error -> setEvent { ProfileEvent.SaveFailed(result.error.toUserMessage()) }
            }
        }
    }

    override fun onReduce(oldState: ProfileUiState, event: ProfileEvent): ProfileUiState =
        when (event) {
            ProfileEvent.Loading -> oldState.copy(isLoading = true, error = null)
            is ProfileEvent.UserLoaded -> oldState.copy(
                isLoading = false,
                user = event.user,
                error = null,
                nameDraft = if (oldState.nameDraft.isEmpty()) event.user.displayName.orEmpty() else oldState.nameDraft,
            )
            is ProfileEvent.Failed -> oldState.copy(isLoading = false, error = event.message)
            is ProfileEvent.DraftChanged -> oldState.copy(nameDraft = event.value, saveError = null)
            ProfileEvent.Saving -> oldState.copy(isSaving = true, saveError = null)
            ProfileEvent.Saved -> oldState.copy(isSaving = false, saveError = null)
            is ProfileEvent.SaveFailed -> oldState.copy(isSaving = false, saveError = event.message)
            is ProfileEvent.PreferencesLoaded -> oldState.copy(preferences = event.preferences)
        }
}
