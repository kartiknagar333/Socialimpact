package com.example.socialimpact.ui.state

import com.example.socialimpact.domain.model.Donation

data class DonationUiState(
    val donations: List<Donation> = emptyList(),
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val stripePaymentData: StripePaymentData? = null
)

data class StripePaymentData(
    val paymentIntentClientSecret: String,
    val ephemeralKeySecret: String,
    val customerId: String,
    val publishableKey: String
)
