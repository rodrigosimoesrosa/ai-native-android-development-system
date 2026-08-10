package com.mirabilis.feature.navigation

import com.mirabilis.feature.auth.navigation.AuthStartState
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * T008 [US3]: the authenticated-only gating decision — Authenticated → shell (start = home); no valid
 * session → sign-in; unresolved → loading. Consumes [AuthStartState] (from `ObserveAuthStateUseCase`);
 * no session logic re-implemented.
 */
class NavGatingTest {

    @Test
    fun `authenticated routes to the shell`() {
        assertEquals(ShellEntry.Shell, NavGating.entryFor(AuthStartState.Authenticated))
    }

    @Test
    fun `no valid session routes to sign-in`() {
        assertEquals(ShellEntry.SignIn, NavGating.entryFor(AuthStartState.Unauthenticated))
    }

    @Test
    fun `unresolved auth state stays loading`() {
        assertEquals(ShellEntry.Loading, NavGating.entryFor(AuthStartState.Loading))
    }

    @Test
    fun `the shell opens on home`() {
        assertEquals(NavDestinations.HOME, NavGating.START_DESTINATION)
    }
}
