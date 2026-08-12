package com.mirabilis.core.designsystem

import androidx.compose.ui.graphics.Color
import com.mirabilis.core.designsystem.token.createMirabilisTypography
import com.mirabilis.core.designsystem.token.darkColorScheme
import com.mirabilis.core.designsystem.token.darkExtraColors
import com.mirabilis.core.designsystem.token.lightColorScheme
import com.mirabilis.core.designsystem.token.lightExtraColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenCompletenessTest {

    private val lightScheme = lightColorScheme()
    private val darkScheme = darkColorScheme()
    private val lightExtras = lightExtraColors()
    private val darkExtras = darkExtraColors()
    private val typography = createMirabilisTypography()

    @Test
    fun `all color roles must have light AND dark values`() {
        val roles = listOf(
            "primary" to { lightScheme.primary to darkScheme.primary },
            "onPrimary" to { lightScheme.onPrimary to darkScheme.onPrimary },
            "primaryContainer" to { lightScheme.primaryContainer to darkScheme.primaryContainer },
            "onPrimaryContainer" to { lightScheme.onPrimaryContainer to darkScheme.onPrimaryContainer },
            "secondary" to { lightScheme.secondary to darkScheme.secondary },
            "onSecondary" to { lightScheme.onSecondary to darkScheme.onSecondary },
            "secondaryContainer" to { lightScheme.secondaryContainer to darkScheme.secondaryContainer },
            "onSecondaryContainer" to { lightScheme.onSecondaryContainer to darkScheme.onSecondaryContainer },
            "tertiary" to { lightScheme.tertiary to darkScheme.tertiary },
            "onTertiary" to { lightScheme.onTertiary to darkScheme.onTertiary },
            "tertiaryContainer" to { lightScheme.tertiaryContainer to darkScheme.tertiaryContainer },
            "onTertiaryContainer" to { lightScheme.onTertiaryContainer to darkScheme.onTertiaryContainer },
            "error" to { lightScheme.error to darkScheme.error },
            "onError" to { lightScheme.onError to darkScheme.onError },
            "errorContainer" to { lightScheme.errorContainer to darkScheme.errorContainer },
            "onErrorContainer" to { lightScheme.onErrorContainer to darkScheme.onErrorContainer },
            "background" to { lightScheme.background to darkScheme.background },
            "onBackground" to { lightScheme.onBackground to darkScheme.onBackground },
            "surface" to { lightScheme.surface to darkScheme.surface },
            "onSurface" to { lightScheme.onSurface to darkScheme.onSurface },
            "surfaceVariant" to { lightScheme.surfaceVariant to darkScheme.surfaceVariant },
            "onSurfaceVariant" to { lightScheme.onSurfaceVariant to darkScheme.onSurfaceVariant },
            "outline" to { lightScheme.outline to darkScheme.outline },
            "outlineVariant" to { lightScheme.outlineVariant to darkScheme.outlineVariant },
            "surfaceContainer" to { lightScheme.surfaceContainer to darkScheme.surfaceContainer },
            "surfaceContainerHigh" to { lightScheme.surfaceContainerHigh to darkScheme.surfaceContainerHigh },
            "surfaceContainerLow" to { lightScheme.surfaceContainerLow to darkScheme.surfaceContainerLow },
            "surfaceContainerLowest" to { lightScheme.surfaceContainerLowest to darkScheme.surfaceContainerLowest },
            "surfaceBright" to { lightScheme.surfaceBright to darkScheme.surfaceBright },
            "surfaceDim" to { lightScheme.surfaceDim to darkScheme.surfaceDim },
            "inverseSurface" to { lightScheme.inverseSurface to darkScheme.inverseSurface },
            "inverseOnSurface" to { lightScheme.inverseOnSurface to darkScheme.inverseOnSurface },
            "inversePrimary" to { lightScheme.inversePrimary to darkScheme.inversePrimary },
        )

        val unspecifiedCount = roles.count { (_, getter) ->
            val (light, dark) = getter()
            light == Color.Unspecified || dark == Color.Unspecified
        }

        val unspecifiedRoles = roles.filter { (name, getter) ->
            val (light, dark) = getter()
            light == Color.Unspecified || dark == Color.Unspecified
        }.map { (name, _) -> name }

        assertTrue(
            "Expected 0 unspecified color roles but found $unspecifiedCount: $unspecifiedRoles",
            unspecifiedCount == 0,
        )
    }

    @Test
    fun `all extra color roles must have light AND dark values`() {
        assertNotNull("lightExtraColors.warning must not be null", lightExtras.warning)
        assertNotNull("lightExtraColors.onWarning must not be null", lightExtras.onWarning)
        assertNotNull("lightExtraColors.success must not be null", lightExtras.success)
        assertNotNull("lightExtraColors.onSuccess must not be null", lightExtras.onSuccess)

        assertNotNull("darkExtraColors.warning must not be null", darkExtras.warning)
        assertNotNull("darkExtraColors.onWarning must not be null", darkExtras.onWarning)
        assertNotNull("darkExtraColors.success must not be null", darkExtras.success)
        assertNotNull("darkExtraColors.onSuccess must not be null", darkExtras.onSuccess)

        assertTrue(
            "Extra colors must not be Color.Unspecified",
            lightExtras.warning != Color.Unspecified &&
                lightExtras.onWarning != Color.Unspecified &&
                lightExtras.success != Color.Unspecified &&
                lightExtras.onSuccess != Color.Unspecified &&
                darkExtras.warning != Color.Unspecified &&
                darkExtras.onWarning != Color.Unspecified &&
                darkExtras.success != Color.Unspecified &&
                darkExtras.onSuccess != Color.Unspecified,
        )
    }

    @Test
    fun `all 15 typography roles must exist`() {
        assertNotNull("displayLarge must exist", typography.displayLarge)
        assertNotNull("displayMedium must exist", typography.displayMedium)
        assertNotNull("displaySmall must exist", typography.displaySmall)
        assertNotNull("headlineLarge must exist", typography.headlineLarge)
        assertNotNull("headlineMedium must exist", typography.headlineMedium)
        assertNotNull("headlineSmall must exist", typography.headlineSmall)
        assertNotNull("titleLarge must exist", typography.titleLarge)
        assertNotNull("titleMedium must exist", typography.titleMedium)
        assertNotNull("titleSmall must exist", typography.titleSmall)
        assertNotNull("bodyLarge must exist", typography.bodyLarge)
        assertNotNull("bodyMedium must exist", typography.bodyMedium)
        assertNotNull("bodySmall must exist", typography.bodySmall)
        assertNotNull("labelLarge must exist", typography.labelLarge)
        assertNotNull("labelMedium must exist", typography.labelMedium)
        assertNotNull("labelSmall must exist", typography.labelSmall)

        val roles = listOf(
            typography.displayLarge,
            typography.displayMedium,
            typography.displaySmall,
            typography.headlineLarge,
            typography.headlineMedium,
            typography.headlineSmall,
            typography.titleLarge,
            typography.titleMedium,
            typography.titleSmall,
            typography.bodyLarge,
            typography.bodyMedium,
            typography.bodySmall,
            typography.labelLarge,
            typography.labelMedium,
            typography.labelSmall,
        )

        assertEquals("All 15 typography roles must be present", 15, roles.size)
    }

    @Test
    fun `typography roles must have non-zero font sizes`() {
        val roles = listOf(
            typography.displayLarge,
            typography.displayMedium,
            typography.displaySmall,
            typography.headlineLarge,
            typography.headlineMedium,
            typography.headlineSmall,
            typography.titleLarge,
            typography.titleMedium,
            typography.titleSmall,
            typography.bodyLarge,
            typography.bodyMedium,
            typography.bodySmall,
            typography.labelLarge,
            typography.labelMedium,
            typography.labelSmall,
        )

        roles.forEach { role ->
            assertTrue(
                "Typography role must have fontSize > 0 (fontSize=${role.fontSize.value}sp)",
                role.fontSize.value > 0f,
            )
        }
    }
}
