package com.example.socialimpact.ui.layouts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.socialimpact.components.PrimaryButton
import com.example.socialimpact.components.PrimaryTextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentLayout(
    onBack: () -> Unit
) {
    val tabs = listOf("Send", "Receive")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            CustomTabs(
                tabs = tabs,
                pagerState = pagerState,
                coroutineScope = coroutineScope)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> SendLayout()
                    1 -> ReceiveLayout()
                }
            }
        }
    }
}

@Composable
fun SendLayout() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CustomTabs(
    tabs: List<String>,
    pagerState: PagerState,
    coroutineScope: CoroutineScope
) {
    SecondaryTabRow(
        selectedTabIndex = pagerState.currentPage,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        containerColor = Color.Transparent,
        divider = {},
        indicator = {}
    ) {
        tabs.forEachIndexed { index, title ->

            val selected = pagerState.currentPage == index

            val backgroundColor by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                } else {
                    Color.Transparent
                },
                animationSpec = tween(durationMillis = 300),
                label = "tabBg"
            )

            val textColor by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = tween(durationMillis = 300),
                label = "tabText"
            )

            Tab(
                selected = selected,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                selectedContentColor = textColor,
                unselectedContentColor = textColor,
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .fillMaxWidth()
                        .animateContentSize()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = textColor,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiveLayout() {
    var currentStep by remember { mutableIntStateOf(1) }

    // Step 1 State
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var countryName by remember { mutableStateOf("") }

    val isStep1Valid = fullName.isNotBlank() && email.isNotBlank() && countryName.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Step Indicator
        LinearProgressIndicator(
            progress = { currentStep / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.tertiary,
            trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        )

        when (currentStep) {
            1 -> {
                ReceiveStep1(
                    fullName = fullName,
                    onFullNameChange = { fullName = it },
                    email = email,
                    onEmailChange = { email = it },
                    countryName = countryName,
                    onCountryChange = { countryName = it },
                    onContinue = { currentStep = 2 },
                    isContinueEnabled = isStep1Valid
                )
            }
            2 -> {
                ReceiveStep2(
                    onBack = { currentStep = 1 },
                    onContinue = { currentStep = 3 }
                )
            }
            3 -> {
                ReceiveStep3(
                    onBack = { currentStep = 2 },
                    onFinish = { /* Handle completion */ }
                )
            }
        }
    }
}

@Composable
fun ReceiveStep1(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    countryName: String,
    onCountryChange: (String) -> Unit,
    onContinue: () -> Unit,
    isContinueEnabled: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Personal Information",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        PrimaryTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = "Full Name",
            leadingIcon = Icons.Default.Person
        )

        PrimaryTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email Address",
            leadingIcon = Icons.Default.Email
        )

        PrimaryTextField(
            value = countryName,
            onValueChange = onCountryChange,
            label = "Country Name",
            leadingIcon = Icons.Default.Language
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = "Continue",
            onClick = onContinue,
            enabled = isContinueEnabled
        )

    }
}

@Composable
fun ReceiveStep2(onBack: () -> Unit, onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Step 2: Payment Method Details", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) { Text("Continue") }
        }
    }
}

@Composable
fun ReceiveStep3(onBack: () -> Unit, onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Step 3: Confirm & Receive", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) { Text("Finish") }
        }
    }
}
