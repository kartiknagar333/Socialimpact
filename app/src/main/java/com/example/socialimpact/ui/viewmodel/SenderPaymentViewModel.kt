package com.example.socialimpact.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialimpact.ui.state.SenderPaymentUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SenderPaymentViewModel : ViewModel() {

    private val functions = FirebaseFunctions.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(SenderPaymentUiState())
    val uiState: StateFlow<SenderPaymentUiState> = _uiState.asStateFlow()

    init {
        refreshStatus()
        observeSenderStatus()
    }

    fun refreshStatus() {
        viewModelScope.launch {
            try {
                functions.getHttpsCallable("syncStripeCustomerStatus").call().await()
            } catch (e: Exception) {
                // Ignore sync errors
            }
        }
    }

    private fun observeSenderStatus() {
        val uid = auth.currentUser?.uid ?: return
        
        firestore.collection("account").document(uid)
            .collection("payment").document("send")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null && snapshot.exists()) {
                    val hasCard = snapshot.getBoolean("hasSavedPaymentMethod") ?: false
                    val brand = snapshot.getString("defaultCardBrand")
                    val last4 = snapshot.getString("defaultCardLast4")
                    
                    _uiState.update { 
                        it.copy(
                            hasSavedPaymentMethod = hasCard,
                            cardBrand = brand,
                            cardLast4 = last4,
                            statusLabel = if (hasCard) "Card saved: $brand ****$last4" else "No card saved"
                        )
                    }
                }
            }
    }

    fun onManagePaymentClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = functions.getHttpsCallable("createStripePortalSession").call().await()
                val url = (result.data as? Map<*, *>)?.get("url") as? String
                if (url != null) {
                    _uiState.update { it.copy(isLoading = false, portalUrl = url) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onUrlOpened() {
        _uiState.update { it.copy(portalUrl = "") }
    }

    fun resetErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
