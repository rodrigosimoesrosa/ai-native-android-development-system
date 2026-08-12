package com.mirabilis.core.designsystem.component.extended

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mirabilis.core.designsystem.theme.MirabilisTheme
import com.mirabilis.core.designsystem.token.spacingSm

/**
 * A branded assist / filter chip component that consumes design-system tokens exclusively.
 *
 * Supports three visual states: selected, unselected, and disabled.
 * The selected state uses `primary` / `onPrimary`; the unselected state uses
 * `surfaceVariant` / `onSurfaceVariant` with an `outline` border at 60 % alpha.
 * The disabled state uses `onSurface` at 12 % alpha for the container and 38 % alpha for text.
 *
 * Every variant is previewable in both light and dark themes (FR-019).
 *
 * @param label The chip label text.
 * @param selected Whether the chip is selected (filter-chip semantics).
 * @param onClick Called when the user taps the chip.
 * @param modifier Modifier for styling or layout positioning.
 * @param enabled Controls clickability and the disabled alpha treatment.
 */
@Composable
fun MirabilisChip(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderStroke = if (selected) {
        null
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    }

    val disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    Surface(
        modifier = modifier
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = MaterialTheme.shapes.small,
        color = if (enabled) containerColor else disabledContainerColor,
        border = if (enabled) {
            borderStroke
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
        },
        shadowElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) contentColor else disabledContentColor,
        )
    }
}

@Preview(name = "Light")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisChipLightPreview() {
    MirabilisTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Selected", modifier = Modifier.padding(end = 8.dp))
                MirabilisChip(label = "Assist", onClick = {})
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Unselected", modifier = Modifier.padding(end = 8.dp))
                MirabilisChip(label = "Filter", onClick = {}, selected = false)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled Selected", modifier = Modifier.padding(end = 8.dp))
                MirabilisChip(label = "Disabled Selected", onClick = {}, enabled = false)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled Unselected", modifier = Modifier.padding(end = 8.dp))
                MirabilisChip(label = "Disabled Unselected", onClick = {}, enabled = false)
            }
        }
    }
}

@Preview(name = "Dark")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisChipDarkPreview() {
    MirabilisTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Selected", modifier = Modifier.padding(end = 8.dp))
                MirabilisChip(label = "Assist", onClick = {})
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Unselected", modifier = Modifier.padding(end = 8.dp))
                MirabilisChip(label = "Filter", onClick = {}, selected = false)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled Selected", modifier = Modifier.padding(end = 8.dp))
                MirabilisChip(label = "Disabled Selected", onClick = {}, enabled = false)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled Unselected", modifier = Modifier.padding(end = 8.dp))
                MirabilisChip(label = "Disabled Unselected", onClick = {}, enabled = false)
            }
        }
    }
}
