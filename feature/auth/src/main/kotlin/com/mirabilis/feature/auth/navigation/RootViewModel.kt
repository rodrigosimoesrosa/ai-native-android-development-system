package com.mirabilis.feature.auth.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mirabilis.domain.auth.usecase.ObserveAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Resolved start state for the app's top-level route (US2/FR-006: restart lands on Home). */
enum class AuthStartState { Loading, Authenticated, Unauthenticated }

@HiltViewModel
class RootViewModel @Inject constructor(
    observeAuthState: ObserveAuthStateUseCase,
) : ViewModel() {

    val startState: StateFlow<AuthStartState> =
        observeAuthState()
            .map { authenticated ->
                if (authenticated) AuthStartState.Authenticated else AuthStartState.Unauthenticated
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = AuthStartState.Loading,
            )
}
