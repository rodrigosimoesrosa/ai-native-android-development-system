package com.mirabilis.core.result

/**
 * Typed, sealed error contract crossing every layer boundary (ADR-0003). No raw exceptions
 * escape a [Safe* data source]; they are converted into one of these. Sealed so consumers'
 * `when` stays exhaustive (OCP).
 */
sealed interface AppError {
    /** Connectivity / IO failure reaching the backend. */
    data object Network : AppError

    /** Non-2xx HTTP response. [body] is a non-sensitive server payload, may be null. */
    data class Server(val status: Int, val body: String?) : AppError

    /** Room / local relational store failure. */
    data object Database : AppError

    /** Proto DataStore read/write failure (ADR-0005). */
    data object DataStore : AppError

    /** A successful call that yielded no usable data. */
    data object EmptyData : AppError

    /** Client-side input validation failure (e.g. malformed phone) — carries a user-safe message. */
    data class Validation(val message: String) : AppError

    /** Anything not otherwise classified; retains the original cause. */
    data class Unknown(val throwable: Throwable) : AppError
}