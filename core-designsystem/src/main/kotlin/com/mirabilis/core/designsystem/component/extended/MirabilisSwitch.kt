package com.mirabilis.core.designsystem.component.extended

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mirabilis.core.designsystem.theme.MirabilisTheme
import com.mirabilis.core.designsystem.token.spacingSm

/**
 * A branded switch component that consumes design-system tokens exclusively.
 *
 * Supports three visual states: checked, unchecked, and disabled.
 * The disabled state uses 38 % / 12 % alpha tokens on foreground / background.
 *
 * Every variant is previewable in both light and dark themes (FR-019).
 *
 * @param checked Whether the switch is on.
 * @param onCheckedChange Called when the value changes.
 * @param modifier Modifier for styling or layout positioning.
 * @param enabled Controls clickability and the disabled alpha treatment.
 */
@Composable
fun MirabilisSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val thumbColor = if (checked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    val trackColor = if (checked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    }

    val disabledThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val disabledTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)

    Switch(
        checked = checked,
        onCheckedChange = if (enabled) onCheckedChange else null,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = if (enabled) thumbColor else disabledThumbColor,
            checkedTrackColor = if (enabled) trackColor else disabledTrackColor,
            uncheckedThumbColor = if (enabled) thumbColor else disabledThumbColor,
            uncheckedTrackColor = if (enabled) trackColor else disabledTrackColor,
            disabledCheckedThumbColor = disabledThumbColor,
            disabledCheckedTrackColor = disabledTrackColor,
            disabledUncheckedThumbColor = disabledThumbColor,
            disabledUncheckedTrackColor = disabledTrackColor,
        ),
    )
}

@Preview(name = "Light")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisSwitchLightPreview() {
    MirabilisTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled ON", modifier = Modifier.padding(end = 8.dp))
                MirabilisSwitch(checked = true, onCheckedChange = {})
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled OFF", modifier = Modifier.padding(end = 8.dp))
                MirabilisSwitch(checked = false, onCheckedChange = {})
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled ON", modifier = Modifier.padding(end = 8.dp))
                MirabilisSwitch(checked = true, onCheckedChange = {}, enabled = false)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled OFF", modifier = Modifier.padding(end = 8.dp))
                MirabilisSwitch(checked = false, onCheckedChange = {}, enabled = false)
            }
        }
    }
}

@Preview(name = "Dark")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisSwitchDarkPreview() {
    MirabilisTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled ON", modifier = Modifier.padding(end = 8.dp))
                MirabilisSwitch(checked = true, onCheckedChange = {})
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Enabled OFF", modifier = Modifier.padding(end = 8.dp))
                MirabilisSwitch(checked = false, onCheckedChange = {})
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled ON", modifier = Modifier.padding(end = 8.dp))
                MirabilisSwitch(checked = true, onCheckedChange = {}, enabled = false)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled OFF", modifier = Modifier.padding(end = 8.dp))
                MirabilisSwitch(checked = false, onCheckedChange = {}, enabled = false)
            }
        }
    }
}
