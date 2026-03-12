package com.example.socialimpact.ui.state

import com.example.socialimpact.ui.layouts.NeedItem

data class UploadPostUiState(
    val title: String = "",
    val selectedNeeds: List<String> = emptyList(),
    val selectedCategories: List<String> = emptyList(),
    val fundAmount: String = "",
    val dynamicNeeds: List<NeedItem> = listOf(NeedItem()),
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val address: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) {
    val isFormValid: Boolean
        get() {
            val isTitleValid = title.isNotBlank()
            val isNeedsValid = selectedNeeds.isNotEmpty()
            val isCategoriesValid = selectedCategories.isNotEmpty()
            val isFundValid = if (selectedNeeds.contains("Fund")) fundAmount.isNotBlank() else true
            val isDynamicNeedsValid = dynamicNeeds.all { it.name.isNotBlank() && it.quantity.isNotBlank() && it.quantity != "0.0" }
            val isDescriptionValid = description.isNotBlank()
            val areDatesValid = startDate.isNotBlank() && endDate.isNotBlank()
            val isAddressValid = address.isNotBlank()

            return isTitleValid && isNeedsValid && isCategoriesValid && isFundValid && 
                   isDynamicNeedsValid && isDescriptionValid && areDatesValid && isAddressValid
        }
}
