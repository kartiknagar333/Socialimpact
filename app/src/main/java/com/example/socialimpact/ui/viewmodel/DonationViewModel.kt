package com.example.socialimpact.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.repository.PostRepository
import com.example.socialimpact.ui.state.DonationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class DonationViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    companion object {
        private const val TAG = "DonationViewModel"
    }

    private val _uiState = MutableStateFlow(DonationUiState())
    val uiState = _uiState.asStateFlow()

    private val _observedPost = MutableStateFlow<HelpRequestPost?>(null)
    val observedPost: StateFlow<HelpRequestPost?> = _observedPost.asStateFlow()

    private val _processingItems = MutableStateFlow<Set<String>>(emptySet())
    val processingItems = _processingItems.asStateFlow()

    fun fetchDonations(postId: String) {
        Log.d(TAG, "fetchDonations: Requesting donations for postId: $postId")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            postRepository.getDonations(postId).collect { result ->
                result.fold(
                    onSuccess = { donations ->
                        Log.d(TAG, "fetchDonations: Successfully fetched ${donations.size} donations")
                        _uiState.update { it.copy(donations = donations, isLoading = false) }
                    },
                    onFailure = { e ->
                        Log.e(TAG, "fetchDonations: Failed to fetch donations", e)
                        _uiState.update { it.copy(error = e.message, isLoading = false) }
                    }
                )
            }
        }
    }

    fun startObservingPost(postId: String) {
        viewModelScope.launch {
            postRepository.observePost(postId).collect { post ->
                _observedPost.value = post
            }
        }
    }

    fun markItemAsReceived(postId: String, donationId: String, itemName: String, quantity: String, itemIndex: Int) {
        val key = "$donationId-$itemIndex" // Unique key using index
        viewModelScope.launch {
            _processingItems.update { it + key }
            postRepository.markDonationItemReceived(postId, donationId, itemName, quantity, itemIndex).collect { result ->
                result.fold(
                    onSuccess = {
                        _processingItems.update { it - key }
                        fetchDonations(postId)
                    },
                    onFailure = { e ->
                        _processingItems.update { it - key }
                        _uiState.update { it.copy(error = e.message) }
                    }
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
