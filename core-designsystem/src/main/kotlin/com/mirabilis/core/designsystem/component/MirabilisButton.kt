package com.mirabilis.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mirabilis.core.designsystem.theme.MirabilisTheme
import com.mirabilis.core.designsystem.token.spacingSm
import com.mirabilis.core.designsystem.token.spacingXl

/**
 * A branded button component that consumes design-system tokens exclusively.
 *
 * Supports three emphasis levels ([MirabilisButtonStyle]) and four visual
 * states: enabled, pressed, focused, and disabled.  Disabled state uses
 * 38 % and 12 % alpha tokens on the foreground/background colours.
 * Long labels are truncated with an ellipsis.
 *
 * Every variant is previewable in both light and dark themes (FR-019).
 *
 * @param text Label text, displayed with ellipsis overflow when too long.
 * @param onClick Callback invoked on click.
 * @param modifier Modifier for styling or layout positioning.
 * @param style Visual emphasis: filled (Primary), outlined (Secondary), or text-only (Text).
 * @param enabled Controls clickability and the disabled alpha treatment.
 * @param leadingIcon Optional icon displayed before the label text.
 */
@Composable
fun MirabilisButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: MirabilisButtonStyle = MirabilisButtonStyle.Primary,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val contentPadding = androidx.compose.foundation.layout.PaddingValues(
        horizontal = spacingXl,
        vertical = spacingSm,
    )

    when (style) {
        MirabilisButtonStyle.Primary -> {
            val disabledBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
            val disabledContent = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (enabled) MaterialTheme.colorScheme.primary else disabledBg,
                    disabledContainerColor = disabledBg,
                    contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary else disabledContent,
                    disabledContentColor = disabledContent,
                ),
                shape = MaterialTheme.shapes.medium,
                contentPadding = contentPadding,
            ) { ButtonContent(text, leadingIcon) }
        }
        MirabilisButtonStyle.Secondary -> {
            val disabledContent = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (enabled) MaterialTheme.colorScheme.primary else disabledContent,
                ),
                shape = MaterialTheme.shapes.medium,
                contentPadding = contentPadding,
            ) { ButtonContent(text, leadingIcon) }
        }
        MirabilisButtonStyle.Text -> {
            val disabledContent = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (enabled) MaterialTheme.colorScheme.primary else disabledContent,
                    disabledContentColor = disabledContent,
                ),
                contentPadding = contentPadding,
            ) { ButtonContent(text, leadingIcon) }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    leadingIcon: (@Composable () -> Unit)?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(spacingSm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "Light")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisButtonLightPreview() {
    MirabilisTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            MirabilisButton("Primary", onClick = {})
            MirabilisButton("Secondary", onClick = {}, style = MirabilisButtonStyle.Secondary)
            MirabilisButton("Text", onClick = {}, style = MirabilisButtonStyle.Text)
            MirabilisButton("Disabled Primary", onClick = {}, enabled = false)
            MirabilisButton("Disabled Secondary", onClick = {}, style = MirabilisButtonStyle.Secondary, enabled = false)
            MirabilisButton("Disabled Text", onClick = {}, style = MirabilisButtonStyle.Text, enabled = false)
            MirabilisButton(
                "Very long label that should truncate with ellipsis",
                onClick = {},
                modifier = Modifier.width(200.dp),
            )
        }
    }
}

@Preview(name = "Dark")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisButtonDarkPreview() {
    MirabilisTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
            MirabilisButton("Primary", onClick = {})
            MirabilisButton("Secondary", onClick = {}, style = MirabilisButtonStyle.Secondary)
            MirabilisButton("Text", onClick = {}, style = MirabilisButtonStyle.Text)
            MirabilisButton("Disabled Primary", onClick = {}, enabled = false)
            MirabilisButton("Disabled Secondary", onClick = {}, style = MirabilisButtonStyle.Secondary, enabled = false)
            MirabilisButton("Disabled Text", onClick = {}, style = MirabilisButtonStyle.Text, enabled = false)
            MirabilisButton(
                "Very long label that should truncate with ellipsis",
                onClick = {},
                modifier = Modifier.width(200.dp),
            )
        }
    }
}
