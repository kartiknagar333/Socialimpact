package com.example.socialimpact.ui.state

/**
 * UI State for the Sender Payment/Management screen.
 */
data class SenderPaymentUiState(
    val hasSavedPaymentMethod: Boolean = false,
    val cardBrand: String? = null,
    val cardLast4: String? = null,
    val isLoading: Boolean = false,
    val portalUrl: String = "",
    val statusLabel: String = "No card saved",
    val errorMessage: String? = null
)
