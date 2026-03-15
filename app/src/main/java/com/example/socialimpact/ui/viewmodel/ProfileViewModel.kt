package com.example.socialimpact.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialimpact.domain.repository.HomeRepository
import com.example.socialimpact.domain.repository.PostRepository
import com.example.socialimpact.ui.state.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun loadProfileData(userId: String? = null) {
        viewModelScope.launch {
            Log.d(TAG, "loadProfileData: Requesting data for userId: $userId")
            _uiState.update { it.copy(isLoading = true) }
            
            val profile = if (userId == null) {
                val local = homeRepository.getLocalProfile()
                Log.d(TAG, "loadProfileData: Loaded local profile: ${local?.fullName ?: local?.organizationName}")
                local
            } else {
                Log.d(TAG, "loadProfileData: userId provided ($userId), local profile skipped")
                null
            }
            
            _uiState.update { it.copy(profile = profile) }

            val targetUid = userId ?: profile?.uid
            if (targetUid != null) {
                Log.d(TAG, "loadProfileData: Fetching posts for UID: $targetUid")
                postRepository.getUserPosts(targetUid).collect { result ->
                    result.fold(
                        onSuccess = { posts ->
                            Log.d(TAG, "loadProfileData: Successfully loaded ${posts.size} posts")
                            _uiState.update { it.copy(myPosts = posts, isLoading = false) }
                        },
                        onFailure = { e ->
                            Log.e(TAG, "loadProfileData: Failed to load posts", e)
                            _uiState.update { it.copy(error = e.message, isLoading = false) }
                        }
                    )
                }
            } else {
                Log.w(TAG, "loadProfileData: No valid UID found to fetch posts")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
