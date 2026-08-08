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
import com.mirabilis.feature.auth.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
) : UiState

sealed interface HomeIntent : UiIntent {
    data object Retry : HomeIntent
}

sealed interface HomeEvent : UiEvent {
    data object Loading : HomeEvent
    data class Loaded(val user: User) : HomeEvent
    data class Failed(val message: String) : HomeEvent
}

/** US1: Home shows the authenticated user, fetched from the protected /me endpoint (FR-012). */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentUser: GetCurrentUserUseCase,
) : MVIViewModel<HomeUiState, HomeEvent, UiEffect, HomeIntent>() {

    init {
        load()
    }

    override fun getInitial() = HomeUiState()

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Retry -> load()
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
