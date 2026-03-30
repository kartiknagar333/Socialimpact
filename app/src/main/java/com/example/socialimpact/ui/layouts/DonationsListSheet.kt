package com.example.socialimpact.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.socialimpact.domain.model.Donation
import com.example.socialimpact.domain.model.HelpRequestPost
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DonationsListSheet(
    post: HelpRequestPost,
    donations: List<Donation>,
    isLoading: Boolean,
    isMyPost: Boolean,
    processingItems: Set<String>,
    onMarkReceived: (String, String, String, Int) -> Unit,
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
                        post = post,
                        donation = donation, 
                        isMyPost = isMyPost,
                        processingItems = processingItems,
                        onMarkReceived = { itemName, quantity, itemIndex -> 
                            onMarkReceived(donation.id, itemName, quantity, itemIndex) 
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
    post: HelpRequestPost,
    donation: Donation, 
    isMyPost: Boolean,
    processingItems: Set<String>,
    onMarkReceived: (String, String, Int) -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf<Triple<String, String, Int>?>(null) }

    if (showConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            title = { Text("Confirm Receipt") },
            text = { Text("Are you sure you have received ${showConfirmDialog?.second} of ${showConfirmDialog?.first}?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog?.let { onMarkReceived(it.first, it.second, it.third) }
                    showConfirmDialog = null
                }) { Text("Yes, Received", color = MaterialTheme.colorScheme.tertiary) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) { Text("Cancel", color = MaterialTheme.colorScheme.tertiary) }
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
            
            donation.dynamicNeed.forEachIndexed { index, item ->
                val isProcessing = processingItems.contains("${donation.id}-$index")
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
                                onClick = { showConfirmDialog = Triple(item.name, item.quantity, index) },
                                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "Approve ${item.quantity} ${post.getUnitByName(item.name)}",
                                    color = Color.Black,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    } else if (!isMyPost && item.isPending) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .padding(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color.White.copy(0.7f),
                                modifier = Modifier.size(16.dp) // Adjust size as needed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pending",
                                color = Color.White.copy(0.7f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )

                            Text(
                                text = "${item.quantity} ${post.getUnitByName(item.name)}",
                                color = Color.White.copy(0.7f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .padding(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint =  MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp) // Adjust size as needed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Received",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )

                            Text(
                                text = "${item.quantity} ${post.getUnitByName(item.name)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    

                }
            }
            
            donation.lastDonated?.let {
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
