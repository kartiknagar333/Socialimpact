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
import com.example.socialimpact.ui.state.SigninUiState
import com.example.socialimpact.ui.state.SignupUiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl(),
    private val validateEmail: ValidateEmail = ValidateEmail(),
    private val validatePassword: ValidatePassword = ValidatePassword(),
    private val validateConfirmPassword: ValidateConfirmPassword = ValidateConfirmPassword()
) : ViewModel() {

    // Signup State
    private val _signupUiState = mutableStateOf(SignupUiState())
    val signupUiState: State<SignupUiState> = _signupUiState

    // Signin State
    private val _signinUiState = mutableStateOf(SigninUiState())
    val signinUiState: State<SigninUiState> = _signinUiState

    // Signup Events
    fun onSignupEmailChange(email: String) {
        _signupUiState.value = _signupUiState.value.copy(email = email, emailError = null)
    }

    fun onSignupPasswordChange(password: String) {
        _signupUiState.value = _signupUiState.value.copy(password = password, passwordError = null)
    }

    fun onSignupConfirmPasswordChange(confirmPassword: String) {
        _signupUiState.value = _signupUiState.value.copy(confirmPassword = confirmPassword, confirmPasswordError = null)
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
            _signupUiState.value = _signupUiState.value.copy(
                emailError = emailResult.errorMessage,
                passwordError = passwordResult.errorMessage,
                confirmPasswordError = confirmPasswordResult.errorMessage
            )
            return
        }

        viewModelScope.launch {
            _signupUiState.value = _signupUiState.value.copy(isLoading = true)
            repository.signUpWithEmail(_signupUiState.value.email, _signupUiState.value.password).collectLatest { result ->
                result.onSuccess {
                    _signupUiState.value = _signupUiState.value.copy(isLoading = false, isSuccess = true)
                }
                result.onFailure { error ->
                    _signupUiState.value = _signupUiState.value.copy(isLoading = false, error = error.message)
                }
            }
        }
    }

    // Signin Events
    fun onSigninEmailChange(email: String) {
        _signinUiState.value = _signinUiState.value.copy(email = email, emailError = null)
    }

    fun onSigninPasswordChange(password: String) {
        _signinUiState.value = _signinUiState.value.copy(password = password, passwordError = null)
    }

    fun signIn() {
        val emailResult = validateEmail.execute(_signinUiState.value.email)
        val passwordResult = validatePassword.execute(_signinUiState.value.password)

        val hasError = listOf(emailResult, passwordResult).any { !it.successful }

        if (hasError) {
            _signinUiState.value = _signinUiState.value.copy(
                emailError = emailResult.errorMessage,
                passwordError = passwordResult.errorMessage
            )
            return
        }

        viewModelScope.launch {
            _signinUiState.value = _signinUiState.value.copy(isLoading = true)
            repository.signInWithEmail(_signinUiState.value.email, _signinUiState.value.password).collectLatest { result ->
                result.onSuccess {
                    _signinUiState.value = _signinUiState.value.copy(isLoading = false, isSuccess = true)
                }
                result.onFailure { error ->
                    _signinUiState.value = _signinUiState.value.copy(isLoading = false, error = error.message)
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String, isSignup: Boolean = true) {
        viewModelScope.launch {
            if (isSignup) _signupUiState.value = _signupUiState.value.copy(isLoading = true)
            else _signinUiState.value = _signinUiState.value.copy(isLoading = true)

            repository.signInWithGoogle(idToken).collectLatest { result ->
                result.onSuccess {
                    if (isSignup) _signupUiState.value = _signupUiState.value.copy(isLoading = false, isSuccess = true)
                    else _signinUiState.value = _signinUiState.value.copy(isLoading = false, isSuccess = true)
                }
                result.onFailure { error ->
                    if (isSignup) _signupUiState.value = _signupUiState.value.copy(isLoading = false, error = error.message)
                    else _signinUiState.value = _signinUiState.value.copy(isLoading = false, error = error.message)
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
        _signupUiState.value = _signupUiState.value.copy(error = null)
    }

    fun clearSigninError() {
        _signinUiState.value = _signinUiState.value.copy(error = null)
    }
}
