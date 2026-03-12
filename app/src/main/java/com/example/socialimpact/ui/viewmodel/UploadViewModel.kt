package com.example.socialimpact.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.socialimpact.domain.repository.HomeRepository
import com.example.socialimpact.ui.layouts.NeedItem
import com.example.socialimpact.ui.layouts.ProfileType
import com.example.socialimpact.ui.state.UploadPostUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class UploadViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadPostUiState())
    val uiState = _uiState.asStateFlow()

    private val _userType = MutableStateFlow<ProfileType?>(null)
    val userType = _userType.asStateFlow()

    init {
        loadUserType()
    }

    private fun loadUserType() {
        val profile = homeRepository.getLocalProfile()
        _userType.value = profile?.type ?: ProfileType.PERSON
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onFundAmountChange(amount: String) {
        _uiState.update { it.copy(fundAmount = amount) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onStartDateChange(date: String) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun onEndDateChange(date: String) {
        _uiState.update { it.copy(endDate = date) }
    }

    fun toggleNeed(need: String) {
        _uiState.update { state ->
            val current = state.selectedNeeds.toMutableList()
            if (current.contains(need)) current.remove(need) else current.add(need)
            state.copy(selectedNeeds = current)
        }
    }

    fun toggleCategory(category: String) {
        _uiState.update { state ->
            val current = state.selectedCategories.toMutableList()
            if (current.contains(category)) current.remove(category) else current.add(category)
            state.copy(selectedCategories = current)
        }
    }

    fun addDynamicNeed() {
        _uiState.update { it.copy(dynamicNeeds = it.dynamicNeeds + NeedItem()) }
    }

    fun removeDynamicNeed(index: Int) {
        _uiState.update { state ->
            if (state.dynamicNeeds.size > 1) {
                val current = state.dynamicNeeds.toMutableList()
                current.removeAt(index)
                state.copy(dynamicNeeds = current)
            } else state
        }
    }

    fun updateDynamicNeed(index: Int, item: NeedItem) {
        _uiState.update { state ->
            val current = state.dynamicNeeds.toMutableList()
            current[index] = item
            state.copy(dynamicNeeds = current)
        }
    }
}
