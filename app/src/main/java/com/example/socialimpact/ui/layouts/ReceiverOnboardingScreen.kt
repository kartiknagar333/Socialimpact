package com.example.socialimpact.ui.layouts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialimpact.ui.state.ReceiverOnboardingUiState
import com.example.socialimpact.ui.viewmodel.ReceiverOnboardingViewModel
import com.example.socialimpact.util.UrlOpener

@Composable
fun ReceiverOnboardingScreen(
    viewModel: ReceiverOnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects: show snackbar on error
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetErrorMessage()
        }
    }

    // Handle side effects: open URL when available
    LaunchedEffect(uiState.onboardingUrl) {
        if (uiState.onboardingUrl.isNotEmpty()) {
            UrlOpener.openUrl(context, uiState.onboardingUrl)
            viewModel.onUrlOpened()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        ReceiverOnboardingContent(
            uiState = uiState,
            onOpenOnboarding = { viewModel.onOpenOnboardingClicked() },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun ReceiverOnboardingContent(
    uiState: ReceiverOnboardingUiState,
    onOpenOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Receiver Onboarding",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (uiState.isOnboardingComplete) 
                "Your account is active. You can manage your payouts and bank details below." 
            else 
                "You must complete your Stripe onboarding to start receiving donations directly to your bank account.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Current Status",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = uiState.statusLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.isOnboardingComplete) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onOpenOnboarding,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = if (uiState.isOnboardingComplete) "Manage Stripe Account" else "Open Onboarding",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReceiverOnboardingPreview() {
    MaterialTheme {
        ReceiverOnboardingContent(
            uiState = ReceiverOnboardingUiState(
                statusLabel = "Not Started",
                isLoading = false
            ),
            onOpenOnboarding = {}
        )
    }
}
