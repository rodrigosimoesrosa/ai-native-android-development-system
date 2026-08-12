package com.mirabilis.core.designsystem.component.extended

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mirabilis.core.designsystem.theme.MirabilisTheme
import com.mirabilis.core.designsystem.token.spacingLg
import com.mirabilis.core.designsystem.token.spacingMd
import com.mirabilis.core.designsystem.token.spacingSm
import com.mirabilis.core.designsystem.token.spacingXl

/**
 * A branded dialog component that displays a title, body text, and action buttons.
 *
 * Built entirely from design-system tokens (colors, typography, spacing, shapes, elevation).
 * The title uses `titleLarge`; the body uses `bodyMedium`; buttons use `labelLarge`.
 * The dialog body text truncates with ellipsis when content exceeds available space.
 *
 * Every variant is previewable in both light and dark themes (FR-019).
 *
 * @param onDismissRequest Called when the user dismisses the dialog (back, scrim, or dismiss button).
 * @param title The dialog title text (truncated with ellipsis if long).
 * @param text The dialog body text (truncated with ellipsis if long).
 * @param modifier Modifier for styling or layout positioning.
 * @param confirmButton The confirm action composable (typically a `MirabilisButton`).
 * @param dismissButton Optional dismiss action composable (e.g. "Cancel").
 */
@Composable
fun MirabilisDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = spacingXl),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column {
                Column(
                    modifier = Modifier.padding(
                        horizontal = spacingLg,
                        vertical = spacingMd,
                    ),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.width(0.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacingLg, vertical = spacingMd),
                    horizontalArrangement = Arrangement.spacedBy(spacingSm),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    dismissButton?.let {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            it()
                        }
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        confirmButton()
                    }
                }
            }
        }
    }
}

@Preview(name = "Light")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisDialogLightPreview() {
    MirabilisTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingLg),
        ) {
            Text("Dialog with Confirm Only", style = MaterialTheme.typography.titleMedium)
            MirabilisDialog(
                onDismissRequest = {},
                title = "Delete Item",
                text = "Are you sure you want to delete this item?" +
                    " This action cannot be undone.",
                confirmButton = {
                    Button(onClick = {}) {
                        Text("Delete")
                    }
                },
            )
            Text("Dialog with Confirm + Dismiss", style = MaterialTheme.typography.titleMedium)
            MirabilisDialog(
                onDismissRequest = {},
                title = "Save Changes",
                text = "You have unsaved changes." +
                    " Do you want to save before leaving?",
                dismissButton = {
                    Button(onClick = {}) {
                        Text("Cancel")
                    }
                },
                confirmButton = {
                    Button(onClick = {}) {
                        Text("Save")
                    }
                },
            )
            Text("Long Title Dialog", style = MaterialTheme.typography.titleMedium)
            MirabilisDialog(
                onDismissRequest = {},
                title = "This is a very long title that should be truncated " +
                    "with an ellipsis to demonstrate the truncation behavior",
                text = "This is a longer piece of dialog body text that " +
                    "demonstrates how the dialog handles multiple lines of text " +
                    "and ensures proper wrapping within the container boundaries.",
                confirmButton = {
                    Button(onClick = {}) {
                        Text("Confirm")
                    }
                },
            )
        }
    }
}

@Preview(name = "Dark")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisDialogDarkPreview() {
    MirabilisTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingLg),
        ) {
            Text("Dialog with Confirm Only", style = MaterialTheme.typography.titleMedium)
            MirabilisDialog(
                onDismissRequest = {},
                title = "Delete Item",
                text = "Are you sure you want to delete this item?" +
                    " This action cannot be undone.",
                confirmButton = {
                    Button(onClick = {}) {
                        Text("Delete")
                    }
                },
            )
            Text("Dialog with Confirm + Dismiss", style = MaterialTheme.typography.titleMedium)
            MirabilisDialog(
                onDismissRequest = {},
                title = "Save Changes",
                text = "You have unsaved changes." +
                    " Do you want to save before leaving?",
                dismissButton = {
                    Button(onClick = {}) {
                        Text("Cancel")
                    }
                },
                confirmButton = {
                    Button(onClick = {}) {
                        Text("Save")
                    }
                },
            )
            Text("Long Title Dialog", style = MaterialTheme.typography.titleMedium)
            MirabilisDialog(
                onDismissRequest = {},
                title = "This is a very long title that should be truncated " +
                    "with an ellipsis to demonstrate the truncation behavior",
                text = "This is a longer piece of dialog body text that " +
                    "demonstrates how the dialog handles multiple lines of text " +
                    "and ensures proper wrapping within the container boundaries.",
                confirmButton = {
                    Button(onClick = {}) {
                        Text("Confirm")
                    }
                },
            )
        }
    }
}
