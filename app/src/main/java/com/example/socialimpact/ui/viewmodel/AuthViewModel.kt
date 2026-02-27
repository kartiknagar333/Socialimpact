package com.example.socialimpact.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialimpact.data.repository.AuthRepositoryImpl
import com.example.socialimpact.domain.repository.AuthRepository
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for Auth screens. 
 * Prevents memory leaks by using viewModelScope and handles UI state safely.
 */
class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _authState = mutableStateOf<Result<AuthResult>?>(null)
    val authState: State<Result<AuthResult>?> = _authState

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.signUpWithEmail(email, password).collectLatest { result ->
                _authState.value = result
                _isLoading.value = false
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.signInWithGoogle(idToken).collectLatest { result ->
                _authState.value = result
                _isLoading.value = false
            }
        }
    }
}
