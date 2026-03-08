package com.example.socialimpact.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.socialimpact.domain.repository.HomeRepository
import com.example.socialimpact.ui.layouts.ProfileType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class UploadViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _userType = MutableStateFlow<ProfileType?>(null)
    val userType = _userType.asStateFlow()

    init {
        loadUserType()
    }

    private fun loadUserType() {
        val profile = homeRepository.getLocalProfile()
        _userType.value = profile?.type ?: ProfileType.PERSON
    }
    
    // TODO: Implement actual upload logic here
}
