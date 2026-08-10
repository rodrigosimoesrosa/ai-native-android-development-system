package com.mirabilis.feature.auth.verifyphone

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
import androidx.compose.material3.TextButton
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
fun VerifyPhoneScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSendPhone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VerifyPhoneViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            VerifyPhoneEffect.NavigateToHome -> onNavigateToHome()
            VerifyPhoneEffect.NavigateToSendPhone -> onNavigateToSendPhone()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(text = "Enter code", style = MaterialTheme.typography.headlineMedium)
        Text(text = "We sent a code to ${state.phone}.")

        OutlinedTextField(
            value = state.code,
            onValueChange = { viewModel.setIntent { VerifyPhoneIntent.CodeChanged(it) } },
            label = { Text("6-digit code") },
            singleLine = true,
            isError = state.error != null,
            enabled = !state.isSubmitting,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
        )

        state.error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = { viewModel.setIntent { VerifyPhoneIntent.Submit } },
            enabled = !state.isSubmitting && state.code.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            } else {
                Text("Verify")
            }
        }

        val cooldown = state.resendCooldownSeconds
        TextButton(
            onClick = { viewModel.setIntent { VerifyPhoneIntent.Resend } },
            enabled = cooldown == 0 && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (cooldown > 0) "Resend code in ${cooldown}s" else "Resend code")
        }
    }
}
