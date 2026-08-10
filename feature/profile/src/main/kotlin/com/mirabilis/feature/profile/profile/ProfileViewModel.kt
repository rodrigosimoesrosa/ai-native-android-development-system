package com.mirabilis.feature.profile.profile

import androidx.lifecycle.viewModelScope
import com.mirabilis.core.result.FlowResult
import com.mirabilis.core.result.asResult
import com.mirabilis.core.ui.mvi.MVIViewModel
import com.mirabilis.core.ui.mvi.UiEffect
import com.mirabilis.core.ui.mvi.UiEvent
import com.mirabilis.core.ui.mvi.UiIntent
import com.mirabilis.core.ui.mvi.UiState
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.usecase.ObserveUserUseCase
import com.mirabilis.feature.profile.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
) : UiState

sealed interface ProfileIntent : UiIntent {
    data object Retry : ProfileIntent
}

sealed interface ProfileEvent : UiEvent {
    data object Loading : ProfileEvent
    data class UserLoaded(val user: User) : ProfileEvent
    data class Failed(val message: String) : ProfileEvent
}

/** Profile has no one-shot effects; the route guard is handled at the navigation layer (FR-005). */
sealed interface ProfileEffect : UiEffect

/** US1: view the authenticated user (display name + phone) with error + retry (FR-001/006). */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeUser: ObserveUserUseCase,
) : MVIViewModel<ProfileUiState, ProfileEvent, ProfileEffect, ProfileIntent>() {

    private var userJob: Job? = null

    init {
        loadUser()
    }

    override fun getInitial() = ProfileUiState()

    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Retry -> loadUser()
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

    override fun onReduce(oldState: ProfileUiState, event: ProfileEvent): ProfileUiState =
        when (event) {
            ProfileEvent.Loading -> oldState.copy(isLoading = true, error = null)
            is ProfileEvent.UserLoaded -> oldState.copy(isLoading = false, user = event.user, error = null)
            is ProfileEvent.Failed -> oldState.copy(isLoading = false, error = event.message)
        }
}
