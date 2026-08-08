package com.mirabilis.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

/** Collect a ViewModel's one-shot effects once per composition (navigation, snackbars). */
@Composable
fun <T> CollectEffects(effects: Flow<T>, onEffect: (T) -> Unit) {
    LaunchedEffect(Unit) {
        effects.collect { onEffect(it) }
    }
}
