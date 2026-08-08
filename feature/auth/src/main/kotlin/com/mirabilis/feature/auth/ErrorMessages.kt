package com.mirabilis.feature.auth

import com.mirabilis.core.result.AppError

/**
 * Maps a typed [AppError] to a clear, **non-sensitive** user message (FR-004/FR-014, SC-006).
 * Backend-owned limits (rate-limit/lockout) are reflected, never defined here.
 */
fun AppError.toUserMessage(): String = when (this) {
    is AppError.Validation -> message
    AppError.Network -> "No connection. Check your network and try again."
    is AppError.Server -> when (status) {
        400 -> "That code isn't valid. Please try again."
        410 -> "That code has expired. Request a new one."
        429 -> "Too many attempts. Please wait a moment and try again."
        in 500..599 -> "Something went wrong on our end. Please try again."
        else -> "Request failed. Please try again."
    }
    AppError.Database, AppError.DataStore -> "Couldn't access local storage. Please try again."
    AppError.EmptyData -> "Nothing to show."
    is AppError.Unknown -> "Something went wrong. Please try again."
}
