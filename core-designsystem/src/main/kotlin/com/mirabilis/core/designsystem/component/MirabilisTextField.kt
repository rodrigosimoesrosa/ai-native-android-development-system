package com.mirabilis.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mirabilis.core.designsystem.theme.MirabilisTheme
import com.mirabilis.core.designsystem.token.spacingLg
import com.mirabilis.core.designsystem.token.spacingSm

/**
 * A branded text-field component that consumes design-system tokens exclusively.
 *
 * Displays label, placeholder, and helper/error text.  When [isError] is true
 * the helper slot is replaced by [errorText] styled with the `error` color role.
 * Focused, filled, and disabled visuals are driven by tokens (FR-012).
 *
 * Every state combination is previewable in both light and dark themes (FR-019).
 *
 * @param value Current text value.
 * @param onValueChange Callback invoked on text change.
 * @param label Field label displayed when focused or filled.
 * @param modifier Modifier for styling or layout positioning.
 * @param placeholder Text shown when the field is empty and not focused.
 * @param helperText Non-error helper text shown below the field.
 * @param isError When true, switches to error visual treatment and shows [errorText].
 * @param errorText Error message displayed in the `error` color role.
 * @param enabled Controls editability and the disabled visual treatment.
 * @param singleLine When true, restricts to a single line (default).
 */
@Suppress("LongParameterList")
@Composable
fun MirabilisTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    isError: Boolean = false,
    errorText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
) {
    val colors = if (isError) {
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.error,
            unfocusedBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = MaterialTheme.colorScheme.error,
            unfocusedLabelColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedSupportingTextColor = MaterialTheme.colorScheme.error,
            unfocusedSupportingTextColor = MaterialTheme.colorScheme.error,
            errorSupportingTextColor = MaterialTheme.colorScheme.error,
        )
    } else {
        OutlinedTextFieldDefaults.colors(
            focusedLabelColor = MaterialTheme.colorScheme.primary,
        )
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = {
            if (isError && errorText != null) {
                Text(errorText)
            } else if (!isError && helperText != null) {
                Text(helperText)
            }
        },
        isError = isError,
        enabled = enabled,
        singleLine = singleLine,
        colors = colors,
    )
}

@Preview(name = "Light")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisTextFieldLightPreview() {
    MirabilisTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(spacingLg),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
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

@Preview(name = "Dark")
@Composable
@Suppress("UnusedPrivateMember")
private fun MirabilisTextFieldDarkPreview() {
    MirabilisTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(spacingLg),
            verticalArrangement = Arrangement.spacedBy(spacingSm),
        ) {
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
