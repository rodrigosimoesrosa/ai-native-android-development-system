package com.mirabilis.core.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Generic unidirectional MVI base (ADR-0003), ported from `mvi-sample/` with two fixes:
 *  - `setIntent` stays **public** (UI input); `setEvent`/`setEffect` are **protected**
 *    (only the ViewModel produces events/effects — closes the unidirectional-flow leak).
 *  - `History` (state time-travel) removed.
 *
 * Flow: `Intent → onIntent(business logic) → Event → onReduce → new State`; `Effect` for one-shots.
 */
abstract class MVIViewModel<STATE : UiState, EVENT : UiEvent, EFFECT : UiEffect, INTENT : UiIntent> :
    ViewModel() {

    protected abstract fun getInitial(): STATE
    protected abstract fun onIntent(intent: INTENT)
    protected abstract fun onReduce(oldState: STATE, event: EVENT): STATE

    private val reducer: Reducer<STATE, EVENT> by lazy {
        object : Reducer<STATE, EVENT>(getInitial()) {
            override fun reduce(oldState: STATE, event: EVENT) {
                setState(this@MVIViewModel.onReduce(oldState, event))
            }
        }
    }

    val state: StateFlow<STATE> get() = reducer.state

    private val _events = Channel<EVENT>(Channel.BUFFERED)

    // Effects are a BUFFERED channel (not a replay-0 SharedFlow): a one-shot effect emitted before
    // the UI subscribes (e.g. a redirect decided in a ViewModel's init) is held and delivered
    // exactly once, never lost and never re-delivered on recomposition.
    private val _effects = Channel<EFFECT>(Channel.BUFFERED)
    val effects: Flow<EFFECT> = _effects.receiveAsFlow()

    private val _intents = MutableSharedFlow<INTENT>(extraBufferCapacity = 16)

    init {
        viewModelScope.launch { _intents.collect { onIntent(it) } }
        viewModelScope.launch { _events.receiveAsFlow().collect { reducer.sendEvent(it) } }
    }

    /** Public: the UI's only way in. */
    fun setIntent(builder: () -> INTENT) {
        val intent = builder()
        viewModelScope.launch { _intents.emit(intent) }
    }

    /** Protected: only the ViewModel emits internal events. */
    protected fun setEvent(builder: () -> EVENT) {
        val event = builder()
        viewModelScope.launch {
            check(_events.trySend(event).isSuccess) { "Missed event $event" }
        }
    }

    /** Protected: only the ViewModel emits one-shot effects. */
    protected fun setEffect(builder: () -> EFFECT) {
        _effects.trySend(builder())
    }
}
