package com.mirabilis.domain.auth.usecase

import com.mirabilis.core.result.AppError
import com.mirabilis.core.result.Result
import com.mirabilis.domain.auth.model.PhoneVerificationChallenge
import com.mirabilis.domain.auth.repository.IAuthRepository
import com.mirabilis.domain.auth.validation.PhoneNumberValidator
import javax.inject.Inject

/**
 * FR-001/FR-002: validate the phone format, then request a one-time code. Malformed input is
 * rejected as a typed [AppError.Validation] before any backend call.
 */
class RequestOtpUseCase @Inject constructor(
    private val repository: IAuthRepository,
) {
    suspend operator fun invoke(phone: String): Result<PhoneVerificationChallenge> {
        val normalized = phone.trim()
        if (!PhoneNumberValidator.isValid(normalized)) {
            return Result.Error(
                AppError.Validation("Enter a valid phone in international format, e.g. +15551234567."),
            )
        }
        return repository.requestOtp(normalized)
    }
}
