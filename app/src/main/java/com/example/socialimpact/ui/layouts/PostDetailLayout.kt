package com.example.socialimpact.ui.layouts

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialimpact.domain.model.Donation
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.model.NeedItem
import com.example.socialimpact.ui.activity.ProfileActivity
import com.example.socialimpact.ui.components.GlassyAuthBackground
import com.example.socialimpact.ui.viewmodel.DonationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PostDetailLayout(
    post: HelpRequestPost,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isMyPost: Boolean,
    donationFactory: ViewModelProvider.Factory,
    onBack: () -> Unit
) {
    val donationViewModel: DonationViewModel = viewModel(factory = donationFactory)
    val donationUiState by donationViewModel.uiState.collectAsStateWithLifecycle()
    val processingItems by donationViewModel.processingItems.collectAsStateWithLifecycle()
    val observedPost by donationViewModel.observedPost.collectAsStateWithLifecycle()
    
    // Use the latest observed post data if available, otherwise use initial post
    val displayPost = observedPost ?: post
    
    var showDonateSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                                else -> {
                                    Icons.Default.Person
                                }
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
                modifier = Modifier
                    .fillMaxWidth(),
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                modifier = Modifier.align(Alignment.End),
                                text = "Pending ${item.pending}  ${item.unit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
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
                            donations = donationUiState.donations,
                            isLoading = donationUiState.isLoading,
                            isMyPost = isMyPost,
                            processingItems = processingItems,
                            onMarkReceived = { donationId, itemName, quantity ->
                                donationViewModel.markItemAsReceived(post.id, donationId, itemName, quantity)
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
private fun DonationsListSheet(
    donations: List<Donation>,
    isLoading: Boolean,
    isMyPost: Boolean,
    processingItems: Set<String>,
    onMarkReceived: (String, String, String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Donation History",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(8.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else if (donations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No donations yet", color = Color.White.copy(0.7f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(donations) { donation ->
                    DonationItemCard(
                        donation = donation, 
                        isMyPost = isMyPost,
                        processingItems = processingItems,
                        onMarkReceived = { itemName, quantity -> 
                            onMarkReceived(donation.id, itemName, quantity) 
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onClose) {
            Text("Close", color = Color.White)
        }
    }
}

@Composable
private fun DonationItemCard(
    donation: Donation, 
    isMyPost: Boolean,
    processingItems: Set<String>,
    onMarkReceived: (String, String) -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    if (showConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { Text("Confirm Receipt") },
            text = { Text("Are you sure you have received ${showConfirmDialog?.second} of ${showConfirmDialog?.first}?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog?.let { onMarkReceived(it.first, it.second) }
                    showConfirmDialog = null
                }) { Text("Yes, Received") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp).clip(CircleShape),
                    color = Color.White.copy(0.2f)
                ) {
                    val icon = when (donation.userType.lowercase()) {
                        "person" -> Icons.Default.Person
                        "ngo" -> Icons.Default.Groups
                        else -> Icons.Default.Business
                    }
                    Icon(icon, null, modifier = Modifier.padding(6.dp), tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(donation.userName, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(donation.userType, color = Color.White.copy(0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            donation.dynamicNeed.forEach { item ->
                val isProcessing = processingItems.contains("${donation.id}-${item.name}")
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (isMyPost && item.isPending) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp).padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            TextButton(
                                onClick = { showConfirmDialog = item.name to item.quantity },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "Received",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .background(
                                            color = Color.Black.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    } else if (!isMyPost && item.isPending) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Pending",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp).padding(end = 8.dp)
                        )

                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Received",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp).padding(end = 8.dp)
                        )
                    }
                    
                    Text(
                        text = item.quantity,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            donation.lastDonated?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(it.toDate()),
                    color = Color.White.copy(0.5f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun DonationSheetContent(
    post: HelpRequestPost,
    onClose: () -> Unit
) {
    var selection by remember { 
        mutableStateOf<Any?>(
            if (post.fundAmount.isNotEmpty()) "fund" else post.dynamicNeeds.firstOrNull()
        ) 
    }
    var amountOrQuantity by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.White
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Make a Donation",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chipColors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color.White,
                selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                labelColor = Color.White
            )

            if (post.fundAmount.isNotEmpty()) {
                FilterChip(
                    selected = selection == "fund",
                    onClick = { 
                        selection = "fund"
                        amountOrQuantity = ""
                    },
                    label = { Text("Fund", modifier = Modifier.padding(horizontal = 8.dp)) },
                    leadingIcon = null,
                    trailingIcon = null,
                    shape = CircleShape,
                    modifier = Modifier.height(48.dp),
                    colors = chipColors,
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selection == "fund",
                        borderColor = Color.White,
                        selectedBorderColor = Color.White
                    )
                )
            }

            post.dynamicNeeds.forEach { item ->
                FilterChip(
                    selected = selection == item,
                    onClick = { 
                        selection = item
                        amountOrQuantity = ""
                    },
                    label = { Text(item.name, modifier = Modifier.padding(horizontal = 8.dp)) },
                    leadingIcon = null,
                    trailingIcon = null,
                    shape = CircleShape,
                    modifier = Modifier.height(48.dp),
                    colors = chipColors,
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selection == item,
                        borderColor = Color.White,
                        selectedBorderColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (val selected = selection) {
            "fund" -> {
                Text(
                    text = "Money goes through Stripe securely to ensure direct impact.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { /* TODO: Stripe Payment */ },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments, 
                        contentDescription = null, 
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Donate Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            is NeedItem -> {
                Text(
                    text = "Physical donations will be officially recorded once received. Your contribution remains in 'Pending' status until confirmation by the recipient.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = selection is NeedItem,
                    enter = slideInHorizontally(animationSpec = tween(durationMillis = 1000)) { -it } + fadeIn()
                ) {
                    val inputTextStyle = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = amountOrQuantity,
                        onValueChange = { amountOrQuantity = it },
                        label = {
                            Text("Quantity", color = Color.White, fontWeight = FontWeight.SemiBold)
                        },
                        prefix = {
                            Text(
                                "${selected.unit} ",
                                style = inputTextStyle // 👈 SAME style
                            )
                        },
                        textStyle = inputTextStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.LightGray,
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { /* TODO: Item Donation Submission */ },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory, 
                        contentDescription = null, 
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Donation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onClose) {
            Text("Cancel", color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun DetailSection(icon: ImageVector, label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline); Spacer(modifier = Modifier.width(16.dp)); Column { Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline); Text(text = value, style = MaterialTheme.typography.bodyLarge) } }
}
