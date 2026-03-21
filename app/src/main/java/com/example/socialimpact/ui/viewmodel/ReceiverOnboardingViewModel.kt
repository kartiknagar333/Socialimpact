package com.example.socialimpact.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialimpact.ui.state.ReceiverOnboardingUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for the Receiver Onboarding feature.
 */
class ReceiverOnboardingViewModel : ViewModel() {

    private val functions = FirebaseFunctions.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ReceiverOnboardingUiState())
    val uiState: StateFlow<ReceiverOnboardingUiState> = _uiState.asStateFlow()

    init {
        // Step 1: Sync status from Stripe to Firestore immediately on load
        syncStripeStatus()
        // Step 2: Listen for real-time changes in Firestore
        observeOnboardingStatus()
    }

    /**
     * Calls Cloud Function to fetch latest status from Stripe and update Firestore.
     */
    private fun syncStripeStatus() {
        viewModelScope.launch {
            try {
                functions.getHttpsCallable("syncStripeAccountStatus").call().await()
            } catch (e: Exception) {
                // Silent fail if no account exists yet
            }
        }
    }

    /**
     * Listens to the specific path: account/{uid}/payment/receive
     */
    private fun observeOnboardingStatus() {
        val uid = auth.currentUser?.uid ?: return
        
        firestore.collection("account").document(uid)
            .collection("payment").document("receive")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null && snapshot.exists()) {
                    val onboardingCompleted = snapshot.getBoolean("onboardingComplete") ?: false
                    val chargesEnabled = snapshot.getBoolean("chargesEnabled") ?: false
                    
                    val status = when {
                        chargesEnabled -> "Completed & Active"
                        onboardingCompleted -> "Pending Verification"
                        else -> "Action Required"
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isOnboardingComplete = chargesEnabled,
                            statusLabel = status
                        )
                    }
                }
            }
    }

    fun onOpenOnboardingClicked() {
        if (_uiState.value.isOnboardingComplete) {
            onManageAccountClicked()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, onboardingUrl = "") }

            try {
                val result = functions
                    .getHttpsCallable("createOrGetOnboardingLink")
                    .call()
                    .await()

                val data = result.data as? Map<*, *>
                val url = data?.get("url") as? String

                if (url != null) {
                    _uiState.update { it.copy(isLoading = false, onboardingUrl = url) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun onManageAccountClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = functions
                    .getHttpsCallable("createStripeDashboardLink")
                    .call()
                    .await()

                val url = (result.data as? Map<*, *>)?.get("url") as? String
                if (url != null) {
                    _uiState.update { it.copy(isLoading = false, onboardingUrl = url) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun resetErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onUrlOpened() {
        _uiState.update { it.copy(onboardingUrl = "") }
    }
}
