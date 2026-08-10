package com.mirabilis.feature.auth.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.mirabilis.feature.auth.home.HomeScreen

/**
 * App entry point. Picks the **start destination once** from the first resolved auth state
 * (Authenticated → Home, else SendPhone) — so a restart with a valid session lands on Home without
 * a code prompt (FR-006). It intentionally latches the first resolved value rather than swapping the
 * whole graph on later auth changes; in-flow navigation and logout-routing are handled inside the
 * graph (US1 effects; US3 for teardown).
 *
 * The **authenticated area** is provided as a slot ([authenticatedContent]) so a higher layer (`:app`)
 * can supply the navigation shell (003) without this module depending on it — keeping the dependency
 * one-directional (`navigation → auth`). The default hosts Home directly, so `:feature:auth` stays
 * independently runnable. The slot receives an `onSignedOut` callback that dismisses the authenticated
 * area and routes back to sign-in (FR-007).
 */
@Composable
fun AuthRoot(
    viewModel: RootViewModel = hiltViewModel(),
    authenticatedContent: @Composable (onSignedOut: () -> Unit) -> Unit = { onSignedOut ->
        HomeScreen(onSignedOut = onSignedOut)
    },
) {
    val startState by viewModel.startState.collectAsState()
    var startRoute by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(startState) {
        if (startRoute == null && startState != AuthStartState.Loading) {
            startRoute = if (startState == AuthStartState.Authenticated) {
                AuthRoutes.HOME
            } else {
                AuthRoutes.SEND_PHONE
            }
        }
    }

    when (val route = startRoute) {
        null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        else -> AuthNavGraph(
            navController = rememberNavController(),
            startDestination = route,
            authenticatedContent = authenticatedContent,
        )
    }
}
