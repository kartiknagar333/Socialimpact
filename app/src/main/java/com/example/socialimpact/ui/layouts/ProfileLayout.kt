package com.example.socialimpact.ui.layouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.repository.LocalProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileLayout(
    profile: LocalProfile?,
    myPosts: List<HelpRequestPost>,
    isMyProfile: Boolean,
    onBack: () -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    // Manual state to track collapsing header height
    var measuredHeaderHeightPx by remember { mutableFloatStateOf(0f) }
    var headerOffsetHeightPx by remember { mutableFloatStateOf(0f) }

    // NestedScrollConnection to coordinate header collapse and list scrolling
    val nestedScrollConnection = remember(measuredHeaderHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (measuredHeaderHeightPx <= 0f) return Offset.Zero
                
                val delta = available.y
                if (delta < 0) { // Scrolling up (finger moving up)
                    val oldOffset = headerOffsetHeightPx
                    // Decrease header height until it reaches 0
                    headerOffsetHeightPx = (headerOffsetHeightPx + delta).coerceIn(-measuredHeaderHeightPx, 0f)
                    val consumed = headerOffsetHeightPx - oldOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (measuredHeaderHeightPx <= 0f) return Offset.Zero
                
                val delta = available.y
                if (delta > 0) { // Scrolling down (finger moving down)
                    val oldOffset = headerOffsetHeightPx
                    // Expand header height until it reaches its original size
                    headerOffsetHeightPx = (headerOffsetHeightPx + delta).coerceIn(-measuredHeaderHeightPx, 0f)
                    val consumedNow = headerOffsetHeightPx - oldOffset
                    return Offset(0f, consumedNow)
                }
                return Offset.Zero
            }
        }
    }

    val showCollapsedInfo by remember {
        derivedStateOf { 
            if (measuredHeaderHeightPx > 0) {
                headerOffsetHeightPx < -measuredHeaderHeightPx * 0.5f 
            } else false
        }
    }

    val tabs = listOf("About", "My Impact", "Impacted By")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = { 
                    if (profile != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AnimatedVisibility(
                                visible = showCollapsedInfo,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                val profileIcon: ImageVector = when (profile.type) {
                                    ProfileType.PERSON -> Icons.Default.Person
                                    ProfileType.NGO -> Icons.Default.Groups
                                    ProfileType.CORPORATION -> Icons.Default.Business
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = profileIcon,
                                            contentDescription = null,
                                            modifier = Modifier.padding(6.dp),
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                            }
                            
                            val displayName = if (profile.type == ProfileType.PERSON) profile.fullName else profile.organizationName
                            Text(
                                text = if (showCollapsedInfo) displayName else (if (isMyProfile) "My Profile" else "Profile"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Text(if (isMyProfile) "My Profile" else "Profile")
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
            if (isMyProfile) {
                FloatingActionButton(
                    onClick = onUploadClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Post")
                }
            }
        }
    ) { innerPadding ->
        if (profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 1. Collapsing Header
                val headerModifier = if (measuredHeaderHeightPx <= 0f) {
                    Modifier.wrapContentHeight()
                } else {
                    Modifier.height(with(density) { (measuredHeaderHeightPx + headerOffsetHeightPx).toDp().coerceAtLeast(0.dp) })
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(headerModifier)
                        .clipToBounds()
                ) {
                    // This internal Box measures the content height only once
                    Box(modifier = Modifier.onGloballyPositioned { 
                        if (measuredHeaderHeightPx <= 0f) {
                            measuredHeaderHeightPx = it.size.height.toFloat()
                        }
                    }) {
                        ProfileHeader(profile = profile)
                    }
                }

                // 2. Tab Layout (Always below header, effectively sticks when header height is 0)
                SecondaryScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    edgePadding = 16.dp, 
                    divider = {},
                    indicator = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        val selected = pagerState.currentPage == index
                        Tab(
                            selected = selected,
                            onClick = { 
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    else Color.Transparent
                                ),
                            text = { 
                                Text(
                                    text = title,
                                    color = if (selected) MaterialTheme.colorScheme.tertiary else Color.Gray,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        )
                    }
                }

                // 3. Pager Content (Fills remaining screen space)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    when (page) {
                        0 -> ProfileAboutTab(profile = profile)
                        1 -> MyImpactTab(posts = myPosts)
                        2 -> ImpactedByTab()
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(profile: LocalProfile) {
    var showUrlDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("Open Website") },
            text = { Text("Do you want to open ${profile.website} in your browser?") },
            confirmButton = {
                TextButton(onClick = {
                    showUrlDialog = false
                    try {
                        val url = if (!profile.website.startsWith("http://") && !profile.website.startsWith("https://")) {
                            "https://${profile.website}"
                        } else {
                            profile.website
                        }
                        uriHandler.openUri(url)
                    } catch (e: Exception) {
                    }
                }) {
                    Text("Open")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val profileIcon: ImageVector = when (profile.type) {
            ProfileType.PERSON -> Icons.Default.Person
            ProfileType.NGO -> Icons.Default.Groups
            ProfileType.CORPORATION -> Icons.Default.Business
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
            modifier = Modifier.size(100.dp)
        ) {
            Icon(
                imageVector = profileIcon,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = profile.type.name,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val displayName = if (profile.type == ProfileType.PERSON) profile.fullName else profile.organizationName
        Text(
            text = displayName.ifBlank { "No Name Provided" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        if (profile.website.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable { showUrlDialog = true }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = profile.website,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.Underline
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun ProfileAboutTab(profile: LocalProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (profile.type == ProfileType.PERSON) "Bio" else "Our Mission",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = profile.bio.ifBlank { "No mission or bio shared yet." },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        if (profile.location.isNotBlank()) {
            InfoRow(icon = Icons.Default.LocationOn, label = "Location", value = profile.location)
        }
        if (profile.phone.isNotBlank()) {
            InfoRow(icon = Icons.Default.Phone, label = "Phone", value = profile.phone)
        }
        if (profile.industry.isNotBlank()) {
            InfoRow(icon = Icons.Default.Category, label = "Industry", value = profile.industry)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            // Changed icon color to tertiary
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
