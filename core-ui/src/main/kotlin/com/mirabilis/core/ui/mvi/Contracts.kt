package com.mirabilis.core.ui.mvi

/** Immutable UI state a feature renders. */
interface UiState

/** Internal, ViewModel-produced fact that drives a reduce into new [UiState]. */
interface UiEvent

/** One-shot side effect (navigation, snackbar) — never part of state. */
interface UiEffect

/** User action entering the ViewModel from the UI. */
interface UiIntent
