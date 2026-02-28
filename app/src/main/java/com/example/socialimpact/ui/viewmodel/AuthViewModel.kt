package com.example.socialimpact.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialimpact.data.repository.AuthRepositoryImpl
import com.example.socialimpact.domain.repository.AuthRepository
import com.example.socialimpact.domain.usecase.ValidateConfirmPassword
import com.example.socialimpact.domain.usecase.ValidateEmail
import com.example.socialimpact.domain.usecase.ValidatePassword
import com.example.socialimpact.ui.viewmodel.SignupUiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl(),
    private val validateEmail: ValidateEmail = ValidateEmail(),
    private val validatePassword: ValidatePassword = ValidatePassword(),
    private val validateConfirmPassword: ValidateConfirmPassword = ValidateConfirmPassword()
) : ViewModel() {

    private val _uiState = mutableStateOf(SignupUiState())
    val uiState: State<SignupUiState> = _uiState

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, emailError = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, passwordError = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, confirmPasswordError = null)
    }

    fun signUp() {
        val emailResult = validateEmail.execute(_uiState.value.email)
        val passwordResult = validatePassword.execute(_uiState.value.password)
        val confirmPasswordResult = validateConfirmPassword.execute(
            _uiState.value.password,
            _uiState.value.confirmPassword
        )

        val hasError = listOf(
            emailResult,
            passwordResult,
            confirmPasswordResult
        ).any { !it.successful }

        if (hasError) {
            _uiState.value = _uiState.value.copy(
                emailError = emailResult.errorMessage,
                passwordError = passwordResult.errorMessage,
                confirmPasswordError = confirmPasswordResult.errorMessage
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.signUpWithEmail(_uiState.value.email, _uiState.value.password).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                result.onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.signInWithGoogle(idToken).collectLatest { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                result.onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
