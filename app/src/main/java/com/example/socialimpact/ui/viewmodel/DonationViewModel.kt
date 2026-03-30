package com.example.socialimpact.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.repository.HomeRepository
import com.example.socialimpact.domain.repository.PostRepository
import com.example.socialimpact.ui.state.DonationUiState
import com.example.socialimpact.ui.state.StripePaymentData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DonationViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val homeRepository: HomeRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFunctions: FirebaseFunctions
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
        val key = "$donationId-$itemIndex"
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

    fun submitDonation(postId: String, itemName: String, quantity: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            
            // Fix: Use FirebaseAuth as the source of truth for the donor UID
            val currentUserId = firebaseAuth.currentUser?.uid
            val profile = homeRepository.getLocalProfile()
            
            if (currentUserId == null || profile == null) {
                _uiState.update { it.copy(isProcessing = false, error = "Authentication or Profile not found") }
                return@launch
            }

            // Path: /profile/{usertype}/{username}/{user_id}
            val donorPath = "profile/${profile.type.name.lowercase()}/${profile.fullName.trim()}/$currentUserId"

            postRepository.submitItemDonation(
                postId = postId,
                donorUid = currentUserId,
                donorProfilePath = donorPath,
                itemName = itemName,
                quantity = quantity
            ).collect { result ->
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isProcessing = false, successMessage = "Donation submitted!") }
                        fetchDonations(postId)
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isProcessing = false, error = e.message) }
                    }
                )
            }
        }
    }

    /**
     * Call the Cloud Function to initialize the Stripe Payment Intent.
     */
    fun startFundDonation(postId: String, amountUsd: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            val currentUserId = firebaseAuth.currentUser?.uid
            
            if (currentUserId == null) {
                _uiState.update { it.copy(isProcessing = false, error = "Please sign in to donate.") }
                return@launch
            }

            if (amountUsd.toDoubleOrNull() == null || amountUsd.toDouble() <= 0) {
                _uiState.update { it.copy(isProcessing = false, error = "Invalid amount") }
                return@launch
            }

            try {
                val data = hashMapOf(
                    "postId" to postId,
                    "donorId" to currentUserId,
                    "amountUsd" to amountUsd
                )

                val result = firebaseFunctions
                    .getHttpsCallable("createDonationIntent")
                    .call(data)
                    .await()

                val response = result.data as? Map<*, *>
                if (response != null) {
                    val paymentIntentClientSecret = response["paymentIntentClientSecret"] as? String
                    val ephemeralKeySecret = response["ephemeralKeySecret"] as? String
                    val customerId = response["customerId"] as? String
                    val publishableKey = response["publishableKey"] as? String

                    if (paymentIntentClientSecret != null && publishableKey != null) {
                        _uiState.update {
                            it.copy(
                                stripePaymentData = StripePaymentData(
                                    paymentIntentClientSecret = paymentIntentClientSecret,
                                    ephemeralKeySecret = ephemeralKeySecret ?: "",
                                    customerId = customerId ?: "",
                                    publishableKey = publishableKey
                                ),
                                isProcessing = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isProcessing = false, error = "Invalid server response") }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "startFundDonation failed: ${e.message}", e)
                _uiState.update { it.copy(isProcessing = false, error = e.message) }
            }
        }
    }

    fun handleStripeResult(isSuccess: Boolean) {
        _uiState.update { 
            it.copy(
                stripePaymentData = null,
                successMessage = if (isSuccess) "Payment Successful! Thank you." else null,
                error = if (!isSuccess) "Payment failed or cancelled." else null
            )
        }
        // If successful, we can optionally refresh the donations list, 
        // though the webhook handles the Firestore update.
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
