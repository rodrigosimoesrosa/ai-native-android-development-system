package com.mirabilis.core.designsystem.component.extended

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mirabilis.core.designsystem.theme.MirabilisTheme
import com.mirabilis.core.designsystem.token.spacingSm

/**
 * A branded checkbox component that consumes design-system tokens exclusively.
 *
 * Supports three visual states: checked, unchecked, and disabled.
 * The disabled state uses 38 % / 12 % alpha tokens on foreground / background.
 *
 * Every variant is previewable in both light and dark themes (FR-019).
 *
 * @param checked Whether the checkbox is checked.
 * @param onCheckedChange Called when the value changes.
 * @param modifier Modifier for styling or layout positioning.
 * @param enabled Controls clickability and the disabled alpha treatment.
 */
@Composable
fun MirabilisCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    Checkbox(
        checked = checked,
        onCheckedChange = if (enabled) onCheckedChange else null,
        modifier = modifier,
        colors = CheckboxDefaults.colors(
            checkedColor = MaterialTheme.colorScheme.primary,
            uncheckedColor = if (enabled) uncheckedBorderColor else disabledBorderColor,
            disabledCheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledUncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
    )
}

@Preview(name = "Light")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisCheckboxLightPreview() {
    MirabilisTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Checked", modifier = Modifier.padding(end = 8.dp))
                MirabilisCheckbox(checked = true, onCheckedChange = {})
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Unchecked", modifier = Modifier.padding(end = 8.dp))
                MirabilisCheckbox(checked = false, onCheckedChange = {})
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled Checked", modifier = Modifier.padding(end = 8.dp))
                MirabilisCheckbox(checked = true, onCheckedChange = {}, enabled = false)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled Unchecked", modifier = Modifier.padding(end = 8.dp))
                MirabilisCheckbox(checked = false, onCheckedChange = {}, enabled = false)
            }
        }
    }
}

@Preview(name = "Dark")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisCheckboxDarkPreview() {
    MirabilisTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Checked", modifier = Modifier.padding(end = 8.dp))
                MirabilisCheckbox(checked = true, onCheckedChange = {})
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Unchecked", modifier = Modifier.padding(end = 8.dp))
                MirabilisCheckbox(checked = false, onCheckedChange = {})
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled Checked", modifier = Modifier.padding(end = 8.dp))
                MirabilisCheckbox(checked = true, onCheckedChange = {}, enabled = false)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled Unchecked", modifier = Modifier.padding(end = 8.dp))
                MirabilisCheckbox(checked = false, onCheckedChange = {}, enabled = false)
            }
        }
    }
}
