package com.mirabilis.core.designsystem.component.extended

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mirabilis.core.designsystem.theme.MirabilisTheme
import com.mirabilis.core.designsystem.token.spacingSm

/**
 * A branded radio button component that consumes design-system tokens exclusively.
 *
 * Supports three visual states: selected, unselected, and disabled.
 * The selected state uses `primary`; the unselected state uses `outline` at 60 % alpha.
 * The disabled state uses `onSurface` at 38 % alpha for both selected and unselected.
 *
 * Every variant is previewable in both light and dark themes (FR-019).
 *
 * @param selected Whether this radio button is selected.
 * @param onClick Called when the user taps the radio button.
 * @param modifier Modifier for styling or layout positioning.
 * @param enabled Controls clickability and the disabled alpha treatment.
 */
@Composable
fun MirabilisRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val unselectedColor = if (enabled) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    RadioButton(
        selected = selected,
        onClick = if (enabled) onClick else null,
        modifier = modifier,
        colors = RadioButtonDefaults.colors(
            selectedColor = MaterialTheme.colorScheme.primary,
            unselectedColor = unselectedColor,
            disabledSelectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledUnselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
    )
}

@Preview(name = "Light")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisRadioButtonLightPreview() {
    MirabilisTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Selected", modifier = Modifier.padding(end = 8.dp))
                MirabilisRadioButton(selected = true, onClick = {})
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Unselected", modifier = Modifier.padding(end = 8.dp))
                MirabilisRadioButton(selected = false, onClick = {})
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled Selected", modifier = Modifier.padding(end = 8.dp))
                MirabilisRadioButton(selected = true, onClick = {}, enabled = false)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled Unselected", modifier = Modifier.padding(end = 8.dp))
                MirabilisRadioButton(selected = false, onClick = {}, enabled = false)
            }
        }
    }
}

@Preview(name = "Dark")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisRadioButtonDarkPreview() {
    MirabilisTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Selected", modifier = Modifier.padding(end = 8.dp))
                MirabilisRadioButton(selected = true, onClick = {})
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Unselected", modifier = Modifier.padding(end = 8.dp))
                MirabilisRadioButton(selected = false, onClick = {})
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled Selected", modifier = Modifier.padding(end = 8.dp))
                MirabilisRadioButton(selected = true, onClick = {}, enabled = false)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disabled Unselected", modifier = Modifier.padding(end = 8.dp))
                MirabilisRadioButton(selected = false, onClick = {}, enabled = false)
            }
        }
    }
}
