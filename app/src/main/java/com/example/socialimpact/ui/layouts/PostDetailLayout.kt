package com.example.socialimpact.ui.layouts

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.ui.activity.ProfileActivity
import com.example.socialimpact.ui.components.GlassyAuthBackground
import com.example.socialimpact.ui.viewmodel.DonationViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PostDetailLayout(
    post: HelpRequestPost,
    animatedVisibilityScope: AnimatedVisibilityScope,
    donationFactory: ViewModelProvider.Factory,
    onBack: () -> Unit
) {
    val donationViewModel: DonationViewModel = viewModel(factory = donationFactory)
    val donationUiState by donationViewModel.uiState.collectAsStateWithLifecycle()
    val processingItems by donationViewModel.processingItems.collectAsStateWithLifecycle()
    val observedPost by donationViewModel.observedPost.collectAsStateWithLifecycle()
    
    // Use the latest observed post data if available, otherwise use initial post
    val displayPost = observedPost ?: post
    // Check if the current user owns this post using PreferenceManager ID
    val isMyPost = remember(post.userId) {
        post.userId == donationViewModel.currentUserId
    }

    var showDonateSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    LaunchedEffect(post.id) {
        donationViewModel.fetchDonations(post.id)
        donationViewModel.startObservingPost(post.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isMyPost) return@clickable
                                val intent = Intent(context, ProfileActivity::class.java).apply {
                                    putExtra("myprofile", isMyPost)
                                    putExtra("userId", post.userId)
                                    putExtra("myusertype", post.userType)
                                    putExtra("username", post.userName)
                                }
                                context.startActivity(intent)
                            },
                        verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.tertiary.copy(0.2f)
                        ) {
                            val profileIcon: ImageVector = when (post.userType.lowercase()) {
                                "person" -> Icons.Default.Person
                                "ngo" -> Icons.Default.Groups
                                "corporation" -> Icons.Default.Business
                                else -> Icons.Default.Person
                            }
                            Icon(
                                imageVector = profileIcon,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = post.userName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = post.userType,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Donation History Button
                ExtendedFloatingActionButton(
                    onClick = { showHistorySheet = true },
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    text = { Text("Donations", fontWeight = FontWeight.Bold) }
                )

                // Right side: Donate Button
                if (!isMyPost) {
                    ExtendedFloatingActionButton(
                        onClick = { showDonateSheet = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black,
                        shape = CircleShape,
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        text = { Text("Donate", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, start = 24.dp, end = 24.dp, bottom = 120.dp)
        ) {
            // Shared Title
            Text(
                text = displayPost.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "title-${post.id}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Shared Description
            Text(
                text = displayPost.description,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 28.sp,
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "desc-${post.id}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )

            if (displayPost.fundAmount.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                val fundProgress = try {
                    val goal = displayPost.fundAmount.toFloat()
                    val received = displayPost.fundReceived.toFloat()
                    if (goal > 0) received / goal else 0f
                } catch (e: Exception) { 0f }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Funding", style = MaterialTheme.typography.labelSmall)
                                Text("${displayPost.fundReceived} / ${displayPost.fundAmount} $", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { fundProgress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // Dynamic Needs Section
            if (displayPost.dynamicNeeds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Special Requirements",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                displayPost.dynamicNeeds.forEach { item ->
                    val needProgress = try {
                        val goal = item.quantity.toFloat()
                        val received = item.received.toFloat()
                        if (goal > 0) received / goal else 0f
                    } catch (e: Exception) { 0f }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${item.received} / ${item.quantity} ${item.unit}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { needProgress.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.tertiary,
                                trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            )
                            val pendingCount = item.pending.toIntOrNull() ?: 0
                            if (pendingCount > 0) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    modifier = Modifier.align(Alignment.End),
                                    text = "Pending ${item.pending}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            DetailSection(Icons.Default.LocationOn, "Location", displayPost.address)
            DetailSection(Icons.Default.CalendarToday, "Timeline", "${displayPost.startDate} - ${displayPost.endDate}")

            // Tags
            val allTags = displayPost.selectedCategories + displayPost.selectedNeeds
            if (allTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Categories & Needs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allTags.forEach { tag ->
                        SuggestionChip(onClick = {}, label = { Text(tag, color = MaterialTheme.colorScheme.onBackground) })
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }

        // Modal Sheet for Donation Form
        if (showDonateSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDonateSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = null,
                containerColor = Color.Black,
                tonalElevation = 0.dp
            ) {
                GlassyAuthBackground(
                    modifier = Modifier.wrapContentHeight(),
                    backgroundColor = Color.Transparent
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BottomSheetDefaults.DragHandle(color = Color.White)
                        DonationSheetContent(
                            post = displayPost,
                            viewModel = donationViewModel,
                            onClose = { showDonateSheet = false }
                        )
                    }
                }
            }
        }

        // Modal Sheet for Donation History
        if (showHistorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showHistorySheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = null,
                containerColor = Color.Black,
                tonalElevation = 0.dp
            ) {
                GlassyAuthBackground(
                    modifier = Modifier.fillMaxHeight(0.8f),
                    backgroundColor = Color.Transparent
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BottomSheetDefaults.DragHandle(color = Color.White)
                        DonationsListSheet(
                            post = displayPost,
                            donations = donationUiState.donations,
                            isLoading = donationUiState.isLoading,
                            isMyPost = isMyPost,
                            processingItems = processingItems,
                            onMarkReceived = { donationId, itemName, quantity, itemIndex ->
                                donationViewModel.markItemAsReceived(post.id, donationId, itemName, quantity, itemIndex)
                            },
                            onClose = { showHistorySheet = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(icon: ImageVector, label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
