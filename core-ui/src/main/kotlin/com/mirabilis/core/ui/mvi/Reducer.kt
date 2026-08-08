package com.mirabilis.core.ui.mvi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds the single immutable state and applies events to it. Ported from `mvi-sample/` with the
 * ADR-0003 fix: state time-travel `History` removed (no dead code until it is a tested feature).
 */
abstract class Reducer<STATE : UiState, EVENT : UiEvent>(initial: STATE) {

    private val _state = MutableStateFlow(initial)
    val state: StateFlow<STATE> get() = _state

    fun sendEvent(event: EVENT) = reduce(_state.value, event)

    protected fun setState(newState: STATE) {
        _state.value = newState
    }

    abstract fun reduce(oldState: STATE, event: EVENT)
}
