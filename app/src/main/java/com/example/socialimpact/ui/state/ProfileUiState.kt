package com.example.socialimpact.ui.state

import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.repository.LocalProfile

data class ProfileUiState(
    val profile: LocalProfile? = null,
    val myPosts: List<HelpRequestPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
