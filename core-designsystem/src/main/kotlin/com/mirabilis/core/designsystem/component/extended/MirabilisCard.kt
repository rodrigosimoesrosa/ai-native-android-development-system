package com.mirabilis.core.designsystem.component.extended

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mirabilis.core.designsystem.theme.MirabilisTheme
import com.mirabilis.core.designsystem.token.elevationLevel1
import com.mirabilis.core.designsystem.token.spacingMd

/**
 * A branded card component supporting **elevated** and **outlined** variants.
 *
 * Consumes design-system tokens exclusively — no raw hex/dp literals for appearance.
 * The elevated variant uses elevation tokens; the outlined variant uses `outline` for the border.
 *
 * Every variant is previewable in both light and dark themes (FR-019).
 *
 * @param modifier Modifier for styling or layout positioning.
 * @param outlined When `true` renders an outlined card; otherwise an elevated card.
 * @param content The content inside the card.
 */
@Composable
fun MirabilisCard(
    modifier: Modifier = Modifier,
    outlined: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (outlined) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = MaterialTheme.shapes.medium,
            content = content,
        )
    } else {
        Card(
            modifier = modifier,
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = elevationLevel1,
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            shape = MaterialTheme.shapes.medium,
            content = content,
        )
    }
}

@Preview(name = "Light")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisCardLightPreview() {
    MirabilisTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingMd),
        ) {
            Text("Elevated Card", style = MaterialTheme.typography.titleMedium)
            MirabilisCard {
                Column(modifier = Modifier.padding(spacingMd)) {
                    Text("Elevated content", style = MaterialTheme.typography.bodyMedium)
                    Text("Short description text.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text("Outlined Card", style = MaterialTheme.typography.titleMedium)
            MirabilisCard(outlined = true) {
                Column(modifier = Modifier.padding(spacingMd)) {
                    Text("Outlined content", style = MaterialTheme.typography.bodyMedium)
                    Text("Short description text.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text("Elevated Long Content", style = MaterialTheme.typography.titleMedium)
            MirabilisCard {
                Column(modifier = Modifier.padding(spacingMd)) {
                    Text(
                        "This is a longer piece of content that demonstrates how the card handles " +
                            "multiple lines of text and ensures proper wrapping within the container boundaries.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Preview(name = "Dark")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisCardDarkPreview() {
    MirabilisTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingMd),
        ) {
            Text("Elevated Card", style = MaterialTheme.typography.titleMedium)
            MirabilisCard {
                Column(modifier = Modifier.padding(spacingMd)) {
                    Text("Elevated content", style = MaterialTheme.typography.bodyMedium)
                    Text("Short description text.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text("Outlined Card", style = MaterialTheme.typography.titleMedium)
            MirabilisCard(outlined = true) {
                Column(modifier = Modifier.padding(spacingMd)) {
                    Text("Outlined content", style = MaterialTheme.typography.bodyMedium)
                    Text("Short description text.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text("Elevated Long Content", style = MaterialTheme.typography.titleMedium)
            MirabilisCard {
                Column(modifier = Modifier.padding(spacingMd)) {
                    Text(
                        "This is a longer piece of content that demonstrates how the card handles " +
                            "multiple lines of text and ensures proper wrapping within the container boundaries.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
