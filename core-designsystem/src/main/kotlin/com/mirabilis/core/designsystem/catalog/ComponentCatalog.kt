package com.mirabilis.core.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mirabilis.core.designsystem.component.MirabilisButton
import com.mirabilis.core.designsystem.component.MirabilisButtonStyle
import com.mirabilis.core.designsystem.component.MirabilisFab
import com.mirabilis.core.designsystem.component.MirabilisFabSize
import com.mirabilis.core.designsystem.component.MirabilisTextField
import com.mirabilis.core.designsystem.component.MirabilisText
import com.mirabilis.core.designsystem.component.extended.MirabilisCard
import com.mirabilis.core.designsystem.component.extended.MirabilisChip
import com.mirabilis.core.designsystem.component.extended.MirabilisCheckbox
import com.mirabilis.core.designsystem.component.extended.MirabilisRadioButton
import com.mirabilis.core.designsystem.component.extended.MirabilisSwitch
import com.mirabilis.core.designsystem.component.extended.MirabilisTopAppBar
import com.mirabilis.core.designsystem.theme.MirabilisTheme
import com.mirabilis.core.designsystem.token.darkColorScheme
import com.mirabilis.core.designsystem.token.darkExtraColors
import com.mirabilis.core.designsystem.token.lightColorScheme
import com.mirabilis.core.designsystem.token.lightExtraColors
import com.mirabilis.core.designsystem.token.spacingLg
import com.mirabilis.core.designsystem.token.spacingMd
import com.mirabilis.core.designsystem.token.spacingSm

/* ── Color-swatch helpers ──────────────────────────────────────────── */

private data class ColorRoleEntry(
    val label: String,
    val color: Color,
    val onColorLabel: String,
    val onColor: Color,
)

private fun buildLightColorEntries(): List<ColorRoleEntry> {
    val s = lightColorScheme()
    val extras = lightExtraColors()
    return listOf(
        ColorRoleEntry("primary", s.primary, "onPrimary", s.onPrimary),
        ColorRoleEntry("primaryContainer", s.primaryContainer, "onPrimaryContainer", s.onPrimaryContainer),
        ColorRoleEntry("secondary", s.secondary, "onSecondary", s.onSecondary),
        ColorRoleEntry("secondaryContainer", s.secondaryContainer, "onSecondaryContainer", s.onSecondaryContainer),
        ColorRoleEntry("tertiary", s.tertiary, "onTertiary", s.onTertiary),
        ColorRoleEntry("tertiaryContainer", s.tertiaryContainer, "onTertiaryContainer", s.onTertiaryContainer),
        ColorRoleEntry("error", s.error, "onError", s.onError),
        ColorRoleEntry("errorContainer", s.errorContainer, "onErrorContainer", s.onErrorContainer),
        ColorRoleEntry("background", s.background, "onBackground", s.onBackground),
        ColorRoleEntry("surface", s.surface, "onSurface", s.onSurface),
        ColorRoleEntry("surfaceVariant", s.surfaceVariant, "onSurfaceVariant", s.onSurfaceVariant),
        ColorRoleEntry("outline", s.outline, "outline", Color.Unspecified),
        ColorRoleEntry("outlineVariant", s.outlineVariant, "outlineVariant", Color.Unspecified),
        ColorRoleEntry("surfaceContainer", s.surfaceContainer, "onSurface", s.onSurface),
        ColorRoleEntry("surfaceContainerHigh", s.surfaceContainerHigh, "onSurface", s.onSurface),
        ColorRoleEntry("surfaceContainerLow", s.surfaceContainerLow, "onSurface", s.onSurface),
        ColorRoleEntry("surfaceContainerLowest", s.surfaceContainerLowest, "onSurface", s.onSurface),
        ColorRoleEntry("surfaceBright", s.surfaceBright, "onSurface", s.onSurface),
        ColorRoleEntry("surfaceDim", s.surfaceDim, "onSurface", s.onSurface),
        ColorRoleEntry("inverseSurface", s.inverseSurface, "inverseOnSurface", s.inverseOnSurface),
        ColorRoleEntry("inversePrimary", s.inversePrimary, "inverseOnSurface", s.inverseOnSurface),
        ColorRoleEntry("warning", extras.warning, "onWarning", extras.onWarning),
        ColorRoleEntry("success", extras.success, "onSuccess", extras.onSuccess),
    )
}

