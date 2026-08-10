package com.mirabilis.feature.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mirabilis.feature.profile.profile.ProfileScreen

/**
 * Registers the Profile destination on a host `NavGraphBuilder`. `003-navigation` composes this into
 * the app graph via [ProfileRoutes.PROFILE]; `:feature:profile` owns the screen, not the graph.
 */
fun NavGraphBuilder.profileScreen() {
    composable(ProfileRoutes.PROFILE) {
        ProfileScreen()
    }
}
