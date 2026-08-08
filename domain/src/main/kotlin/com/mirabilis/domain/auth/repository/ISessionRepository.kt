package com.mirabilis.domain.auth.repository

import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Local session state (persisted securely per ADR-0005/0006). Declared in `:domain`, implemented
 * in `:data`. Drives top-level auth routing via [observeAuthState].
 */
interface ISessionRepository {

    /** Emits `true` while a valid session is stored (FR-006); drives Home vs SendPhone routing. */
    fun observeAuthState(): Flow<Boolean>

    /** The current authenticated user for Home, or `null` when signed out. */
    fun observeUser(): Flow<User?>

    /** FR-011: user-initiated sign-out — clears session + user. */
    suspend fun signOut(): Result<Unit>

    /** FR-010: teardown when the session can no longer be renewed. */
    suspend fun clearSession(): Result<Unit>
}
