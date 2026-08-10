package com.mirabilis.feature.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mirabilis.feature.auth.home.HomeScreen

/**
 * Whether the Profile destination's route (owned by `002-user-profile`) is present in this build.
 * On the `003-navigation` branch (cut from main, without 002) it is **false**, so the Profile tab
 * degrades gracefully (FR-006). At the merge gate with 002 this flips to `true` and the placeholder is
 * replaced by the real Profile screen hosted at the shared `profile` route id. The single seam with 002.
 */
const val PROFILE_ROUTE_AVAILABLE: Boolean = false

/**
 * The authenticated navigation shell (US1): a `Scaffold` + bottom `NavigationBar` over an inner
 * `NavHost` that **hosts** Home (001) and Profile (002 route) — it does not rebuild them (FR-005).
 * Opens on Home (US3 / [NavGating.START_DESTINATION]). State is preserved per destination (FR-003) via
 * [NavShellScaffold]. When [PROFILE_ROUTE_AVAILABLE] is false the Profile tab is disabled and its slot
 * shows a placeholder, Home stays usable, no crash (FR-006 / T010).
 *
 * @param onSignedOut invoked when the hosted Home signs out — the caller (`AuthRoot`) dismisses the
 *   shell and routes to sign-in (FR-007).
 */
@Composable
fun NavShell(
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    profileAvailable: Boolean = PROFILE_ROUTE_AVAILABLE,
) {
    NavShellScaffold(
        destinations = Destinations.ALL,
        modifier = modifier,
        navController = navController,
        startDestination = NavGating.START_DESTINATION,
        isEnabled = { dest -> dest.route != NavDestinations.PROFILE || profileAvailable },
    ) {
        composable(NavDestinations.HOME) {
            HomeScreen(onSignedOut = onSignedOut)
        }
        composable(NavDestinations.PROFILE) {
            // Real Profile (002) is hosted here at merge; until then, a graceful placeholder (FR-006).
            ProfileUnavailablePlaceholder()
        }
    }
}

/**
 * Reusable shell mechanics: a bottom-nav `Scaffold` wrapping a state-preserving inner `NavHost`.
 * Kept separate from [NavShell]'s concrete destinations so the state-preservation behavior (FR-003)
 * can be exercised by instrumented tests with placeholder destinations (US1 independent-test note).
 *
 * State preservation (T006): each tab switch uses `saveState`/`restoreState` + `launchSingleTop`, so a
 * destination's own back stack / scroll / input state survives switching away and back. Re-selecting the
 * active tab is a no-op reload thanks to `launchSingleTop` (AC1.2).
 */
@Composable
fun NavShellScaffold(
    destinations: List<Destination>,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavDestinations.HOME,
    isEnabled: (Destination) -> Boolean = { true },
    content: NavGraphBuilder.() -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                destinations.forEach { dest ->
                    val enabled = isEnabled(dest)
                    val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        modifier = Modifier.testTag("tab_${dest.route}"),
                        selected = selected,
                        enabled = enabled,
                        onClick = {
                            navController.navigate(dest.route) {
                                // Preserve the current tab's state and restore the target's (FR-003).
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(dest.icon) },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            builder = content,
        )
    }
}

/** FR-006: the Profile route is unavailable in this build; the tab is disabled and this is its slot. */
@Composable
private fun ProfileUnavailablePlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Profile is not available yet.", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "It arrives when the profile feature is merged.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
