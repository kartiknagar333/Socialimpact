package com.example.socialimpact.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialimpact.domain.repository.PostRepository
import com.example.socialimpact.ui.state.DonatedPostsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImpactedByViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DonatedPostsUiState())
    val uiState: StateFlow<DonatedPostsUiState> = _uiState.asStateFlow()

    fun fetchDonatedPosts(profilePath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            postRepository.getDonatedPosts(profilePath).collect { result ->
                result.fold(
                    onSuccess = { posts ->
                        _uiState.update { it.copy(posts = posts, isLoading = false) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(error = e.message, isLoading = false) }
                    }
                )
            }
        }
    }
}
