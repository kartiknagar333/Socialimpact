package com.example.socialimpact.ui.layouts

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.socialimpact.domain.model.HelpRequestPost

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun SharedTransitionScope.PostDetailLayout(
    post: HelpRequestPost,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Funding Goal", style = MaterialTheme.typography.labelSmall)
                            Text(post.fundAmount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Dynamic Needs / Special Requirements Section
            if (post.dynamicNeeds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Special Requirements",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                post.dynamicNeeds.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${item.quantity} ${item.unit}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
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
                Text("Categories & Needs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allTags.forEach { tag ->
                        SuggestionChip(onClick = {}, label = { Text(tag) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}



@Composable
private fun DetailSection(icon: ImageVector, label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline); Spacer(modifier = Modifier.width(16.dp)); Column { Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline); Text(text = value, style = MaterialTheme.typography.bodyLarge) } }

}
