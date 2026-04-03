package com.example.socialimpact.ui.state

import com.example.socialimpact.domain.model.HelpRequestPost

data class DonatedPostsUiState(
    val posts: List<HelpRequestPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
