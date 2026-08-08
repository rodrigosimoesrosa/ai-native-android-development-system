package com.mirabilis.domain.auth.model

/** The authenticated person; basic identity/profile displayed on Home (FR-012). */
data class User(
    val id: String,
    val phone: String,
    val displayName: String? = null,
)
