package com.example.socialimpact.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialimpact.domain.repository.AuthRepository
import com.example.socialimpact.ui.state.SigninUiState
import com.example.socialimpact.ui.state.SignupUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AuthViewModel — handles all authentication logic.
 */
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // ─── Sign Up State ─────────────────────────────────────────────────
    private val _signupUiState = MutableStateFlow(SignupUiState())
    val signupUiState: StateFlow<SignupUiState> = _signupUiState.asStateFlow()

    // ─── Sign In State ─────────────────────────────────────────────────
    private val _signinUiState = MutableStateFlow(SigninUiState())
    val signinUiState: StateFlow<SigninUiState> = _signinUiState.asStateFlow()

    // ─── Sign Up Input Handlers ─────────────────────────────────────────
    fun onSignupEmailChange(email: String) {
        _signupUiState.update { it.copy(email = email, emailError = null) }
    }

    fun onSignupPasswordChange(password: String) {
        _signupUiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onSignupConfirmPasswordChange(confirmPassword: String) {
        _signupUiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    // ─── Sign In Input Handlers ─────────────────────────────────────────
    fun onSigninEmailChange(email: String) {
        _signinUiState.update { it.copy(email = email, emailError = null) }
    }

    fun onSigninPasswordChange(password: String) {
        _signinUiState.update { it.copy(password = password, passwordError = null) }
    }

    // ─── Sign Up ────────────────────────────────────────────────────────
    fun signUp() {
        val state = _signupUiState.value

        // Validation
        var hasError = false
        if (state.email.isBlank()) {
            _signupUiState.update { it.copy(emailError = "Email is required") }
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _signupUiState.update { it.copy(emailError = "Enter a valid email") }
            hasError = true
        }
        if (state.password.isBlank()) {
            _signupUiState.update { it.copy(passwordError = "Password is required") }
            hasError = true
        } else if (state.password.length < 6) {
            _signupUiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            hasError = true
        }
        if (state.confirmPassword != state.password) {
            _signupUiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            hasError = true
        }
        if (hasError) return

        // Call repository
        viewModelScope.launch {
            _signupUiState.update { it.copy(isLoading = true) }
            authRepository.signUpWithEmail(state.email, state.password).collect { result ->
                result.fold(
                    onSuccess = {
                        _signupUiState.update { it.copy(isLoading = false, isSuccess = true) }
                    },
                    onFailure = { throwable ->
                        _signupUiState.update {
                            it.copy(isLoading = false, error = throwable.message)
                        }
                    }
                )
            }
        }
    }

    // ─── Sign In ────────────────────────────────────────────────────────
    fun signIn() {
        val state = _signinUiState.value

        // Validation
        var hasError = false
        if (state.email.isBlank()) {
            _signinUiState.update { it.copy(emailError = "Email is required") }
            hasError = true
        }
        if (state.password.isBlank()) {
            _signinUiState.update { it.copy(passwordError = "Password is required") }
            hasError = true
        }
        if (hasError) return

        // Call repository
        viewModelScope.launch {
            _signinUiState.update { it.copy(isLoading = true) }
            authRepository.signInWithEmail(state.email, state.password).collect { result ->
                result.fold(
                    onSuccess = {
                        _signinUiState.update { it.copy(isLoading = false, isSuccess = true) }
                    },
                    onFailure = { throwable ->
                        _signinUiState.update {
                            it.copy(isLoading = false, error = throwable.message)
                        }
                    }
                )
            }
        }
    }

    // ─── Google Sign In ─────────────────────────────────────────────────
    fun signInWithGoogle(idToken: String, isSignup: Boolean) {
        viewModelScope.launch {
            if (isSignup) {
                _signupUiState.update { it.copy(isLoading = true) }
                authRepository.signInWithGoogle(idToken, isSignup = true).collect { result ->
                    result.fold(
                        onSuccess = {
                            _signupUiState.update { it.copy(isLoading = false, isSuccess = true) }
                        },
                        onFailure = { throwable ->
                            _signupUiState.update {
                                it.copy(isLoading = false, error = throwable.message)
                            }
                        }
                    )
                }
            } else {
                _signinUiState.update { it.copy(isLoading = true) }
                authRepository.signInWithGoogle(idToken, isSignup = false).collect { result ->
                    result.fold(
                        onSuccess = {
                            _signinUiState.update { it.copy(isLoading = false, isSuccess = true) }
                        },
                        onFailure = { throwable ->
                            _signinUiState.update {
                                it.copy(isLoading = false, error = throwable.message)
                            }
                        }
                    )
                }
            }
        }
    }

    // ─── Logout ─────────────────────────────────────────────────────────
    /**
     * Correctly logs out from Firebase and resets the ViewModel state.
     */
    fun logout() {
        authRepository.logout()
        resetAuthState()
    }

    // ─── Reset State ────────────────────────────────────────────────────
    /**
     * Resets the UI states. Call this when navigating away or logging out
     * to ensure the next login attempt starts from a clean slate.
     */
    fun resetAuthState() {
        _signinUiState.value = SigninUiState()
        _signupUiState.value = SignupUiState()
    }

    // ─── Error Clearers ─────────────────────────────────────────────────
    fun clearSignupError() {
        _signupUiState.update { it.copy(error = null) }
    }

    fun clearSigninError() {
        _signinUiState.update { it.copy(error = null) }
    }
}
