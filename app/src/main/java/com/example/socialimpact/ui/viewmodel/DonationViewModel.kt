package com.example.socialimpact.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialimpact.domain.repository.PostRepository
import com.example.socialimpact.ui.state.DonationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class DonationViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DonationUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchDonations(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            postRepository.getDonations(postId).collect { result ->
                result.fold(
                    onSuccess = { donations ->
                        _uiState.update { it.copy(donations = donations, isLoading = false) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(error = e.message, isLoading = false) }
                    }
                )
            }
        }
    }
}
