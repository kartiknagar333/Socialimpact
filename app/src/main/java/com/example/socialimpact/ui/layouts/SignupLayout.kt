package com.example.socialimpact.ui.layouts

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialimpact.components.LightButton
import com.example.socialimpact.components.PrimaryButton
import com.example.socialimpact.components.PrimaryTextField
import com.example.socialimpact.ui.theme.SocialimpactTheme
import com.example.socialimpact.ui.viewmodel.AuthViewModel

/**
 * Sign Up Screen following Clean Architecture and modern Android practices.
 * Handles Email/Password sign up and Google authentication.
 */
@Composable
fun SignupLayout(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState
    val isLoading by viewModel.isLoading
    val context = LocalContext.current

    // Handle Auth States
    LaunchedEffect(authState) {
        authState?.let { result ->
            result.onSuccess {
                onSuccess()
            }
            result.onFailure { error ->
                Toast.makeText(context, error.message ?: "Authentication Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    SignupContent(
        isLoading = isLoading,
        onSignUp = { email, password -> viewModel.signUp(email, password) },
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun SignupContent(
    isLoading: Boolean,
    onSignUp: (String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    fun validateInputs(): Boolean {
        var isValid = true

        // Email validation
        if (email.isEmpty()) {
            emailError = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Invalid email format"
            isValid = false
        } else {
            emailError = null
        }

        // Password validation
        val passwordRegex = "^(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$".toRegex()
        if (password.isEmpty()) {
            passwordError = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            passwordError = "At least 8 characters required"
            isValid = false
        } else if (!password.contains("[A-Z]".toRegex())) {
            passwordError = "At least one uppercase letter required"
            isValid = false
        } else if (!password.contains("[!@#$%^&*(),.?\":{}|<>]".toRegex())) {
            passwordError = "At least one special character required"
            isValid = false
        } else {
            passwordError = null
        }

        // Confirm Password validation
        if (confirmPassword.isEmpty()) {
            confirmPasswordError = "Please confirm your password"
            isValid = false
        } else if (confirmPassword != password) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        } else {
            confirmPasswordError = null
        }

        return isValid
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Join our community to make a social impact",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Email Field
        PrimaryTextField(
            value = email,
            onValueChange = { 
                email = it
                if (emailError != null) emailError = null
            },
            label = "Email Address",
            leadingIcon = Icons.Default.Email,
            isError = emailError != null,
            supportingText = emailError,
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        PrimaryTextField(
            value = password,
            onValueChange = { 
                password = it
                if (passwordError != null) passwordError = null
            },
            label = "Password",
            leadingIcon = Icons.Default.Lock,
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError,
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        PrimaryTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                if (confirmPasswordError != null) confirmPasswordError = null
            },
            label = "Confirm Password",
            leadingIcon = Icons.Default.CheckCircle,
            visualTransformation = PasswordVisualTransformation(),
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError,
            imeAction = ImeAction.Done
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Up Button
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            PrimaryButton(
                text = "Sign Up",
                onClick = {
                    if (validateInputs()) {
                        onSignUp(email, password)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Divider or "Or" text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(" OR ", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign In Button
            LightButton(
                text = "Continue with Google",
                onClick = {
                    // Integration point for Google One Tap / Google Sign In
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onBack) {
            Text("Already have an account? Sign In")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupPreview() {
    SocialimpactTheme {
        SignupContent(
            isLoading = false,
            onSignUp = { _, _ -> },
            onBack = {}
        )
    }
}
