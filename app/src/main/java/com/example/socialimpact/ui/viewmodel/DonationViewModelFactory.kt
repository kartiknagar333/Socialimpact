package com.example.socialimpact.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class DonationViewModelFactory @Inject constructor(
    private val donationViewModel: DonationViewModel,
    private val impactedByViewModel: ImpactedByViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DonationViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                donationViewModel as T
            }
            modelClass.isAssignableFrom(ImpactedByViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                impactedByViewModel as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
