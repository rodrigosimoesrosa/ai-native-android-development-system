package com.mirabilis.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mirabilis.core.designsystem.theme.MirabilisTheme
import com.mirabilis.core.designsystem.token.spacingSm

/** Size variants for the floating action button. */
@Suppress("MatchingDeclarationName")
enum class MirabilisFabSize {
    Small,
    Regular,
}

/**
 * A branded floating action button supporting Small, Regular, and Extended sizes.
 *
 * When [text] is non-null the component renders as an Extended FAB with
 * both icon and label.  Supports enabled, pressed, and disabled visual states;
 * the disabled state uses 38 % / 12 % alpha tokens on foreground / background.
 *
 * Every variant is previewable in both light and dark themes (FR-019).
 *
 * @param onClick Callback invoked on click.
 * @param icon Icon content displayed inside the FAB circle.
 * @param modifier Modifier for styling or layout positioning.
 * @param size Size variant: Small or Regular (default).  When [text] is non-null
 *   the component renders as an Extended FAB regardless of this value.
 * @param text Optional label text.  When non-null the FAB renders in extended
 *   form with icon + label side-by-side.
 * @param enabled Controls clickability and the disabled alpha treatment.
 */
@Composable
fun MirabilisFab(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    size: MirabilisFabSize = MirabilisFabSize.Regular,
    text: String? = null,
    enabled: Boolean = true,
) {
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val disabledContainerColor = containerColor.copy(alpha = 0.38f)
    val disabledContentColor = contentColor.copy(alpha = 0.12f)

    val isExtended = text != null

    if (isExtended) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            icon = icon,
            text = {
                Text(
                    text = text!!,
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            modifier = modifier,
            containerColor = if (enabled) containerColor else disabledContainerColor,
            contentColor = if (enabled) contentColor else disabledContentColor,
            elevation = FloatingActionButtonDefaults.elevation(),
        )
    } else {
        when (size) {
            MirabilisFabSize.Small -> {
                SmallFloatingActionButton(
                    onClick = onClick,
                    modifier = modifier,
                    containerColor = if (enabled) containerColor else disabledContainerColor,
                    contentColor = if (enabled) contentColor else disabledContentColor,
                    elevation = FloatingActionButtonDefaults.elevation(),
                    content = icon,
                )
            }
            MirabilisFabSize.Regular -> {
                FloatingActionButton(
                    onClick = onClick,
                    modifier = modifier,
                    containerColor = if (enabled) containerColor else disabledContainerColor,
                    contentColor = if (enabled) contentColor else disabledContentColor,
                    elevation = FloatingActionButtonDefaults.elevation(),
                    content = icon,
                )
            }
        }
    }
}

@Preview(name = "Light")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisFabLightPreview() {
    MirabilisTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            // Small FAB (enabled / disabled)
            MirabilisFab(
                onClick = {},
                icon = { Text("+") },
            )
            Spacer(modifier = Modifier.width(16.dp))
            MirabilisFab(
                onClick = {},
                icon = { Text("+") },
                enabled = false,
                size = MirabilisFabSize.Small,
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Regular FAB (enabled / disabled)
            MirabilisFab(
                onClick = {},
                icon = { Text("+") },
            )
            Spacer(modifier = Modifier.width(16.dp))
            MirabilisFab(
                onClick = {},
                icon = { Text("+") },
                enabled = false,
            )
            Spacer(modifier = Modifier.width(16.dp))
            // Extended FAB (enabled / disabled)
            MirabilisFab(
                onClick = {},
                icon = { Text("+") },
                text = "Sync",
            )
            Spacer(modifier = Modifier.width(16.dp))
            MirabilisFab(
                onClick = {},
                icon = { Text("+") },
                text = "Sync",
                enabled = false,
            )
        }
    }
}

@Preview(name = "Dark")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisFabDarkPreview() {
    MirabilisTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            MirabilisFab(
                onClick = {},
                icon = { Text("+") },
                size = MirabilisFabSize.Small,
            )
            Spacer(modifier = Modifier.width(16.dp))
            MirabilisFab(
                onClick = {},
                icon = { Text("+") },
                enabled = false,
                size = MirabilisFabSize.Small,
            )
            Spacer(modifier = Modifier.width(16.dp))
            MirabilisFab(
                onClick = {},
                icon = { Text("+") },
            )
            Spacer(modifier = Modifier.width(16.dp))
            MirabilisFab(
                onClick = {},
                icon = { Text("+") },
                enabled = false,
            )
            Spacer(modifier = Modifier.width(16.dp))
            MirabilisFab(
                onClick = {},
                icon = { Text("+") },
                text = "Sync",
            )
            Spacer(modifier = Modifier.width(16.dp))
            MirabilisFab(
                onClick = {},
                icon = { Text("+") },
                text = "Sync",
                enabled = false,
            )
        }
    }
}
