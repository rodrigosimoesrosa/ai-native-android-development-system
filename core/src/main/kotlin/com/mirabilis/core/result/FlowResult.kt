package com.mirabilis.core.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Reactive boundary result (ADR-0003). Adds [Loading] over [Result] for streams — pairs with the
 * offline-first source-of-truth pattern (ADR-0005).
 */
sealed interface FlowResult<out T> {
    data class Success<T>(val data: T) : FlowResult<T>
    data class Error(val error: AppError) : FlowResult<Nothing>
    data object Loading : FlowResult<Nothing>
}

/** Wrap a raw [Flow] into a [FlowResult] stream: Loading first, then Success, Error on throw. */
fun <T> Flow<T>.asResult(): Flow<FlowResult<T>> =
    map<T, FlowResult<T>> { FlowResult.Success(it) }
        .onStart { emit(FlowResult.Loading) }
        .catch { emit(FlowResult.Error(AppError.Unknown(it))) }
