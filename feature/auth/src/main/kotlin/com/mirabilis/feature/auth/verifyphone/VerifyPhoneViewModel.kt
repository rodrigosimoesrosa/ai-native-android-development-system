package com.mirabilis.feature.auth.verifyphone

import androidx.lifecycle.viewModelScope
import com.mirabilis.core.result.Result
import com.mirabilis.core.ui.mvi.MVIViewModel
import com.mirabilis.core.ui.mvi.UiEffect
import com.mirabilis.core.ui.mvi.UiEvent
import com.mirabilis.core.ui.mvi.UiIntent
import com.mirabilis.core.ui.mvi.UiState
import com.mirabilis.domain.auth.usecase.RequestOtpUseCase
import com.mirabilis.domain.auth.usecase.VerifyOtpUseCase
import com.mirabilis.feature.auth.PendingVerificationStore
import com.mirabilis.feature.auth.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerifyPhoneUiState(
    val phone: String = "",
    val code: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val resendCooldownSeconds: Int = 0,
) : UiState

sealed interface VerifyPhoneIntent : UiIntent {
    data class CodeChanged(val value: String) : VerifyPhoneIntent
    data object Submit : VerifyPhoneIntent
    data object Resend : VerifyPhoneIntent
}

sealed interface VerifyPhoneEvent : UiEvent {
    data class Initialized(val phone: String) : VerifyPhoneEvent
    data class CodeChanged(val value: String) : VerifyPhoneEvent
    data object Submitting : VerifyPhoneEvent
    data class Failed(val message: String) : VerifyPhoneEvent
    data object Verified : VerifyPhoneEvent
    data class ResendCooldown(val seconds: Int) : VerifyPhoneEvent
}

sealed interface VerifyPhoneEffect : UiEffect {
    data object NavigateToHome : VerifyPhoneEffect
    data object NavigateToSendPhone : VerifyPhoneEffect
}

/** US1: verify the code for the pending challenge; resend with cooldown (FR-003/004/005, FR-013). */
@HiltViewModel
class VerifyPhoneViewModel @Inject constructor(
    private val verifyOtp: VerifyOtpUseCase,
    private val requestOtp: RequestOtpUseCase,
    private val pending: PendingVerificationStore,
) : MVIViewModel<VerifyPhoneUiState, VerifyPhoneEvent, VerifyPhoneEffect, VerifyPhoneIntent>() {

    init {
        val challenge = pending.challenge
        if (challenge == null) {
            // Transient challenge gone (e.g. process death mid-flow) → restart from SendPhone (FR-013).
            setEffect { VerifyPhoneEffect.NavigateToSendPhone }
        } else {
            setEvent { VerifyPhoneEvent.Initialized(challenge.phone) }
        }
    }

    override fun getInitial() = VerifyPhoneUiState()

    override fun onIntent(intent: VerifyPhoneIntent) {
        when (intent) {
            is VerifyPhoneIntent.CodeChanged -> setEvent { VerifyPhoneEvent.CodeChanged(intent.value) }
            VerifyPhoneIntent.Submit -> submit()
            VerifyPhoneIntent.Resend -> resend()
        }
    }

    private fun submit() {
        val challenge = pending.challenge ?: run {
            setEffect { VerifyPhoneEffect.NavigateToSendPhone }
            return
        }
        val code = state.value.code
        setEvent { VerifyPhoneEvent.Submitting }
        viewModelScope.launch {
            when (val result = verifyOtp(challenge.verificationToken, code)) {
                is Result.Success -> {
                    pending.clear()
                    setEvent { VerifyPhoneEvent.Verified }
                    setEffect { VerifyPhoneEffect.NavigateToHome }
                }
                is Result.Error -> setEvent { VerifyPhoneEvent.Failed(result.error.toUserMessage()) }
            }
        }
    }

    private fun resend() {
        if (state.value.resendCooldownSeconds > 0) return
        viewModelScope.launch {
            when (val result = requestOtp(state.value.phone)) {
                is Result.Success -> {
                    pending.challenge = result.data
                    startCooldown()
                }
                is Result.Error -> setEvent { VerifyPhoneEvent.Failed(result.error.toUserMessage()) }
            }
        }
    }

    private fun startCooldown() {
        viewModelScope.launch {
            for (seconds in RESEND_COOLDOWN_SECONDS downTo 0) {
                setEvent { VerifyPhoneEvent.ResendCooldown(seconds) }
                if (seconds > 0) delay(1_000)
            }
        }
    }

    override fun onReduce(oldState: VerifyPhoneUiState, event: VerifyPhoneEvent): VerifyPhoneUiState =
        when (event) {
            is VerifyPhoneEvent.Initialized -> oldState.copy(phone = event.phone)
            is VerifyPhoneEvent.CodeChanged -> oldState.copy(code = event.value, error = null)
            VerifyPhoneEvent.Submitting -> oldState.copy(isSubmitting = true, error = null)
            is VerifyPhoneEvent.Failed -> oldState.copy(isSubmitting = false, error = event.message)
            VerifyPhoneEvent.Verified -> oldState.copy(isSubmitting = false)
            is VerifyPhoneEvent.ResendCooldown -> oldState.copy(resendCooldownSeconds = event.seconds)
        }

    private companion object {
        const val RESEND_COOLDOWN_SECONDS = 30
    }
}
