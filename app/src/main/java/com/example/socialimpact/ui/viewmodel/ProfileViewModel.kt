package com.example.socialimpact.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialimpact.domain.repository.HomeRepository
import com.example.socialimpact.domain.repository.PostRepository
import com.example.socialimpact.ui.state.ProfileUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.socialimpact.domain.repository.LocalProfile
import com.example.socialimpact.ui.layouts.ProfileType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val postRepository: PostRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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
            
            // Determine the UID we need to fetch
            val effectiveUid = userId ?: firebaseAuth.currentUser?.uid
            
            if (effectiveUid == null) {
                Log.e(TAG, "loadProfileData: No UID available (user not logged in and no userId provided)")
                _uiState.update { it.copy(isLoading = false, error = "User not identified") }
                return@launch
            }

            // Load profile data
            try {
                val profile = if (userId == null) {
                    // It's "My Profile", try local first
                    homeRepository.getLocalProfile() ?: fetchProfileFromFirestore(effectiveUid)
                } else {
                    // It's someone else's profile, fetch from Firestore
                    fetchProfileFromFirestore(effectiveUid)
                }
                
                Log.d(TAG, "loadProfileData: Profile loaded: ${profile?.fullName ?: profile?.organizationName}")
                _uiState.update { it.copy(profile = profile) }

                // Fetch posts for this UID
                Log.d(TAG, "loadProfileData: Fetching posts for UID: $effectiveUid")
                postRepository.getUserPosts(effectiveUid).collect { result ->
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
            } catch (e: Exception) {
                Log.e(TAG, "loadProfileData: Unexpected error", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun fetchProfileFromFirestore(uid: String): LocalProfile? {
        return try {
            Log.d(TAG, "fetchProfileFromFirestore: Fetching from account collection for UID: $uid")
            val doc = firestore.collection("account").document(uid).get().await()
            if (doc.exists()) {
                val data = doc.data ?: return null
                LocalProfile(
                    uid = uid,
                    type = ProfileType.valueOf(data["type"] as String),
                    fullName = data["fullName"] as? String ?: "",
                    organizationName = data["organizationName"] as? String ?: "",
                    registrationId = data["registrationId"] as? String ?: "",
                    website = data["website"] as? String ?: "",
                    industry = data["industry"] as? String ?: "",
                    phone = data["phone"] as? String ?: "",
                    location = data["location"] as? String ?: "",
                    bio = data["bio"] as? String ?: ""
                )
            } else {
                Log.w(TAG, "fetchProfileFromFirestore: Document does not exist for UID: $uid")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchProfileFromFirestore: Error", e)
            null
        }
    }
}
