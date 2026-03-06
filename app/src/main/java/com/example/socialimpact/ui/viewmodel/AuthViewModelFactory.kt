package com.example.socialimpact.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

/**
 * AuthViewModelFactory — bridges Dagger and ViewModel creation.
 * 
 * Updated to use Provider<AuthViewModel> to ensure a fresh instance
 * is created whenever requested.
 */
class AuthViewModelFactory @Inject constructor(
    private val authViewModelProvider: Provider<AuthViewModel>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return authViewModelProvider.get() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
