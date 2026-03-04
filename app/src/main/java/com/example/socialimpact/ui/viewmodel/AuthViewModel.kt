package com.example.socialimpact.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialimpact.data.repository.AuthRepositoryImpl
import com.example.socialimpact.domain.repository.AuthRepository
import com.example.socialimpact.domain.usecase.ValidateConfirmPassword
import com.example.socialimpact.domain.usecase.ValidateEmail
import com.example.socialimpact.domain.usecase.ValidatePassword
import com.example.socialimpact.ui.state.SigninUiState
import com.example.socialimpact.ui.state.SignupUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl(),
    private val validateEmail: ValidateEmail = ValidateEmail(),
    private val validatePassword: ValidatePassword = ValidatePassword(),
    private val validateConfirmPassword: ValidateConfirmPassword = ValidateConfirmPassword()
) : ViewModel() {

    // Signup State
    private val _signupUiState = MutableStateFlow(SignupUiState())
    val signupUiState: StateFlow<SignupUiState> = _signupUiState.asStateFlow()

    // Signin State
    private val _signinUiState = MutableStateFlow(SigninUiState())
    val signinUiState: StateFlow<SigninUiState> = _signinUiState.asStateFlow()

    // Signup Events
    fun onSignupEmailChange(email: String) {
        _signupUiState.update { it.copy(email = email, emailError = null) }
    }

    fun onSignupPasswordChange(password: String) {
        _signupUiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onSignupConfirmPasswordChange(confirmPassword: String) {
        _signupUiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun signUp() {
        val emailResult = validateEmail.execute(_signupUiState.value.email)
        val passwordResult = validatePassword.execute(_signupUiState.value.password)
        val confirmPasswordResult = validateConfirmPassword.execute(
            _signupUiState.value.password,
            _signupUiState.value.confirmPassword
        )

        val hasError = listOf(emailResult, passwordResult, confirmPasswordResult).any { !it.successful }

        if (hasError) {
            _signupUiState.update {
                it.copy(
                    emailError = emailResult.errorMessage,
                    passwordError = passwordResult.errorMessage,
                    confirmPasswordError = confirmPasswordResult.errorMessage
                )
            }
            return
        }

        viewModelScope.launch {
            _signupUiState.update { it.copy(isLoading = true) }
            repository.signUpWithEmail(_signupUiState.value.email, _signupUiState.value.password).collectLatest { result ->
                result.onSuccess {
                    _signupUiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                result.onFailure { error ->
                    _signupUiState.update { it.copy(isLoading = false, error = error.message) }
                }
            }
        }
    }

    // Signin Events
    fun onSigninEmailChange(email: String) {
        _signinUiState.update { it.copy(email = email, emailError = null) }
    }

    fun onSigninPasswordChange(password: String) {
        _signinUiState.update { it.copy(password = password, passwordError = null) }
    }

    fun signIn() {
        val emailResult = validateEmail.execute(_signinUiState.value.email)
        val passwordResult = validatePassword.execute(_signinUiState.value.password)

        val hasError = listOf(emailResult, passwordResult).any { !it.successful }

        if (hasError) {
            _signinUiState.update {
                it.copy(
                    emailError = emailResult.errorMessage,
                    passwordError = passwordResult.errorMessage
                )
            }
            return
        }

        viewModelScope.launch {
            _signinUiState.update { it.copy(isLoading = true) }
            repository.signInWithEmail(_signinUiState.value.email, _signinUiState.value.password).collectLatest { result ->
                result.onSuccess {
                    _signinUiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                result.onFailure { error ->
                    _signinUiState.update { it.copy(isLoading = false, error = error.message) }
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String, isSignup: Boolean = true) {
        viewModelScope.launch {
            if (isSignup) _signupUiState.update { it.copy(isLoading = true) }
            else _signinUiState.update { it.copy(isLoading = true) }

            repository.signInWithGoogle(idToken).collectLatest { result ->
                result.onSuccess {
                    if (isSignup) _signupUiState.update { it.copy(isLoading = false, isSuccess = true) }
                    else _signinUiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                result.onFailure { error ->
                    if (isSignup) _signupUiState.update { it.copy(isLoading = false, error = error.message) }
                    else _signinUiState.update { it.copy(isLoading = false, error = error.message) }
                }
            }
        }
    }

    fun logout() {
        repository.logout()
        // Reset states
        _signinUiState.value = SigninUiState()
        _signupUiState.value = SignupUiState()
    }

    fun clearSignupError() {
        _signupUiState.update { it.copy(error = null) }
    }

    fun clearSigninError() {
        _signinUiState.update { it.copy(error = null) }
    }
}