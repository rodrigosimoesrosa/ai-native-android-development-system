package com.mirabilis.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.mirabilis.core.designsystem.theme.MirabilisTheme
import com.mirabilis.core.designsystem.token.spacingLg
import com.mirabilis.core.designsystem.token.spacingSm

/**
 * A branded text component that consumes design-system tokens exclusively.
 *
 * Bound to the typography scale + color roles; respects system font scale
 * (FR-016). When [color] is unspecified, defaults to the appropriate
 * [MaterialTheme.colorScheme.onSurface] role for the current surface.
 *
 * Every typography role is previewable in both light and dark themes (FR-019).
 *
 * @param text Displayed text.
 * @param modifier Modifier for styling or layout positioning.
 * @param style Typography role (defaults to [MaterialTheme.typography.bodyLarge]).
 * @param color Color override; falls back to the color role for the surface when unspecified.
 * @param maxLines Maximum lines before truncation (default: no limit).
 */
@Composable
fun MirabilisText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current.merge(
        MaterialTheme.typography.bodyLarge,
    ),
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = if (color == Color.Unspecified) {
            MaterialTheme.colorScheme.onSurface
        } else {
            color
        },
        maxLines = maxLines,
    )
}

@Preview(name = "Light")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisTextLightPreview() {
    MirabilisTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(spacingLg),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            MirabilisText("Display Large", style = MaterialTheme.typography.displayLarge)
            MirabilisText("Headline Large", style = MaterialTheme.typography.headlineLarge)
            MirabilisText("Title Large", style = MaterialTheme.typography.titleLarge)
            MirabilisText("Body Large (default)", style = MaterialTheme.typography.bodyLarge)
            MirabilisText("Body Medium", style = MaterialTheme.typography.bodyMedium)
            MirabilisText("Body Small", style = MaterialTheme.typography.bodySmall)
            MirabilisText("Label Large", style = MaterialTheme.typography.labelLarge)
            MirabilisText("Label Medium", style = MaterialTheme.typography.labelMedium)
            MirabilisText("Label Small", style = MaterialTheme.typography.labelSmall)
            MirabilisText(
                "Long text that exceeds maxLines and should truncate",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
            )
            MirabilisText(
                "Colored text using the primary role",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
            )
            MirabilisText(
                "Error text using the error role",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview(name = "Dark")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisTextDarkPreview() {
    MirabilisTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(spacingLg),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            MirabilisText("Display Large", style = MaterialTheme.typography.displayLarge)
            MirabilisText("Headline Large", style = MaterialTheme.typography.headlineLarge)
            MirabilisText("Title Large", style = MaterialTheme.typography.titleLarge)
            MirabilisText("Body Large (default)", style = MaterialTheme.typography.bodyLarge)
            MirabilisText("Body Medium", style = MaterialTheme.typography.bodyMedium)
            MirabilisText("Body Small", style = MaterialTheme.typography.bodySmall)
            MirabilisText("Label Large", style = MaterialTheme.typography.labelLarge)
            MirabilisText("Label Medium", style = MaterialTheme.typography.labelMedium)
            MirabilisText("Label Small", style = MaterialTheme.typography.labelSmall)
            MirabilisText(
                "Long text that exceeds maxLines and should truncate",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
            )
            MirabilisText(
                "Colored text using the primary role",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
            )
            MirabilisText(
                "Error text using the error role",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
