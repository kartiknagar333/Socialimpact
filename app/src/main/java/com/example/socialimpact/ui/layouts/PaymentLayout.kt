package com.example.socialimpact.ui.layouts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
                    0 -> SenderPaymentScreen()
                    1 -> ReceiverOnboardingScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