@Suppress("UnusedPrivateMember")
private fun buildDarkColorEntries(): List<ColorRoleEntry> {
    val s = darkColorScheme()
    val extras = darkExtraColors()
    return listOf(
        ColorRoleEntry("primary", s.primary, "onPrimary", s.onPrimary),
        ColorRoleEntry("primaryContainer", s.primaryContainer, "onPrimaryContainer", s.onPrimaryContainer),
        ColorRoleEntry("secondary", s.secondary, "onSecondary", s.onSecondary),
        ColorRoleEntry("secondaryContainer", s.secondaryContainer, "onSecondaryContainer", s.onSecondaryContainer),
        ColorRoleEntry("tertiary", s.tertiary, "onTertiary", s.onTertiary),
        ColorRoleEntry("tertiaryContainer", s.tertiaryContainer, "onTertiaryContainer", s.onTertiaryContainer),
        ColorRoleEntry("error", s.error, "onError", s.onError),
        ColorRoleEntry("errorContainer", s.errorContainer, "onErrorContainer", s.onErrorContainer),
        ColorRoleEntry("background", s.background, "onBackground", s.onBackground),
        ColorRoleEntry("surface", s.surface, "onSurface", s.onSurface),
        ColorRoleEntry("surfaceVariant", s.surfaceVariant, "onSurfaceVariant", s.onSurfaceVariant),
        ColorRoleEntry("outline", s.outline, "outline", Color.Unspecified),
        ColorRoleEntry("outlineVariant", s.outlineVariant, "outlineVariant", Color.Unspecified),
        ColorRoleEntry("surfaceContainer", s.surfaceContainer, "onSurface", s.onSurface),
        ColorRoleEntry("surfaceContainerHigh", s.surfaceContainerHigh, "onSurface", s.onSurface),
        ColorRoleEntry("surfaceContainerLow", s.surfaceContainerLow, "onSurface", s.onSurface),
        ColorRoleEntry("surfaceContainerLowest", s.surfaceContainerLowest, "onSurface", s.onSurface),
        ColorRoleEntry("surfaceBright", s.surfaceBright, "onSurface", s.onSurface),
        ColorRoleEntry("surfaceDim", s.surfaceDim, "onSurface", s.onSurface),
        ColorRoleEntry("inverseSurface", s.inverseSurface, "inverseOnSurface", s.inverseOnSurface),
        ColorRoleEntry("inversePrimary", s.inversePrimary, "inverseOnSurface", s.inverseOnSurface),
        ColorRoleEntry("warning", extras.warning, "onWarning", extras.onWarning),
        ColorRoleEntry("success", extras.success, "onSuccess", extras.onSuccess),
    )
}

