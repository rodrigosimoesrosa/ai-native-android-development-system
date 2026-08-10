package com.mirabilis.feature.profile

import com.mirabilis.core.result.AppError

/**
 * Maps a typed [AppError] to a clear, **non-sensitive** user message for Profile (FR-006). Mirrors
 * the `:feature:auth` mapping but with profile-appropriate copy (400 = invalid display name); kept
 * per-feature to avoid a feature→feature dependency (ADR-0003 layering).
 */
fun AppError.toUserMessage(): String = when (this) {
    is AppError.Validation -> message
    AppError.Network -> "No connection. Check your network and try again."
    is AppError.Server -> when (status) {
        400 -> "That name isn't valid. Please try another."
        401 -> "Your session expired. Please sign in again."
        in 500..599 -> "Something went wrong on our end. Please try again."
        else -> "Request failed. Please try again."
    }
    AppError.Database, AppError.DataStore -> "Couldn't access local storage. Please try again."
    AppError.EmptyData -> "Nothing to show."
    is AppError.Unknown -> "Something went wrong. Please try again."
}
