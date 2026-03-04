package com.example.socialimpact.ui.layouts

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialimpact.R
import com.example.socialimpact.components.GlassyAuthBackground
import com.example.socialimpact.components.LightButton
import com.example.socialimpact.components.PrimaryButton
import com.example.socialimpact.components.PrimaryTextField
import com.example.socialimpact.ui.state.SignupUiState
import com.example.socialimpact.ui.theme.SocialimpactTheme
import com.example.socialimpact.ui.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun SignupLayout(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.signupUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            val errorMessage = if (it.contains("email address is already in use")) {
                "This email is already registered."
            } else {
                it
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearSignupError()
        }
    }

    SignupContent(
        uiState = uiState,
        onEmailChange = viewModel::onSignupEmailChange,
        onPasswordChange = viewModel::onSignupPasswordChange,
        onConfirmPasswordChange = viewModel::onSignupConfirmPasswordChange,
        onSignUp = viewModel::signUp,
        onGoogleSignIn = {
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            coroutineScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = context
                    )
                    val credential = result.credential
                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        viewModel.signInWithGoogle(googleIdTokenCredential.idToken, isSignup = true)
                    }
                } catch (e: GetCredentialException) {
                    Log.e("Auth", "Credential Manager Error: ${e.message}")
                    Toast.makeText(context, "Google registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun SignupContent(
    uiState: SignupUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSignUp: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassyAuthBackground(modifier = modifier) {
        Column(
            modifier = Modifier
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
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Join our community to make a social impact",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                textAlign = TextAlign.Center
            )

            PrimaryTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Email Address",
                leadingIcon = Icons.Default.Email,
                isError = uiState.emailError != null,
                supportingText = uiState.emailError,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "Confirm Password",
                leadingIcon = Icons.Default.CheckCircle,
                visualTransformation = PasswordVisualTransformation(),
                isError = uiState.confirmPasswordError != null,
                supportingText = uiState.confirmPasswordError,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                PrimaryButton(
                    text = "Sign Up",
                    onClick = onSignUp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f),color = MaterialTheme.colorScheme.onBackground)
                    Text(" OR ", modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onBackground)
                    HorizontalDivider(modifier = Modifier.weight(1f),color = MaterialTheme.colorScheme.onBackground)
                }

                Spacer(modifier = Modifier.height(16.dp))

                LightButton(
                    text = "Continue with Google",
                    icon = Icons.Default.MailOutline,
                    onClick = onGoogleSignIn
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onBack) {
                Text("Already have an account? Sign In")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupPreview() {
    SocialimpactTheme {
        SignupContent(
            uiState = SignupUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSignUp = {},
            onGoogleSignIn = {},
            onBack = {}
        )
    }
}