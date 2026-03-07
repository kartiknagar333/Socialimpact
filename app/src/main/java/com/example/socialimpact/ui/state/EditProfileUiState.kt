package com.example.socialimpact.ui.state

import com.example.socialimpact.domain.repository.LocalProfile

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val initialProfile: LocalProfile? = null
)
