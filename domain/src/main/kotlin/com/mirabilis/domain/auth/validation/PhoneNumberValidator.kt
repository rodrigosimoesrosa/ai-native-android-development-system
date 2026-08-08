package com.mirabilis.domain.auth.validation

/**
 * E.164-like international phone validation (spec Assumptions, FR-002). Pure domain — no framework.
 * `+` followed by a non-zero leading digit and 7–14 more digits (8–15 total).
 */
object PhoneNumberValidator {
    private val E164 = Regex("^\\+[1-9]\\d{7,14}$")

    fun isValid(phone: String): Boolean = E164.matches(phone.trim())
}