@Composable
private fun ColorSwatchRow(entry: ColorRoleEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(entry.color, MaterialTheme.shapes.small)
                .padding(2.dp),
        ) {
            if (entry.onColor != Color.Unspecified) {
                Text(
                    text = "Aa",
                    color = entry.onColor,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        Column {
            Text(
                text = entry.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (entry.onColor != Color.Unspecified) {
                Text(
                    text = entry.onColorLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ColorSwatchSection(entries: List<ColorRoleEntry>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "Color Tokens",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            entries.forEach { entry ->
                ColorSwatchRow(entry)
            }
        }
    }
}

/* ── Typography preview helpers ─────────────────────────────────────── */

private data class TypographyRoleEntry(
    val name: String,
    val style: androidx.compose.ui.text.TextStyle,
)

@Composable
private fun TypographyRow(entry: TypographyRoleEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = entry.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = "The quick brown fox",
            style = entry.style,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f),
        )
    }
}

@Composable
private fun TypographySection() {
    val t = MaterialTheme.typography
    val entries = listOf(
        TypographyRoleEntry("displayLarge", t.displayLarge),
        TypographyRoleEntry("displayMedium", t.displayMedium),
        TypographyRoleEntry("displaySmall", t.displaySmall),
        TypographyRoleEntry("headlineLarge", t.headlineLarge),
        TypographyRoleEntry("headlineMedium", t.headlineMedium),
        TypographyRoleEntry("headlineSmall", t.headlineSmall),
        TypographyRoleEntry("titleLarge", t.titleLarge),
        TypographyRoleEntry("titleMedium", t.titleMedium),
        TypographyRoleEntry("titleSmall", t.titleSmall),
        TypographyRoleEntry("bodyLarge", t.bodyLarge),
        TypographyRoleEntry("bodyMedium", t.bodyMedium),
        TypographyRoleEntry("bodySmall", t.bodySmall),
        TypographyRoleEntry("labelLarge", t.labelLarge),
        TypographyRoleEntry("labelMedium", t.labelMedium),
        TypographyRoleEntry("labelSmall", t.labelSmall),
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "Typography Tokens",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            entries.forEach { entry ->
                TypographyRow(entry)
            }
        }
    }
}

/* ── Token section: full gallery (light + dark) ─────────────────────── */

@Composable
private fun TokenSection() {
    val lightEntries = buildLightColorEntries()
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        ColorSwatchSection(lightEntries)
        TypographySection()
    }
}

/* ── US1: Core component sections ──────────────────────────────────── */

@Composable
private fun ButtonsSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "Buttons",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Primary (enabled / disabled)", style = MaterialTheme.typography.bodySmall)
                MirabilisButton("Primary", onClick = {})
                MirabilisButton("Disabled Primary", onClick = {}, enabled = false)
                Spacer(modifier = Modifier.size(0.dp))
                Text("Secondary (enabled / disabled)", style = MaterialTheme.typography.bodySmall)
                MirabilisButton("Secondary", onClick = {}, style = MirabilisButtonStyle.Secondary)
                MirabilisButton(
                    "Disabled Secondary",
                    onClick = {},
                    style = MirabilisButtonStyle.Secondary,
                    enabled = false,
                )
                Spacer(modifier = Modifier.size(0.dp))
                Text("Text (enabled / disabled)", style = MaterialTheme.typography.bodySmall)
                MirabilisButton("Text", onClick = {}, style = MirabilisButtonStyle.Text)
                MirabilisButton("Disabled Text", onClick = {}, style = MirabilisButtonStyle.Text, enabled = false)
                Spacer(modifier = Modifier.size(0.dp))
                Text("Long label (ellipsis)", style = MaterialTheme.typography.bodySmall)
                MirabilisButton(
                    "Very long label that should truncate with ellipsis",
                    onClick = {},
                    modifier = Modifier.width(200.dp),
                )
            }
        }
    }
}

