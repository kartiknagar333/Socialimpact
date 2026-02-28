package com.example.socialimpact.domain.usecase

class ValidateConfirmPassword {
    fun execute(password: String, confirmPassword: String): ValidationResult {
        if (confirmPassword.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Please confirm your password"
            )
        }
        if (password != confirmPassword) {
            return ValidationResult(
                successful = false,
                errorMessage = "Passwords do not match"
            )
        }
        return ValidationResult(successful = true)
    }
}
