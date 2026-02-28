package com.example.socialimpact.domain.usecase

class ValidatePassword {
    fun execute(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult(
                successful = false,
                errorMessage = "At least 8 characters required"
            )
        }
        val hasUppercase = password.any { it.isUpperCase() }
        if (!hasUppercase) {
            return ValidationResult(
                successful = false,
                errorMessage = "At least one uppercase letter required"
            )
        }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        if (!hasSpecialChar) {
            return ValidationResult(
                successful = false,
                errorMessage = "At least one special character required"
            )
        }
        return ValidationResult(successful = true)
    }
}
