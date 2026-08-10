package com.mirabilis.data.profile.mapper

import com.mirabilis.data.profile.preferences.PreferencesProto
import com.mirabilis.data.profile.preferences.ThemeProto
import com.mirabilis.domain.profile.model.Theme
import com.mirabilis.domain.profile.model.UserPreferences

// Boundary mappers (ADR-0003). Colocated in :data, never domain-visible. `notifications_disabled`
// is inverted so the proto zero value maps to the domain default `notificationsEnabled = true`.

fun PreferencesProto.toDomain(): UserPreferences = UserPreferences(
    theme = theme.toDomain(),
    notificationsEnabled = !notificationsDisabled,
)

fun ThemeProto.toDomain(): Theme = when (this) {
    ThemeProto.THEME_LIGHT -> Theme.Light
    ThemeProto.THEME_DARK -> Theme.Dark
    else -> Theme.System // THEME_SYSTEM + UNRECOGNIZED
}

fun Theme.toProto(): ThemeProto = when (this) {
    Theme.System -> ThemeProto.THEME_SYSTEM
    Theme.Light -> ThemeProto.THEME_LIGHT
    Theme.Dark -> ThemeProto.THEME_DARK
}
