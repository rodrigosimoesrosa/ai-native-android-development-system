package com.mirabilis.core.designsystem

import androidx.compose.ui.graphics.Color
import com.mirabilis.core.designsystem.token.darkColorScheme
import com.mirabilis.core.designsystem.token.darkExtraColors
import com.mirabilis.core.designsystem.token.lightColorScheme
import com.mirabilis.core.designsystem.token.lightExtraColors
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastTest {

    private val light = lightColorScheme()
    private val dark = darkColorScheme()
    private val lightExtras = lightExtraColors()
    private val darkExtras = darkExtraColors()

    private fun Color.linearize(channel: Float): Double {
        val s = channel.toDouble()
        return if (s <= 0.04045) s / 12.92 else Math.pow((s + 0.055) / 1.055, 2.4)
    }

    private fun Color.relativeLuminance(): Double {
        val r = linearize(red)
        val g = linearize(green)
        val b = linearize(blue)
        return 0.2126 * r + 0.587 * g + 0.114 * b
    }

    private fun contrastRatio(a: Color, b: Color): Double {
        val l1 = a.relativeLuminance()
        val l2 = b.relativeLuminance()
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun `debug print all contrast ratios`() {
        println("=== LIGHT TEXT ===")
        println("onPrimary on primary: ${contrastRatio(light.onPrimary, light.primary)}")
        val r1 = contrastRatio(light.onPrimaryContainer, light.primaryContainer)
        println("onPrimaryContainer on primaryContainer: $r1")
        val r2 = contrastRatio(light.onSecondary, light.secondary)
        println("onSecondary on secondary: $r2")
        val r3 = contrastRatio(light.onSecondaryContainer, light.secondaryContainer)
        println("onSecondaryContainer on secondaryContainer: $r3")
        println("onTertiary on tertiary: ${contrastRatio(light.onTertiary, light.tertiary)}")
        val r4 = contrastRatio(light.onTertiaryContainer, light.tertiaryContainer)
        println("onTertiaryContainer on tertiaryContainer: $r4")
        println("onError on error: ${contrastRatio(light.onError, light.error)}")
        val r5 = contrastRatio(light.onErrorContainer, light.errorContainer)
        println("onErrorContainer on errorContainer: $r5")
        println("onBackground on background: ${contrastRatio(light.onBackground, light.background)}")
        println("onSurface on surface: ${contrastRatio(light.onSurface, light.surface)}")
        val r6 = contrastRatio(light.onSurfaceVariant, light.surfaceVariant)
        println("onSurfaceVariant on surfaceVariant: $r6")
        val r7 = contrastRatio(light.inverseOnSurface, light.inverseSurface)
        println("inverseOnSurface on inverseSurface: $r7")
        println("=== LIGHT UI ===")
        val r8 = contrastRatio(light.inversePrimary, light.inverseSurface)
        println("inversePrimary on inverseSurface: $r8")
        println("outline on surface: ${contrastRatio(light.outline, light.surface)}")
        println("outlineVariant on surface: ${contrastRatio(light.outlineVariant, light.surface)}")
        println("=== LIGHT EXTRAS ===")
        println("onWarning on warning: ${contrastRatio(lightExtras.onWarning, lightExtras.warning)}")
        println("onSuccess on success: ${contrastRatio(lightExtras.onSuccess, lightExtras.success)}")
        println("=== DARK TEXT ===")
        println("onPrimary on primary: ${contrastRatio(dark.onPrimary, dark.primary)}")
        val r9 = contrastRatio(dark.onPrimaryContainer, dark.primaryContainer)
        println("onPrimaryContainer on primaryContainer: $r9")
        val r10 = contrastRatio(dark.onSecondary, dark.secondary)
        println("onSecondary on secondary: $r10")
        val r11 = contrastRatio(dark.onSecondaryContainer, dark.secondaryContainer)
        println("onSecondaryContainer on secondaryContainer: $r11")
        println("onTertiary on tertiary: ${contrastRatio(dark.onTertiary, dark.tertiary)}")
        val r12 = contrastRatio(dark.onTertiaryContainer, dark.tertiaryContainer)
        println("onTertiaryContainer on tertiaryContainer: $r12")
        println("onError on error: ${contrastRatio(dark.onError, dark.error)}")
        val r13 = contrastRatio(dark.onErrorContainer, dark.errorContainer)
        println("onErrorContainer on errorContainer: $r13")
        println("onBackground on background: ${contrastRatio(dark.onBackground, dark.background)}")
        println("onSurface on surface: ${contrastRatio(dark.onSurface, dark.surface)}")
        val r14 = contrastRatio(dark.onSurfaceVariant, dark.surfaceVariant)
        println("onSurfaceVariant on surfaceVariant: $r14")
        val r15 = contrastRatio(dark.inverseOnSurface, dark.inverseSurface)
        println("inverseOnSurface on inverseSurface: $r15")
        println("=== DARK UI ===")
        val r16 = contrastRatio(dark.inversePrimary, dark.inverseSurface)
        println("inversePrimary on inverseSurface: $r16")
        println("outline on surface: ${contrastRatio(dark.outline, dark.surface)}")
        println("outlineVariant on surface: ${contrastRatio(dark.outlineVariant, dark.surface)}")
        println("=== DARK EXTRAS ===")
        println("onWarning on warning: ${contrastRatio(darkExtras.onWarning, darkExtras.warning)}")
        println("onSuccess on success: ${contrastRatio(darkExtras.onSuccess, darkExtras.success)}")
    }

    // -- Light theme text contrast (WCAG AA >= 4.5:1) --

    @Test
    fun `light theme text on primary meets WCAG AA`() {
        val ratio = contrastRatio(light.onPrimary, light.primary)
        assertTrue(
            "onPrimary on primary = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `light theme text on primaryContainer meets WCAG AA`() {
        val ratio = contrastRatio(light.onPrimaryContainer, light.primaryContainer)
        assertTrue(
            "onPrimaryContainer on primaryContainer = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `light theme text on secondary meets WCAG AA`() {
        val ratio = contrastRatio(light.onSecondary, light.secondary)
        assertTrue(
            "onSecondary on secondary = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `light theme text on secondaryContainer meets WCAG AA`() {
        val ratio = contrastRatio(light.onSecondaryContainer, light.secondaryContainer)
        assertTrue(
            "onSecondaryContainer on secondaryContainer = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `light theme text on tertiary meets WCAG AA`() {
        val ratio = contrastRatio(light.onTertiary, light.tertiary)
        assertTrue(
            "onTertiary on tertiary = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `light theme text on tertiaryContainer meets WCAG AA`() {
        val ratio = contrastRatio(light.onTertiaryContainer, light.tertiaryContainer)
        assertTrue(
            "onTertiaryContainer on tertiaryContainer = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `light theme text on error meets WCAG AA`() {
        val ratio = contrastRatio(light.onError, light.error)
        assertTrue(
            "onError on error = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `light theme text on errorContainer meets WCAG AA`() {
        val ratio = contrastRatio(light.onErrorContainer, light.errorContainer)
        assertTrue(
            "onErrorContainer on errorContainer = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `light theme onBackground on background meets WCAG AA`() {
        val ratio = contrastRatio(light.onBackground, light.background)
        assertTrue(
            "onBackground on background = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `light theme onSurface on surface meets WCAG AA`() {
        val ratio = contrastRatio(light.onSurface, light.surface)
        assertTrue(
            "onSurface on surface = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `light theme onSurfaceVariant on surfaceVariant meets WCAG AA`() {
        val ratio = contrastRatio(light.onSurfaceVariant, light.surfaceVariant)
        assertTrue(
            "onSurfaceVariant on surfaceVariant = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `light theme onInverseSurface on inverseSurface meets WCAG AA`() {
        val ratio = contrastRatio(light.inverseOnSurface, light.inverseSurface)
        assertTrue(
            "inverseOnSurface on inverseSurface = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `light theme onWarning on warning meets WCAG AA`() {
        val ratio = contrastRatio(lightExtras.onWarning, lightExtras.warning)
        assertTrue(
            "onWarning on warning = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `light theme onSuccess on success meets WCAG AA`() {
        val ratio = contrastRatio(lightExtras.onSuccess, lightExtras.success)
        assertTrue(
            "onSuccess on success = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    // -- Light theme large text / UI contrast (WCAG >= 3:1) --

    @Test
    fun `light theme inversePrimary on inverseSurface meets WCAG UI`() {
        val ratio = contrastRatio(light.inversePrimary, light.inverseSurface)
        assertTrue(
            "inversePrimary on inverseSurface = $ratio:1, expected >= 3:1 for UI",
            ratio >= 3.0,
        )
    }

    @Test
    fun `light theme outline on surface meets WCAG UI`() {
        val ratio = contrastRatio(light.outline, light.surface)
        assertTrue(
            "outline on surface = $ratio:1, expected >= 3:1 for UI",
            ratio >= 3.0,
        )
    }

    @Test
    fun `light theme outlineVariant on surface meets WCAG UI`() {
        val ratio = contrastRatio(light.outlineVariant, light.surface)
        assertTrue(
            "outlineVariant on surface = $ratio:1, expected >= 3:1 for UI",
            ratio >= 3.0,
        )
    }

    // -- Dark theme text contrast (WCAG AA >= 4.5:1) --

    @Test
    fun `dark theme text on primary meets WCAG AA`() {
        val ratio = contrastRatio(dark.onPrimary, dark.primary)
        assertTrue(
            "onPrimary on primary = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `dark theme text on primaryContainer meets WCAG AA`() {
        val ratio = contrastRatio(dark.onPrimaryContainer, dark.primaryContainer)
        assertTrue(
            "onPrimaryContainer on primaryContainer = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `dark theme text on secondary meets WCAG AA`() {
        val ratio = contrastRatio(dark.onSecondary, dark.secondary)
        assertTrue(
            "onSecondary on secondary = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `dark theme text on secondaryContainer meets WCAG AA`() {
        val ratio = contrastRatio(dark.onSecondaryContainer, dark.secondaryContainer)
        assertTrue(
            "onSecondaryContainer on secondaryContainer = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `dark theme text on tertiary meets WCAG AA`() {
        val ratio = contrastRatio(dark.onTertiary, dark.tertiary)
        assertTrue(
            "onTertiary on tertiary = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `dark theme text on tertiaryContainer meets WCAG AA`() {
        val ratio = contrastRatio(dark.onTertiaryContainer, dark.tertiaryContainer)
        assertTrue(
            "onTertiaryContainer on tertiaryContainer = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `dark theme text on error meets WCAG AA`() {
        val ratio = contrastRatio(dark.onError, dark.error)
        assertTrue(
            "onError on error = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `dark theme text on errorContainer meets WCAG AA`() {
        val ratio = contrastRatio(dark.onErrorContainer, dark.errorContainer)
        assertTrue(
            "onErrorContainer on errorContainer = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `dark theme onBackground on background meets WCAG AA`() {
        val ratio = contrastRatio(dark.onBackground, dark.background)
        assertTrue(
            "onBackground on background = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `dark theme onSurface on surface meets WCAG AA`() {
        val ratio = contrastRatio(dark.onSurface, dark.surface)
        assertTrue(
            "onSurface on surface = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `dark theme onSurfaceVariant on surfaceVariant meets WCAG AA`() {
        val ratio = contrastRatio(dark.onSurfaceVariant, dark.surfaceVariant)
        assertTrue(
            "onSurfaceVariant on surfaceVariant = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `dark theme onInverseSurface on inverseSurface meets WCAG AA`() {
        val ratio = contrastRatio(dark.inverseOnSurface, dark.inverseSurface)
        assertTrue(
            "inverseOnSurface on inverseSurface = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `dark theme onWarning on warning meets WCAG AA`() {
        val ratio = contrastRatio(darkExtras.onWarning, darkExtras.warning)
        assertTrue(
            "onWarning on warning = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    @Test
    fun `dark theme onSuccess on success meets WCAG AA`() {
        val ratio = contrastRatio(darkExtras.onSuccess, darkExtras.success)
        assertTrue(
            "onSuccess on success = $ratio:1, expected >= 4.5:1",
            ratio >= 4.5,
        )
    }

    // -- Dark theme large text / UI contrast (WCAG >= 3:1) --

    @Test
    fun `dark theme inversePrimary on inverseSurface meets WCAG UI`() {
        val ratio = contrastRatio(dark.inversePrimary, dark.inverseSurface)
        assertTrue(
            "inversePrimary on inverseSurface = $ratio:1, expected >= 3:1 for UI",
            ratio >= 3.0,
        )
    }

    @Test
    fun `dark theme outline on surface meets WCAG UI`() {
        val ratio = contrastRatio(dark.outline, dark.surface)
        assertTrue(
            "outline on surface = $ratio:1, expected >= 3:1 for UI",
            ratio >= 3.0,
        )
    }

    @Test
    fun `dark theme outlineVariant on surface meets WCAG UI`() {
        val ratio = contrastRatio(dark.outlineVariant, dark.surface)
        assertTrue(
            "outlineVariant on surface = $ratio:1, expected >= 3:1 for UI",
            ratio >= 3.0,
        )
    }
}
