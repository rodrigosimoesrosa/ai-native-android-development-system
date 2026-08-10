package com.mirabilis.feature.navigation

import com.mirabilis.feature.auth.navigation.AuthStartState
import org.junit.Assert.assertEquals
import org.junit.Test

/** T003 [US1]: the destinations list is correct and the Authenticated → shell decision holds. */
class DestinationsTest {

    @Test
    fun `destinations are Home then Profile, in order, with the shared route ids`() {
        val routes = Destinations.ALL.map { it.route }
        assertEquals(listOf(NavDestinations.HOME, NavDestinations.PROFILE), routes)
    }

    @Test
    fun `every destination has a non-blank label and icon`() {
        Destinations.ALL.forEach { dest ->
            assertEquals("route ${dest.route}: label", true, dest.label.isNotBlank())
            assertEquals("route ${dest.route}: icon", true, dest.icon.isNotBlank())
        }
    }

    @Test
    fun `home route id matches the auth feature's home route (001 reuse)`() {
        assertEquals(com.mirabilis.feature.auth.navigation.AuthRoutes.HOME, NavDestinations.HOME)
    }

    @Test
    fun `authenticated resolves to the shell`() {
        assertEquals(ShellEntry.Shell, NavGating.entryFor(AuthStartState.Authenticated))
    }
}
