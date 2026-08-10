package com.mirabilis.feature.auth.home

import androidx.lifecycle.viewModelScope
import com.mirabilis.core.result.Result
import com.mirabilis.core.ui.mvi.MVIViewModel
import com.mirabilis.core.ui.mvi.UiEffect
import com.mirabilis.core.ui.mvi.UiEvent
import com.mirabilis.core.ui.mvi.UiIntent
import com.mirabilis.core.ui.mvi.UiState
import com.mirabilis.domain.auth.model.User
import com.mirabilis.domain.auth.usecase.GetCurrentUserUseCase
import com.mirabilis.domain.auth.usecase.ObserveAuthStateUseCase
import com.mirabilis.domain.auth.usecase.SignOutUseCase
import com.mirabilis.feature.auth.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
) : UiState

sealed interface HomeIntent : UiIntent {
    data object Retry : HomeIntent
    data object SignOut : HomeIntent
}

sealed interface HomeEvent : UiEvent {
    data object Loading : HomeEvent
    data class Loaded(val user: User) : HomeEvent
    data class Failed(val message: String) : HomeEvent
}

sealed interface HomeEffect : UiEffect {
    /** Session ended — sign-out (FR-011) or a non-renewable session (FR-010). */
    data object NavigateToSendPhone : HomeEffect
}

/** US1: show the authenticated user (FR-012). US3: sign out / route out when the session ends. */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    observeAuthState: ObserveAuthStateUseCase,
) : MVIViewModel<HomeUiState, HomeEvent, HomeEffect, HomeIntent>() {

    init {
        load()
        // One route out for BOTH endings: explicit sign-out AND non-renewable teardown (the
        // authenticator clears the session in the background → auth-state flips to false).
        observeAuthState()
            .onEach { authenticated -> if (!authenticated) setEffect { HomeEffect.NavigateToSendPhone } }
            .launchIn(viewModelScope)
    }

    override fun getInitial() = HomeUiState()

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Retry -> load()
            HomeIntent.SignOut -> viewModelScope.launch { signOutUseCase() }
        }
    }

    private fun load() {
        setEvent { HomeEvent.Loading }
        viewModelScope.launch {
            when (val result = getCurrentUser()) {
                is Result.Success -> setEvent { HomeEvent.Loaded(result.data) }
                is Result.Error -> setEvent { HomeEvent.Failed(result.error.toUserMessage()) }
            }
        }
    }

    override fun onReduce(oldState: HomeUiState, event: HomeEvent): HomeUiState =
        when (event) {
            HomeEvent.Loading -> oldState.copy(isLoading = true, error = null)
            is HomeEvent.Loaded -> oldState.copy(isLoading = false, user = event.user, error = null)
            is HomeEvent.Failed -> oldState.copy(isLoading = false, error = event.message)
        }
}
