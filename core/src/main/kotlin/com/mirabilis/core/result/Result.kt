package com.mirabilis.core.result

/**
 * One-shot boundary result (ADR-0003). Deliberately shadows [kotlin.Result] inside this package;
 * consumers in other modules import `com.mirabilis.core.result.Result` explicitly.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val error: AppError) : Result<Nothing>
}

/** Collapse both arms into a single value. */
inline fun <T, R> Result<T>.fold(onError: (AppError) -> R, onSuccess: (T) -> R): R = when (this) {
    is Result.Success -> onSuccess(data)
    is Result.Error -> onError(error)
}

/** The success payload, or null on error. */
fun <T> Result<T>.getOrNull(): T? = (this as? Result.Success)?.data

/** Map the success payload; errors pass through unchanged. */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
}

/** Run [action] when successful; returns the original result for chaining. */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

/** Run [action] when failed; returns the original result for chaining. */
inline fun <T> Result<T>.onError(action: (AppError) -> Unit): Result<T> {
    if (this is Result.Error) action(error)
    return this
}
