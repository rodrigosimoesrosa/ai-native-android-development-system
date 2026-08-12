package com.mirabilis.core.designsystem.component

/**
 * Size variants for the floating action button.
 *
 * **Variants**
 * - `Small` — compact circular FAB.
 * - `Regular` — standard circular FAB (default).
 * - **Extended** — when [MirabilisFab.text] is non-null, renders as an extended FAB
 *   with icon + label side-by-side, regardless of this value.
 *
 * **States** (all variants, including extended)
 * - **Enabled** — full `primary`/`onPrimary` colours.
 * - **Pressed** — standard Compose ripple interaction.
 * - **Disabled** — 38 % alpha on background, 12 % alpha on foreground content.
 *
 * @see MirabilisFab
 */
enum class MirabilisFabSize {
    Small,
    Regular,
}
