package com.mirabilis.feature.profile.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mirabilis.domain.profile.model.Theme

/** US1: view display name + phone; recoverable error + retry (FR-001/006). */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            state.isLoading -> CircularProgressIndicator()

            state.user != null -> {
                val user = state.user!!
                Text(text = "Profile", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = user.displayName ?: user.phone,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(text = user.phone)

                // US2 — edit the display name (FR-002/006).
                OutlinedTextField(
                    value = state.nameDraft,
                    onValueChange = { value -> viewModel.setIntent { ProfileIntent.NameChanged(value) } },
                    label = { Text("Display name") },
                    singleLine = true,
                    isError = state.saveError != null,
                    enabled = !state.isSaving,
                )
                state.saveError?.let { message ->
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = { viewModel.setIntent { ProfileIntent.Save } },
                    enabled = !state.isSaving,
                ) {
                    Text(if (state.isSaving) "Saving…" else "Save")
                }

                // US3 — preferences (FR-004): theme + notifications persist immediately.
                Text(text = "Preferences", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Theme.entries.forEach { theme ->
                        TextButton(
                            onClick = { viewModel.setIntent { ProfileIntent.SetTheme(theme) } },
                            enabled = state.preferences.theme != theme,
                        ) { Text(theme.name) }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "Notifications")
                    Switch(
                        checked = state.preferences.notificationsEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.setIntent { ProfileIntent.SetNotifications(enabled) }
                        },
                    )
                }
            }

            else -> {
                Text(
                    text = state.error ?: "Couldn't load your profile.",
                    color = MaterialTheme.colorScheme.error,
                )
                Button(onClick = { viewModel.setIntent { ProfileIntent.Retry } }) { Text("Retry") }
            }
        }
    }
}