@Composable
private fun TextSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "Text",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                MirabilisText("Body Large (default)", style = MaterialTheme.typography.bodyLarge)
                MirabilisText("Body Medium", style = MaterialTheme.typography.bodyMedium)
                MirabilisText("Body Small", style = MaterialTheme.typography.bodySmall)
                MirabilisText("Label Large", style = MaterialTheme.typography.labelLarge)
                MirabilisText("Label Medium", style = MaterialTheme.typography.labelMedium)
                MirabilisText("Label Small", style = MaterialTheme.typography.labelSmall)
                MirabilisText(
                    "Colored text (primary role)",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                )
                MirabilisText(
                    "Error text (error role)",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
                MirabilisText(
                    "Long text that exceeds maxLines",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun TextFieldSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "Text Field",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                MirabilisTextField(
                    value = "",
                    onValueChange = {},
                    label = "Empty unfilled",
                )
                MirabilisTextField(
                    value = "Filled value",
                    onValueChange = {},
                    label = "Filled",
                )
                MirabilisTextField(
                    value = "",
                    onValueChange = {},
                    label = "With placeholder",
                    placeholder = "Type here...",
                )
                MirabilisTextField(
                    value = "With helper",
                    onValueChange = {},
                    label = "Helper text",
                    helperText = "This is helper text",
                )
                MirabilisTextField(
                    value = "Error state",
                    onValueChange = {},
                    label = "Error",
                    isError = true,
                    errorText = "This field has an error",
                )
                MirabilisTextField(
                    value = "",
                    onValueChange = {},
                    label = "Disabled",
                    enabled = false,
                )
            }
        }
    }
}

/* ── US3: Extended component sections ──────────────────────────────── */

@Composable
private fun FabSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "Floating Action Button",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Small (enabled / disabled)", style = MaterialTheme.typography.bodySmall)
                MirabilisFab(onClick = {}, icon = { Text("+") })
                MirabilisFab(
                    onClick = {},
                    icon = { Text("+") },
                    enabled = false,
                    size = MirabilisFabSize.Small,
                )
                Spacer(modifier = Modifier.size(0.dp))
                Text("Regular (enabled / disabled)", style = MaterialTheme.typography.bodySmall)
                MirabilisFab(onClick = {}, icon = { Text("+") })
                MirabilisFab(onClick = {}, icon = { Text("+") }, enabled = false)
                Spacer(modifier = Modifier.size(0.dp))
                Text("Extended (enabled / disabled)", style = MaterialTheme.typography.bodySmall)
                MirabilisFab(onClick = {}, icon = { Text("+") }, text = "Sync")
                MirabilisFab(
                    onClick = {},
                    icon = { Text("+") },
                    text = "Sync",
                    enabled = false,
                )
            }
        }
    }
}

@Composable
private fun SwitchSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "Switch",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enabled ON", modifier = Modifier.padding(end = 8.dp))
                    MirabilisSwitch(checked = true, onCheckedChange = {})
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enabled OFF", modifier = Modifier.padding(end = 8.dp))
                    MirabilisSwitch(checked = false, onCheckedChange = {})
                }
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
}

@Composable
private fun CheckboxSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "Checkbox",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Checked", modifier = Modifier.padding(end = 8.dp))
                    MirabilisCheckbox(checked = true, onCheckedChange = {})
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Unchecked", modifier = Modifier.padding(end = 8.dp))
                    MirabilisCheckbox(checked = false, onCheckedChange = {})
                }
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
}

@Composable
private fun RadioButtonSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "Radio Button",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Selected", modifier = Modifier.padding(end = 8.dp))
                    MirabilisRadioButton(selected = true, onClick = {})
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Unselected", modifier = Modifier.padding(end = 8.dp))
                    MirabilisRadioButton(selected = false, onClick = {})
                }
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
}

@Composable
private fun CardSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "Card",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Elevated", style = MaterialTheme.typography.bodySmall)
                MirabilisCard {
                    Column(modifier = Modifier.padding(spacingMd)) {
                        Text("Elevated content", style = MaterialTheme.typography.bodyMedium)
                        Text("Short description.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.size(0.dp))
                Text("Outlined", style = MaterialTheme.typography.bodySmall)
                MirabilisCard(outlined = true) {
                    Column(modifier = Modifier.padding(spacingMd)) {
                        Text("Outlined content", style = MaterialTheme.typography.bodyMedium)
                        Text("Short description.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "Chip",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Selected", modifier = Modifier.padding(end = 8.dp))
                    MirabilisChip(label = "Assist", onClick = {})
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Unselected", modifier = Modifier.padding(end = 8.dp))
                    MirabilisChip(label = "Filter", onClick = {}, selected = false)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Disabled Selected", modifier = Modifier.padding(end = 8.dp))
                    MirabilisChip(label = "Disabled", onClick = {}, enabled = false)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Disabled Unselected", modifier = Modifier.padding(end = 8.dp))
                    MirabilisChip(label = "Disabled", onClick = {}, selected = false, enabled = false)
                }
            }
        }
    }
}

