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
import com.example.socialimpact.domain.model.ProfileType
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

    fun loadProfileData(userId: String,userType: String? = null,username: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            var currentUser = userId

            if(userId.isEmpty() || userType == null) {
                 currentUser = firebaseAuth.currentUser?.uid ?: ""
            }

            // Load profile data
            try {
                val profile = if (currentUser == firebaseAuth.currentUser?.uid ) {
                    // It's "My Profile", try local first
                    Log.d(TAG, "loadProfileData: Fetching local profile for UID: $currentUser")
                    homeRepository.getLocalProfile()
                } else {
                    // It's someone else's profile, fetch from Firestore
                    Log.d(TAG, "loadProfileData: Fetching remote profile for UID: $currentUser")
                    fetchProfileFromFirestore(currentUser,userType.toString().lowercase(),username)
                }
                
                _uiState.update { it.copy(profile = profile) }

                // Fetch posts for this UID
                Log.d(TAG, "loadProfileData: Fetching posts for UID: $currentUser")
                postRepository.getUserPosts(currentUser).collect { result ->
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

    private suspend fun fetchProfileFromFirestore(uid: String,type: String? = null,uname: String? = null): LocalProfile? {
        return try {
            Log.d(TAG, "fetchProfileFromFirestore: Fetching from account collection for UID: $uid")
            val doc = firestore.collection("profile")
                .document(type ?: "")
                .collection(uname ?: "")
                .document(uid).get().await()


            if (doc.exists()) {
                val data = doc.data ?: return null
                LocalProfile(
                    uid = uid,
                    type = ProfileType.valueOf(data["type"] as String),
                    fullName = data["fullName"] as? String ?: "",
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
