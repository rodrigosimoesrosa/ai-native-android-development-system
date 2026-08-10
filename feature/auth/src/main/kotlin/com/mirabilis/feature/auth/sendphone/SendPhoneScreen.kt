package com.mirabilis.feature.auth.sendphone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mirabilis.feature.auth.CollectEffects

@Composable
fun SendPhoneScreen(
    onNavigateToVerify: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SendPhoneViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    CollectEffects(viewModel.effects) { effect ->
        if (effect is SendPhoneEffect.NavigateToVerify) onNavigateToVerify()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(text = "Sign in", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Enter your phone number to receive a one-time code.")

        OutlinedTextField(
            value = state.phone,
            onValueChange = { viewModel.setIntent { SendPhoneIntent.PhoneChanged(it) } },
            label = { Text("Phone (e.g. +15551234567)") },
            singleLine = true,
            isError = state.error != null,
            enabled = !state.isSubmitting,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )

        state.error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = { viewModel.setIntent { SendPhoneIntent.Submit } },
            enabled = !state.isSubmitting && state.phone.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            } else {
                Text("Send code")
            }
        }
    }
}