@Composable
private fun TopAppBarSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "Top App Bar",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("With navigation icon", style = MaterialTheme.typography.bodySmall)
                MirabilisTopAppBar(
                    title = "Screen Title",
                    navigationIcon = {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    },
                )
                Spacer(modifier = Modifier.size(0.dp))
                Text("With actions", style = MaterialTheme.typography.bodySmall)
                MirabilisTopAppBar(
                    title = "Screen Title",
                    actions = {
                        Text("Action", style = MaterialTheme.typography.bodyMedium)
                    },
                )
                Spacer(modifier = Modifier.size(0.dp))
                Text("Navigation + actions", style = MaterialTheme.typography.bodySmall)
                MirabilisTopAppBar(
                    title = "Screen Title",
                    navigationIcon = {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    },
                    actions = {
                        Text("More", style = MaterialTheme.typography.bodyMedium)
                    },
                )
                Spacer(modifier = Modifier.size(0.dp))
                Text("Minimal (no icon, no actions)", style = MaterialTheme.typography.bodySmall)
                MirabilisTopAppBar(title = "Minimal Title")
            }
        }
    }
}

@Composable
private fun DialogSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "Dialog",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp),
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Confirm only", style = MaterialTheme.typography.bodySmall)
                DialogPreviewContent(
                    title = "Delete Item",
                    text = "Are you sure you want to delete this item? This action cannot be undone.",
                    confirmButton = {
                        Button(onClick = {}) { Text("Delete") }
                    },
                    dismissButton = null,
                )
                Spacer(modifier = Modifier.size(0.dp))
                Text("Confirm + Dismiss", style = MaterialTheme.typography.bodySmall)
                DialogPreviewContent(
                    title = "Save Changes",
                    text = "You have unsaved changes. Do you want to save before leaving?",
                    confirmButton = {
                        Button(onClick = {}) { Text("Save") }
                    },
                    dismissButton = {
                        Button(onClick = {}) { Text("Cancel") }
                    },
                )
            }
        }
    }
}

@Composable
private fun DialogPreviewContent(
    title: String,
    text: String,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column {
            Column(modifier = Modifier.padding(horizontal = spacingLg, vertical = spacingMd)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 3,
                )
                Spacer(modifier = Modifier.width(0.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 6,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacingLg, vertical = spacingMd),
                horizontalArrangement = Arrangement.spacedBy(spacingSm),
                verticalAlignment = Alignment.Bottom,
            ) {
                if (dismissButton != null) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        dismissButton()
                    }
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    confirmButton()
                }
            }
        }
    }
}

/* ── Component gallery section ──────────────────────────────────────── */

@Composable
private fun ComponentGallerySection() {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        ButtonsSection()
        TextSection()
        TextFieldSection()
        FabSection()
        SwitchSection()
        CheckboxSection()
        RadioButtonSection()
        CardSection()
        ChipSection()
        TopAppBarSection()
        DialogSection()
    }
}

/* ── Previews ──────────────────────────────────────────────────────── */

@Preview(name = "Light Theme", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Composable
@Suppress("UnusedPrivateMember")
private fun PreviewLight() {
    MirabilisTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                TokenSection()
                ComponentGallerySection()
            }
        }
    }
}

@Preview(name = "Dark Theme", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
@Suppress("UnusedPrivateMember")
private fun PreviewDark() {
    MirabilisTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                TokenSection()
                ComponentGallerySection()
            }
        }
    }
}
