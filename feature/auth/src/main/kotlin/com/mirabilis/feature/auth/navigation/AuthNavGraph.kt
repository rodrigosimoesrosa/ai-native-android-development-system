package com.mirabilis.feature.auth.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mirabilis.feature.auth.home.HomeScreen
import com.mirabilis.feature.auth.sendphone.SendPhoneScreen
import com.mirabilis.feature.auth.verifyphone.VerifyPhoneScreen

/** Route keys for the auth flow (SendPhone → VerifyPhone → Home). */
object AuthRoutes {
    const val SEND_PHONE = "send_phone"
    const val VERIFY_PHONE = "verify_phone"
    const val HOME = "home"
}

/**
 * Sign-in navigation (US1). Screens drive navigation via one-shot effects; the graph maps them to
 * `navController` calls. Top-level auth-state routing (start at Home when a session exists) lands in
 * User Story 2 (task T051).
 */
@Composable
fun AuthNavGraph(
    navController: NavHostController,
    startDestination: String = AuthRoutes.SEND_PHONE,
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
            HomeScreen()
        }
    }
}
