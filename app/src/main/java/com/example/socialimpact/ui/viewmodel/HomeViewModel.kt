package com.example.socialimpact.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    val posts: Flow<PagingData<HelpRequestPost>> = repository.getHomePosts()
        .cachedIn(viewModelScope)

    fun logout() {
        // Handled via AuthViewModel or specific implementation if needed
    }
}
