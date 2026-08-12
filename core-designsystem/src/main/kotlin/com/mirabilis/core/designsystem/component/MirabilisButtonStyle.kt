package com.mirabilis.core.designsystem.component

/**
 * Button emphasis variants for the design system.
 *
 * **Variants**
 * - `Primary` — filled button using `primary`/`onPrimary` color roles.
 * - `Secondary` — outlined button using `primary` color for the stroke.
 * - `Text` — text-only button using `primary` color.
 *
 * **States** (all variants)
 * - **Enabled** — full opacity colours from tokens.
 * - **Pressed / Focused** — standard Compose ripple interaction.
 * - **Disabled** — 38 % alpha on background, 12 % alpha on foreground text.
 *
 * @see MirabilisButton
 */
enum class MirabilisButtonStyle {
    Primary,
    Secondary,
    Text,
}
