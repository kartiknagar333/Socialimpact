package com.example.socialimpact.ui.layouts

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.socialimpact.domain.model.HelpRequestPost
import com.example.socialimpact.domain.model.NeedItem
import com.example.socialimpact.ui.components.GlassyAuthBackground

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun SharedTransitionScope.PostDetailLayout(
    post: HelpRequestPost,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    // skipPartiallyExpanded = true ensures the sheet opens to fit its content immediately
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
            ExtendedFloatingActionButton(
                onClick = { showSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
                shape = CircleShape,
                icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                text = { Text("Donate Now", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            // Shared Title
            Text(
                text = post.title,
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
                text = post.description,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 28.sp,
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "desc-${post.id}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )

            if (post.fundAmount.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                val fundProgress = try {
                    val goal = post.fundAmount.toFloat()
                    val received = post.fundReceived.toFloat()
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
                                Text("${post.fundReceived} / ${post.fundAmount} $", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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

            // Dynamic Needs / Special Requirements Section
            if (post.dynamicNeeds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Special Requirements",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                post.dynamicNeeds.forEach { item ->
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
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            DetailSection(Icons.Default.LocationOn, "Location", post.address)
            DetailSection(Icons.Default.CalendarToday, "Timeline", "${post.startDate} - ${post.endDate}")

            // Tags
            val allTags = post.selectedCategories + post.selectedNeeds
            if (allTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Categories & Needs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allTags.forEach { tag ->
                        SuggestionChip(onClick = {}, label = { Text(tag,
                            color = MaterialTheme.colorScheme.onBackground) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = null, // Disable default handle
                containerColor = Color.Black, // Solid black base
                tonalElevation = 0.dp
            ) {
                // Glassy background now covers the entire sheet content
                GlassyAuthBackground(
                    modifier = Modifier.wrapContentHeight(),
                    backgroundColor = Color.Transparent
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Manual DragHandle inside the glassy area
                        BottomSheetDefaults.DragHandle(color = Color.White)
                        
                        DonationSheetContent(
                            post = post,
                            onClose = { showSheet = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonationSheetContent(
    post: HelpRequestPost,
    onClose: () -> Unit
) {
    // Default selection logic: select first available option
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

        // Horizontal Chip Row
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chipColors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color.White.copy(0.9f),
                selectedLabelColor = MaterialTheme.colorScheme.tertiary,
                labelColor = Color.White.copy(0.5f),
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
                        borderColor = Color.White.copy(0.5f),
                        selectedBorderColor = Color.White,
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
                        borderColor = Color.White.copy(0.5f),
                        selectedBorderColor = Color.White,                    )
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
                    text = "Donation will be counted when they receive meanwhile it will be under pending status.",
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
                    OutlinedTextField(
                        value = amountOrQuantity,
                        onValueChange = { amountOrQuantity = it },
                        label = { Text("Quantity", color = Color.White, fontWeight = FontWeight.SemiBold) },
                        prefix = { Text("${selected.unit} ", fontWeight = FontWeight.Bold, color = Color.White) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            focusedLeadingIconColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedBorderColor = Color.LightGray,
                            unfocusedLeadingIconColor = Color.LightGray,
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
            Text("Cancel", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DetailSection(icon: ImageVector, label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline); Spacer(modifier = Modifier.width(16.dp)); Column { Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline); Text(text = value, style = MaterialTheme.typography.bodyLarge) } }

}
