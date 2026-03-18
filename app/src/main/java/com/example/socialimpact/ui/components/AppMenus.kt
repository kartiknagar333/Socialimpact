package com.example.socialimpact.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.socialimpact.ui.theme.AppTheme

@Composable
fun ProfileDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onEditProfile: () -> Unit,
    onPaymentClick: () -> Unit,
    onLogoutClick: () -> Unit,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    var showThemeOptions by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            showThemeOptions = false
            onDismissRequest()
        },
        containerColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        if (!showThemeOptions) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Edit Profile",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                onClick = {
                    onDismissRequest()
                    onEditProfile()
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit,
                        tint = MaterialTheme.colorScheme.tertiary,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = "Payment",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                onClick = {
                    onDismissRequest()
                    onPaymentClick()
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Payments,
                        tint = MaterialTheme.colorScheme.tertiary,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            )
            
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Theme",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                onClick = {
                    showThemeOptions = true
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Brightness4,
                        tint = MaterialTheme.colorScheme.tertiary,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    Text(
                        text = currentTheme.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Logout",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                },
                onClick = {
                    onDismissRequest()
                    onLogoutClick()
                },
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        tint = MaterialTheme.colorScheme.tertiary,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            )
        } else {
            // Theme selection "Submenu"
            DropdownMenuItem(
                text = { Text("Select Theme", fontWeight = FontWeight.Bold) },
                onClick = { showThemeOptions = false },
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 8.dp)
            )
            
            AppTheme.entries.forEach { theme ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = currentTheme == theme,
                                onClick = null // Handled by MenuItem click
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    onClick = {
                        onThemeChange(theme)
                        // Keep menu open to show selection or close it? usually dismiss.
                        onDismissRequest()
                        showThemeOptions = false
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            
            TextButton(
                onClick = { showThemeOptions = false },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Back")
            }
        }
    }
}
