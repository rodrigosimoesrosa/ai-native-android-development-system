package com.mirabilis.feature.profile.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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
