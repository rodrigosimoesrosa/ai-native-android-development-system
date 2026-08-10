package com.mirabilis.feature.navigation

/**
 * Stable route ids for the shell's top-level destinations. `HOME` intentionally matches
 * [com.mirabilis.feature.auth.navigation.AuthRoutes.HOME] (001). `PROFILE` is the single cross-feature
 * seam with 002 — a route id string only; the `:feature:profile` module is not depended upon here.
 */
object NavDestinations {
    const val HOME = "home"
    const val PROFILE = "profile"
}

/**
 * A top-level section reachable from the navigation shell: a stable [route] id plus a display [label]
 * and [icon]. No persisted data (data-model.md). Icon is an emoji glyph — a design detail kept
 * dependency-free (no material-icons artifact), consistent with the "boring tech / no new deps" check.
 */
data class Destination(
    val route: String,
    val label: String,
    val icon: String,
)

/** The static, ordered set of shell destinations (data-model.md): Home (001) then Profile (002). */
object Destinations {
    val ALL: List<Destination> = listOf(
        Destination(route = NavDestinations.HOME, label = "Home", icon = "🏠"),
        Destination(route = NavDestinations.PROFILE, label = "Profile", icon = "👤"),
    )
}
