package com.example.socialimpact.ui.state

/**
 * UI State for the Receiver Onboarding screen.
 */
data class ReceiverOnboardingUiState(
    val onboardingUrl: String = "",
    val isLoading: Boolean = false,
    val isOnboardingComplete: Boolean = false,
    val statusLabel: String = "Not Started",
    val errorMessage: String? = null
)
