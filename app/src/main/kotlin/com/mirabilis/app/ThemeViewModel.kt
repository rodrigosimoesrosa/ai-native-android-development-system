package com.mirabilis.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mirabilis.domain.profile.model.Theme
import com.mirabilis.domain.profile.usecase.ObservePreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** US3/FR-004: exposes the persisted theme preference so the app applies it to `MaterialTheme`. */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    observePreferences: ObservePreferencesUseCase,
) : ViewModel() {

    val theme: StateFlow<Theme> =
        observePreferences()
            .map { it.theme }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = Theme.System,
            )
}
