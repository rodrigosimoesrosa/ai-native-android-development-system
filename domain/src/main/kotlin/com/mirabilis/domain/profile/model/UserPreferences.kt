package com.mirabilis.domain.profile.model

/**
 * Per-user, on-device app preferences (US3). Persisted locally only (no backend). Defaults match the
 * proto3 zero values so an unset store reads as [Theme.System] + notifications on.
 */
data class UserPreferences(
    val theme: Theme = Theme.System,
    val notificationsEnabled: Boolean = true,
)

/** App theme preference (FR-004). */
enum class Theme { System, Light, Dark }
