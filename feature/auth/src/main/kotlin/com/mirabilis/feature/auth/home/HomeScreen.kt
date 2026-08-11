package com.mirabilis.feature.auth.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mirabilis.feature.auth.CollectEffects

@Composable
fun HomeScreen(
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    CollectEffects(viewModel.effects) { effect ->
        if (effect is HomeEffect.NavigateToSendPhone) onSignedOut()
    }

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
                Text(text = "Welcome", style = MaterialTheme.typography.headlineMedium)
                Text(text = user.displayName ?: user.phone, style = MaterialTheme.typography.titleLarge)
                Text(text = user.phone)
                TextButton(onClick = { viewModel.setIntent { HomeIntent.SignOutRequested } }) {
                    Text("Sign out")
                }
            }

            else -> {
                Text(
                    text = state.error ?: "Couldn't load your profile.",
                    color = MaterialTheme.colorScheme.error,
                )
                Button(onClick = { viewModel.setIntent { HomeIntent.Retry } }) { Text("Retry") }
            }
        }

        if (state.showSignOutConfirm) {
            AlertDialog(
                onDismissRequest = { viewModel.setIntent { HomeIntent.SignOutCancelled } },
                confirmButton = {
                    TextButton(onClick = { viewModel.setIntent { HomeIntent.SignOutConfirmed } }) {
                        Text("Sign out")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setIntent { HomeIntent.SignOutCancelled } }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Sign out") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.user?.displayName ?: "You will need to sign in again.")
                        state.signOutError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    }
}
