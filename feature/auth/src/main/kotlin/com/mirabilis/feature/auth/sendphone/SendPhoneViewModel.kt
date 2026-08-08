package com.mirabilis.feature.auth.sendphone

import androidx.lifecycle.viewModelScope
import com.mirabilis.core.result.Result
import com.mirabilis.core.ui.mvi.MVIViewModel
import com.mirabilis.core.ui.mvi.UiEffect
import com.mirabilis.core.ui.mvi.UiEvent
import com.mirabilis.core.ui.mvi.UiIntent
import com.mirabilis.core.ui.mvi.UiState
import com.mirabilis.domain.auth.usecase.RequestOtpUseCase
import com.mirabilis.feature.auth.PendingVerificationStore
import com.mirabilis.feature.auth.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SendPhoneUiState(
    val phone: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
) : UiState

sealed interface SendPhoneIntent : UiIntent {
    data class PhoneChanged(val value: String) : SendPhoneIntent
    data object Submit : SendPhoneIntent
}

sealed interface SendPhoneEvent : UiEvent {
    data class PhoneChanged(val value: String) : SendPhoneEvent
    data object Submitting : SendPhoneEvent
    data class Failed(val message: String) : SendPhoneEvent
    data object Succeeded : SendPhoneEvent
}

sealed interface SendPhoneEffect : UiEffect {
    data object NavigateToVerify : SendPhoneEffect
}

/** US1: enter a phone, request a code, advance to VerifyPhone (FR-001/002). */
@HiltViewModel
class SendPhoneViewModel @Inject constructor(
    private val requestOtp: RequestOtpUseCase,
    private val pending: PendingVerificationStore,
) : MVIViewModel<SendPhoneUiState, SendPhoneEvent, SendPhoneEffect, SendPhoneIntent>() {

    override fun getInitial() = SendPhoneUiState()

    override fun onIntent(intent: SendPhoneIntent) {
        when (intent) {
            is SendPhoneIntent.PhoneChanged -> setEvent { SendPhoneEvent.PhoneChanged(intent.value) }
            SendPhoneIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val phone = state.value.phone
        setEvent { SendPhoneEvent.Submitting }
        viewModelScope.launch {
            when (val result = requestOtp(phone)) {
                is Result.Success -> {
                    pending.challenge = result.data
                    setEvent { SendPhoneEvent.Succeeded }
                    setEffect { SendPhoneEffect.NavigateToVerify }
                }
                is Result.Error -> setEvent { SendPhoneEvent.Failed(result.error.toUserMessage()) }
            }
        }
    }

    override fun onReduce(oldState: SendPhoneUiState, event: SendPhoneEvent): SendPhoneUiState =
        when (event) {
            is SendPhoneEvent.PhoneChanged -> oldState.copy(phone = event.value, error = null)
            SendPhoneEvent.Submitting -> oldState.copy(isSubmitting = true, error = null)
            is SendPhoneEvent.Failed -> oldState.copy(isSubmitting = false, error = event.message)
            SendPhoneEvent.Succeeded -> oldState.copy(isSubmitting = false)
        }
}
