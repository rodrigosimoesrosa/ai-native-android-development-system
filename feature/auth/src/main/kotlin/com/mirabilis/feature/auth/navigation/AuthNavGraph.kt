package com.mirabilis.feature.auth.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mirabilis.feature.auth.home.HomeScreen
import com.mirabilis.feature.auth.sendphone.SendPhoneScreen
import com.mirabilis.feature.auth.verifyphone.VerifyPhoneScreen

/**
 * Sign-in navigation (US1). Screens drive navigation via one-shot effects; the graph maps them to
 * `navController` calls. Top-level auth-state routing (start at Home when a session exists) lands in
 * User Story 2 (task T051).
 *
 * The `HOME` destination renders [authenticatedContent] — a slot so the navigation shell (003) can be
 * hosted here (supplied by `:app`) without this module depending on it. It defaults to Home, so the
 * post-verify landing works standalone. Its `onSignedOut` dismisses the authenticated area back to
 * sign-in (FR-007).
 */
@Composable
fun AuthNavGraph(
    navController: NavHostController,
    startDestination: String = AuthRoutes.SEND_PHONE,
    authenticatedContent: @Composable (onSignedOut: () -> Unit) -> Unit = { onSignedOut ->
        HomeScreen(onSignedOut = onSignedOut)
    },
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(AuthRoutes.SEND_PHONE) {
            SendPhoneScreen(
                onNavigateToVerify = { navController.navigate(AuthRoutes.VERIFY_PHONE) },
            )
        }

        composable(AuthRoutes.VERIFY_PHONE) {
            VerifyPhoneScreen(
                onNavigateToHome = {
                    navController.navigate(AuthRoutes.HOME) {
                        popUpTo(AuthRoutes.SEND_PHONE) { inclusive = true }
                    }
                },
                onNavigateToSendPhone = {
                    navController.navigate(AuthRoutes.SEND_PHONE) {
                        popUpTo(AuthRoutes.SEND_PHONE) { inclusive = true }
                    }
                },
            )
        }

        composable(AuthRoutes.HOME) {
            authenticatedContent {
                navController.navigate(AuthRoutes.SEND_PHONE) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}
