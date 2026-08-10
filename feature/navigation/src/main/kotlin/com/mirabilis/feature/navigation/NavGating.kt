package com.mirabilis.feature.navigation

import com.mirabilis.feature.auth.navigation.AuthStartState

/** Where the app's root should route, given the resolved auth state (US3 / FR-004). */
enum class ShellEntry { Loading, Shell, SignIn }

/**
 * The authenticated-only gating decision, expressed as a pure function so it is JVM-testable
 * (research.md: the auth-gating decision is the small testable slice). It **consumes**
 * [AuthStartState] — which `RootViewModel` derives from `ObserveAuthStateUseCase` (001) — and does not
 * re-implement any session logic (T009). `AuthRoot` performs the same mapping at runtime.
 */
object NavGating {
    /** The shell always opens on Home (US3 / SC-001). */
    const val START_DESTINATION: String = NavDestinations.HOME

    fun entryFor(state: AuthStartState): ShellEntry = when (state) {
        AuthStartState.Loading -> ShellEntry.Loading
        AuthStartState.Authenticated -> ShellEntry.Shell
        AuthStartState.Unauthenticated -> ShellEntry.SignIn
    }
}
