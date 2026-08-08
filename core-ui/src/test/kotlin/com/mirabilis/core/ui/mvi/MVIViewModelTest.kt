package com.mirabilis.core.ui.mvi

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MVIViewModelTest {

    // --- A minimal counter VM exercising Intent → Event → Reducer → State + Effect. ---
    private data class CounterState(val count: Int) : UiState
    private sealed interface CounterIntent : UiIntent {
        data object Increment : CounterIntent
    }
    private sealed interface CounterEvent : UiEvent {
        data object Incremented : CounterEvent
    }
    private sealed interface CounterEffect : UiEffect {
        data object Buzzed : CounterEffect
    }

    private class CounterViewModel : MVIViewModel<CounterState, CounterEvent, CounterEffect, CounterIntent>() {
        override fun getInitial() = CounterState(0)
        override fun onIntent(intent: CounterIntent) {
            when (intent) {
                CounterIntent.Increment -> {
                    setEvent { CounterEvent.Incremented }
                    setEffect { CounterEffect.Buzzed }
                }
            }
        }
        override fun onReduce(oldState: CounterState, event: CounterEvent): CounterState = when (event) {
            CounterEvent.Incremented -> oldState.copy(count = oldState.count + 1)
        }
    }

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `intent drives event through reducer into new state`() = runTest(dispatcher) {
        val vm = CounterViewModel()
        vm.state.test {
            assertEquals(CounterState(0), awaitItem())
            vm.setIntent { CounterIntent.Increment }
            assertEquals(CounterState(1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `intent emits one-shot effect`() = runTest(dispatcher) {
        val vm = CounterViewModel()
        vm.effects.test {
            vm.setIntent { CounterIntent.Increment }
            assertEquals(CounterEffect.Buzzed, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
