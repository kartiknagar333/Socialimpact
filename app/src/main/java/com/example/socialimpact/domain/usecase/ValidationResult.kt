package com.example.socialimpact.domain.usecase

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)
