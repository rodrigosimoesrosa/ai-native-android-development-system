package com.mirabilis.core.designsystem.token

import androidx.compose.ui.graphics.Color

/* ── Brand greens (seed values — FR-002) ───────────────────────────── */

/** Olive Leaf — light primary */
internal val rawOliveLeaf = Color(0xFF3C5A14)

/** Bright Fern — dark primary */
internal val rawBrightFern = Color(0xFF71B340)

/** Sage Green — secondary (shared light/dark) */
internal val rawSageGreen = Color(0xFF669D31)

/** Forest Moss — tertiary (light) */
internal val rawForestMoss = Color(0xFF598B2C)

/** Bright Fern — dark tertiary */
internal val rawTertiaryDark = Color(0xFF8FC95F)

/* ── Semantic colors (derived — FR-004) ─────────────────────────────── */

val semanticError = Color(0xFFB3261E)
val semanticOnError = Color(0xFFFFFFFF)
val semanticErrorDark = Color(0xFFF2B8B5)
val semanticOnErrorDark = Color(0xFF601410)

val semanticWarning = Color(0xFF8A5A00)
val semanticOnWarning = Color(0xFFFFFFFF)
val semanticWarningDark = Color(0xFFE9B949)
val semanticOnWarningDark = Color(0xFF11270B)

val semanticSuccess = Color(0xFF2E7D5B)
val semanticOnSuccess = Color(0xFFFFFFFF)
val semanticSuccessDark = Color(0xFF7FD1AE)
val semanticOnSuccessDark = Color(0xFF11270B)

/* ── Neutral ramp — light theme (derived — FR-003) ─────────────────── */

val neutralBg = Color(0xFFFBFDF6) // bg / surface: green-tinted white
val neutralText = Color(0xFF11270B) // onBg / onSurface text
val neutralSurfaceVariant = Color(0xFFE0E5D6)
val neutralOnSurfaceVariant = Color(0xFF42493B)
val neutralOutline = Color(0xFF72796A)
val neutralOutlineVariant = Color(0xFFC2C9B8)

/* ── Neutral ramp — dark theme (derived — FR-003) ──────────────────── */

val neutralBgDark = Color(0xFF0E1A08) // bg / surface: near-Evergreen
val neutralTextDark = Color(0xFFE3E8DC) // onBg / onSurface text
val neutralSurfaceVariantDark = Color(0xFF42493B)
val neutralOnSurfaceVariantDark = Color(0xFFC2C9B8)
val neutralOutlineDark = Color(0xFF8C9382)
