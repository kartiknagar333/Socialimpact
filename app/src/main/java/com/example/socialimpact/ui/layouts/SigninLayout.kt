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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialimpact.R
import com.example.socialimpact.ui.components.GlassyAuthBackground
import com.example.socialimpact.ui.components.LightButton
import com.example.socialimpact.ui.components.PrimaryButton
import com.example.socialimpact.ui.components.PrimaryTextField
import com.example.socialimpact.ui.state.SigninUiState
import com.example.socialimpact.ui.theme.SocialimpactTheme
import com.example.socialimpact.ui.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun SigninLayout(
    onSignup: () -> Unit,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    factory: ViewModelProvider.Factory,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(factory = factory)
) {
    val uiState by viewModel.signinUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Log.d("AUTH_DEBUG", "Signin Success - Navigating Home")
            Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
            onSuccess()
            viewModel.resetAuthState() 
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Log.e("AUTH_DEBUG", "Signin Error: $it")
            val errorMessage = when {
                it.contains("invalid-credential") || it.contains("wrong-password") ->
                    "Incorrect email or password."
                it.contains("user-not-found") ->
                    "No account found with this email."
                it.contains("network-request-failed") ->
                    "Network error. Check your connection."
                else -> it
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearSigninError()
        }
    }

    SigninContent(
        uiState = uiState,
        onEmailChange = viewModel::onSigninEmailChange,
        onPasswordChange = viewModel::onSigninPasswordChange,
        onSignIn = { 
            Log.d("AUTH_DEBUG", "Sign In Button Clicked")
            viewModel.signIn() 
        },
        onGoogleSignIn = {
            Log.d("AUTH_DEBUG", "Google Sign In Clicked")
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
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        viewModel.signInWithGoogle(
                            googleIdTokenCredential.idToken,
                            isSignup = false
                        )
                    }
                } catch (e: GetCredentialException) {
                    Log.e("AUTH_DEBUG", "Credential Manager Error: ${e.message}")
                    Toast.makeText(
                        context,
                        "Google Sign-In failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        },
        onSignup = onSignup,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun SigninContent(
    uiState: SigninUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onSignup: () -> Unit,
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
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Sign in to continue your social impact journey",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondary,
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
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                PrimaryButton(
                    text = "Sign In",
                    onClick = onSignIn
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        " OR ",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LightButton(
                    text = "Continue with Google",
                    icon = Icons.Default.MailOutline,
                    onClick = onGoogleSignIn
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onSignup) {
                Text("Don't have an account? Sign Up",color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SigninPreview() {
    SocialimpactTheme {
        SigninContent(
            uiState = SigninUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onSignIn = {},
            onGoogleSignIn = {},
            onSignup = {},
            onBack = {}
        )
    }
}
