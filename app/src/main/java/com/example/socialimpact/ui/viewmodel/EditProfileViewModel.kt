package com.example.socialimpact.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialimpact.domain.repository.HomeRepository
import com.example.socialimpact.ui.layouts.ProfileType
import com.example.socialimpact.ui.state.EditProfileUiState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class EditProfileViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadLocalProfile()
    }

    private fun loadLocalProfile() {
        val localProfile = homeRepository.getLocalProfile()
        _uiState.update { it.copy(initialProfile = localProfile) }
    }

    fun saveProfile(
        type: ProfileType,
        fullName: String,
        organizationName: String,
        registrationId: String,
        website: String,
        industry: String,
        phone: String,
        location: String,
        bio: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            homeRepository.saveProfile(
                type, fullName, organizationName, registrationId,
                website, industry, phone, location, bio
            ).collect { result ->
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    },
                    onFailure = { throwable ->
                        _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                    }
                )
            }
        }
    }

    fun updateProfile(
        type: ProfileType,
        fullName: String,
        organizationName: String,
        registrationId: String,
        website: String,
        industry: String,
        phone: String,
        location: String,
        bio: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val profileData = mutableMapOf<String, Any>(
                "type" to type.name,
                "phone" to phone,
                "location" to location,
                "bio" to bio,
                "fullName" to fullName,
                "organizationName" to organizationName,
                "registrationId" to registrationId,
                "website" to website,
                "industry" to industry
            )
            val uid = firebaseAuth.currentUser?.uid ?: ""
            homeRepository.updateProfile(uid, profileData).collect { result ->
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    },
                    onFailure = { throwable ->
                        _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                    }
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
